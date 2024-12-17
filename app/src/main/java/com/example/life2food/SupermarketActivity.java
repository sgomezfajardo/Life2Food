package com.example.life2food;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupermarketActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener, ProductAdapter.OnEditProduct {


    //User and product info
    private ProductAdapter productAdapter;
    private List<Product> productList;


    //Firebase
    private final Firebase firebase = new Firebase();
    private final FirebaseFirestore DB = firebase.getDB();
    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private String currentUserRole;


    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private LottieAnimationView lottieAnimation;
    private final String url = "https://wa.me/573104083853";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supermarket);
        setupBottomNavigation();
        LottieAnimationView lottieAnimation = findViewById(R.id.lottie_add_product);
        ImageButton whatsapp = findViewById(R.id.btn_whatsapp);
        whatsapp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getUserRole();

        productList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, currentUserEmail, this);
        recyclerView.setAdapter(productAdapter);
        productAdapter.setOnEditProduct(this);

        loadProducts();

        Button btnAddProduct = findViewById(R.id.btn_add_product);
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
    }

    private void getUserRole() {
        DB.collection("users")
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
        DB.collection("products")
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

        if (currentUserRole == null || currentUserRole.equals("user")) {
            Log.d("SupermarketActivity", "ROL: " + currentUserRole);
            Toast.makeText(this, "No tienes permisos para agregar productos.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(customLayout);

        final TextInputEditText productNameInput = customLayout.findViewById(R.id.product_name);
        final TextInputEditText productQuantityInput = customLayout.findViewById(R.id.product_quantity);
        final TextInputEditText productPriceInput = customLayout.findViewById(R.id.product_price);
        final TextInputEditText productDescriptionInput = customLayout.findViewById(R.id.product_description);
        final Spinner productTypeSpinner = customLayout.findViewById(R.id.product_type_spinner);
        Button btnSelectImage = customLayout.findViewById(R.id.btn_select_image);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.product_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productTypeSpinner.setAdapter(adapter);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String productName = productNameInput.getText().toString().trim();
            String productQuantity = productQuantityInput.getText().toString().trim();
            String productPrice = productPriceInput.getText().toString().trim();
            String productDescription = productDescriptionInput.getText().toString().trim();
            String productType = productTypeSpinner.getSelectedItem().toString();

            if (productName.isEmpty() || productQuantity.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty()) {
                Toast.makeText(SupermarketActivity.this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Product newProduct = new Product(productName, Integer.parseInt(productQuantity), productType, currentUserEmail, Double.parseDouble(productPrice), null, productDescription);
            newProduct.setQuantity(Integer.parseInt(productQuantity));
            productList.add(newProduct);
            productAdapter.notifyItemInserted(productList.size() - 1);
            Toast.makeText(SupermarketActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();

            if (imageUri != null) {
                uploadImageToFirebase(newProduct);
            } else {
                addProductToFirestore(newProduct);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
        }
    }

    private void uploadImageToFirebase(Product product) {
        StorageReference fileReference = storageRef.child("products/" + System.currentTimeMillis() + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        product.setImageUrl(uri.toString());
                        addProductToFirestore(product);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SupermarketActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addProductToFirestore(Product product) {
        DB.collection("products").add(product)
                .addOnSuccessListener(documentReference -> {
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
            DB.collection("products").document(product.getId()).delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(SupermarketActivity.this, "Producto eliminado de la base de datos", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(SupermarketActivity.this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No tienes permisos para eliminar este producto", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        builder.setView(customLayout);

        final TextInputEditText productNameInput = customLayout.findViewById(R.id.product_name);
        final TextInputEditText productQuantityInput = customLayout.findViewById(R.id.product_quantity);
        final TextInputEditText productPriceInput = customLayout.findViewById(R.id.product_price);
        final TextInputEditText productDescriptionInput = customLayout.findViewById(R.id.product_description);
        final Spinner productTypeSpinner = customLayout.findViewById(R.id.product_type_spinner);
        Button btnSelectImage = customLayout.findViewById(R.id.btn_select_image);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.product_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productTypeSpinner.setAdapter(adapter);

        // Set the existing product details in the input fields
        productNameInput.setText(product.getName());
        productQuantityInput.setText(String.valueOf(product.getQuantity()));
        productPriceInput.setText(String.valueOf(product.getPrice()));
        productDescriptionInput.setText(product.getDescription());
        int spinnerPosition = adapter.getPosition(product.getType());
        productTypeSpinner.setSelection(spinnerPosition);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        builder.setPositiveButton("Confirmar cambio", (dialog, which) -> {
            String productName = productNameInput.getText().toString().trim();
            String productQuantity = productQuantityInput.getText().toString().trim();
            String productPrice = productPriceInput.getText().toString().trim();
            String productDescription = productDescriptionInput.getText().toString().trim();
            String productType = productTypeSpinner.getSelectedItem().toString();

            if (productName.isEmpty() || productQuantity.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty()) {
                Toast.makeText(SupermarketActivity.this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update product with new values
            product.setName(productName);
            product.setQuantity(Integer.parseInt(productQuantity));
            product.setPrice(Double.parseDouble(productPrice));
            product.setDescription(productDescription);
            product.setType(productType);

            int productIndex = productList.indexOf(product);
            if (productIndex >= 0) {
                productAdapter.notifyItemChanged(productIndex);
            }

            Toast.makeText(SupermarketActivity.this, "Producto editado", Toast.LENGTH_SHORT).show();

            // Handle image upload if there's a new image selected
            if (imageUri != null) {
                uploadImageToFirebase(product);
            } else {
                updateProductInFirestore(product);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProductInFirestore(Product product) {
        DocumentReference productRef = DB.collection("products").document(product.getId());
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", product.getName());
        updatedData.put("quantity", product.getQuantity());
        updatedData.put("type", product.getType());
        updatedData.put("userEmail", product.getUserEmail());
        updatedData.put("price", product.getPrice());
        updatedData.put("description", product.getDescription());
        if (product.getImageUrl() != null) {
            updatedData.put("imageUrl", product.getImageUrl());
        }
        productRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SupermarketActivity.this, "Producto actualizado en Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SupermarketActivity.this, "Error al actualizar el producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void setupBottomNavigation() {
        ImageView profileIcon = findViewById(R.id.action_profile);
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView ecommerceIcon = findViewById(R.id.action_ecommerce);
        ImageView OrdersIcon = findViewById(R.id.action_Orders);

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        ecommerceIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        OrdersIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    @Override
    public void onCardClick(Product product) {

    }
}