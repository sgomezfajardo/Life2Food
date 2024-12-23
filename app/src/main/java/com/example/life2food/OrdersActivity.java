package com.example.life2food;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private LinearLayout orderTicketsLayout;
    private LinearLayout productsLayout;
    private TextView noOrdersTextView;
    private Firebase firebase;
    private FirebaseFirestore db;
    private String currentUserAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        orderTicketsLayout = findViewById(R.id.order_tickets_layout);
        productsLayout = findViewById(R.id.products_layout);
        noOrdersTextView = findViewById(R.id.no_orders_text);

        firebase = new Firebase();
        db = firebase.getDB();

        setupBottomNavigation();
        getCurrentUserAddress();
    }

    // Method to set up the bottom navigation bar.
    private void setupBottomNavigation() {
        ImageView profileIcon = findViewById(R.id.action_profile);
        ImageView cartIcon = findViewById(R.id.action_cart);
        ImageView supermarketIcon = findViewById(R.id.action_supermarket);
        ImageView ecommerceIcon = findViewById(R.id.action_ecommerce);

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        supermarketIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, SupermarketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        ecommerceIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    // Method to get the current address of the user.
    private void getCurrentUserAddress() {
        firebase.getUserCurrentAddress()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUserAddress = task.getResult();
                        fetchUserOrderTickets();
                    } else {
                        noOrdersTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // Method to fetch user orders from the database.
    private void fetchUserOrderTickets() {
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userOrderTickets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String orderTicket = document.getString("orderTicket");
                        checkOrderTicketForUserAddress(orderTicket, userOrderTickets);
                    }
                });
    }

    // Method to check if the user's address is associated with a specific order ticket and add the ticket to the user's order list.
    private void checkOrderTicketForUserAddress(String orderTicket, List<String> userOrderTickets) {
        db.collection("orders")
                .document(orderTicket)
                .collection("addresses")
                .document(currentUserAddress)
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        userOrderTickets.add(orderTicket);
                        createOrderTicketButtons(orderTicket);
                    }
                });
    }

    // Method to create buttons for the order tickets.
    private void createOrderTicketButtons(String orderTicket) {
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(8, 8, 8, 8);

        // "Order Ticket" button
        Button orderTicketButton = new Button(this);
        orderTicketButton.setText("Order Ticket: " + orderTicket);
        orderTicketButton.setOnClickListener(v -> showOrderTicketProducts(orderTicket));

        // Set fixed width and height for the button
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        orderTicketButton.setLayoutParams(buttonParams);

        // "Order Delivered" button
        Button deliveredButton = new Button(this);
        deliveredButton.setText("Orden entregada");
        deliveredButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_orders_background_black));
        deliveredButton.setTextColor(Color.WHITE);
        deliveredButton.setTextSize(11);
        deliveredButton.setOnClickListener(v -> markOrderAsDelivered(orderTicket));

        // Set layout parameters with a top margin to lower the position of the button
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 10, 0, 0);
        deliveredButton.setLayoutParams(params);

        buttonLayout.addView(orderTicketButton);
        buttonLayout.addView(deliveredButton);

        orderTicketsLayout.addView(buttonLayout);
    }

    // Method to mark an order as delivered by the current user.
    private void markOrderAsDelivered(String orderTicket) {
        db.collection("orders")
                .document(orderTicket)
                .update("userConfirmedDelivery", true)
                .addOnSuccessListener(aVoid -> checkAndDeleteOrder(orderTicket));
    }

    // Method to check if both parties have confirmed the delivery of an order and, if so, delete the order from the database.
    private void checkAndDeleteOrder(String orderTicket) {
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

                                })
                                .addOnFailureListener(e -> {

                                });
                    }
                });
    }

    // Method to display the products associated with a specific order ticket.
    private void showOrderTicketProducts(String orderTicket) {
        productsLayout.removeAllViews();
        db.collection("orders")
                .document(orderTicket)
                .collection("addresses")
                .document(currentUserAddress)
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        noOrdersTextView.setText("No hay productos para este ticket");
                        noOrdersTextView.setVisibility(View.VISIBLE);
                    } else {
                        noOrdersTextView.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String productName = document.getString("productName");
                            Double productPrice = document.getDouble("productPrice");
                            Long quantity = document.getLong("quantity");

                            View productView = getLayoutInflater().inflate(R.layout.item_product_order, null);
                            TextView productNameTextView = productView.findViewById(R.id.product_name);
                            TextView productPriceTextView = productView.findViewById(R.id.product_price);
                            TextView productQuantityTextView = productView.findViewById(R.id.product_quantity);

                            productNameTextView.setText(productName);
                            productPriceTextView.setText("$" + productPrice);
                            productQuantityTextView.setText("Cantidad: " + quantity);

                            productsLayout.addView(productView);
                        }
                    }
                });
    }
}