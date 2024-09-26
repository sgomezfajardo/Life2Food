package com.example.life2food;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EcommerceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);

        // Inicializar RecyclerView y lista de productos
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the dialog layout
                View dialogView = getLayoutInflater().inflate(R.layout.add_product_dialog, null);

                // Get references to the input fields and buttons
                EditText productNameEditText = dialogView.findViewById(R.id.product_name_edittext);
                EditText productPriceEditText = dialogView.findViewById(R.id.product_price_edittext);
                Button saveButton = dialogView.findViewById(R.id.save_button);
                Button cancelButton = dialogView.findViewById(R.id.cancel_button);

                // Create and show the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EcommerceActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();

                // Set up click listeners for the buttons
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String productName = productNameEditText.getText().toString();
                        double productPrice = Double.parseDouble(productPriceEditText.getText().toString());
                        addProduct(productName, productPrice);
                        dialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        // Agregar algunos productos de ejemplo
        productList.add(new Product("Papas margarita", 10.00));
        productList.add(new Product("Postobon manzana", 15.00));
        productList.add(new Product("Coca cola flexy", 20.00));

        // Configurar el adaptador del RecyclerView
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);
    }

    // Método para agregar productos
    private void addProduct(String name, double price) {
        productList.add(new Product(name, price));
        productAdapter.notifyItemInserted(productList.size() - 1);
    }

    // Método para eliminar productos
    private void removeProduct(int position) {
        if (position >= 0 && position < productList.size()) {
            productList.remove(position);
            productAdapter.notifyItemRemoved(position);
        }
    }
}
