package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CartActivity extends AppCompatActivity {

    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        back = findViewById(R.id.button_back);
        back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
