package com.example.gossipapp;

public class UserAccessModel {

    private String userId;
    private String username;
    private String email;
    private String avatarResName;
    private long notesAccessExpiry = 0;

    public UserAccessModel() {}

    public UserAccessModel(String userId, String username, String email, String avatarResName, long notesAccessExpiry) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarResName = avatarResName;
        this.notesAccessExpiry = notesAccessExpiry;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username != null ? username : "Unknown";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email != null ? email : "No email";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarResName() {
        return avatarResName != null ? avatarResName : "default_avatar";
    }

    public void setAvatarResName(String avatarResName) {
        this.avatarResName = avatarResName;
    }

    public long getNotesAccessExpiry() {
        return notesAccessExpiry;
    }

    public void setNotesAccessExpiry(long notesAccessExpiry) {
        this.notesAccessExpiry = notesAccessExpiry;
    }

    /**
     * Checks if the user currently has access to notes.
     * @return true if access is permanent (-1) or still within the expiry time.
     */
    public boolean hasNotesAccess() {
        if (notesAccessExpiry == -1) return true; // permanent
        if (notesAccessExpiry == 0) return false; // no access
        return notesAccessExpiry > System.currentTimeMillis(); // timed access still valid
    }

    /**
     * Returns a readable access status string (useful for UI).
     */
    public String getAccessStatusText() {
        if (notesAccessExpiry == -1) return "Permanent Access";
        if (notesAccessExpiry == 0) return "No Access";

        long remaining = notesAccessExpiry - System.currentTimeMillis();
        if (remaining <= 0) return "Expired";

        long hours = remaining / (1000 * 60 * 60);
        if (hours < 1) return "Access active (<1h left)";
        return "Access active (" + hours + "h left)";
    }
}
