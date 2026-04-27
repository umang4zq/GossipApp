package com.example.gossipapp;

public class ChatModel {
    private String id;
    private String senderEmail;
    private String receiverEmail;
    private String messageText;
    private long timestamp;
    private String status;
    private String replyTo;
    private String replyPreviewText;
    private String replyPreviewSender;
    private String imageBase64;
    private String reaction;
    private String senderName;



    public ChatModel() { }

    public ChatModel(String id,
                     String senderEmail,
                     String receiverEmail,
                     String messageText,
                     long timestamp,
                     String status,
                     String replyTo,
                     String replyPreviewText,
                     String replyPreviewSender) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.status = status;
        this.replyTo = replyTo;
        this.replyPreviewText = replyPreviewText;
        this.replyPreviewSender = replyPreviewSender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getReplyPreviewText() {
        return replyPreviewText;
    }

    public void setReplyPreviewText(String replyPreviewText) {
        this.replyPreviewText = replyPreviewText;
    }

    public String getReplyPreviewSender() {
        return replyPreviewSender;
    }

    public void setReplyPreviewSender(String replyPreviewSender) {
        this.replyPreviewSender = replyPreviewSender;
    }
    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public String getReaction() {
        return reaction;
    }

    // Setter
    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
