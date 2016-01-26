package com.likelab.likepet.notification;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class AlarmContents {

    String noticeId;
    String userId;
    String contentId;
    String commentId;
    String actUserId;
    String actUserName;
    String registryDate;
    String notifyType;
    String profileImageUrl;
    String gender;
    String clan;

   AlarmContents(String noticeId, String userId, String contentId, String commentId, String actUserId, String actUserName, String registryDate,
                 String notifyType, String profileImageUrl, String gender, String clan) {

       this.noticeId = noticeId;
       this.userId = userId;
       this.contentId = contentId;
       this.commentId = commentId;
       this.actUserId = actUserId;
       this.actUserName = actUserName;
       this. registryDate = registryDate;
       this.notifyType = notifyType;
       this.profileImageUrl = profileImageUrl;
       this.gender = gender;
       this.clan = clan;
   }
}
