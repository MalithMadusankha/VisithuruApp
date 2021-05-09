package com.example.visithurunewapp;

public class Contacts {

    public String userNameW, userStatusW, image;

    public Contacts(){

    }

    public Contacts(String userNameW, String statusW, String image) {
        this.userNameW = userNameW;
        this.userStatusW = statusW;
        this.image = image;
    }

    public String getUserNameW() {
        return userNameW;
    }

    public void setUserNameW(String userNameW) {
        this.userNameW = userNameW;
    }

    public String getStatusW() {
        return userStatusW;
    }

    public void setStatusW(String statusW) {
        this.userStatusW = statusW;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
