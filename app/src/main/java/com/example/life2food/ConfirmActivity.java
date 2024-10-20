package com.example.life2food;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfirmActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final EditText tokenEditText = findViewById(R.id.tokenEditText);
        Button confirmButton = findViewById(R.id.confirmButton);

        String email = getIntent().getStringExtra("email");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String password = getIntent().getStringExtra("password");
        String expectedToken = getIntent().getStringExtra("token");

        confirmButton.setOnClickListener(v -> {
            String enteredToken = tokenEditText.getText().toString().trim();

            if (enteredToken.equals(expectedToken)) {
                // Crear cuenta en Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(ConfirmActivity.this, task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                User user = new User(firstName, lastName, email);

                                db.collection("users").document(userId)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(ConfirmActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ConfirmActivity.this, "Error saving user", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(ConfirmActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ConfirmActivity.this, "Invalid token", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
