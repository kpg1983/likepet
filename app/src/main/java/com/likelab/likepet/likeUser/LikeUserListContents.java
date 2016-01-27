package com.likelab.likepet.likeUser;

import java.io.Serializable;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class LikeUserListContents implements Serializable{

    String userName;
    String userGender;
    int likeType;
    public String clan;
    public String profileImageUrl;
    String userId;
    String myFriend;

    public LikeUserListContents(String userId, String name, String gender, String clan, String profileImageUrl, int likeType, String myFriend) {

        this.userId = userId;
        this.userName = name;
        this.userGender = gender;
        this.clan = clan;
        this.profileImageUrl = profileImageUrl;
        this.likeType = likeType;
        this.myFriend = myFriend;
    }
}
