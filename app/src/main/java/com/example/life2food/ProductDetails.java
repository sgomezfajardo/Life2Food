package com.example.life2food;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class ProductDetails extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView productName = findViewById(R.id.productName);
        TextView productDescription = findViewById(R.id.productDescription);
        TextView productPrice = findViewById(R.id.productPrice);
        TextView productQuantity = findViewById(R.id.productQuantityValue);
        ImageView productImage = findViewById(R.id.productImage);
        TextView productCreator = findViewById(R.id.productCreator);

        Product product = getIntent().getParcelableExtra("product");

        if (product != null) {
            Picasso.get().load(product.getImageUrl()).into(productImage);
            productName.setText(product.getName());
            productDescription.setText(product.getDescription());
            productPrice.setText("$" + String.valueOf(product.getPrice()));
            productQuantity.setText(String.valueOf(product.getQuantity()));
            productCreator.setText(product.getEmail());
        } else {
            finish();
        }

    }

}