package com.example.android.instaline;

/**
 * Created by JERRYYI on 2018-03-04.
 */

public class RestaurantModel {
    private String id;
    private String restaurant_title;
    private String time;
    private int image;
    private String imageURL;
    private String queueRank;

    public RestaurantModel(String id, String restaurant_title, String time, int image) {
        this.id = id;
        this.restaurant_title = restaurant_title;
        this.time = time;
        this.image = image;
    }

    public RestaurantModel(String id, String restaurant_title, String time, String imageURL) {
        this.id = id;
        this.restaurant_title = restaurant_title;
        this.time = time;
        this.imageURL = imageURL;
    }

    public RestaurantModel(String id, String restaurant_title, String time, String imageURL, String queueRank){
        this.id = id;
        this.restaurant_title = restaurant_title;
        this.time = time;
        this.imageURL = imageURL;
        this.queueRank = queueRank;
    }



    public String getId() {
        return id;
    }

    public String getRestaurantTitle() {
        return restaurant_title;
    }

    public String getTime() {
        return time;
    }

    public String getImageURL(){ return imageURL; }

    public int getImage() {
        return image;
    }

    public String getQueueRank() {
        return queueRank;
    }
}
