package com.likelab.likepet.view;

/**
 * Created by kpg1983 on 2015-10-12.
 */
public class ViewContents {

    String mUserProfileName;
    String mTxtComment;

    String commentId;
    String commentUrl;
    String contentType;
    String registryDate;

    String contentId;

    int likeCount;
    int reportCount;

    String profileImageUrl;
    String gender;
    String clan;
    String iLikeThis;
    String userId;

    int bestCommentFlag;


    ViewContents(String commentId, String description, String commentUrl, String contentType, String registryDate, int likeCount, int reportCount, String mUserProfileName, String profileImageUrl, String gender, String clan, String contentId, String iLikeThis, String userId, int bestCommentFlag) {
        this.commentId = commentId;
        this.mTxtComment = description;
        this.commentUrl = commentUrl;
        this.contentType = contentType;
        this.registryDate = registryDate;
        this.likeCount = likeCount;
        this.reportCount = reportCount;
        this.mUserProfileName = mUserProfileName;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
        this.clan = clan;
        this.contentId = contentId;
        this.iLikeThis = iLikeThis;
        this.userId = userId;
        this.bestCommentFlag = bestCommentFlag;
    }

}
