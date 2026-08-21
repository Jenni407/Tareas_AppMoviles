package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbShowPassword = findViewById(R.id.cbShowPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Evento para mostrar u ocultar contraseña
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int selectionStart = etPassword.getSelectionStart();
            int selectionEnd = etPassword.getSelectionEnd();

            if (isChecked) {
                // Mostrar contraseña
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Ocultar contraseña
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            // Restaurar la posición del cursor para que no vuelva al inicio
            etPassword.setSelection(selectionStart, selectionEnd);
        });

        // Evento "Registrarse"
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                mostrarAlerta("¡Bienvenido!", "Usuario registrado con éxito.", () -> {
                                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                String mensajeError = obtenerMensajeError(task.getException());
                                mostrarAlerta("Error al registrarse", mensajeError, null);
                            }
                        });
            } else {
                mostrarAlerta("Campos incompletos", "Por favor ingresa un correo y una contraseña.", null);
            }
        });

        // Evento "Iniciar Sesión"
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String mensajeError = obtenerMensajeError(task.getException());
                                mostrarAlerta("Error de autenticación", mensajeError, null);
                            }
                        });
            } else {
                mostrarAlerta("Campos incompletos", "Por favor ingresa un correo y una contraseña.", null);
            }
        });
    }

    // Método auxiliar para mostrar alerta
    private void mostrarAlerta(String titulo, String mensaje, Runnable accionAlCerrar) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    if (accionAlCerrar != null) {
                        accionAlCerrar.run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // Traduce las excepciones de Firebase a español
    private String obtenerMensajeError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "El formato del correo electrónico no es válido.";
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_MISMATCH":
                    return "La contraseña es incorrecta.";
                case "ERROR_USER_NOT_FOUND":
                    return "No existe ninguna cuenta registrada con este correo.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Este correo electrónico ya está registrado por otro usuario.";
                case "ERROR_WEAK_PASSWORD":
                    return "La contraseña es muy débil. Debe tener al menos 6 caracteres.";
                default:
                    return "Ocurrió un error con las credenciales. Intenta de nuevo.";
            }
        }
        return "No se pudo conectar con el servidor. Revisa tu conexión a internet.";
    }
}