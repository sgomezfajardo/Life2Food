package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);

        // Inicializar RecyclerView y lista de productos
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        // Inicializar Firestore y FirebaseAuth
        db = FirebaseFirestore.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Obtener correo del usuario actual

        // Cargar productos de Firestore
        loadProductsFromFirestore();

        // Configurar el adaptador del RecyclerView
        productAdapter = new ProductAdapter(productList, currentUserEmail, this);
        recyclerView.setAdapter(productAdapter);

        // Configurar el menú inferior
        setupBottomNavigation();
    }

    private void loadProductsFromFirestore() {
        db.collection("products") // Cambia "products" por el nombre de tu colección
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); // Asegúrate de limpiar la lista antes de agregar nuevos productos
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId(); // Obtiene el ID del documento
                            String name = document.getString("name");
                            double price = document.getDouble("price"); // Obtener el precio como double
                            int quantity = document.getLong("quantity") != null ? document.getLong("quantity").intValue() : 0;
                            String type = document.getString("type"); // Obtener tipo de producto
                            String email = document.getString("email"); // Correo del usuario que subió el producto

                            // Agregar el producto a la lista
                            productList.add(new Product(id, name, quantity, type, email, price));
                        }
                        productAdapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                    }
                });
    }

    @Override
    public void onDeleteProductClick(Product product) {
        // Implementar lógica para eliminar el producto de Firestore
        db.collection("products")
                .document(product.getId()) // Cambia esto para obtener el documento correcto usando el ID
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
}
