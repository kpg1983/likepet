package com.likelab.likepet.bookmark;

/**
 * Created by kpg1983 on 2015-12-03.
 */
public class BookmarkContents {

    String contentId;
    String userId;
    String description;
    String contentType;
    String contentUrl;
    int likeCount;
    int reportCount;
    int commentCount;
    int readCount;
    String writerName;
    String status;
    String registryDate;
    String recommendation;
    String iLikeThis;
    String gender;
    String clan;
    String profileImageUrl;
    String videoScreenshotUrl;

    BookmarkContents(String contentId, String userId, String description, String contentType, String contentUrl, int likeCount, int reportCount,
                     int commentCount, int readCount, String writerName, String status, String registryDate, String recommendation, String iLikeThis,
                     String gender, String clan, String profileImageUrl, String videoScreenshotUrl) {

        this.contentId = contentId;
        this.userId = userId;
        this.description = description;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
        this.likeCount = likeCount;
        this.reportCount = reportCount;
        this.commentCount = commentCount;
        this.readCount = readCount;
        this.writerName = writerName;
        this.status = status;
        this.registryDate = registryDate;
        this.recommendation = recommendation;
        this.iLikeThis = iLikeThis;
        this.gender = gender;
        this.clan = clan;
        this.profileImageUrl = profileImageUrl;
        this.videoScreenshotUrl = videoScreenshotUrl;
    }

}
