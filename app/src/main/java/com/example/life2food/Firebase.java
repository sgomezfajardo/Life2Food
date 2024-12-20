package com.example.life2food;


import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

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
                        return document.getString("role");
                    } else {
                        throw task.getException();
                    }
                });
    }

    public void fetchUserRoleAndHideIcon(View supermarketIcon) {
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            getUserRole(currentUserId).addOnSuccessListener(userRole -> {
                if ("user".equalsIgnoreCase(userRole)) {
                    supermarketIcon.setVisibility(View.GONE);
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Error obteniendo el rol: ", e);
            });
        } else {
            Log.e("Auth", "Usuario no autenticado");
        }
    }

    // Gets the products in the current user's cart
    public void fetchCartProducts(OnCartProductsFetchedListener listener) {
        if (currentUser != null) {
            DB.collection("carts")
                    .whereEqualTo("id_usuario", USERID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                                if (items != null) {
                                    for (Map<String, Object> item : items) {
                                        String productName = (String) item.get("productName");
                                        double productPrice = ((Number) item.get("price")).doubleValue();
                                        int quantity = ((Number) item.get("quantity")).intValue();
                                        String address = (String) item.get("address");
                                        listener.onProductFetched(productName, productPrice, quantity, address);
                                    }
                                }
                            }
                        } else {
                            Log.e("Firestore", "No se encontraron productos o error en la consulta.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error al obtener el carrito: ", e);
                    });
        } else {
            Log.e("Auth", "Usuario no autenticado");
        }
    }

    public interface OnCartProductsFetchedListener {
        void onProductFetched(String productName, double productPrice, int quantity, String address);

    }

    // Method to get the current address of the user in session
    public Task<String> getUserCurrentAddress() {
        if (currentUser != null) {

            return DB.collection("users")
                    .document(USERID)
                    .get()
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            return document.getString("address");
                        } else {
                            throw task.getException();
                        }
                    });
        } else {
            return Tasks.forException(new Exception("Usuario no autenticado"));
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



