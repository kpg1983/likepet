package com.likelab.likepet.upload;

import android.graphics.Bitmap;

/**
 * Created by darkjun99 on 12/24/15.
 */
public class UploadGalleryMediaItem {
    Integer contentsType; // 0 - image, 1 - video
    Integer imageId, videoId, dateModified;
    Bitmap thumbnailBitmap, imageContentsBitmap;
    String imageContentsFilePath, videoContentsFilePath;
    String durationString;
    Boolean isSelected;

    public UploadGalleryMediaItem(Integer contentsType, Integer imageId, Integer videoId, Integer dateModified) {
        this.contentsType = contentsType;
        this.imageId = imageId;
        this.videoId = videoId;
        this.dateModified = dateModified;
        this.isSelected = false;
    }

    public Integer getContentsType() {
        return contentsType;
    }
    public void setContentsType(Integer contentsType) {
        this.contentsType = contentsType;
    }
    public Integer getImageId() {
        return imageId;
    }
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
    public Integer getVideoId() {
        return videoId;
    }
    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }
    public Integer getDateModified() {
        return dateModified;
    }
    public void setDateModified(Integer dateModified) {
        this.dateModified = dateModified;
    }
    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }
    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }
    public Bitmap getImageContentsBitmap() {
        return imageContentsBitmap;
    }
    public void setImageContentsBitmap(Bitmap imageContentsBitmap) {
        this.imageContentsBitmap = imageContentsBitmap;
    }
    public String getImageContentsFilePath() {
        return imageContentsFilePath;
    }
    public void setImageContentsFilePath(String imageContentsFilePath) {
        this.imageContentsFilePath = imageContentsFilePath;
    }
    public String getVideoContentsFilePath() {
        return videoContentsFilePath;
    }
    public void setVideoContentsFilePath(String videoContentsFilePath) {
        this.videoContentsFilePath = videoContentsFilePath;
    }
    public String getDurationString() {
        return durationString;
    }
    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }
    public Boolean getIsSelected() {
        return isSelected;
    }
    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }
}
