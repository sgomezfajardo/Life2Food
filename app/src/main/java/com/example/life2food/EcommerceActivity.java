package com.example.life2food;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcommerceActivity extends AppCompatActivity
        implements ProductAdapter.OnProductClickListener, ProductAdapter.OnAddToCartClickListener {


    //Grapchis and variables
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private Toolbar toolbar;

    //Firebase
    private final Firebase firebase = new Firebase();
    private final String USERID = firebase.getUSERID();
    private final FirebaseFirestore DB = firebase.getDB();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        setupBottomNavigation();

        // Verificar permisos al iniciar
        checkLocationPermission();

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

        Firebase firebaseHelper = new Firebase();
        View supermarketIcon = findViewById(R.id.action_supermarket);
        firebaseHelper.fetchUserRoleAndHideIcon(supermarketIcon);

        loadProductsFromFirestore();

        productAdapter = new ProductAdapter(productList, this);
        productAdapter.setOnAddToCartClickListener(this);
        recyclerView.setAdapter(productAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

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

    private void loadProductsFromFirestore() {
        DB.collection("products")
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
                        String imageUrl = document.getString("imageUrl");
                        String description = document.getString("description");

                        if(quantity>0)
                            productList.add(new Product(id, name, quantity, type, email, price, imageUrl, description));

                    }
                    productAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDeleteProductClick(Product product) {}

    private void setupBottomNavigation() {
        ImageView profileIcon = findViewById(R.id.action_profile);
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);

        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        cartIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        supermarketIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, SupermarketActivity.class));
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

    @Override
    public void onAddToCartClick(Product product) {
        createCartIfNotExists();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_product_to_cart, null);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText productQuantityInput = customLayout.findViewById(R.id.product_quantity);
        Button confirm_add_to_cart = customLayout.findViewById(R.id.btn_add_to_cart);

        confirm_add_to_cart.setOnClickListener(v -> {
            String productQuantity = productQuantityInput.getText().toString();

            if (productQuantity.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese la cantidad", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantityToAdd = Integer.parseInt(productQuantity);
            if (quantityToAdd > product.getQuantity()) {
                Toast.makeText(this, "No hay suficiente stock", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener el email del producto para buscar la dirección del creador
            String productCreatorEmail = product.getEmail();  // Asegúrate de que este método obtenga el email del producto

            // Buscar la dirección del usuario que creó el producto
            FirebaseFirestore DB = FirebaseFirestore.getInstance();
            DB.collection("users")
                    .whereEqualTo("email", productCreatorEmail)  // Comparar con el email del producto
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String userAddress = userDocument.getString("address"); // Obtener la dirección del usuario

                            DocumentReference cartRef = DB.collection("carts").document(USERID);
                            cartRef.get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");

                                    if (items != null) {
                                        boolean productExists = false;

                                        for (Map<String, Object> item : items) {
                                            if (item.get("productId").equals(product.getId())) {
                                                int currentQuantity = ((Long) item.get("quantity")).intValue();
                                                int newQuantity = currentQuantity + quantityToAdd;

                                                // Actualizamos la cantidad en el carrito
                                                item.put("quantity", newQuantity);
                                                item.put("address", userAddress); // Agregar dirección al producto
                                                productExists = true;
                                                break;
                                            }
                                        }

                                        if (!productExists) {
                                            // Si el producto no está en el carrito, lo añadimos
                                            Map<String, Object> newProduct = new HashMap<>();
                                            newProduct.put("productId", product.getId());
                                            newProduct.put("productName", product.getName());
                                            newProduct.put("quantity", quantityToAdd);
                                            newProduct.put("price", product.getPrice());
                                            newProduct.put("imageUrl", product.getImageUrl());
                                            newProduct.put("address", userAddress); // Agregar dirección al nuevo producto

                                            items.add(newProduct);
                                        }

                                        // Actualizamos el carrito en la base de datos
                                        cartRef.update("items", items)
                                                .addOnSuccessListener(aVoid -> {
                                                    product.updateQuantity(product.getQuantity() - quantityToAdd);
                                                    Toast.makeText(this, "Producto actualizado en el carrito", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error al actualizar el carrito", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al obtener la dirección del creador del producto", Toast.LENGTH_SHORT).show();
                    });

            dialog.dismiss();
        });
    }


    public void createCartIfNotExists() {
        FirebaseFirestore DB = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();  // UID del usuario autenticado

        // Referencia al documento del carrito del usuario
        DocumentReference cartRef = DB.collection("carts").document(userId);

        // Verificar si el carrito ya existe
        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    // Si el carrito no existe, creamos un nuevo documento
                    Map<String, Object> newCart = new HashMap<>();
                    newCart.put("id_usuario", userId);  // Guardar el UID del usuario
                    newCart.put("items", new ArrayList<>());  // Crear un array vacío para los productos

                    cartRef.set(newCart)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Nuevo carrito creado exitosamente.");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error al crear el carrito", e);
                            });
                } else {
                    Log.d("Firestore", "El carrito ya existe.");
                }
            } else {
                Log.w("Firestore", "Error al verificar si el carrito existe", task.getException());
            }
        });
    }


    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                initLocationServices();
            }
        } else {
            initLocationServices();
        }
    }

    // Método que se llama cuando se recibe la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, puedes continuar
                initLocationServices();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Permiso de ubicación denegado. Algunas funciones pueden no estar disponibles.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para inicializar los servicios de ubicación (implementa tu lógica aquí)
    private void initLocationServices() {
        // Aquí puedes inicializar la lógica que requiere el permiso de ubicación
    }
}