package com.example.android.nepalfishkeepers;

public class User {
    private String Image, Name, firebasetoken, subscription, userimage, username;
    public User() {
    }

    public User(String image, String name, String firebasetoken, String subscription, String userimage, String username) {
        Image = image;
        Name = name;
        this.firebasetoken = firebasetoken;
        this.subscription = subscription;
        this.userimage = userimage;
        this.username = username;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFirebasetoken() {
        return firebasetoken;
    }

    public void setFirebasetoken(String firebasetoken) {
        this.firebasetoken = firebasetoken;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
