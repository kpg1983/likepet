package com.likelab.likepet.Feed;

/**
 * Created by kpg1983 on 2015-10-08.
 */
public class FeedContents {

    int mainContent;
    int blackFlag;
    int numberOfBestComment;
    int likeCount;
    int readCount;
    int reportCount;
    int commentCount;
    int mediaWidth, mediaHeight;

    String contentId;
    String userId;
    String description;
    String registryDate;
    String contentUrl;
    String name;
    String status;
    String contentType;
    String profileImageUrl;
    String clan;
    String gender;
    String iLikeThis;
    String videoScreenshotUrl;

    String bestCommentUrl[] = new String[3];
    String bestCommentType[] = new String[3];
    String bestCommentDescription[] = new String[3];

    FeedContents(String contentId, String userId, String description, String contentType, String registryDate, String contentUrl, int likeCount,
                 String name, String status, String profileImageUrl, String clan, String gender, int readCount, int reportCount, int commentCount, int numberOfBestComment, String firstBestCommentUrl, String secondBestCommentUrl, String thirdBestCommentUrl,
                 String firstBestCommentType, String secondBestCommentType, String thirdBestCommentType, String firstBestCommentDescription, String secondBestCommentDescription, String thirdBestCommentDescription, String iLikeThis
    , String videoScreenshotUrl, int mediaWidth, int mediaHeight) {

        this.contentId = contentId;
        this.userId =userId;
        this.contentType = contentType;
        this.registryDate = registryDate;
        this.contentUrl = contentUrl;
        this.likeCount = likeCount;
        this.name = name;
        this.status = status;
        this.profileImageUrl = profileImageUrl;
        this.clan = clan;
        this.gender = gender;
        this.readCount = readCount;
        this.reportCount = reportCount;
        this.commentCount = commentCount;
        this.description = description;
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
        this.iLikeThis = iLikeThis;
        this.videoScreenshotUrl = videoScreenshotUrl;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }

}
