package com.example.android.nepalfishkeepers;

/**
 * Created by admin on 2/25/2018.
 * This contains the values from Title,Image, Description of the post, Username and category.
 */

public class Nfk {

    private String title,desc,image,username, category, userimage, uid;
    private boolean reported;
    public int tradeCount = 0;
    private float price;

    public Nfk(){

    }

    public Nfk(String title, String desc, String image, String username,
               String category, String userimage, String uid, boolean reported, float price){
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.category = category;
        this.userimage = userimage;
        this.uid = uid;
        this.reported = reported;
        this.price = price;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }


    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
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

    public String getUsername() { return username;}

    public String getUserimage() {
        return userimage;
    }

    public String getUid() { return uid; }

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

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public void setCategory(String category) { this.category = category; }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

