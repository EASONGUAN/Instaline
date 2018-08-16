package com.example.android.instalineenterprise;

/**
 * Created by GOR on 2018-03-26.
 */

public class User {
    private String name;
    private String profileImage;
    private int queueNumber;

    public User(String name, String profileImage, int queueNumber){
        this.name = name;
        this.profileImage = profileImage;
        this.queueNumber = queueNumber;
    }

    public String getName(){
        return name;
    }

    public String getProfileImage(){
        return profileImage;
    }

    public int getQueueNumber(){
        return queueNumber;
    }


}
