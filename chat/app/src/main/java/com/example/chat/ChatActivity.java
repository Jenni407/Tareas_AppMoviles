package com.example.chat;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MessageAdapter adapter;
    private List<Message> messageList;

    // Selector de imágenes de la galería
    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    subirImagenBase64(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Firestore y Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Enlazar los elementos de la interfaz
        RecyclerView rvMessages = findViewById(R.id.rvMessages);
        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        ImageButton btnAttachImage = findViewById(R.id.btnAttachImage);

        // Configurar RecyclerView
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // Escuchar mensajes en tiempo real
        db.collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(ChatActivity.this, "Error al leer: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            if (messageList.size() > 0) {
                                rvMessages.scrollToPosition(messageList.size() - 1);
                            }
                        });
                    }
                });

        // Evento para enviar mensaje de texto
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                FirebaseUser currentUser = auth.getCurrentUser();
                String senderEmail = (currentUser != null && currentUser.getEmail() != null)
                        ? currentUser.getEmail()
                        : "Usuario";

                Message message = new Message(senderEmail, text, null, System.currentTimeMillis());

                db.collection("chats").add(message)
                        .addOnSuccessListener(documentReference -> etMessage.setText(""))
                        .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // Evento para abrir la galería
        btnAttachImage.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
    }

    // Convierte y comprime la foto a Base64 para guardarla en Firestore
    private void subirImagenBase64(Uri imageUri) {
        Toast.makeText(this, "Procesando imagen...", Toast.LENGTH_SHORT).show();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Redimensionar para optimizar tamaño en la base de datos
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            String imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);

            FirebaseUser currentUser = auth.getCurrentUser();
            String senderEmail = (currentUser != null && currentUser.getEmail() != null)
                    ? currentUser.getEmail()
                    : "Usuario";

            Message message = new Message(senderEmail, "", imageBase64, System.currentTimeMillis());
            db.collection("chats").add(message)
                    .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error al enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}