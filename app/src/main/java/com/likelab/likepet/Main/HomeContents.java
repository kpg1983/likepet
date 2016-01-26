package com.likelab.likepet.Main;

/**
 * Created by kpg1983 on 2015-11-12.
 */
public class HomeContents {

    int leftContents;
    int rightContents;

    String firstLeftTag;
    String secondLeftTag;

    String firstRightTag;
    String secondRightTag;

    int contentsType;



    String pageId;
    String language;
    String status;
    String registryDate;
    String displayStartDate;
    String displayEndDate;
    String groupId;

    int feedCount;
    int readCount;

    String thumbnailType;
    String groupRegistryDate;
    String ownerName;
    String thumbnailUrl;
    String description;

    String pageIdRight;
    String languageRight;
    String statusRight;
    String registryDateRight;
    String displayStartDateRight;
    String displayEndDateRight;
    String groupIdRight;

    int feedCountRight;
    int readCountRight;

    String thumbnailTypeRight;
    String groupRegistryDateRight;
    String ownerNameRight;
    String thumbnailUrlRight;
    String descriptionRight;

    public HomeContents(int leftContents, int rightContents, String firstLeftTag, String secondLeftTag, String firstRightTag, String secondRightTag, int contentsType) {

        this.leftContents = leftContents;
        this.rightContents = rightContents;

        this.firstLeftTag = firstLeftTag;
        this.firstRightTag = firstRightTag;

        this.secondLeftTag = secondLeftTag;
        this.secondRightTag = secondRightTag;

        this.contentsType = contentsType;

    }

    public HomeContents(int leftContents, String firstLeftTag, String secondLeftTag, int contentsType) {

        this.leftContents = leftContents;
        this.firstLeftTag = firstLeftTag;
        this.secondLeftTag = secondLeftTag;
        this.contentsType = contentsType;

    }

    public HomeContents(String pageId, String language, String status, String registryDate, String displayStartDate, String displayEndDate, String groupId,
                        int feedCount, int readCount, String thumbnailType, String groupRegistryDate, String ownerName, String thumbnailUrl, String description) {

        this.pageId = pageId;
        this.language = language;
        this.registryDate = registryDate;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.groupId = groupId;
        this.feedCount = feedCount;
        this.readCount = readCount;
        this.thumbnailType = thumbnailType;
        this.groupRegistryDate = groupRegistryDate;
        this.ownerName = ownerName;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.status = status;
    }

    public HomeContents(String pageId, String language, String status, String registryDate, String displayStartDate, String displayEndDate, String groupId,
                        int feedCount, int readCount, String thumbnailType, String groupRegistryDate, String ownerName, String thumbnailUrl, String description,
                        String pageIdRight, String languageRight, String statusRight, String registryDateRight, String displayStartDateRight, String displayEndDateRight, String groupIdRight,
                        int feedCountRight, int readCountRight, String thumbnailTypeRight, String groupRegistryDateRight, String ownerNameRight, String thumbnailUrlRight, String descriptionRight) {

        this.pageId = pageId;
        this.language = language;
        this.registryDate = registryDate;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.groupId = groupId;
        this.feedCount = feedCount;
        this.readCount = readCount;
        this.thumbnailType = thumbnailType;
        this.groupRegistryDate = groupRegistryDate;
        this.ownerName = ownerName;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.status = status;

        this.pageIdRight = pageIdRight;
        this.languageRight = languageRight;
        this.registryDateRight = registryDateRight;
        this.displayStartDateRight = displayStartDateRight;
        this.displayEndDateRight = displayEndDateRight;
        this.statusRight = statusRight;
        this.groupIdRight = groupIdRight;
        this.feedCountRight = feedCountRight;
        this.readCountRight = readCountRight;
        this.thumbnailTypeRight = thumbnailTypeRight;
        this.groupRegistryDateRight = groupRegistryDateRight;
        this.ownerNameRight = ownerNameRight;
        this.thumbnailUrlRight = thumbnailUrlRight;
        this.descriptionRight = descriptionRight;


    }



}
