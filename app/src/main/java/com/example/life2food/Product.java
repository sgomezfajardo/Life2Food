package com.example.life2food;

import com.google.firebase.firestore.FirebaseFirestore;

public class Product {
    private String id;
    private String name;
    private int quantity;
    private String type;
    private String email;
    private double price;
    private String imageUrl;
    private String description;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Product() {
    }

    public Product(String name, int quantity, String type, String email, double price, String imageUrl, String description) {
        this.name = name;
        this.quantity = quantity;
        this.type = type;
        this.email = email;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserEmail() {
        return email;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
        //db.collection("products").document(getId()).update("quantity", getQuantity());
        db.collection("products").document(String.valueOf(getId())).update("quantity", getQuantity());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}