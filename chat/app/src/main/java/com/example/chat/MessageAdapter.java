package com.example.chat;

import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvSender.setText(message.getSender());

        // Manejar texto de mensaje
        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            holder.tvMessage.setText(message.getMessage());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        // Manejar la imagen con Glide
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.ivMessageImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(message.getImageUrl())
                    .into(holder.ivMessageImage);
        } else {
            holder.ivMessageImage.setVisibility(View.GONE);
        }

        // Evento al hacer clic en la imagen para verla en pantalla completa
        holder.ivMessageImage.setOnClickListener(v -> {
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                Dialog dialog = new Dialog(holder.itemView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                ImageView imageView = new ImageView(holder.itemView.getContext());

                Glide.with(holder.itemView.getContext())
                        .load(message.getImageUrl())
                        .into(imageView);

                dialog.setContentView(imageView);
                imageView.setOnClickListener(close -> dialog.dismiss());
                dialog.show();
            }
        });

        // Formatear hora
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeStr = sdf.format(new Date(message.getTimestamp()));
        holder.tvTime.setText(timeStr);

        // Alinear mensaje y mostrar estado (Palomitas) según el emisor
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String myEmail = (currentUser != null) ? currentUser.getEmail() : "";

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.layoutContainer.getLayoutParams();

        if (message.getSender() != null && message.getSender().equalsIgnoreCase(myEmail)) {
            // Mi mensaje
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.layoutContainer.setBackgroundResource(R.drawable.bubble_sent);
            holder.tvSender.setVisibility(View.GONE);

            // Mostrar estado (Palomita)
            holder.tvStatus.setVisibility(View.VISIBLE);
            if (message.isRead()) {
                holder.tvStatus.setText("✓✓");
                holder.tvStatus.setTextColor(Color.parseColor("#34B7F1")); // Azul
            } else {
                holder.tvStatus.setText("✓");
                holder.tvStatus.setTextColor(Color.parseColor("#888888")); // Gris
            }
        } else {
            // Mensaje recibido
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            holder.layoutContainer.setBackgroundResource(R.drawable.bubble_received);
            holder.tvSender.setVisibility(View.VISIBLE);
            holder.tvStatus.setVisibility(View.GONE); // No mostrar palomitas en mensajes recibidos
        }

        holder.layoutContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage, tvTime, tvStatus;
        ImageView ivMessageImage;
        LinearLayout layoutContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            layoutContainer = itemView.findViewById(R.id.layoutMessageContainer);
        }
    }
}