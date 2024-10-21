package com.example.life2food;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Firebase {

    private FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private String USERID;


    public Firebase() {
        if (currentUser != null) {
            USERID = currentUser.getUid();
        } else {
            USERID = "Usuario no autenticado";
        }
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }

    public FirebaseFirestore getDB() {
        return DB;
    }

    public void setDB(FirebaseFirestore DB) {
        this.DB = DB;
    }
    public String getUSERID() {
        return USERID;
    }



}



