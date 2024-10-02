package com.example.life2food;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SupermarketActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private String currentUserEmail;
    private String currentUserRole;
    private FirebaseFirestore db;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supermarket);


        db = FirebaseFirestore.getInstance();


        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();


        getUserRole();


        productList = new ArrayList<>();


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, currentUserEmail, this);
        recyclerView.setAdapter(productAdapter);


        loadProducts();


        Button btnAddProduct = findViewById(R.id.btn_add_product);
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            startActivity(intent);
            finish();
        });


    }

    private void getUserRole() {
        db.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {

                                    if (document.contains("role")) {
                                        currentUserRole = document.getString("role");
                                        Log.d("SupermarketActivity", "User Role: " + currentUserRole);
                                    } else {
                                        Log.d("SupermarketActivity", "Role field does not exist");
                                    }
                                }
                            }
                        } else {
                            Log.d("SupermarketActivity", "No user document found with the provided email");
                        }
                    } else {
                        Log.d("SupermarketActivity", "Error getting user role", task.getException());
                    }
                });
    }




    private void loadProducts() {
        db.collection("products")
                .whereEqualTo("userEmail", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SupermarketActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddProductDialog() {
        if (currentUserRole == null || currentUserRole.equals("usuario")) {
            Toast.makeText(this, "No tienes permisos para agregar productos.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(customLayout);

        final TextInputEditText productNameInput = customLayout.findViewById(R.id.product_name);
        final TextInputEditText productQuantityInput = customLayout.findViewById(R.id.product_quantity);
        final TextInputEditText productPriceInput = customLayout.findViewById(R.id.product_price);
        final Spinner productTypeSpinner = customLayout.findViewById(R.id.product_type_spinner);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.product_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productTypeSpinner.setAdapter(adapter);

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String productName = productNameInput.getText().toString().trim();
                String productQuantity = productQuantityInput.getText().toString().trim();
                String productPrice = productPriceInput.getText().toString().trim();
                String productType = productTypeSpinner.getSelectedItem().toString();


                if (productName.isEmpty() || productQuantity.isEmpty() || productPrice.isEmpty()) {
                    Toast.makeText(SupermarketActivity.this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }


                Product newProduct = new Product(null, productName, Integer.parseInt(productQuantity), productType, currentUserEmail, Double.parseDouble(productPrice));
                productList.add(newProduct);
                productAdapter.notifyItemInserted(productList.size() - 1);
                Toast.makeText(SupermarketActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();


                addProductToFirestore(newProduct);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addProductToFirestore(Product product) {
        db.collection("products").add(product) // Agregar el producto a Firestore
                .addOnSuccessListener(documentReference -> {
                    // Asignar el ID del documento generado al producto
                    product.setId(documentReference.getId());
                    Toast.makeText(SupermarketActivity.this, "Producto guardado en la base de datos", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SupermarketActivity.this, "Error al guardar el producto", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteProductClick(Product product) {

        if (product.getUserEmail().equals(currentUserEmail)) {

            productList.remove(product);
            productAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();


            db.collection("products").document(product.getId()).delete()
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                    });
        } else {
            Toast.makeText(this, "No puedes eliminar este producto", Toast.LENGTH_SHORT).show();
        }
    }
}
