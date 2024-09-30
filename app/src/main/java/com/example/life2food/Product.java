package com.example.life2food;

public class Product {
    private String id; // ID de Firestore
    private String name;
    private int quantity;
    private String type;
    private String email; // Correo del usuario que subió el producto
    private double price; // Agregar campo para el precio

    public Product() {
        // Constructor vacío requerido para Firestore
    }

    public Product(String id, String name, int quantity, String type, String email, double price) {
        this.id = id; // Inicializa el ID de Firestore
        this.name = name;
        this.quantity = quantity;
        this.type = type;
        this.email = email;
        this.price = price; // Inicializa el precio
    }

    // Métodos getter y setter
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

    public String getUserEmail() { // Método que se utiliza para obtener el correo del usuario
        return email;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price; // Cambiado a double para reflejar el precio
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
}
