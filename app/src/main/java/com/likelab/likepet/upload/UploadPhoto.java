package com.likelab.likepet.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalUploadBitmapImage;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;

/**
 * Created by kpg1983 on 2015-09-24.
 */

public class UploadPhoto extends Fragment implements View.OnClickListener {

    RelativeLayout layout;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private ImageButton capture, switchCamera, record;
    private Context myContext;
    private RelativeLayout cameraPreview;
    private boolean cameraFront = false, isRecording;
    private boolean isTakingPicture = false;

    private ImageButton btn_camera;
    private ImageButton btnVideo;
    private ImageButton btnTimer;


    private TextView mTxtGallery;
    private TextView mTxtPhoto;
    private TextView mTxtVideo;

    private RelativeLayout tabGallery;
    private RelativeLayout tabPhoto;
    private RelativeLayout tabMovie;

    private ProgressBar progressBar;
    int timer;

    boolean flashCheck = false;

    private GPUImage mGPUImage;

    TextView txt_title;
    public static int fragmentState= 1;
    private int rateFlag;   //촬영한 사진의 비율을 알려준다. 0은 1:1,  1은 4:3이다
    private int cameraFlag; //후면은 1, 전면카메라는 0;
    android.os.Handler mHandler = new android.os.Handler();
    private Bitmap bmp;

    Thread thread;


    private boolean threadFlag = false;

    private RelativeLayout btnFlashContainer;
    private RelativeLayout btnChangeContainer;
    private RelativeLayout btnTimerContainer;

    private TextView txtTimer;

    private static ViewPager pager;

    private RelativeLayout uploadTitleBarContainer; //사진을 자르기 위한 높이 측정을 위하여

    RelativeLayout preview;

    int gifCount;
    int gifBitmapCount;

    private boolean isGif;

    public static String gifFilePath;

    private RelativeLayout transparentBox;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (RelativeLayout)inflater.inflate(R.layout.upload_photo, container, false);

        layout.setFocusableInTouchMode(true);
        layout.requestFocus();
        layout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == event.KEYCODE_BACK && event.getAction() == event.ACTION_DOWN) {
                    if (threadFlag == true) {
                        threadFlag = false;
                        TimerThread.currentThread().interrupt();
                        txtTimer.setText(Integer.toString(timer));
                        isTakingPicture = false;
                        txtTimer.setVisibility(View.GONE);

                    } else {
                        getActivity().finish();
                    }

                    return true;
                }
                return false;
            }
        });

        gifCount = 0;
        gifBitmapCount = 0;
        rateFlag =0;
        isGif = false;

        mGPUImage = new GPUImage(getActivity());

        uploadTitleBarContainer = (RelativeLayout)getActivity().findViewById(R.id.upload_menu_bar_container);

        tabGallery = (RelativeLayout)getActivity().findViewById(R.id.upload_tab_gallery);
        tabPhoto = (RelativeLayout)getActivity().findViewById(R.id.upload_tab_photo);
        tabMovie = (RelativeLayout)getActivity().findViewById(R.id.upload_tab_video);

        transparentBox = (RelativeLayout)layout.findViewById(R.id.upload_photo_transparent_box);

        mTxtGallery = (TextView)getActivity().findViewById(R.id.btn_gallery_bar);
        mTxtPhoto = (TextView)getActivity().findViewById(R.id.btn_gallery_bar);
        mTxtVideo = (TextView)getActivity().findViewById(R.id.btn_movie_bar);

        btnFlashContainer = (RelativeLayout)layout.findViewById(R.id.upload_photo_flash_container);
        btnChangeContainer = (RelativeLayout)layout.findViewById(R.id.upload_photo_change_camera_container);
        btnTimerContainer = (RelativeLayout)layout.findViewById(R.id.upload_photo_timer_container);

        txtTimer = (TextView)layout.findViewById(R.id.upload_photo_txt_timer);
        preview = (RelativeLayout)layout.findViewById(R.id.camera_preview);
        btnFlashContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnChangeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1 && isRecording == false && isTakingPicture == false) {
                    //release the old camera instance
                    //switch camera, from the front and the back and vice versa

                    releaseCamera();
                    chooseCamera();
                }

            }
        });

        btnTimerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fragmentState != 2) {
                    switch (timer) {
                        case 0: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_2sec);
                            timer = 2;

                            break;
                        }
                        case 2: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_5sec);
                            timer = 5;
                            break;

                        }
                        case 5: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_10sec);
                            timer = 10;
                            break;
                        }
                        case 10: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_00);
                            timer = 0;
                            break;
                        }

                        default:
                            btnTimer.setScaleType(ImageView.ScaleType.FIT_XY);

                    }
                }

            }
        });

        pager = (ViewPager)getActivity().findViewById(R.id.upload_viewPager);

        RelativeLayout btnGalleryLayout = (RelativeLayout)getActivity().findViewById(R.id.upload_gallery_bar_container);
        RelativeLayout btnPhotoLayout = (RelativeLayout)getActivity().findViewById(R.id.upload_photo_bar_container);
        RelativeLayout btnMovieLayout = (RelativeLayout)getActivity().findViewById(R.id.upload_movie_bar_container);

        mTxtGallery = (TextView)getActivity().findViewById(R.id.btn_gallery_bar);
        mTxtPhoto = (TextView)getActivity().findViewById(R.id.btn_photo_bar);
        mTxtVideo = (TextView)getActivity().findViewById(R.id.btn_movie_bar);

        layout.setOnTouchListener(new RelativeLayoutTouchListener());

        final RelativeLayout transparentBox = (RelativeLayout)layout.findViewById(R.id.upload_photo_transparent_box);

        progressBar = (ProgressBar)layout.findViewById(R.id.upload_photo_progress);

        btn_camera = (ImageButton)layout.findViewById(R.id.upload_photo_camera_button);
        btn_camera.setScaleType(ImageView.ScaleType.FIT_XY);

        btnVideo = (ImageButton)layout.findViewById(R.id.upload_photo_video_button);
        btnVideo.setScaleType(ImageView.ScaleType.FIT_XY);

        btnTimer = (ImageButton) layout.findViewById(R.id.upload_photo_camera_timer);

        ImageButton btn_change_camera = (ImageButton)layout.findViewById(R.id.upload_photo_change_camera);
        btn_change_camera.setScaleType(ImageView.ScaleType.FIT_XY);

        final ImageButton btn_size_1_1 = (ImageButton)layout.findViewById(R.id.upload_photo_btn_size_1_1);
        btn_size_1_1.setScaleType(ImageView.ScaleType.FIT_XY);

        final ImageButton btn_size_4_3 = (ImageButton)layout.findViewById(R.id.upload_photo_btn_size_4_3);
        btn_size_4_3.setScaleType(ImageView.ScaleType.FIT_XY);

        ImageButton btn_camera_flash = (ImageButton)layout.findViewById(R.id.upload_photo_camera_flash);
        btn_camera_flash.setScaleType(ImageView.ScaleType.FIT_XY);

        ImageButton btn_camera_timer = (ImageButton)layout.findViewById(R.id.upload_photo_camera_timer);
        btn_camera_timer.setScaleType(ImageView.ScaleType.FIT_XY);

        txt_title = (TextView)getActivity().findViewById(R.id.btn_upload_title);

        layout.findViewById(R.id.upload_photo_camera_button).setOnClickListener(this);
        layout.findViewById(R.id.upload_photo_video_button).setOnClickListener(this);

        myContext = getActivity().getApplicationContext();
        initialize();
        isRecording = false;


        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fragmentState != 2) {
                    switch (timer) {
                        case 0: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_2sec);
                            timer = 2;

                            break;
                        }
                        case 2: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_5sec);
                            timer = 5;
                            break;

                        }
                        case 5: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_10sec);
                            timer = 10;
                            break;
                        }
                        case 10: {
                            btnTimer.setImageResource(R.drawable.upload_btn_time_00);
                            timer = 0;
                            break;
                        }

                        default:
                            btnTimer.setScaleType(ImageView.ScaleType.FIT_XY);

                    }
                }
            }
        });


        mTxtGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording == false  && isTakingPicture == false) {
                    txt_title.setText(getResources().getString(R.string.upload_title_select_picture));
                    mTxtGallery.setTextColor(Color.parseColor("#f7c243"));
                    mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                    mTxtVideo.setTextColor(Color.parseColor("#ffffff"));
                    Upload.startFragment();
                    progressBar.setVisibility(View.INVISIBLE);

                    tabGallery.setVisibility(View.VISIBLE);
                    tabPhoto.setVisibility(View.INVISIBLE);
                    tabMovie.setVisibility(View.INVISIBLE);
                    pager.setCurrentItem(Upload.fragment_page_1);

                    btnTimer.setImageResource(R.drawable.upload_btn_time_00);
                    timer = 0;
                }

            }
        });

        mTxtPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isRecording == false && isTakingPicture == false) {
                    fragmentState = 1;
                    pager.setCurrentItem(Upload.fragment_page_2);
                    mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                    mTxtPhoto.setTextColor(Color.parseColor("#f7c243"));
                    mTxtVideo.setTextColor(Color.parseColor("#ffffff"));
                    progressBar.setVisibility(View.INVISIBLE);
                    txt_title.setText(getResources().getString(R.string.upload_title_take_picture));

                    tabGallery.setVisibility(View.INVISIBLE);
                    tabPhoto.setVisibility(View.VISIBLE);
                    tabMovie.setVisibility(View.INVISIBLE);

                    btn_camera.setVisibility(View.VISIBLE);
                    btnVideo.setVisibility(View.INVISIBLE);
                }

            }
        });

        mTxtVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording == false && isTakingPicture == false) {
                    fragmentState = 2;
                    pager.setCurrentItem(Upload.fragment_page_2);
                    mTxtVideo.setTextColor(Color.parseColor("#f7c243"));
                    mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                    mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                    txt_title.setText(getResources().getString(R.string.upload_title_take_gif));
                    progressBar.setVisibility(View.VISIBLE);

                    tabGallery.setVisibility(View.INVISIBLE);
                    tabPhoto.setVisibility(View.INVISIBLE);
                    tabMovie.setVisibility(View.VISIBLE);

                    btn_camera.setVisibility(View.INVISIBLE);
                    btnVideo.setVisibility(View.VISIBLE);

                    btnTimer.setImageResource(R.drawable.upload_btn_time_00);
                    timer = 0;

                }

            }
        });


        btnGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isRecording == false && isTakingPicture == false) {
                    txt_title.setText(getResources().getString(R.string.upload_title_select_picture));
                    mTxtGallery.setTextColor(Color.parseColor("#f7c243"));
                    mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                    mTxtVideo.setTextColor(Color.parseColor("#ffffff"));
                    Upload.startFragment();
                    progressBar.setVisibility(View.INVISIBLE);

                    tabGallery.setVisibility(View.VISIBLE);
                    tabPhoto.setVisibility(View.INVISIBLE);
                    tabMovie.setVisibility(View.INVISIBLE);
                    pager.setCurrentItem(Upload.fragment_page_1);
                }
            }
        });

        btnPhotoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isRecording == false && isTakingPicture == false) {
                    fragmentState = 1;
                    pager.setCurrentItem(Upload.fragment_page_2);
                    mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                    mTxtPhoto.setTextColor(Color.parseColor("#f7c243"));
                    mTxtVideo.setTextColor(Color.parseColor("#ffffff"));
                    progressBar.setVisibility(View.INVISIBLE);
                    txt_title.setText(getResources().getString(R.string.upload_title_take_picture));

                    tabGallery.setVisibility(View.INVISIBLE);
                    tabPhoto.setVisibility(View.VISIBLE);
                    tabMovie.setVisibility(View.INVISIBLE);

                    btn_camera.setVisibility(View.VISIBLE);
                    btnVideo.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnMovieLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isRecording == false && isTakingPicture == false) {
                    fragmentState = 2;
                    pager.setCurrentItem(Upload.fragment_page_2);
                    mTxtVideo.setTextColor(Color.parseColor("#f7c243"));
                    mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                    mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                    txt_title.setText(getResources().getString(R.string.upload_title_take_gif));
                    progressBar.setVisibility(View.VISIBLE);

                    tabGallery.setVisibility(View.INVISIBLE);
                    tabPhoto.setVisibility(View.INVISIBLE);
                    tabMovie.setVisibility(View.VISIBLE);

                    btn_camera.setVisibility(View.INVISIBLE);
                    btnVideo.setVisibility(View.VISIBLE);
                    //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

            }
        });

        btn_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1 && isTakingPicture == false && isRecording == false) {
                    //release the old camera instance
                    //switch camera, from the front and the back and vice versa

                    releaseCamera();
                    chooseCamera();
                }

            }
        });


        btn_camera_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cameraFront == false) {

                    Camera.Parameters p = mCamera.getParameters();

                    if (flashCheck == false) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        flashCheck = true;
                        mCamera.setParameters(p);
                        Log.d("Flash", "플래쉬 온");
                        //Camera_ON();

                    } else {
                        //Camera_OFF();

                        flashCheck = false;
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(p);
                        Log.d("Flash", "플래쉬 오프");
                    }
                }
            }

        });

        btn_size_1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_size_1_1.setVisibility(View.INVISIBLE);
                btn_size_4_3.setVisibility(View.VISIBLE);
                transparentBox.setVisibility(View.VISIBLE);
                rateFlag = 1;

            }
        });

        btn_size_4_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_size_1_1.setVisibility(View.VISIBLE);
                btn_size_4_3.setVisibility(View.INVISIBLE);
                transparentBox.setVisibility(View.INVISIBLE);
                rateFlag = 0;

            }
        });


        return layout;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.upload_photo_camera_button:

                if(timer != 0) {
                    txtTimer.setVisibility(View.VISIBLE);

                    TimerThread timerThread = new TimerThread();
                    //timerThread.start();
                    timerThread.start();
                    timerThread = null;
                    threadFlag = true;

                }
                else {

                    mCamera.takePicture(null, null, mPicture);

                }

                break;

            case R.id.upload_photo_video_button:
                mCamera.cancelAutoFocus();

                if(timer != 0) {
                    txtTimer.setVisibility(View.VISIBLE);

                    TimerThread timerThread = new TimerThread();
                    timerThread.start();
                    timerThread = null;
                    threadFlag = true;

                }
                else {
                    if(isRecording == true) {
                        mCamera.stopPreview();
                        isRecording = false;
                        progressBar.setProgress(0);
                    } else {
                        isRecording = true;
                        gifCount = 0;
                        getFrameFromPreview();

                        Thread progressThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int progress = 0;
                                while(progress < 24) {
                                    progress++;
                                    progressBar.setProgress(progress * 3);

                                    try {
                                        thread.sleep(50);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        progressThread.start();
                    }
                }

                break;
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }


    @Override
    public void onResume() {
        super.onResume();

        cameraFlag = 1;
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            //finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(getActivity(), "No front facing camera found.", Toast.LENGTH_LONG).show();
                try {
                    switchCamera.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            mCamera = Camera.open(findBackFacingCamera());

            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);

        }

        //카메라 타이머 초기화
        try {
            txtTimer.setVisibility(View.INVISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        btnTimer.setImageResource(R.drawable.upload_btn_time_00);
        timer = 0;

        if(gifFilePath != null) {
            File files = new File(gifFilePath);
            if (files.exists()) {
                files.delete();
            }

            GlobalUploadBitmapImage.bitmapList.removeAll(GlobalUploadBitmapImage.bitmapList);
            GlobalUploadBitmapImage.filteredBitmapList.removeAll(GlobalUploadBitmapImage.filteredBitmapList);
        }

        progressBar.setProgress(0);

        String pageName;

        if(fragmentState == 1) {
            pageName = "photo";
        } else {
            pageName = "GIF";
        }

        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void initialize() {
        cameraPreview = (RelativeLayout) layout.findViewById(R.id.camera_preview);
        //mCamera.setParameters("orientation", "protrait");
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);


        //capture = (ImageButton) layout.findViewById(R.id.upload_photo_camera_button);

        switchCamera = (ImageButton) layout.findViewById(R.id.change_camera);

    }

    public void chooseCamera() {
        //if the camera preview is the front

        if(isTakingPicture ==false && isRecording == false) {
            if (cameraFront) {
                int cameraId = findBackFacingCamera();
                if (cameraId >= 0) {
                    //open the backFacingCamera
                    //set a picture callback
                    //refresh the preview

                    mCamera = Camera.open(cameraId);
                    mPicture = getPictureCallback();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mPreview.refreshCamera(mCamera);
                        }
                    });
                    thread.start();
                    int softKeyHeight = getSoftButtonsBarHeight();

                    Log.d("softKey", Integer.toString(softKeyHeight));
                    if (softKeyHeight > 0) {
                        int width = cameraPreview.getWidth();
                        int height = (width / 9) * 16;
                        //cameraPreview.setLayoutParams(new RelativeLayout.LayoutParams(width, 2500));
                    }

                }
            } else {
                int cameraId = findFrontFacingCamera();
                if (cameraId >= 0) {
                    //open the backFacingCamera
                    //set a picture callback
                    //refresh the preview

                    mCamera = Camera.open(cameraId);
                    mPicture = getPictureCallback();

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mPreview.refreshCamera(mCamera);
                        }
                    });
                    thread.start();
                }
            }
        }

    }

    private int getSoftButtonsBarHeight() {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }


    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                //make a new picture file

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

                        if (pictureFile == null) {
                            return;
                        }
                        try {
                            //write the file
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Bitmap bitmap = readImageWithSampling(pictureFile.getAbsolutePath(), 960, 960, null);
                        int degree = GetExifOrientation(pictureFile.getAbsolutePath());
                        Bitmap rotatedBitmap = GetRotatedBitmap(bitmap, degree);
//                        Bitmap resizedBitmap = resizeBitmapImageFn(rotatedBitmap, 960);

                        //GlobalUploadBitmapImage.bitmap = rotatedBitmap;


                        //GlobalUploadBitmapImage.bitmap = Bitmap.createBitmap(rotatedBitmap, 0, uploadTitleBarContainer.getHeight(), 960, 960);

                        GlobalUploadBitmapImage.bitmap = rotatedBitmap;

                        Intent intent = new Intent(getActivity(), Filtering.class);

                        String activityName = "UploadPhotoActivity";

                        int titleBarHeight = uploadTitleBarContainer.getHeight();

                        intent.putExtra("ACTIVITY_NAME", activityName);
                        intent.putExtra("TITLE_HEIGHT", titleBarHeight);
                        intent.putExtra("RATE", rateFlag);
                        intent.putExtra("CAMERA_FRONT", cameraFront);
                        intent.putExtra("DEGREE", degree);

                        //TimerThread.stop();
                        startActivity(intent);

                        //refresh camera to continue preview
                        //mPreview.refreshCamera(mCamera);

                    }

                });
                thread.start();
            }
        };
        return picture;
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

    //make picture and save to a folder
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "JCG Camera");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");


        return mediaFile;
    }


    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
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


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            GlobalUploadBitmapImage.fileList.add(mediaFile);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onPause() {
        super.onPause();
        // if you are using MediaRecorder, release it first
        releaseCamera();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragmentState =1 ;

        RecycleUtils.recursiveRecycle(btn_camera);
        RecycleUtils.recursiveRecycle(btnTimer);
        RecycleUtils.recursiveRecycle(btnVideo);

        RecycleUtils.recursiveRecycle(getActivity().getWindow().getDecorView());
        System.gc();

    }

    public class RelativeLayoutTouchListener implements View.OnTouchListener {

        static final String logTag = "ActivitySwipeDetector";
        private Activity activity = getActivity();
        static final int MIN_DISTANCE = 100;// TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
        private float downX, downY, upX, upY;

        // private MainActivity mMainActivity;

        public void onRightToLeftSwipe() {

            getFragmentManager().popBackStack();

        }

        public void onLeftToRightSwipe() {

        }

        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    downX = event.getX();
                    downY = event.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    if (fragmentState == 1) {
                        if (Math.abs(deltaX) > MIN_DISTANCE) {

                            if(isRecording == false && isTakingPicture == false) {
                                // left or right
                                if (deltaX < 0) {
                                    this.onLeftToRightSwipe();
                                    txt_title.setText(getResources().getString(R.string.upload_title_select_picture));
                                    mTxtGallery.setTextColor(Color.parseColor("#f7c243"));
                                    mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                                    mTxtVideo.setTextColor(Color.parseColor("#ffffff"));

                                    tabGallery.setVisibility(View.VISIBLE);
                                    tabPhoto.setVisibility(View.INVISIBLE);
                                    tabMovie.setVisibility(View.INVISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);

                                    Upload.startFragment();
                                    return true;
                                } else if (deltaX > 0) {
                                    this.onRightToLeftSwipe();
                                    fragmentState = 2;
                                    selectTabBar(fragmentState);
                                    progressBar.setVisibility(View.VISIBLE);

                                    return true;
                                }
                            }
                        }

                    } else if (fragmentState == 2) {

                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            // left or right

                            if(isRecording == false && isTakingPicture == false) {
                                if (deltaX < 0) {
                                    this.onLeftToRightSwipe();
                                    fragmentState = 1;
                                    selectTabBar(fragmentState);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    return true;
                                } else if (deltaX > 0) {
                                    this.onRightToLeftSwipe();
                                    return true;
                                }
                            }
                        }
                    }

                    return false; // no swipe horizontally and no swipe vertically
                }// case MotionEvent.ACTION_UP:
            }
            return false;
        }
    }

    private Bitmap imgRotate(Boolean cameraFront){

        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();

        //전면 카메라와 후면 카메라가 찍었을때 bitmap 회전 방향을 다르게 설정한다.
        if(cameraFront) {
            matrix.postRotate(270);
        }
        else {
            matrix.postRotate(90);
        }
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }

    private void selectTabBar(int fragmentState) {

        switch (fragmentState) {
            case 0: {

                break;
            }
            case 1: {

                mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                mTxtPhoto.setTextColor(Color.parseColor("#f7c243"));
                mTxtVideo.setTextColor(Color.parseColor("#ffffff"));
                txt_title.setText(getResources().getString(R.string.upload_title_take_picture));

                tabGallery.setVisibility(View.INVISIBLE);
                tabPhoto.setVisibility(View.VISIBLE);
                tabMovie.setVisibility(View.INVISIBLE);

                btn_camera.setVisibility(View.VISIBLE);
                btnVideo.setVisibility(View.INVISIBLE);

                break;
            }
            case 2: {
                mTxtVideo.setTextColor(Color.parseColor("#f7c243"));
                mTxtGallery.setTextColor(Color.parseColor("#ffffff"));
                mTxtPhoto.setTextColor(Color.parseColor("#ffffff"));
                txt_title.setText(getResources().getString(R.string.upload_title_take_gif));

                tabGallery.setVisibility(View.INVISIBLE);
                tabPhoto.setVisibility(View.INVISIBLE);
                tabMovie.setVisibility(View.VISIBLE);

                btn_camera.setVisibility(View.INVISIBLE);
                btnVideo.setVisibility(View.VISIBLE);

                btnTimer.setImageResource(R.drawable.upload_btn_time_00);
                timer = 0;
                break;
            }
        }
    }




    public void Camera_ON() {
        Camera.Parameters param = mCamera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        mCamera.setParameters(param);
        mCamera.startPreview();

        flashCheck = true;

    }

    public void Camera_OFF() {
        Camera.Parameters param = mCamera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        mCamera.setParameters(param);
        //mCamera.stopPreview();

        flashCheck = false;
    }

    public class TimerThread extends Thread {

        int tempTime = timer;

        public void run() {

            isTakingPicture = true;

            while (timer > 0 && threadFlag == true) {
                // 값을 하나씩 늘립니다.
                --timer;
                mHandler.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        txtTimer.setText(Integer.toString(timer));
                    }
                });

                try {
                    thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(mCamera != null && threadFlag == true)
                mCamera.takePicture(null, null, mPicture);

            isTakingPicture = false;
            timer = tempTime;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtTimer.setVisibility(View.INVISIBLE);
                }
            });

        }
    }

    public void getFrameFromPreview() {
        TimerTask mTask;
        final Timer mTimer = new Timer();
        GlobalUploadBitmapImage.bitmapList.clear();
        GlobalUploadBitmapImage.filteredBitmapList.clear();
        GlobalUploadBitmapImage.filteredBitmapListCopy.clear();

        mTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if(mCamera != null) {
                        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {

                            @Override
                            public void onPreviewFrame(final byte[] data, final Camera camera) {

                                if(mCamera != null) {
                                    if (gifBitmapCount >= 6) {
                                        mTimer.cancel();
                                        gifBitmapCount = 0;
                                        isRecording = false;
                                        mCamera.stopPreview();
                                        Camera.Parameters mParam = mCamera.getParameters();
                                        mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                                        if (cameraFront == false)
                                            mCamera.setParameters(mParam);

                                        Intent intent = new Intent(getActivity(), Filtering.class);
                                        int titleBarHeight = uploadTitleBarContainer.getHeight();
                                        String activityName = "UploadPhotoActivity";

                                        intent.putExtra("ACTIVITY_NAME", activityName);
                                        intent.putExtra("TITLE_HEIGHT", titleBarHeight);
                                        intent.putExtra("MEDIA_TYPE", "gif");
                                        intent.putExtra("RATE", rateFlag);
                                        intent.putExtra("CAMERA_FRONT", cameraFront);

                                        startActivity(intent);
                                    }
                                    Camera.Parameters parameters = camera.getParameters();
                                    Camera.Size size = parameters.getPreviewSize();

                                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                                    YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);

                                    image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 30, out);
                                    byte[] bytes = out.toByteArray();

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = Math.min(size.width / 512, size.height / 512);

                                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                                    int titleBarHeight = uploadTitleBarContainer.getHeight();
                                    int transBoxHeight = transparentBox.getHeight();


                                    if (cameraFront) {
                                        bmp = imgRotate(cameraFront);
                                    } else {
                                        bmp = imgRotate(cameraFront);
                                    }

                                    //흐음.... 이부분 차후에 다시 한 번 확인해 봐야 할듯
                                    //왜 /2를 해야 정확하게 나오는지 시간나면 한번 봐볼까!@!!

                                    bmp = Bitmap.createBitmap(bmp, 0, titleBarHeight / 2, bmp.getWidth(), bmp.getWidth());
                                    if (rateFlag == 0) {
                                        //bmp = Bitmap.createBitmap(bmp, 0, titleBarHeight / 2, bmp.getWidth(), bmp.getWidth());
                                    } else {
                                        //bmp = Bitmap.createBitmap(bmp, 0,  (titleBarHeight + transBoxHeight) / 2, bmp.getWidth(), bmp.getWidth() - transBoxHeight);
                                    }

//                        bmp = Bitmap.createScaledBitmap(bmp, 512, 512, false);

                                    if (gifBitmapCount == 0) {
                                        GlobalUploadBitmapImage.bitmap = bmp;
                                    }

                                    GlobalUploadBitmapImage.bitmapList.add(bmp);

                                    gifBitmapCount++;
                                }

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mTimer.schedule(mTask, 0, 200);
    }

    /**
     * 임시 저장 파일의 경로를 반환
     */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /**
     * 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환
     */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    System.currentTimeMillis() + "_temp.gif");

            try {
                f.createNewFile();      // 외장메모리에 temp.gif 파일 생성
            } catch (IOException e) {

            }

            return f;
        } else
            return null;
    }

    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

}
