package com.example.life2food;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RealizarPagosActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String buyerEmail;
    private double totalPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtener el total del precio desde el Intent
        totalPrice = Double.parseDouble(getIntent().getStringExtra("TOTAL"));

        // Obtener el email del usuario desde Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        buyerEmail = document.getString("email");
                        if (buyerEmail != null) {
                            iniciarPago();
                        }
                    }
                }
            });
        }
    }

    // Método para generar la firma
    private String generateSignature(String apiKey, String merchantId, String referenceCode, String amount, String currency) {
        // Crear la cadena que se usará para generar la firma
        String data = String.format("%s~%s~%s~%s~%s", apiKey, merchantId, referenceCode, amount, currency);

        // Generar la firma MD5
        return md5(data);
    }

    private String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes());
            byte[] digest = md.digest();

            // Convertir el resultado a un formato hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void iniciarPago() {
        // Valores necesarios para PayU
        String apiKey = "8osNspIaVor8L3H9U8QE6a4Ed5";
        String merchantId = "508029";
        String accountId = "512321";
        String referenceCode = "L2F" + System.currentTimeMillis();
        String currency = "COP";
        String amount = String.valueOf((int) totalPrice);
        double numericAmount = Double.parseDouble(amount); // Convertir amount a double
        double taskAmount = numericAmount * 0.05; // Calcular el 5%
        String task = String.valueOf((int) taskAmount);

        // Generar la firma
        String signature = generateSignature(apiKey, merchantId, referenceCode, amount, currency);

        Log.d("RealizarPagosActivity", "API Key: " + apiKey);
        Log.d("RealizarPagosActivity", "Merchant ID: " + merchantId);
        Log.d("RealizarPagosActivity", "Account ID: " + accountId);
        Log.d("RealizarPagosActivity", "Reference Code: " + referenceCode);
        Log.d("RealizarPagosActivity", "Amount: " + amount);
        Log.d("RealizarPagosActivity", "Currency: " + currency);
        Log.d("RealizarPagosActivity", "Signature: " + signature);
        Log.d("RealizarPagosActivity", "Buyer Email: " + buyerEmail);

        // Construir la URI de PayU
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("sandbox.checkout.payulatam.com")
                .appendPath("ppp-web-gateway-payu")
                .appendQueryParameter("merchantId", merchantId)
                .appendQueryParameter("accountId", accountId)
                .appendQueryParameter("description", "Compra en Life2Food")
                .appendQueryParameter("referenceCode", referenceCode)
                .appendQueryParameter("amount", amount)
                .appendQueryParameter("tax", task)
                .appendQueryParameter("currency", currency)
                .appendQueryParameter("signature", signature)
                .appendQueryParameter("buyerEmail", buyerEmail)
                .appendQueryParameter("responseUrl", "https://sandbox.checkout.payulatam.com/response")
                .appendQueryParameter("confirmationUrl", "https://sandbox.checkout.payulatam.com/confirmation");

        // Iniciar el intent con la URL construida
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        startActivity(intent);
    }




    }

