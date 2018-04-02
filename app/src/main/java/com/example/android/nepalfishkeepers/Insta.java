package com.example.android.nepalfishkeepers;

/**
 * Created by admin on 2/25/2018.
 * This contains the values from Title,Image, Description of the post, Username and
 */

public class Insta {

    private String title,desc,image,username, category;
    public Insta(){

    }

    public Insta(String title, String desc, String image, String username, String category){
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.category = category;
    }

    public String getCategory() { return category; }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDesc() {
        return desc;
    }

    public String getUsername() {
        return username;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCategory(String category) { this.category = category; }
}

