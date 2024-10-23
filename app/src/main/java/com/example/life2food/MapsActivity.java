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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Firebase firebase;
    private String userId; // Cambiado a userId

    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firebase = new Firebase();
        userId = firebase.getCurrentUser().getUid(); // Cambiado a getUid()

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
    }

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
    }

    private void mostrarUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Ubicación Actual"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            Log.d(TAG, "Ubicación actual: " + currentLocation);
                        } else {
                            Log.e(TAG, "No se pudo obtener la ubicación actual.");
                        }
                    }
                });
    }

    private void obtenerUbicacionesProductosEnCarrito() {
        Log.d(TAG, "Consultando productos en el carrito para el usuario con ID: " + userId);
        db.collection("carts")
                .whereEqualTo("id_usuario", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Consulta exitosa. Documentos recuperados: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Obtenemos el array de items
                            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                            if (items != null) {
                                for (Map<String, Object> item : items) {
                                    String direccion = (String) item.get("address"); // Obtenemos la dirección de cada item
                                    Log.d(TAG, "Dirección recuperada para el producto " + document.getId() + ": " + direccion);
                                    if (direccion != null) {
                                        convertirDireccionACoordenadas(direccion);
                                    } else {
                                        Log.e(TAG, "Dirección no encontrada para el producto: " + document.getId());
                                    }
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



    private void convertirDireccionACoordenadas(String direccion) {
        Log.d(TAG, "Convirtiendo dirección a coordenadas: " + direccion);
        GeocodingService geocodingService = new GeocodingService("AIzaSyBrrXaiHB3RbAkY-4dnDk7pEwp1_7RGRZ0");
        geocodingService.getLocationFromAddress(direccion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LatLng latLng = task.getResult();
                        if (latLng != null) {

                            Bitmap smallIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icono_productos_mapa);
                            Bitmap resizedIcon = Bitmap.createScaledBitmap(smallIcon, 100, 100, false); //////

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title(direccion)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

                            mMap.addMarker(markerOptions);
                            Log.d(TAG, "Dirección: " + direccion + " Coordenadas: " + latLng);
                        } else {
                            Log.e(TAG, "No se pudieron obtener coordenadas para la dirección: " + direccion);
                        }
                    } else {
                        Log.e(TAG, "Error al convertir dirección a coordenadas: " + task.getException());
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarUbicacionActual();
            } else {
                Log.e(TAG, "Permiso de ubicación denegado.");
            }
        }
    }
}
