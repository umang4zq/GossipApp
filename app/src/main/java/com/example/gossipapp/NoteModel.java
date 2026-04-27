package com.example.gossipapp;

import java.util.List;

public class NoteModel {
    private String id;
    private String title;
    private String type; // "image" or "pdf"
    private String imageBase64;
    private List<String> chunks;
    private String ownerName;
    private String uploadDate;
    private String uploadTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public List<String> getChunks() { return chunks; }
    public void setChunks(List<String> chunks) { this.chunks = chunks; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
}
