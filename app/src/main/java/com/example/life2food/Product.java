package com.example.life2food;

import com.google.firebase.firestore.FirebaseFirestore;

public class Product {

    //Product info
    private String id;
    private String name;
    private int quantity;
    private String type;
    private String email;
    private double price;
    private String imageUrl;
    private String description;
    private String adress;


    //Firebase
    private final Firebase firebase = new Firebase();
    private final FirebaseFirestore DB = firebase.getDB();;
    private final String USERID = firebase.getUSERID();


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
        this.adress = adress;
    }

    public Product(String id, String name, int quantity, String type, String email, double price, String imageUrl, String description) {
        this.id = id;
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

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
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
        DB.collection("products").document(getId()).update("quantity", getQuantity());
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