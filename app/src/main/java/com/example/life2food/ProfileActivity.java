package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private EditText editEmail, editAddress, editPhone;
    private TextView nameTextView, lastnameTextView;
    private RadioGroup radioGroupRole;
    private ImageView profileImage;
    private Button buttonUpdate, buttonLogout, back;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editEmail = findViewById(R.id.edit_email);
        nameTextView = findViewById(R.id.name_text_view);
        lastnameTextView = findViewById(R.id.lastname_text_view);
        radioGroupRole = findViewById(R.id.radio_group_role);
        editAddress = findViewById(R.id.edit_address);
        editPhone = findViewById(R.id.edit_phone);
        profileImage = findViewById(R.id.profile_image);
        buttonUpdate = findViewById(R.id.button_update);
        buttonLogout = findViewById(R.id.button_logout);
        back = findViewById(R.id.button_back);
        back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            startActivity(intent);
            finish();
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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
                editEmail.setText(documentSnapshot.getString("email"));
                String firstname = documentSnapshot.getString("firstName");
                String lastname = documentSnapshot.getString("lastName");
                nameTextView.setText(firstname);
                lastnameTextView.setText(lastname);

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
        String email = editEmail.getText().toString();
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
        docRef.update("email", email, "role", role, "address", address, "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    finish();
                });
    }
}
