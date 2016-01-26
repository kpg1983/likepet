package com.likelab.likepet.mypage;

/**
 * Created by kpg1983 on 2015-09-17.
 *
 * 마이페이지에 들어갈 콘텐츠 황목들 클래스
 */
public class Contents {

    int mainContent;

    String bestCommentUrl[] = new String[3];
    String bestCommentType[] = new String[3];
    String bestCommentDescription[] = new String[3];

    int blackFlag;
    String status;

    int numberOfBestComment;
    int contentType;
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

    Contents(String contentsUrl, String contentsType, String registryDate, int likeCount, int numberOfBestComment, String firstBestCommentUrl, String secondBestCommentUrl, String thirdBestCommentUrl,
             String firstBestCommentType, String secondBestCommentType, String thirdBestCommentType, String firstBestCommentDescription, String secondBestCommentDescription, String thirdBestCommentDescription, int commentCount, int blackFlag
            , String contentId, String iLikeThis, String descriptionTag, String userId, String videoScreenShotUrl, String status, int reportCount, int mediaWidth, int mediaHeight) {

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
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
    }

}
