package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PagoExitosoActivity extends AppCompatActivity {

    private LinearLayout productsLayout;
    private TextView totalCostTextView;
    private Button toggleMapButton;
    private Firebase firebase;
    private double totalCost = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_exitoso);

        productsLayout = findViewById(R.id.products_layout);
        totalCostTextView = findViewById(R.id.total_cost);
        toggleMapButton = findViewById(R.id.toggle_map_button);
        firebase = new Firebase();

        fetchCartProducts();

        toggleMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(PagoExitosoActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }

    private void fetchCartProducts() {
        firebase.fetchCartProducts(new Firebase.OnCartProductsFetchedListener() {
            @Override
            public void onProductFetched(String productName, double productPrice, int quantity) {
                agregarProductoALista(productName, productPrice, quantity);
                totalCost += productPrice * quantity;
                totalCostTextView.setText("Costo Total: $" + totalCost); // Muestra el costo total
            }
        });
    }

    private void agregarProductoALista(String productName, double productPrice, int quantity) {
        View productView = getLayoutInflater().inflate(R.layout.item_product_pago, null);
        TextView productNameTextView = productView.findViewById(R.id.product_name);
        TextView productPriceTextView = productView.findViewById(R.id.product_price);
        TextView productQuantityTextView = productView.findViewById(R.id.product_quantity); // Asegúrate de que este TextView exista en tu layout

        productNameTextView.setText(productName);
        productPriceTextView.setText("$" + productPrice);
        productQuantityTextView.setText("Cantidad: " + quantity);

        productsLayout.addView(productView);
    }
}
