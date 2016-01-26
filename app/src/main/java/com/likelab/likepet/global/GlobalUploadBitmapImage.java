package com.likelab.likepet.global;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by kpg1983 on 2015-10-06.
 */
public class GlobalUploadBitmapImage {
    public static Bitmap bitmap;
    public static Bitmap bitmapCopy;
    public static Bitmap ThumbnailBitmap;
    public static Bitmap userProfileImage;

    public static ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
    public static ArrayList<Bitmap> filteredBitmapList = new ArrayList<Bitmap>();
    public static Bitmap[] bitmapArray = new Bitmap[8];

    public static void CreateThumbnail() {

        if(GlobalUploadBitmapImage.bitmap != null) {
            GlobalUploadBitmapImage.ThumbnailBitmap = Bitmap.createScaledBitmap(GlobalUploadBitmapImage.bitmap,
                    GlobalUploadBitmapImage.bitmap.getWidth() / 8, GlobalUploadBitmapImage.bitmap.getWidth() / 8, true);
        }
    }
}
