package com.example.life2food;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText, confirmPasswordEditText;
    private EditText streetEditText, numberStreetEditText, cityEditText, postalCodeEditText;
    private Spinner directionTypeSpinner;
    private Button continueButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        continueButton = findViewById(R.id.continueButton);

        streetEditText = findViewById(R.id.streetEditText);
        numberStreetEditText = findViewById(R.id.numberStreetEditText);
        cityEditText = findViewById(R.id.cityEditText);
        postalCodeEditText = findViewById(R.id.postalCodeEditText);
        directionTypeSpinner = findViewById(R.id.direction_type_spinner);
        registerButton = findViewById(R.id.registerButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String email = emailEditText.getText().toString().trim();
                    String firstName = firstNameEditText.getText().toString().trim();
                    String lastName = lastNameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos de registro", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    findViewById(R.id.imageView2).setVisibility(View.GONE);
                    emailEditText.setVisibility(View.GONE);
                    firstNameEditText.setVisibility(View.GONE);
                    lastNameEditText.setVisibility(View.GONE);
                    passwordEditText.setVisibility(View.GONE);
                    confirmPasswordEditText.setVisibility(View.GONE);
                    continueButton.setVisibility(View.GONE);
                    findViewById(R.id.background_layout).setBackgroundColor(Color.WHITE);

                    findViewById(R.id.ubication_image).setVisibility(View.VISIBLE);
                    streetEditText.setVisibility(View.VISIBLE);
                    numberStreetEditText.setVisibility(View.VISIBLE);
                    cityEditText.setVisibility(View.VISIBLE);
                    postalCodeEditText.setVisibility(View.VISIBLE);
                    directionTypeSpinner.setVisibility(View.VISIBLE);
                    registerButton.setVisibility(View.VISIBLE);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RegisterActivity.this,
                            R.array.direction_types, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    directionTypeSpinner.setAdapter(adapter);
                } catch (Exception e) {
                    Log.e("RegisterActivity", "Error en onClick: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "Ocurrió un error, por favor intenta de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String street = streetEditText.getText().toString().trim();
                String numberStreet = numberStreetEditText.getText().toString().trim();
                String city = cityEditText.getText().toString().trim();
                String postalCode = postalCodeEditText.getText().toString().trim();

                if (street.isEmpty() || numberStreet.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos de dirección", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                User user = new User(
                                        firstNameEditText.getText().toString(),
                                        lastNameEditText.getText().toString(),
                                        emailEditText.getText().toString(),
                                        street,
                                        numberStreet,
                                        city,
                                        postalCode
                                );

                                db.collection("users").document(userId)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Error al guardar los datos del usuario", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
