package com.example.life2food;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PagoExitosoActivity extends AppCompatActivity {

    private LinearLayout productsLayout;
    private TextView totalCostTextView;
    private TextView orderTicketValueTextView;
    private Button toggleMapButton;
    private Firebase firebase;
    private double totalCost = 0.0;
    private String orderTicket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_exitoso);

        // References to views
        productsLayout = findViewById(R.id.products_layout);
        totalCostTextView = findViewById(R.id.total_cost);
        orderTicketValueTextView = findViewById(R.id.order_ticket_value);
        toggleMapButton = findViewById(R.id.toggle_map_button);
        firebase = new Firebase();
        fetchAndProcessCartProducts();
    }

    // Calculate total price. Group products by store and generate OrderTicket
    private void fetchAndProcessCartProducts() {
        firebase.fetchCartProducts((productName, productPrice, quantity, address) -> {
            agregarProductoALista(productName, productPrice, quantity);
            totalCost += productPrice * quantity;
            totalCostTextView.setText("Costo Total: $" + totalCost);
            generateAndSaveOrderTickets(productName, productPrice, quantity, address);
        });
    }

    // Add products to the list
    private void agregarProductoALista(String productName, double productPrice, int quantity) {
        View productView = getLayoutInflater().inflate(R.layout.item_product_pago, null);
        TextView productNameTextView = productView.findViewById(R.id.product_name);
        TextView productPriceTextView = productView.findViewById(R.id.product_price);
        TextView productQuantityTextView = productView.findViewById(R.id.product_quantity);
        productNameTextView.setText(productName);
        productPriceTextView.setText("$" + productPrice);
        productQuantityTextView.setText("Cantidad: " + quantity);

        productsLayout.addView(productView);
    }

    // Create an OrderTicket. Save the order in the database. call the method to add the products to the corresponding subcollection.
    private void generateAndSaveOrderTickets(String productName, double productPrice, int quantity, String address) {

        if (orderTicket == null) {
            orderTicket = generateOrderTicket();
            orderTicketValueTextView.setText("Order Ticket: " + orderTicket);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderTicket", orderTicket);
        orderData.put("userId", firebase.getUSERID());
        orderData.put("totalCost", totalCost);

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

    // Method to add products to the subcollection
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

    // Create a map with product details
    private Map<String, Object> createProductMap(String productName, double productPrice, int quantity) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", productName);
        productData.put("productPrice", productPrice);
        productData.put("quantity", quantity);
        return productData;
    }

    // OrderTicket Generator
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
}