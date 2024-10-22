package com.example.life2food;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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

    public Task<String> getUserRole(String userId) {
        return DB.collection("users")
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        return document.getString("role"); // Asegúrate de que "role" sea el campo correcto en tu base de datos
                    } else {
                        throw task.getException();
                    }
                });
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



