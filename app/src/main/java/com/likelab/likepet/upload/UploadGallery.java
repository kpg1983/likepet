package com.likelab.likepet.upload;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalUploadBitmapImage;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by kpg1983 on 2015-09-24.
 */
public class UploadGallery extends Fragment implements AdapterView.OnItemClickListener {

    private GridView sdcardImages;
    private GalleryImageViewAdapter imageAdapter;
    private FrameLayout videoViewContainer;
    private ImageView imageView;
    private VideoView videoView;
    RelativeLayout layout;
    private ImageView btn_next;
    LoadImagesFromSDCard loadImagesFromSDCardAsyncTask;
    private Cursor mImageCursor, mVideoCursor;
    private ArrayList<UploadGalleryMediaItem> mediaItemsArrayList;
    private ThreadPoolExecutor threadPool;
    private MediaController mediaControls;
    private int lastSelectedPosition = 0;
    private Boolean firstLaunching = true;

    private static int REQ_UPLOAD_CONTENTS = 0;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (RelativeLayout)inflater.inflate(R.layout.upload_gallery, container, false);

        setupViews();
        //setProgressBarIndeterminateVisibility(true);

        imageView = (ImageView)layout.findViewById(R.id.upload_gallery_imageView);
        videoViewContainer = (FrameLayout) layout.findViewById(R.id.upload_gallery_videoView_container);

        //mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);

        btn_next = (ImageView)getActivity().findViewById(R.id.btn_upload_next);


        RelativeLayout btn_next_container = (RelativeLayout)getActivity().findViewById(R.id.btn_upload_next_container);

        threadPool = new ThreadPoolExecutor( 2, 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        loadImagesFromSDCardAsyncTask = new LoadImagesFromSDCard();
        loadImagesFromSDCardAsyncTask.execute();

        btn_next_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // when the selected item is image
                if (mediaItemsArrayList.get(lastSelectedPosition).getContentsType() == 0){
                    Intent intent = new Intent(getActivity(), Filtering.class);
                    String activityName = "UploadGalleryActivity";
                    intent.putExtra("ACTIVITY_NAME", activityName);

                    loadImagesFromSDCardAsyncTask.cancel(true);

                    if (videoView.isPlaying()) {
                        videoView.pause();
                    }

                    startActivity(intent);
                }
                // when the selected item is video
                else{
                    Intent intent = new Intent(getActivity(), InputContents.class);
                    intent.putExtra("MEDIA_TYPE", "video");
                    intent.putExtra("VIDEO_CONTENTS_FILE_PATH",
                            mediaItemsArrayList.get(lastSelectedPosition).getVideoContentsFilePath());

                    loadImagesFromSDCardAsyncTask.cancel(true);

                    if (videoView.isPlaying()) {
                        videoView.pause();
                    }

                    startActivity(intent);
                }
            }
        });

        RelativeLayout btn_cancel = (RelativeLayout)getActivity().findViewById(R.id.btn_upload_cancel_container);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (threadPool.getQueue().size() > 0)
                    threadPool.getQueue().remove();

                getActivity().finish();
            }
        });

        sdcardImages.setOnItemClickListener(this);

        if (mediaControls == null) {
            mediaControls = new MediaController(getActivity());
        }
        videoView = (VideoView)layout.findViewById(R.id.upload_gallery_videoView);
        videoView.setMediaController(mediaControls);

        return layout;
    }

    private void setupViews() {
        sdcardImages = (GridView)layout.findViewById(R.id.upload_gallery_media_thumbnail_grid_view);
        //sdcardImages.setNumColumns(display.getWidth()/95);
        sdcardImages.setClipToPadding(false);

        mediaItemsArrayList = new ArrayList<UploadGalleryMediaItem>();
        imageAdapter = new GalleryImageViewAdapter(getActivity().getApplicationContext(), mediaItemsArrayList);
        sdcardImages.setAdapter(imageAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        String pageName = "Gallery";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if(videoView.isPlaying())
            videoView.pause();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(videoView.isPlaying()) {
            videoView.pause();
        }
    }


    /**
     * Async task for loading the images from the SD card.
     *
     * @author Mihai Fonoage
     *
     */
    class LoadImagesFromSDCard extends AsyncTask<Object, UploadGalleryMediaItem, Object> {

        /**
         * Load images from SD Card in the background, and display each image on the screen.
         *
         //  * @see android.os.AsyncTask#doInBackground(Params[])
         */

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object... params) {
            //setProgressBarIndeterminateVisibility(true);

            Uri uri ;

            // Set up an array of the Thumbnail Image ID column we want

            String[] imageColumns = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.PICASA_ID,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATE_MODIFIED
            };

            String[] videoColumns = {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DURATION
            };

            mVideoCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.getContentUri("external"),
                    videoColumns, // Which columns to return
                    null,       // Return all rows
                    null,
                    MediaStore.Video.Media.DATE_MODIFIED + " DESC");

            int videoFileCount = mVideoCursor.getCount();
            for(int i = 0; i < videoFileCount; i++) {
                mVideoCursor.moveToPosition(i);
                if (mVideoCursor.getString(2).equals("video/mp4")) {
                    final UploadGalleryMediaItem mediaItem = new UploadGalleryMediaItem(1, null, mVideoCursor.getInt(0), mVideoCursor.getInt(3));
                    mediaItem.setVideoContentsFilePath(mVideoCursor.getString(1));
                    mediaItem.setDurationString(getVideoDurationString(mVideoCursor.getInt(4) / 1000));

                    threadPool.execute(new Runnable() {
                        public void run() {

                            try {

                                final Bitmap thumbnailBitmap = MediaStore.Video.Thumbnails.getThumbnail(
                                        getActivity().getContentResolver(), mediaItem.getVideoId(),
                                        MediaStore.Video.Thumbnails.MICRO_KIND, null);
                                if (thumbnailBitmap != null) {
                                    mediaItem.setThumbnailBitmap(thumbnailBitmap);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    mediaItemsArrayList.add(mediaItem);
                }
            }

            // Create the cursor pointing to the SDCard
            mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.getContentUri("external"),
                    imageColumns, // Which columns to return
                    null,       // Return all rows
                    null,
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC");

            int imageFileCount = mImageCursor.getCount();
            for(int i = 0; i < imageFileCount; i++) {
                mImageCursor.moveToPosition(i);
                if (mImageCursor.getString(3).equals("image/jpeg") || mImageCursor.getString(3).equals("image/png")) {

                    final UploadGalleryMediaItem mediaItem = new UploadGalleryMediaItem(0, mImageCursor.getInt(0), null, mImageCursor.getInt(4));
                    mediaItem.setImageContentsFilePath(mImageCursor.getString(1));
                    threadPool.execute(new Runnable() {
                        public void run() {


                            try {

                                final Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(
                                        getActivity().getContentResolver(), mediaItem.getImageId(),
                                        MediaStore.Images.Thumbnails.MICRO_KIND, null);

                                BitmapFactory.Options options = new BitmapFactory.Options();

//                                Bitmap orgImage = BitmapFactory.decodeFile(thumbPath, options);
                                int degree = GetExifOrientation(mediaItem.getImageContentsFilePath());
                                Bitmap rotatedThumbnailBitmap = GetRotatedBitmap(thumbnailBitmap, degree);

                                if (rotatedThumbnailBitmap != null) {
                                    mediaItem.setThumbnailBitmap(rotatedThumbnailBitmap);
                                }

                            } catch (Exception e) {
                                //Error fetching image, try to recover
                            }
                        }
                    });

                    mediaItemsArrayList.add(mediaItem);
                }
            }

            return null;
        }

        private String getVideoDurationString(int totalSecs){
            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;

            if (hours > 0)
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            else
                return String.format("%02d:%02d", minutes, seconds);
        }

        @Override
        protected void onProgressUpdate(UploadGalleryMediaItem... uploadGalleryMediaItems)
        {
//            addImage(uploadGalleryMediaItems);
        }

        @Override
        protected void onPostExecute(Object result) {
            if (mediaItemsArrayList.size() > 0){
                Collections.sort(mediaItemsArrayList, compare);
//            mediaItemsArrayList.get(0).setIsSelected(true);
                imageAdapter.notifyDataSetChanged();
                mImageCursor.close();
                mVideoCursor.close();
                sdcardImages.performItemClick(
                        imageAdapter.getView(0, null, null),
                        0,
                        imageAdapter.getItemId(0));
            }

            //setProgressBarIndeterminateVisibility(false);
        }
    }

    private static Bitmap readImageWithSampling(String imagePath, int targetWidth, int targetHeight,
                                               Bitmap.Config bmConfig) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        int photoWidth  = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inPreferredConfig = bmConfig;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        bmOptions.inDither = false;

        Bitmap  orgImage = BitmapFactory.decodeFile(imagePath, bmOptions);

        return orgImage;

    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        if (mImageCursor != null) {
            videoView.pause();
            videoViewContainer.setVisibility(View.INVISIBLE);
            if (mediaItemsArrayList.get(position).getContentsType() == 0) { // image
                String imagePath = mediaItemsArrayList.get(position).getImageContentsFilePath();

                FileInputStream is = null;
                BufferedInputStream bis = null;
                try {

                    Bitmap bitmap = readImageWithSampling(imagePath, 960, 960, null);
                    int degree = GetExifOrientation(imagePath);
                    Bitmap rotatedBitmap = GetRotatedBitmap(bitmap, degree);
                    Bitmap resizedBitmap = resizeBitmapImageFn(rotatedBitmap, 960);
                    GlobalUploadBitmapImage.bitmap = cropBitmap(resizedBitmap, 960, 960, 0.5f, 0.4f);

//                    mAttacher = new PhotoViewAttacher(imageView);
//                    mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(GlobalUploadBitmapImage.bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    mAttacher.update();

                } catch (Exception e) {
                    //Try to recover
                } finally {
                    try {
                        if (bis != null) {
                            bis.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception e) {

                    }
                }
            } else { // video
                videoViewContainer.setVisibility(View.VISIBLE);
                videoView.setVideoPath(mediaItemsArrayList.get(position).getVideoContentsFilePath());
                videoView.start();
                Log.d("getVideoContents %s", mediaItemsArrayList.get(position).getVideoContentsFilePath());
            }
        }
        int firstPosition = sdcardImages.getFirstVisiblePosition();
        int lastPosition = sdcardImages.getLastVisiblePosition();
        int selectedChildPosition = position - firstPosition;
        int lastSelectedChildPosition = lastSelectedPosition - firstPosition;

        if (lastSelectedPosition >= firstPosition && lastSelectedPosition <= lastPosition){
            View view = (View) sdcardImages.getChildAt(lastSelectedChildPosition).findViewById(R.id.upload_gallery_gridview_item_selected_view);
            view.setVisibility(View.INVISIBLE);
        }

        if (selectedChildPosition < 0 || selectedChildPosition >= sdcardImages.getChildCount()){
            selectedChildPosition = 0;
        }else{
            View view = (View) sdcardImages.getChildAt(selectedChildPosition).findViewById(R.id.upload_gallery_gridview_item_selected_view);
            view.setVisibility(View.VISIBLE);
        }

        mediaItemsArrayList.get(lastSelectedPosition).setIsSelected(false);
        mediaItemsArrayList.get(position).setIsSelected(true);
        lastSelectedPosition = position;
    }

    public synchronized static int GetExifOrientation(String filePath) {

        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if(orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }

        return degree;
    }

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {

        if ( degrees != 0 && bitmap != null ) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );

            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

                if (bitmap != b2) {
                    //bitmap.recycle();
                    bitmap = b2;
                }

            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }

        }
        return bitmap;
    }

    public Bitmap resizeBitmapImageFn( Bitmap bmpSource, int maxResolution){
        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
        int newWidth = iWidth ;
        int newHeight = iHeight ;
        float rate = 0.0f;

        //이미지의 가로 세로 비율에 맞게 조절
        if(iWidth > iHeight ){
            if(maxResolution < iWidth ){
                rate = maxResolution / (float) iWidth ;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        }else{
            if(maxResolution < iHeight ){
                rate = maxResolution / (float) iHeight ;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(
                bmpSource, newWidth, newHeight, true);
    }

    public static Bitmap cropBitmap(final Bitmap src, final int w, final int h,
                              final float horizontalCenterPercent, final float verticalCenterPercent) {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0
                || verticalCenterPercent > 1) {
            throw new IllegalArgumentException(
                    "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                            + "1.0f, inclusive.");
        }
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src;
        }
        final Matrix m = new Matrix();
        final float scale = Math.max(
                (float) w / srcWidth,
                (float) h / srcHeight);
        m.setScale(scale, scale);
        final int srcCroppedW, srcCroppedH;
        int srcX, srcY;
        srcCroppedW = Math.round(w / scale);
        srcCroppedH = Math.round(h / scale);
        srcX = (int) (srcWidth * horizontalCenterPercent - srcCroppedW / 2);
        srcY = (int) (srcHeight * verticalCenterPercent - srcCroppedH / 2);
        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);
        final Bitmap cropped = Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */);

        return cropped;
    }

    Comparator<UploadGalleryMediaItem> compare = new Comparator<UploadGalleryMediaItem>()
    {
        @Override
        public int compare(UploadGalleryMediaItem lhs, UploadGalleryMediaItem rhs)
        {
            return rhs.getDateModified().compareTo(lhs.getDateModified());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        while (threadPool.getQueue().size() > 0)
            threadPool.getQueue().remove();

        if(imageAdapter != null) {
            imageAdapter.recycle();
        }

        imageView.setImageDrawable(null);

        RecycleUtils.recursiveRecycle(imageView);
        RecycleUtils.recursiveRecycle(getActivity().getWindow().getDecorView());
        System.gc();


    }
}
