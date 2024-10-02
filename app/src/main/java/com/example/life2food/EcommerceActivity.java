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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        SearchView searchView = findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        db = FirebaseFirestore.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); //

        loadProductsFromFirestore();

        productAdapter = new ProductAdapter(productList, currentUserEmail, this);
        recyclerView.setAdapter(productAdapter);

        setupBottomNavigation();
    }

    //Metodo que se usa para crear el espacio de la barra de tareas
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    //Metodo que se usa para obtener el id de la barra de tareas
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.all_products) {
            productAdapter.updateData(productList);
        } else if (itemId == R.id.category_snacks) {
            filterProductsByCategory("Snacks");
        } else if (itemId == R.id.category_bebidas) {
            filterProductsByCategory("Bebidas");
        } else if (itemId == R.id.category_frutas) {
            filterProductsByCategory("Frutas");
        } else if (itemId == R.id.category_verduras) {
            filterProductsByCategory("Verduras");
        } else if (itemId == R.id.category_legumbres) {
            filterProductsByCategory("Legumbres");
        } else if (itemId == R.id.category_pastas) {
            filterProductsByCategory("Pastas");
        }

        return super.onOptionsItemSelected(item);
    }

    //Metodo que se usa para cargar los productos de la base de datos
    private void loadProductsFromFirestore() {
        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
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

    //Metodo que se usa para eliminar un producto de la base de datos
    @Override
    public void onDeleteProductClick(Product product) {
        db.collection("products")
                .document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(product);
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
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

    //Metodo que se usa para buscar un producto en la base de datos
    private void performSearch(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateData(filteredList);
    }

    //Metodo que se usa para filtrar los productos por categoria
    private void filterProductsByCategory(String category) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            System.out.println("Product: " + product.getName());
            System.out.println("Category: " + product.getType());
            if (product.getType().equals(category)) {
                filteredList.add(product);
            }
        }
        productAdapter.updateData(filteredList);
    }
}
