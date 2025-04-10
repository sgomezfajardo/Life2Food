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
import android.widget.Toast;

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
    private TextView nameTextView;
    //private ImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private static final int COD_SEL_IMAGE = 300;

    //private Uri image_url;
    //String photo = "photo";
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigation();


        nameTextView = findViewById(R.id.name_text_view);
        editAddress = findViewById(R.id.edit_address);
        editPhone = findViewById(R.id.edit_phone);
        Button buttonUpdate = findViewById(R.id.button_update);
        Button buttonLogout = findViewById(R.id.button_logout);
        Button add_address = findViewById(R.id.add_address);
        add_address.setOnClickListener(view ->
                Toast.makeText(this, "Nueva direccion", Toast.LENGTH_SHORT).show());

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        loadProfileData();
        buttonUpdate.setOnClickListener(view -> updateProfile());

        buttonLogout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


    }


    private void loadProfileData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                String firstname = documentSnapshot.getString("firstName");
                String lastname = documentSnapshot.getString("lastName");
                nameTextView.setText(firstname + " " + lastname);

                editAddress.setText(documentSnapshot.getString("address"));
                editPhone.setText(documentSnapshot.getString("phone"));

            }
        });
    }

    private void updateProfile() {
        String userId = auth.getCurrentUser().getUid();
        String address = editAddress.getText().toString();
        String phone = editPhone.getText().toString();
        try {
            DocumentReference docRef = firestore.collection("users").document(userId);
            docRef.update("address", address, "phone", phone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "No se ha podido actualizar tu perfil", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }



    private void setupBottomNavigation() {
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView ecommerceIcon = findViewById(R.id.action_ecommerce);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);
        ImageView OrdersIcon = findViewById(R.id.action_Orders);

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        ecommerceIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        supermarketIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, SupermarketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
}
