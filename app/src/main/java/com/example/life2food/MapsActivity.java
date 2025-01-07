package com.example.life2food;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionsRequest.DirectionsRequestCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db; // Add a variable to store the Firestore instance
    private FusedLocationProviderClient fusedLocationClient; // Add a variable to store the FusedLocationProviderClient instance
    private Firebase firebase; // Add a variable to store the Firebase instance
    private String userId; // Add a variable to store the current user ID
    private LatLng currentLocation; // Add a variable to store the current location
    private LatLng productLocation; // Add a variable to store the product location
    private List<Polyline> polylines = new ArrayList<>(); // To keep track of drawn polylines
    private Polyline currentPolyline; // To keep track of the current polyline
    private TextView tvTimeDriving; // Add a variable to store the TextView for the driving time
    private TextView tvTimeWalking; // Add a variable to store the TextView for the walking time

    private static final String TAG = "MapsActivity";

    // Initialize the activity, set the content view, and get the current user ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firebase = new Firebase();
        userId = firebase.getCurrentUser().getUid();

        tvTimeDriving = findViewById(R.id.tv_time_driving);
        tvTimeWalking = findViewById(R.id.tv_time_walking);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton btnRouteDriving = findViewById(R.id.btn_route_driving);
        ImageButton btnRouteWalking = findViewById(R.id.btn_route_walking);

        db = firebase.getDB();
        Log.d(TAG, "Firebase Firestore initialized");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "Map fragment is not null, requesting map async");
        } else {
            Log.e(TAG, "Map fragment is null");
        }

        btnRouteDriving.setOnClickListener(v -> trazarRuta(currentLocation, productLocation, "driving"));
        btnRouteWalking.setOnClickListener(v -> trazarRuta(currentLocation, productLocation, "walking"));
    }

    // Request location permissions
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mostrarUbicacionActual();
        obtenerUbicacionesProductosEnCarrito();

        mMap.setOnMarkerClickListener(marker -> {
            LatLng newProductLocation = marker.getPosition();
            if (currentPolyline != null) {
                currentPolyline.remove();
                currentPolyline = null;
                if (newProductLocation.equals(productLocation)) {
                    productLocation = null;
                    return true;
                }
            }
            productLocation = newProductLocation;
            return false;
        });
    }

    // Get the current location of the device and add a marker to the map
    private void mostrarUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Ubicación Actual"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        Log.d(TAG, "Ubicación actual: " + currentLocation);
                    } else {
                        Log.e(TAG, "No se pudo obtener la ubicación actual.");
                    }
                });
    }

    // Query the 'carts' collection to get the products in the user's cart
    private void obtenerUbicacionesProductosEnCarrito() {
        Log.d(TAG, "Consultando productos en el carrito para el usuario con ID: " + userId);
        db.collection("carts")
                .whereEqualTo("id_usuario", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Consulta exitosa. Documentos recuperados: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                            if (items != null) {
                                for (Map<String, Object> item : items) {
                                    String productId = (String) item.get("productId");
                                    verificarProductoYUbicacion(productId, item);
                                }
                            } else {
                                Log.e(TAG, "No se encontraron items para el carrito: " + document.getId());
                            }
                        }
                    } else {
                        Log.e(TAG, "Error al obtener productos: " + task.getException());
                    }
                });
    }

    // Check if the product exists in the 'products' collection, then check if the user's location is available. If not, convert the address to coordinates.
    private void verificarProductoYUbicacion(String productId, Map<String, Object> item) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(productDocument -> {
                    if (productDocument.exists()) {
                        String email = productDocument.getString("email");
                        if (email != null) {
                            db.collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnSuccessListener(userQuery -> {
                                        if (!userQuery.isEmpty()) {
                                            for (QueryDocumentSnapshot userDoc : userQuery) {
                                                Double latitude = userDoc.getDouble("latitude");
                                                Double longitude = userDoc.getDouble("longitude");
                                                if (latitude != null && longitude != null) {
                                                    productLocation = new LatLng(latitude, longitude); // Update productLocation
                                                    Log.d(TAG, "Ubicación del producto: " + productLocation);
                                                    agregarMarcadorEnMapa(productLocation, "Ubicación de " + email);
                                                    return; // No necesitamos seguir buscando otros usuarios
                                                }
                                            }
                                        }
                                        String direccion = (String) item.get("address");
                                        if (direccion != null) {
                                            convertirDireccionACoordenadas(direccion);
                                        } else {
                                            Log.e(TAG, "Dirección no encontrada para el producto: " + productId);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error al buscar usuario: " + e));
                        } else {
                            Log.e(TAG, "No se encontró email para el producto: " + productId);
                        }
                    } else {
                        Log.e(TAG, "Producto no encontrado en la colección 'products': " + productId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al buscar producto: " + e));
    }

    // Converts an address to coordinates and then updates a marker on the map
    private void convertirDireccionACoordenadas(String direccion) {
        Log.d(TAG, "Convirtiendo dirección a coordenadas: " + direccion);
        GeocodingService geocodingService = new GeocodingService("AIzaSyBrrXaiHB3RbAkY-4dnDk7pEwp1_7RGRZ0");
        geocodingService.getLocationFromAddress(direccion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productLocation = task.getResult(); // Update productLocation
                        if (productLocation != null) {
                            Log.d(TAG, "Coordenadas de la dirección: " + productLocation);
                            agregarMarcadorEnMapa(productLocation, direccion);
                        } else {
                            Log.e(TAG, "No se pudieron obtener coordenadas para la dirección: " + direccion);
                        }
                    } else {
                        Log.e(TAG, "Error al convertir dirección a coordenadas: " + task.getException());
                    }
                });
    }

    // Add the corresponding product icons to the map
    private void agregarMarcadorEnMapa(LatLng location, String title) {
        Bitmap smallIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icono_productos_mapa);
        Bitmap resizedIcon = Bitmap.createScaledBitmap(smallIcon, 100, 100, false);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

        mMap.addMarker(markerOptions);
        Log.d(TAG, "Marcador agregado en: " + location + " con título: " + title);
    }

    // Checks if the start and end points are not null, constructs a URL to request directions from the Google API, and then makes the request. When the response is received, the method updates the UI with the route duration based on the selected mode of transport.
    private void trazarRuta(LatLng inicio, LatLng destino, String mode) {
        if (inicio == null || destino == null) {
            Log.e(TAG, "Puntos de inicio o destino son nulos. No se puede trazar la ruta.");
            return;
        }

        Log.d(TAG, "Trazando ruta desde " + inicio.toString() + " hasta " + destino.toString() + " en modo: " + mode);

        String directionsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + inicio.latitude + "," + inicio.longitude +
                "&destination=" + destino.latitude + "," + destino.longitude +
                "&mode=" + mode +
                "&key=AIzaSyBrrXaiHB3RbAkY-4dnDk7pEwp1_7RGRZ0";

        new DirectionsRequest(this, directionsUrl, mMap, mode, new DirectionsRequest.DirectionsRequestCallback() {

            @Override
            public void onDurationRetrieved(String mode, String duration) {
                if ("driving".equals(mode)) {
                    tvTimeDriving.setText(duration );
                } else if ("walking".equals(mode)) {
                    tvTimeWalking.setText(duration );
                }
            }
        }).execute();
    }

    // Updates the UI elements (TextView) with the route duration, depending on the mode of transport
    @Override
    public void onDurationRetrieved(String mode, String duration) {
        if ("driving".equals(mode)) {
            tvTimeDriving.setText(duration );
        } else if ("walking".equals(mode)) {
            tvTimeWalking.setText(duration );
        }
    }
}