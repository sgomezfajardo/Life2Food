package com.example.life2food;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button back;
    private String items = " ";
    private String cart = "";
    private LinearLayout linearLayout;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String userId = String.valueOf(currentUser.getUid());

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

        db.collection("carts")
                .whereEqualTo("id_usuario", userId) //
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                items = document.getData().toString();
                            }
                            if(items.length()<100){
                                ImageView imageView = new ImageView(CartActivity.this);

                                Glide.with(CartActivity.this)
                                                .load("https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/img%2Fcart.png?alt=media&token=42b49254-1d55-4546-afdd-7a310869d030")
                                                .into(imageView);

                                linearLayout.addView(imageView, linearLayout.indexOfChild(back));

                            } else {
                                while(items.length() > 100){
                                    getInfo();
                                }
                            }

                        } else {
                            //userCart.setText("Error al obtener productos");
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
        ImageView imageView = new ImageView(CartActivity.this);

        Glide.with(CartActivity.this)
                .load("https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/img%2Fcart.png?alt=media&token=42b49254-1d55-4546-afdd-7a310869d030")
                .into(imageView);

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
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(getResources().getColor(android.R.color.holo_orange_light));
        drawable.setCornerRadius(60);
        Button newDeleteButton = new Button(this);
        newDeleteButton.setText("Eliminar producto del carrito");
        newDeleteButton.setBackground(drawable);
        newDeleteButton.setTextColor(Color.WHITE);
        newDeleteButton.setTextSize(11);
        newDeleteButton.setPadding(30, 2, 30, 2);
        newDeleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView newTextView = new TextView(this);
        newTextView.setText(cart);
        ImageView newImage = new ImageView(this);
        Glide.with(this).load(image_url).override(350,350).into(newImage);
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(layoutParams);
        cardView.setRadius(16);
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(16, 16, 16, 16);
        innerLayout.addView(newImage);
        innerLayout.addView(newTextView);
        innerLayout.addView(newDeleteButton);
        cardView.addView(innerLayout);
        cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        newDeleteButton.setOnClickListener(v -> {
            db.collection("products").document(productId).update("quantity", FieldValue.increment(Integer.parseInt(productQuantity)));
            Map<String, Object> itemToRemove = new HashMap<>();
            itemToRemove.put("imageUrl", image_url);
            itemToRemove.put("price", Integer.parseInt(productPrice.substring(0, productPrice.indexOf("."))));
            itemToRemove.put("productId", productId);
            itemToRemove.put("productName", productName);
            itemToRemove.put("quantity", Integer.parseInt(productQuantity));
            removeItemFromCart(productId, itemToRemove);
            linearLayout.removeView(cardView);
            if(linearLayout.getChildCount()<=2){
                linearLayout.addView(imageView, linearLayout.indexOfChild(back));
            }
        });
        linearLayout.addView(cardView, linearLayout.indexOfChild(back));
        cart = "";
    }

    private void removeItemFromCart(String productId, Map<String, Object> itemToRemove) {

        db.collection("carts").document(userId)
                .update("items", FieldValue.arrayRemove(itemToRemove))
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {

                });
    }
}