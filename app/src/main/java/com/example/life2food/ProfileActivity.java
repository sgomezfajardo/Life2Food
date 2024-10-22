package com.example.life2food;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private EditText editAddress, editPhone;
    private TextView email, nameTextView;
    private RadioGroup radioGroupRole;
    private ImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private static final int COD_SEL_IMAGE = 300;

    private Uri image_url;
    String photo = "photo";
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigation();

        // Inicializar las vistas
        email = findViewById(R.id.email);
        nameTextView = findViewById(R.id.name_text_view);
        radioGroupRole = findViewById(R.id.radio_group_role);
        editAddress = findViewById(R.id.edit_address);
        editPhone = findViewById(R.id.edit_phone);
        profileImage = findViewById(R.id.profile_image);
        Button buttonUpdate = findViewById(R.id.button_update);
        Button buttonLogout = findViewById(R.id.button_logout);
        Button pic_button = findViewById(R.id.pic_button);

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        loadProfileData();

        String currentUserId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role"); // Cambia "role" según tu estructura de Firestore

                        if ("user".equalsIgnoreCase(userRole)) {
                            findViewById(R.id.action_supermarket).setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error obteniendo el rol: ", e);
                });

        // Configurar los listeners de los botones
        buttonUpdate.setOnClickListener(view -> updateProfile());

        buttonLogout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        pic_button.setOnClickListener(view -> updatePhoto());
    }


    private void loadProfileData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                this.email.setText(email);
                String firstname = documentSnapshot.getString("firstName");
                String lastname = documentSnapshot.getString("lastName");
                nameTextView.setText(firstname + " " + lastname);

                String role = documentSnapshot.getString("role");
                if ("business".equals(role)) {
                    ((RadioButton) findViewById(R.id.radio_business)).setChecked(true);
                } else if ("restaurant".equals(role)) {
                    ((RadioButton) findViewById(R.id.radio_restaurant)).setChecked(true);
                } else {
                    ((RadioButton) findViewById(R.id.radio_user)).setChecked(true);
                }

                editAddress.setText(documentSnapshot.getString("address"));
                editPhone.setText(documentSnapshot.getString("phone"));

                String imageUrl = documentSnapshot.getString("profileImage");
                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.ic_default_profile);
                }
            }
        });
    }

    private void updateProfile() {
        String userId = auth.getCurrentUser().getUid();
        String address = editAddress.getText().toString();
        String phone = editPhone.getText().toString();
        String role = "";

        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_business) {
            role = "business";
        } else if (selectedId == R.id.radio_restaurant) {
            role = "restaurant";
        } else {
            role = "user";
        }

        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.update("role", role, "address", address, "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    finish();
                });
    }

    private void updatePhoto() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, COD_SEL_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == COD_SEL_IMAGE) {
                image_url = data.getData();
                profileImage.setImageURI(image_url);
                uploadImageToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (image_url != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Subiendo Imagen...");
            progressDialog.show();

            String userId = auth.getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child("profile_images/" + userId + ".jpg"); // Cambia el nombre de archivo según sea necesario

            fileReference.putFile(image_url)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            DocumentReference docRef = firestore.collection("users").document(userId);
                            docRef.update("profileImage", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();

                    });
        }
    }

    private void setupBottomNavigation() {
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView restaurantIcon = findViewById(R.id.action_ecommerce);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);

        cartIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        restaurantIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, EcommerceActivity.class));
        });

        supermarketIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, SupermarketActivity.class));
        });
    }
}
