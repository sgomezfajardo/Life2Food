package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class EcommerceActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String currentUserEmail;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);

        // Inicializar el Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Configura el Toolbar como la ActionBar

        // Inicializar RecyclerView y lista de productos
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        SearchView searchView = findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Manejar la búsqueda
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Manejar el cambio en el texto de búsqueda
                performSearch(newText);
                return true;
            }
        });

        // Inicializar Firestore y FirebaseAuth
        db = FirebaseFirestore.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Obtener correo del usuario actual

        // Cargar productos desde Firestore
        loadProductsFromFirestore();

        // Configurar el adaptador del RecyclerView
        productAdapter = new ProductAdapter(productList, currentUserEmail, this);
        recyclerView.setAdapter(productAdapter);

        // Configurar el menú inferior (navegación)
        setupBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú con las categorías
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.all_products) {
            // Mostrar todos los productos
            productAdapter.updateData(productList);
        } else if (itemId == R.id.category_snacks) {
            // Filtrar por la categoría de snacks
            filterProductsByCategory("Snacks");
        } else if (itemId == R.id.category_bebidas) {
            // Filtrar por la categoría de bebidas
            filterProductsByCategory("Bebidas");
        }
        // Manejar otras categorías si es necesario
        return super.onOptionsItemSelected(item);
    }

    private void loadProductsFromFirestore() {
        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Manejar el error
                        return;
                    }

                    productList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        String id = document.getId();
                        String name = document.getString("name");
                        double price = document.getDouble("price");
                        int quantity = document.getLong("quantity") != null ? document.getLong("quantity").intValue() : 0;
                        String type = document.getString("type");
                        String email = document.getString("email");

                        productList.add(new Product(id, name, quantity, type, email, price));
                    }
                    productAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDeleteProductClick(Product product) {
        // Lógica para eliminar el producto de Firestore
        db.collection("products")
                .document(product.getId()) // Usa el ID del documento correcto
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar el producto de la lista local
                    productList.remove(product);
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                });
    }

    private void setupBottomNavigation() {
        ImageView profileIcon = findViewById(R.id.action_profile);
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);
        ImageView restaurantIcon = findViewById(R.id.action_restaurant);

        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(EcommerceActivity.this, ProfileActivity.class));
        });

        cartIcon.setOnClickListener(v -> {
            startActivity(new Intent(EcommerceActivity.this, CartActivity.class));
        });

        supermarketIcon.setOnClickListener(v -> {
            startActivity(new Intent(EcommerceActivity.this, SupermarketActivity.class));
        });

        restaurantIcon.setOnClickListener(v -> {
            startActivity(new Intent(EcommerceActivity.this, RestaurantActivity.class));
        });
    }

    private void performSearch(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateData(filteredList);
    }

    private void filterProductsByCategory(String category) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getType().equals(category)) {
                filteredList.add(product);
            }
        }
        productAdapter.updateData(filteredList);
    }
}
