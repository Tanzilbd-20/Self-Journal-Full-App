package com.hbdevbd.selfjournal.model;

import com.google.firebase.Timestamp;

public class Journal {
    private String userName;
    private String userId;
    private String title;
    private String thought;
    private String imageUrl;
    private Timestamp addedTime;

    public Journal() {
    }

    public Journal(String userName, String userId, String title, String thought, String imageUrl, Timestamp addedTime) {
        this.userName = userName;
        this.userId = userId;
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.addedTime = addedTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(Timestamp addedTime) {
        this.addedTime = addedTime;
    }
}
