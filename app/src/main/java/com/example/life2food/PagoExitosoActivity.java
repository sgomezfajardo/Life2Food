package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PagoExitosoActivity extends AppCompatActivity {

    private LinearLayout productsLayout;
    private TextView totalCostTextView;
    private TextView orderTicketValueTextView;
    private TextView warningTextView;
    private Button toggleMapButton;
    private Button orderReceivedButton;
    private Firebase firebase;
    private double totalCost = 0.0;
    private String orderTicket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_exitoso);

        // Initialize views
        productsLayout = findViewById(R.id.products_layout);
        totalCostTextView = findViewById(R.id.total_cost);
        orderTicketValueTextView = findViewById(R.id.order_ticket_value);
        warningTextView = findViewById(R.id.warning_text);
        toggleMapButton = findViewById(R.id.toggle_map_button);
        orderReceivedButton = findViewById(R.id.order_received_button);
        firebase = new Firebase();

        // Set warning message
        warningTextView.setText("NO PRESIONAR \"ORDEN RECIBIDA\" HASTA TENER TODOS LOS PRODUCTOS");

        // Fetch and process cart products
        fetchAndProcessCartProducts();

        // Set up listener for the order received button
        orderReceivedButton.setOnClickListener(v -> {
            markOrderAsReceived();
            Toast.makeText(PagoExitosoActivity.this, "Todos los productos han sido recibidos", Toast.LENGTH_SHORT).show();

        });

        // Set up listener for the toggle map button
        toggleMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(PagoExitosoActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }

    // Method to fetch and process cart products.
    private void fetchAndProcessCartProducts() {
        firebase.fetchCartProducts((productName, productPrice, quantity, address) -> {
            addProductToList(productName, productPrice, quantity);
            totalCost += productPrice * quantity;
            totalCostTextView.setText("Costo total: $" + totalCost);
            generateAndSaveOrderTickets(productName, productPrice, quantity, address);
        });
    }

    // Method to add a product to the product list in the user interface.
    private void addProductToList(String productName, double productPrice, int quantity) {
        View productView = getLayoutInflater().inflate(R.layout.item_product_pago, null);
        TextView productNameTextView = productView.findViewById(R.id.product_name);
        TextView productPriceTextView = productView.findViewById(R.id.product_price);
        TextView productQuantityTextView = productView.findViewById(R.id.product_quantity);
        productNameTextView.setText(productName);
        productPriceTextView.setText("$" + productPrice);
        productQuantityTextView.setText("Cantidad: " + quantity);

        productsLayout.addView(productView);
    }

    // Method to generate and save order tickets in the database.
    private void generateAndSaveOrderTickets(String productName, double productPrice, int quantity, String address) {
        if (orderTicket == null) {
            orderTicket = generateOrderTicket();
            orderTicketValueTextView.setText("Order Ticket: " + orderTicket);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderTicket", orderTicket);
        orderData.put("userId", firebase.getUSERID());
        orderData.put("totalCost", totalCost);
        orderData.put("otherUserConfirmedDelivery", false); // Add this line

        FirebaseFirestore db = firebase.getDB();

        db.collection("orders")
                .document(orderTicket)
                .set(orderData)
                .addOnSuccessListener(documentReference -> {
                    addProductToSubCollection(db, orderTicket, address, productName, productPrice, quantity);
                    Log.d("OrderTicket", "Order saved: " + orderTicket);
                })
                .addOnFailureListener(e -> Log.e("OrderTicket", "Error saving order: ", e));
    }

    // Method to add a product to the product sub-collection in the database.
    private void addProductToSubCollection(FirebaseFirestore db, String orderTicket, String address, String productName, double productPrice, int quantity) {
        db.collection("orders")
                .document(orderTicket)
                .collection("addresses")
                .document(address)
                .collection("products")
                .document()
                .set(createProductMap(productName, productPrice, quantity))
                .addOnSuccessListener(aVoid -> Log.d("Product", "Product added to " + address + ": " + productName))
                .addOnFailureListener(e -> Log.e("Product", "Error adding product: ", e));
    }

    // Method to create a product data map.
    private Map<String, Object> createProductMap(String productName, double productPrice, int quantity) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", productName);
        productData.put("productPrice", productPrice);
        productData.put("quantity", quantity);
        return productData;
    }

    // Method to generate a random order ticket.
    private String generateOrderTicket() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder ticket = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            ticket.append(letters.charAt(random.nextInt(letters.length())));
        }
        for (int i = 0; i < 3; i++) {
            ticket.append(random.nextInt(10));
        }

        return ticket.toString();
    }

    // Method to mark an order as received.
    private void markOrderAsReceived() {
        FirebaseFirestore db = firebase.getDB();
        db.collection("orders")
                .document(orderTicket)
                .update("otherUserConfirmedDelivery", true)
                .addOnSuccessListener(aVoid -> checkAndDeleteOrder(orderTicket));
    }

    // Method to check if both parties have confirmed the delivery of an order and, if so, delete the order from the database.
    private void checkAndDeleteOrder(String orderTicket) {
        FirebaseFirestore db = firebase.getDB();
        db.collection("orders")
                .document(orderTicket)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean userConfirmedDelivery = documentSnapshot.getBoolean("userConfirmedDelivery");
                    Boolean otherUserConfirmedDelivery = documentSnapshot.getBoolean("otherUserConfirmedDelivery");

                    if (Boolean.TRUE.equals(userConfirmedDelivery) && Boolean.TRUE.equals(otherUserConfirmedDelivery)) {
                        db.collection("orders")
                                .document(orderTicket)
                                .delete()
                                .addOnSuccessListener(aVoid -> {

                                });
                    }
                });
    }
}