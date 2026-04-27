package com.example.gossipapp;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Model representing a chat group in Firestore.
 */
public class GroupModel {

    private String groupId;           // Firestore document ID
    private String name;
    private List<String> members;     // UIDs of participants
    private String createdBy;         // UID of creator
    private Timestamp createdAt;      // Firestore server timestamp
    private String avatarBase64;      // Optional Base64-encoded avatar image

    // Required empty constructor for Firestore deserialization
    public GroupModel() {}

    public GroupModel(String groupId, String name, List<String> members,
                      String createdBy, Timestamp createdAt, String avatarBase64) {
        this.groupId = groupId;
        this.name = name;
        this.members = members;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.avatarBase64 = avatarBase64;
    }

    // Firestore ID helper methods
    public String getId() {
        return groupId;
    }

    public void setId(String id) {
        this.groupId = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }
}
