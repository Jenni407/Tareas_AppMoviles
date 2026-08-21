package com.example.chat;

public class Message {
    private String sender;
    private String message;

    private String imageUrl;
    private long timestamp;

    private boolean isRead;

    // Constructor vacío necesario para que Firestore lea los datos
    public Message() {}

    public Message(String sender, String message, String imageUrl, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }

    public String getImageUrl() { return imageUrl;}

    public long getTimestamp() { return timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}