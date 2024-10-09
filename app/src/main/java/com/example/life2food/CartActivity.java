package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button back;
    private TextView userCart;
    private String items = " ";
    private String cart = "";
    private LinearLayout linearLayout;
    private int times = 0;
    private String userId;

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
        linearLayout = findViewById(R.id.linearLayout);
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

        String productId;
        String productName;
        String productQuantity;
        String productPrice;
        String image_url;
        if (times == 0) {
            items = items.substring(items.indexOf("id_usuario="));
            userId = items.substring(11, items.indexOf(","));
            items = items.substring(items.indexOf(","));
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
            items = items.substring(items.indexOf(","));
            cart =   productName + "\n" +
                     productQuantity + "\n" +
                     productPrice +  "\n";
            Button newDeleteButton = new Button(this);
            newDeleteButton.setText("Eliminar producto del carrito");
            newDeleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            TextView newTextView = new TextView(this);
            newTextView.setText(cart);
            ImageView newImage = new ImageView(this);
            Glide.with(this).load(image_url).override(350,350).into(newImage);
            CardView cardView = new CardView(this);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            cardView.setRadius(16);
            cardView.setCardElevation(8);
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(16, 16, 16, 16);
            innerLayout.addView(newImage);
            innerLayout.addView(newTextView);
            innerLayout.addView(newDeleteButton);
            cardView.addView(innerLayout);
            newDeleteButton.setOnClickListener(v -> {
                db.collection("products").document(productId).update("quantity", FieldValue.increment(Integer.parseInt(productQuantity)));
                Map<String, Object> itemToRemove = new HashMap<>();
                itemToRemove.put("imageUrl", image_url);
                itemToRemove.put("price", Integer.parseInt(productPrice.substring(0, productPrice.indexOf("."))));
                itemToRemove.put("productId", productId);
                itemToRemove.put("productName", productName);
                itemToRemove.put("quantity", 4);
                removeItemFromCart(productId, itemToRemove);
                linearLayout.removeView(cardView);
            });
            linearLayout.addView(cardView, linearLayout.indexOfChild(back));
            cart = "";
            times++;
        } else {
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
            cart =   productName + "\n" +
                    productQuantity + "\n" +
                    productPrice +  "\n";
            Button newDeleteButton = new Button(this);
            newDeleteButton.setText("Eliminar producto del carrito");
            newDeleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            TextView newTextView = new TextView(this);
            newTextView.setText(cart);
            ImageView newImage = new ImageView(this);
            Glide.with(this).load(image_url).override(350,350).into(newImage);
            CardView cardView = new CardView(this);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            cardView.setRadius(16);
            cardView.setCardElevation(8);
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(16, 16, 16, 16);
            innerLayout.addView(newImage);
            innerLayout.addView(newTextView);
            innerLayout.addView(newDeleteButton);
            cardView.addView(innerLayout);
            newDeleteButton.setOnClickListener(v -> {
                db.collection("products").document(productId).update("quantity", FieldValue.increment(Integer.parseInt(productQuantity)));
                Map<String, Object> itemToRemove = new HashMap<>();
                itemToRemove.put("imageUrl", image_url);
                itemToRemove.put("price", Integer.parseInt(productPrice.substring(0, productPrice.indexOf("."))));
                itemToRemove.put("productId", productId);
                itemToRemove.put("productName", productName);
                itemToRemove.put("quantity", 1);
                removeItemFromCart(productId, itemToRemove);

                linearLayout.removeView(cardView);
            });
            linearLayout.addView(cardView, linearLayout.indexOfChild(back));
            cart = "";
        }
    }

    private void removeItemFromCart(String productId, Map<String, Object> itemToRemove) {

        db.collection("carts").document(userId)
                .update("items", FieldValue.arrayRemove(itemToRemove))
                .addOnSuccessListener(aVoid -> {
                    System.out.println("eliminado");
                })
                .addOnFailureListener(e -> {
                    System.out.println("perdedor");

                });
    }
}
