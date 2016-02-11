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

    String newStory;
    String newStoryRight;


    public HomeContents(String pageId, String language, String status, String registryDate, String displayStartDate, String displayEndDate, String groupId,
                        int feedCount, int readCount, String thumbnailType, String groupRegistryDate, String ownerName, String thumbnailUrl, String description, String newStory) {

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
        this.newStory = newStory;
    }

    public HomeContents(String pageId, String language, String status, String registryDate, String displayStartDate, String displayEndDate, String groupId,
                        int feedCount, int readCount, String thumbnailType, String groupRegistryDate, String ownerName, String thumbnailUrl, String description,
                        String pageIdRight, String languageRight, String statusRight, String registryDateRight, String displayStartDateRight, String displayEndDateRight, String groupIdRight,
                        int feedCountRight, int readCountRight, String thumbnailTypeRight, String groupRegistryDateRight, String ownerNameRight, String thumbnailUrlRight, String descriptionRight,
                        String newStory, String newStoryRight) {

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
        this.newStory = newStory;

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
        this.newStoryRight = newStoryRight;



    }



}
