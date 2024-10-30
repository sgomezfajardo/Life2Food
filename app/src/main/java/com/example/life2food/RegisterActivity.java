package com.example.life2food;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Geocoder geocoder;

    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText, confirmPasswordEditText, addressEditText;
    private Button continueButton, registerButton, selectLocationButton, searchAddressButton;
    private GoogleMap googleMap;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this, Locale.getDefault());

        emailEditText = findViewById(R.id.emailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        addressEditText = findViewById(R.id.addressEditText);
        continueButton = findViewById(R.id.continueButton);
        registerButton = findViewById(R.id.registerButton);
        selectLocationButton = findViewById(R.id.selectLocationButton);
        searchAddressButton = findViewById(R.id.searchAddressButton);


        Places.initialize(getApplicationContext(), "AIzaSyBrrXaiHB3RbAkY-4dnDk7pEwp1_7RGRZ0");
        PlacesClient placesClient = Places.createClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        continueButton.setOnClickListener(v -> {
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

            findViewById(R.id.initialLayout).setVisibility(View.GONE);
            findViewById(R.id.mapLayout).setVisibility(View.VISIBLE);
        });

        selectLocationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Selecciona una ubicación en el mapa tocando el lugar deseado.", Toast.LENGTH_SHORT).show();
        });

        searchAddressButton.setOnClickListener(v -> {
            String address = addressEditText.getText().toString().trim();
            if (!address.isEmpty()) {
                searchLocation(address);
            } else {
                Toast.makeText(RegisterActivity.this, "Por favor ingresa una dirección", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> {
            if (selectedLocation == null) {
                Toast.makeText(RegisterActivity.this, "Por favor selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show();
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
                                    selectedLocation.latitude,
                                    selectedLocation.longitude
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
                            Log.e("RegisterActivity", "Error de autenticación: " + task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "Error de autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void searchLocation(String address) {
        try {
            List<android.location.Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                android.location.Address location = addressList.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                selectedLocation = latLng;
            } else {
                Toast.makeText(RegisterActivity.this, "No se encontró la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error al buscar la ubicación: " + e.getMessage());
            Toast.makeText(RegisterActivity.this, "Error al buscar la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            selectedLocation = latLng;
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        });
    }
}
