package com.example.gossipapp;

public class AnnouncementModel {
    public String title;
    public String message;
    public String link;
    public long timestamp;
    public boolean visible;

    // Empty constructor needed for Firebase
    public AnnouncementModel() {}

    public AnnouncementModel(String title, String message, String link, long timestamp) {
        this.title = title;
        this.message = message;
        this.link = link;
        this.timestamp = timestamp;
        this.visible = true; // always visible when sending
    }
}
