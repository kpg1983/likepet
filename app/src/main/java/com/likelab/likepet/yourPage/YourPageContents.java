package com.likelab.likepet.yourPage;

/**
 * Created by kpg1983 on 2016-01-03.
 */
public class YourPageContents {

    String bestCommentUrl[] = new String[3];
    String bestCommentType[] = new String[3];
    String bestCommentDescription[] = new String[3];

    int blackFlag;
    String status;

    int numberOfBestComment;
    int likeCount;
    int commentCount;
    int reportCount;
    int mediaWidth;
    int mediaHeight;

    String contentsId;
    String descriptionTag;
    String contentsUrl;
    String contentsType;
    String registryDate;
    String iLikeThis;

    String userId;
    String videoScreenShotUrl;
    String profileImageUrl;
    String name;


    YourPageContents(String contentsUrl, String contentsType, String registryDate, int likeCount, int numberOfBestComment, String firstBestCommentUrl, String secondBestCommentUrl, String thirdBestCommentUrl,
             String firstBestCommentType, String secondBestCommentType, String thirdBestCommentType, String firstBestCommentDescription, String secondBestCommentDescription, String thirdBestCommentDescription, int commentCount, int blackFlag
            , String contentId, String iLikeThis, String descriptionTag, String userId, String videoScreenShotUrl, String status, int reportCount, String profileImageUrl, String name, int mediaWidth, int mediaHeight) {

        this.contentsUrl = contentsUrl;
        this.contentsType = contentsType;
        this.likeCount = likeCount;
        this.registryDate = registryDate;
        this.numberOfBestComment = numberOfBestComment;
        this.bestCommentUrl[0] = firstBestCommentUrl;
        this.bestCommentUrl[1] = secondBestCommentUrl;
        this.bestCommentUrl[2] = thirdBestCommentUrl;
        this.bestCommentType[0] = firstBestCommentType;
        this.bestCommentType[1] = secondBestCommentType;
        this.bestCommentType[2] = thirdBestCommentType;
        this.bestCommentDescription[0] = firstBestCommentDescription;
        this.bestCommentDescription[1] = secondBestCommentDescription;
        this.bestCommentDescription[2] = thirdBestCommentDescription;
        this.commentCount = commentCount;
        this.blackFlag = blackFlag;
        this.contentsId = contentId;
        this.iLikeThis = iLikeThis;
        this.descriptionTag = descriptionTag;
        this.userId = userId;
        this.videoScreenShotUrl = videoScreenShotUrl;
        this.status = status;
        this.reportCount = reportCount;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }
}
