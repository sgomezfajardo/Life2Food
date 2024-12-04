package com.example.life2food;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener, ProductAdapter.OnAddToCartClickListener {

    //Product adapter
    private ProductAdapter productAdapter;
    private List<Product> productList;

    //Firebase
    private final Firebase firebase = new Firebase();
    private final String USERID = firebase.getUSERID();
    private final FirebaseFirestore DB = firebase.getDB();

    //Variables
    String productEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Graphics elements
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productAdapter.setOnAddToCartClickListener(this);
        recyclerView.setAdapter(productAdapter);
        TextView nameMarket = findViewById(R.id.nameMarket);
        Product product = getIntent().getParcelableExtra("product");
        String productEmail = "";
        if (product != null) {
            productEmail = product.getEmail();
            //Creating query to know the name of market
            DB.collection("users").whereEqualTo("email", productEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("firstName");
                            nameMarket.setText(name);
                        }
                    }
                }
            });
            //load only products from the select market
            loadProductsFromFirestore(productEmail);
        } else {
            finish();
        }
    }

    /**
     * @noinspection DataFlowIssue
     */
    @SuppressLint("NotifyDataSetChanged")
    //This method loadProductsFromFirestore is called when the activity is created
    private void loadProductsFromFirestore(String productEmail) {
        System.out.println("product email: " + productEmail);
        DB.collection("products")
                .whereEqualTo("email", productEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String name = document.getString("name");
                                Double price = document.getDouble("price");
                                Long quantityLong = document.getLong("quantity");
                                int quantity = (quantityLong != null) ? quantityLong.intValue() : 0;
                                String type = document.getString("type");
                                String email = document.getString("email");
                                String imageUrl = document.getString("imageUrl");
                                String description = document.getString("description");

                                if (name != null && price != null && type != null && email != null && imageUrl != null && description != null && quantity > 0) {
                                    productList.add(new Product(id, name, quantity, type, email, price, imageUrl, description));
                                }
                            }
                            productAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //This method createCartIfNotExists is called when the activity is created
    public void createCartIfNotExists() {
        DocumentReference cartRef = DB.collection("carts").document(USERID);

        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    Map<String, Object> newCart = new HashMap<>();
                    newCart.put("id_usuario", USERID);
                    newCart.put("items", new ArrayList<>());

                    cartRef.set(newCart).addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Nuevo carrito creado exitosamente.");
                    }).addOnFailureListener(e -> {
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

    /** @noinspection DataFlowIssue*/ //This method onAddToCartClick is called when the user clicks on the add to cart button
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

            String productCreatorEmail = product.getEmail();

            FirebaseFirestore DB = FirebaseFirestore.getInstance();
            DB.collection("users").whereEqualTo("email", productCreatorEmail).get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                    String userAddress = userDocument.getString("address");

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


                                        item.put("quantity", newQuantity);
                                        item.put("address", userAddress);
                                        productExists = true;
                                        break;
                                    }
                                }

                                if (!productExists) {

                                    Map<String, Object> newProduct = new HashMap<>();
                                    newProduct.put("productId", product.getId());
                                    newProduct.put("productName", product.getName());
                                    newProduct.put("quantity", quantityToAdd);
                                    newProduct.put("price", product.getPrice());
                                    newProduct.put("imageUrl", product.getImageUrl());
                                    newProduct.put("address", userAddress);

                                    items.add(newProduct);
                                }


                                cartRef.update("items", items).addOnSuccessListener(aVoid -> {
                                    product.updateQuantity(product.getQuantity() - quantityToAdd);
                                    Toast.makeText(this, "Producto actualizado en el carrito", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al actualizar el carrito", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener la dirección del creador del producto", Toast.LENGTH_SHORT).show();
            });

            dialog.dismiss();
        });
        Intent intent = new Intent(this, MarketActivity.class);
    }

    //This method onDeleteProductClick is called when the user clicks on the delete button
    @Override
    public void onDeleteProductClick(Product product) {

    }

    //This method onCardClick is called when the user clicks on a product card
    @Override
    public void onCardClick(Product product) {
        Intent intent = new Intent(this, ProductDetails.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
}