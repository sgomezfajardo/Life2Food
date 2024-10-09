package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartActivity extends AppCompatActivity {

    Button back;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userCart;
    String items = " ";
    String productName;
    String productQuantity;
    String productPrice;
    String cart = "";
    int times = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, EcommerceActivity.class);
            startActivity(intent);
            finish();
        });

        userCart = findViewById(R.id.cart);

        db.collection("carts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {




                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                items = document.getData().toString();
                            }
                            while(items.length() > 100){
                                getInfo();
                            }


                            userCart.setText(cart);
                        } else {
                            userCart.setText("Error al obtener productos");
                        }
                    }
                });
    }

    private void getInfo(){

        if (times == 0) {
            items = items.substring(items.indexOf("quantity="));
            productQuantity = items.substring(9, items.indexOf(","));
            items = items.substring(items.indexOf(","));
            items = items.substring(items.indexOf("price="));
            productPrice = items.substring(6, items.indexOf(","));
            items = items.substring(items.indexOf(","));
            items = items.substring(items.indexOf("productName="));
            productName = items.substring(12, items.indexOf("}") -1 );
            items = items.substring(items.indexOf(","));

            cart += "Nombre del producto: " + productName + "\n" +
                    "Cantidad: " + productQuantity + "\n" +
                    "Precio: " + productPrice +  "\n";
            times++;
        } else {
            cart += "\n";
            items = items.substring(2);
            items = items.substring(items.indexOf("quantity="));
            productQuantity = items.substring(9, items.indexOf(","));
            items = items.substring(items.indexOf(","));
            items = items.substring(items.indexOf("price="));
            productPrice = items.substring(6, items.indexOf(","));
            items = items.substring(items.indexOf(","));
            items = items.substring(items.indexOf("productName="));
            productName = items.substring(12, items.indexOf("}"));
            items = items.substring(items.indexOf("}"));

            cart += "Nombre del producto: " + productName + "\n" +
                    "Cantidad: " + productQuantity + "\n" +
                    "Precio: " + productPrice+ "\n";
        }
    }
}
