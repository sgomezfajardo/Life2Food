package com.example.life2food;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    //Graphic elements
    private Button back;
    private Button payButton;
    private TextView totalTextView;
    private LinearLayout linearLayout;

    //Firebase
    private final Firebase firebase = new Firebase();
    private final String USERID = firebase.getUSERID();
    private final FirebaseFirestore DB = firebase.getDB();

    //Cart info
    private String items = " ";
    private double totalPrice = 0;
    private int numberItems = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100; // Código de solicitud para permisos de ubicación

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupBottomNavigation();

        linearLayout = findViewById(R.id.linearLayout);
        totalTextView = findViewById(R.id.text_total);  // Inicializar el TextView del total
        payButton = findViewById(R.id.Realizar_pago);  // Inicializar el botón de pago

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar y solicitar permisos de ubicación
                if (ContextCompat.checkSelfPermission(CartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CartActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // Permiso concedido, iniciar MapsActivity
                    startMapsActivity();
                }
            }
        });

        Firebase firebaseHelper = new Firebase();
        View supermarketIcon = findViewById(R.id.action_supermarket); // Icono del supermercado
        firebaseHelper.fetchUserRoleAndHideIcon(supermarketIcon);


        // Obtener los productos del carrito
        DB.collection("carts").whereEqualTo("id_usuario", USERID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        items = document.getData().toString();
                    }
                    if (items.length() < 100) {
                        ImageView imageView = new ImageView(CartActivity.this);
                        Glide.with(CartActivity.this).load("https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/img%2Fcart.png?alt=media&token=42b49254-1d55-4546-afdd-7a310869d030").into(imageView);
                        linearLayout.addView(imageView, linearLayout.indexOfChild(back));
                    } else {
                        while (items.length() > 100) {
                            getInfo();
                            numberItems++;
                        }
                    }
                } else {
                    Toast.makeText(CartActivity.this, "Error al obtener productos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void startMapsActivity() {
        // Crear un Intent para iniciar la nueva actividad
        Intent intent = new Intent(CartActivity.this, MapsActivity.class); // Reemplaza TargetActivity con el nombre de tu clase de destino
        startActivity(intent); // Inicia la actividad
    }

    // Método para manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar MapsActivity
                startMapsActivity();
            } else {
                // Permiso denegado, mostrar un mensaje
                Toast.makeText(this, "Se necesita permiso de ubicación para acceder al mapa", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getInfo() {
        // Resto de tu método getInfo permanece igual
        String productId;
        String productName;
        String productQuantity;
        String productPrice;
        String image_url;
        ImageView imageView = new ImageView(CartActivity.this);

        Glide.with(CartActivity.this).load("https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/img%2Fcart.png?alt=media&token=42b49254-1d55-4546-afdd-7a310869d030").into(imageView);

        items = items.substring(2);
        items = items.substring(items.indexOf("quantity="));
        productQuantity = items.substring(9, items.indexOf(","));
        items = items.substring(items.indexOf(","));
        items = items.substring(items.indexOf("productId="));
        productId = items.substring(10, items.indexOf(","));
        items = items.substring(items.indexOf(","));
        items = items.substring(items.indexOf("price="));
        productPrice = items.substring(6, items.indexOf(","));
        items = items.substring(items.indexOf(","));
        items = items.substring(items.indexOf("imageUrl="));
        image_url = items.substring(9, items.indexOf(","));
        items = items.substring(items.indexOf(","));
        items = items.substring(items.indexOf("productName="));
        productName = items.substring(12, items.indexOf("}"));
        items = items.substring(items.indexOf("}"));

        // Calcular el precio total
        double priceValue = Double.parseDouble(productPrice.substring(0, productPrice.indexOf(".")));
        totalPrice += priceValue * Integer.parseInt(productQuantity);  // Sumar al total
        updateTotalPrice();  // Actualizar el TextView del total

        String cart = productName + "\n" + productQuantity + "\n" + productPrice + "\n";
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(getResources().getColor(android.R.color.holo_orange_light));
        drawable.setCornerRadius(60);
        Button newDeleteButton = new Button(this);
        newDeleteButton.setText("Eliminar producto del carrito");
        newDeleteButton.setBackground(drawable);
        newDeleteButton.setTextColor(Color.WHITE);
        newDeleteButton.setTextSize(11);
        newDeleteButton.setPadding(30, 2, 30, 2);
        newDeleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView newTextView = new TextView(this);
        newTextView.setText(cart);
        ImageView newImage = new ImageView(this);
        Glide.with(this).load(image_url).override(350, 350).into(newImage);
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(layoutParams);
        cardView.setRadius(16);
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(16, 16, 16, 16);
        innerLayout.addView(newImage);
        innerLayout.addView(newTextView);
        innerLayout.addView(newDeleteButton);
        cardView.addView(innerLayout);
        cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        newDeleteButton.setOnClickListener(v -> {
            DB.collection("products").document(productId).update("quantity", FieldValue.increment(Integer.parseInt(productQuantity)));
            Map<String, Object> itemToRemove = new HashMap<>();
            itemToRemove.put("imageUrl", image_url);
            itemToRemove.put("price", Integer.parseInt(productPrice.substring(0, productPrice.indexOf("."))));
            itemToRemove.put("productId", productId);
            itemToRemove.put("productName", productName);
            itemToRemove.put("quantity", Integer.parseInt(productQuantity));
            removeItemFromCart(itemToRemove);
            numberItems--;
            linearLayout.removeView(cardView);
            totalPrice -= priceValue * Integer.parseInt(productQuantity);  // Restar del total
            updateTotalPrice();  // Actualizar el TextView del total
            if (numberItems <= 0) {
                linearLayout.addView(imageView, linearLayout.indexOfChild(back));
            }
        });
        linearLayout.addView(cardView, linearLayout.indexOfChild(back));
    }

    private void updateTotalPrice() {
        totalTextView.setText("Total a pagar: $" + String.format("%.2f", totalPrice));
    }

    private void removeItemFromCart(Map<String, Object> itemToRemove) {
        DB.collection("carts").document(USERID).update("items", FieldValue.arrayRemove(itemToRemove)).addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
        });
    }

    private void setupBottomNavigation() {
        ImageView profileIcon = findViewById(R.id.action_profile);
        ImageView restaurantIcon = findViewById(R.id.action_ecommerce);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);

        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        restaurantIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, EcommerceActivity.class));
        });

        supermarketIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, SupermarketActivity.class));
        });
    }
}
