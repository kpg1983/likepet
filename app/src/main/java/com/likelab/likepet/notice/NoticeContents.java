package com.likelab.likepet.notice;


class NoticeContents {

    String title;
    String description;
    String registryDate;
    int image;
    int flag;

    String noticeId;
    int readCount;
    String noticeType;
    String userId;
    String writerName;
    String modifyDate;
    String language;

    NoticeContents(String noticeId, String title, String description, int readCount, String noticeType, String registryDate, String userId,
                   String writerName, String modifyDate, String language) {

        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.readCount = readCount;
        this.noticeType = noticeType;
        this.registryDate = registryDate;
        this.userId = userId;
        this.writerName = writerName;
        this.modifyDate = modifyDate;
        this.language = language;
        flag = 0;
    }

}