package com.example.life2food;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
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

        getCurrentUserAddress();
    }

    // Gets the orderTicket based on the userAddress
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

    // Get all codes and filter by checkOrderTicketForUserAddress
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

    // Check tickets that contain the address of the user in session
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
                        createOrderTicketButton(orderTicket);
                    }
                });
    }

    // Creates a button with the ticket and allows you to see its associated content
    private void createOrderTicketButton(String orderTicket) {
        Button orderTicketButton = new Button(this);
        orderTicketButton.setText("Order Ticket: " + orderTicket);
        orderTicketButton.setOnClickListener(v -> showOrderTicketProducts(orderTicket));
        orderTicketsLayout.addView(orderTicketButton);
    }

    // queries the products associated with a specific order ticket and its address, then displays them in the user interface.
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