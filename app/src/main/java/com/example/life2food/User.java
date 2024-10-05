package com.example.life2food;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String profileImage;


    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImage = "https://firebasestorage.googleapis.com/v0/b/life2food-ec030.appspot.com/o/profile_images%2Fdefault_profile.png?alt=media&token=6caf9356-80ca-419d-b894-bd58d2878631";
    }

    // Getters y Setters
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
