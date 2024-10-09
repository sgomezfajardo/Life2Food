package com.example.life2food;

import com.google.firebase.firestore.FirebaseFirestore;

public class Product {
    private String id;
    private String name;
    private int quantity;
    private String type;
    private String email;
    private double price;
    private String imageUrl; // Campo para la URL de la imagen
    private String description; // Nuevo campo para la descripción
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Constructor por defecto
    public Product() {
    }

    // Constructor completo (con todos los campos)
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

    // Constructor sin imagen (sobrecarga)
    public Product(String id, String name, int quantity, String type, String email, double price, String description) {
        this(id, name, quantity, type, email, price, null, description); // Llama al constructor completo con imageUrl como null
    }

    // Constructor con solo nombre y cantidad (sobrecarga)
    public Product(String id, String name, int quantity) {
        this(id, name, quantity, null, null, 0.0, null, null); // Llama al constructor completo con valores por defecto
    }

    // Getters y Setters

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
        // Actualizar Firebase cada vez que se cambie la cantidad
        if (this.id != null && !this.id.isEmpty()) {
            db.collection("products").document(getId()).update("quantity", getQuantity());
        }
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
