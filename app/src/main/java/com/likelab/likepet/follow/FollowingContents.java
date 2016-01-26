package com.likelab.likepet.follow;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class FollowingContents {

    int friendFlag;     //유저의 친구인지 아닌지 판단
    String userProfileImage;  //유저의 프로필 사진
    String userName;
    String clan;
    String userGender;
    String userId;

    String crossFollow;


    public FollowingContents(String userId, String userProfileImage, String userName, String userType, String userGender) {

        this.userId = userId;
        this.userProfileImage = userProfileImage;
        this.userName = userName;
        this.userGender = userGender;
        this.clan = userType;

    }

    public FollowingContents(String userId, String userProfileImage, String userName, String userType, String userGender, String crossFollow) {

        this.userId = userId;
        this.userProfileImage = userProfileImage;
        this.userName = userName;
        this.userGender = userGender;
        this.clan = userType;
        this.crossFollow = crossFollow;


    }
}
