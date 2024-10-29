


package com.example.life2food;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String street;
    private String numberStreet;
    private String city;
    private String postalCode;
    private String profileImage;

    // Constructor vacío para Firestore
    public User() {
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImage = "https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/profile_images%2Fdefault_profile.png?alt=media&token=6caf9356-80ca-419d-b894-bd58d2878631";
    }

    public User(String firstName, String lastName, String email, String street, String numberStreet, String city, String postalCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.street = street;
        this.numberStreet = numberStreet;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumberStreet() {
        return numberStreet;
    }

    public void setNumberStreet(String numberStreet) {
        this.numberStreet = numberStreet;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
