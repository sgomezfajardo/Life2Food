package com.example.life2food;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Declarar FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Encontrar los elementos de la interfaz por su ID
        final EditText emailEditText = findViewById(R.id.email_edit_text);
        final EditText passwordEditText = findViewById(R.id.password_edit_text);
        Button loginButton = findViewById(R.id.login_button);

        // Configurar la acción del botón de inicio de sesión
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los campos de texto
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Verificar que los campos no estén vacíos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Intentar iniciar sesión con Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Si el inicio de sesión es exitoso, ir a MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Cerrar esta actividad
                            } else {
                                // Si el inicio de sesión falla, mostrar un mensaje
                                Toast.makeText(LoginActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
