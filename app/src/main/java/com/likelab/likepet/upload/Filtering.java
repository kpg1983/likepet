package com.likelab.likepet.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.likelab.likepet.global.ActivityList;
import com.likelab.likepet.GPUImageFilterTools;
import com.likelab.likepet.R;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.filter.IFAmaroFilter;
import com.likelab.likepet.filter.IFBrannanFilter;
import com.likelab.likepet.filter.IFEarlybirdFilter;
import com.likelab.likepet.filter.IFHefeFilter;
import com.likelab.likepet.filter.IFHudsonFilter;
import com.likelab.likepet.filter.IFImageFilter;
import com.likelab.likepet.filter.IFInkwellFilter;
import com.likelab.likepet.filter.IFLomoFilter;
import com.likelab.likepet.filter.IFLordKelvinFilter;
import com.likelab.likepet.global.GlobalUploadBitmapImage;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by kpg1983 on 2015-09-24.
 */


public class Filtering extends Activity {

    private static final int REQUEST_PICK_IMAGE = 1;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;
    private GPUImageFilterTools filterTools;

    private GifImageView imageView;
    private ImageButton btnBrightness;
    private ImageButton btnRotation;
    private ImageButton btnRate_1_1;
    private ImageButton btnRate_4_3;

    private RelativeLayout transparentBoxTop;
    private RelativeLayout transparentBoxBottom;

    private RelativeLayout filterImageContainerOriginal;
    private RelativeLayout filterImageContainer_2;
    private RelativeLayout filterImageContainer_3;
    private RelativeLayout filterImageContainer_4;
    private RelativeLayout filterImageContainer_5;
    private RelativeLayout filterImageContainer_6;
    private RelativeLayout filterImageContainer_7;
    private RelativeLayout filterImageContainer_8;
    private RelativeLayout filterImageContainer_9;

    private ImageView imgFilterOriginal;

    private ImageView imgFilter_2;

    private ImageView imgFilter_3;

    private ImageView imgFilter_4;
    private ImageView imgFilter_5;
    private ImageView imgFilter_6;
    private ImageView imgFilter_7;
    private ImageView imgFilter_8;
    private ImageView imgFilter_9;

    private int rateFlag;
    private int filterRateFlag;

    Bitmap bitmap;
    Bitmap bmp;

    Bitmap filterBitmap;
    Bitmap filterBitmap_1;
    Bitmap filterBitmap_2;
    Bitmap filterBitmap_3;
    Bitmap filterBitmap_4;
    Bitmap filterBitmap_5;
    private TimerTask mTask;
    private Timer mTimer;
    private Boolean isThisGif, wentToInputContents;

    private Intent intent;

    private int gifIndex, gifCount, selectedFilterIndex;
    private IFImageFilter[] filterArray;
    private Thread threadForGifFiltered;
    private ProgressDialog progressDialog;
    private GPUImage gpuImage;

    android.os.Handler mHandler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtering);

        LoadFilterImage loadFilterImage = new LoadFilterImage();

        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
        gpuImage = new GPUImage(this); isThisGif = false; wentToInputContents = false;

        filterTools = new GPUImageFilterTools();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActivityList.activityArrayList.add(this);

        imageView = (GifImageView) findViewById(R.id.filtering_image_contents);

        filterRateFlag = 0;       //0: 1:1    1: 4:3 비율율

        intent = getIntent();
        if ( intent.getExtras().getString("MEDIA_TYPE") != null ){
            if( intent.getExtras().getString("MEDIA_TYPE").equals("gif"))
                isThisGif = true;
        }

        imgFilterOriginal = (ImageView) findViewById(R.id.filtering_img_filter_original);
        filterImageContainerOriginal = (RelativeLayout) findViewById(R.id.filtering_filter_original_container);

        imgFilter_2 = (ImageView)findViewById(R.id.filtering_img_filter_2);
        filterImageContainer_2 = (RelativeLayout)findViewById(R.id.filtering_filter_container_2);

        imgFilter_3 = (ImageView)findViewById(R.id.filtering_img_filter_3);
        filterImageContainer_3 = (RelativeLayout)findViewById(R.id.filtering_filter_container_3);

        imgFilter_4 = (ImageView)findViewById(R.id.filtering_img_filter_4);
        imgFilter_5 = (ImageView)findViewById(R.id.filtering_img_filter_5);
        imgFilter_6 = (ImageView)findViewById(R.id.filtering_img_filter_6);
        imgFilter_7 = (ImageView)findViewById(R.id.filtering_img_filter_7);
        imgFilter_8 = (ImageView)findViewById(R.id.filtering_img_filter_8);
        imgFilter_9 = (ImageView)findViewById(R.id.filtering_img_filter_9);

        filterImageContainer_4 = (RelativeLayout)findViewById(R.id.filtering_filter_container_4);
        filterImageContainer_5 = (RelativeLayout)findViewById(R.id.filtering_filter_container_5);
        filterImageContainer_6 = (RelativeLayout)findViewById(R.id.filtering_filter_container_6);
        filterImageContainer_7 = (RelativeLayout)findViewById(R.id.filtering_filter_container_7);
        filterImageContainer_8 = (RelativeLayout)findViewById(R.id.filtering_filter_container_8);
        filterImageContainer_9 = (RelativeLayout)findViewById(R.id.filtering_filter_container_9);

        btnBrightness = (ImageButton) findViewById(R.id.filtering_btn_brightness);
        btnRate_1_1 = (ImageButton) findViewById(R.id.filtering_btn_rate_1_1);
        btnRotation = (ImageButton) findViewById(R.id.filtering_btn_rotation);
        btnRate_4_3 = (ImageButton) findViewById(R.id.filtering_btn_rate_4_3);

        transparentBoxTop = (RelativeLayout) findViewById(R.id.filtering_transparent_box_top);
        transparentBoxBottom = (RelativeLayout) findViewById(R.id.filtering_transparent_box_bottom);

        RelativeLayout btn_cancel_container = (RelativeLayout) findViewById(R.id.btn_filtering_cancel_container);
        RelativeLayout btn_next_container = (RelativeLayout) findViewById(R.id.btn_filtering_next_container);

        ImageView btn_cancel = (ImageView) findViewById(R.id.btn_filtering_cancel);
        btn_cancel.setImageResource(R.drawable.upload_btn_back);

        ImageView btn_next = (ImageView) findViewById(R.id.btn_filtering_next);
        btn_next.setImageResource(R.drawable.upload_btn_next);

        btnBrightness.setImageResource(R.drawable.upload_btn_filter_001);
        btnRotation.setImageResource(R.drawable.upload_btn_filter_002);
        btnRate_1_1.setImageResource(R.drawable.upload_btn_size_1_1);
        btnRate_4_3.setImageResource(R.drawable.upload_btn_size_4_3);

        btnBrightness.setScaleType(ImageView.ScaleType.FIT_XY);
        btnRotation.setScaleType(ImageView.ScaleType.FIT_XY);
        btnRate_1_1.setScaleType(ImageView.ScaleType.FIT_XY);
        btnRate_4_3.setScaleType(ImageView.ScaleType.FIT_XY);

        filterArray = new IFImageFilter[8];
        filterArray[0] = new IFAmaroFilter(Filtering.this);
        filterArray[1] = new IFBrannanFilter(Filtering.this);
        filterArray[2] = new IFEarlybirdFilter(Filtering.this);
        filterArray[3] = new IFHefeFilter(Filtering.this);
        filterArray[4] = new IFHudsonFilter(Filtering.this);
        filterArray[5] = new IFInkwellFilter(Filtering.this);
        filterArray[6] = new IFLomoFilter(Filtering.this);
        filterArray[7] = new IFLordKelvinFilter(Filtering.this);

        progressDialog  = new ProgressDialog(Filtering.this);
        progressDialog.setMessage("Filtering");

        if (intent.getExtras().getString("ACTIVITY_NAME").equals("UploadPhotoActivity")) {

            rateFlag = intent.getExtras().getInt("RATE");
            int titleBarHeight = intent.getExtras().getInt("TITLE_HEIGHT");
            boolean cameraFront = intent.getBooleanExtra("CAMERA_FRONT", false);
            int degree = intent.getIntExtra("DEGREE", 0);


            //전면 카메라로 찍은 사진은 왼쪽으로 90도 회전 후 좌우 반전 시킨다
           if(cameraFront) {
                GlobalUploadBitmapImage.bitmap = imgRotate(cameraFront);
                GlobalUploadBitmapImage.bitmap = reverse(GlobalUploadBitmapImage.bitmap);

           } else if(!cameraFront && degree == 0) {
               GlobalUploadBitmapImage.bitmap = imgRotate(cameraFront);
           }

            bitmap = GlobalUploadBitmapImage.bitmap;

            if (isThisGif) {
                GlobalUploadBitmapImage.filteredBitmapList.removeAll(GlobalUploadBitmapImage.filteredBitmapList);
                GlobalUploadBitmapImage.filteredBitmapList.addAll(GlobalUploadBitmapImage.bitmapList);
            }else{

//                이미지 자르기 정사각형 또는 4:3
                if (rateFlag == 1) {
                    btnRate_1_1.setVisibility(View.INVISIBLE);
                    btnRate_4_3.setVisibility(View.VISIBLE);
                    transparentBoxTop.setVisibility(View.VISIBLE);
                    transparentBoxBottom.setVisibility(View.VISIBLE);
                    filterRateFlag = 1;
                }
            }

        } else if (intent.getExtras().getString("ACTIVITY_NAME").equals("UploadGalleryActivity")){

            bitmap = GlobalUploadBitmapImage.bitmap;
            bmp = bitmap;

        } else {

        }

        GlobalUploadBitmapImage.bitmap = bitmap;

        if(isThisGif){
            filterBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, true);
        }else{
            filterBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 8, bitmap.getHeight() / 8, true);
        }
        startThread();

        imageView.setImageBitmap(GlobalUploadBitmapImage.bitmap);

        //필터 이미지들
        imgFilterOriginal.setImageBitmap(filterBitmap);

        //loadFilterImage.execute(new IFAmaroFilter(this), new IFBrannanFilter(this), new IFEarlybirdFilter(this),
            //    new IFHefeFilter(this), new IFHudsonFilter(this), new IFInkwellFilter(this), new IFLomoFilter(this), new IFLordKelvinFilter(this));

        //gpuImage.setImage(bitmap);

        gpuImage.setImage(filterBitmap);
        gpuImage.setFilter(filterArray[0]);
        filterBitmap = gpuImage.getBitmapWithFilterApplied();
        imgFilter_2.setImageBitmap(filterBitmap);

        gpuImage.setFilter(filterArray[1]);
        filterBitmap = gpuImage.getBitmapWithFilterApplied();
        imgFilter_3.setImageBitmap(filterBitmap);

        gpuImage.setFilter(filterArray[2]);
        filterBitmap = gpuImage.getBitmapWithFilterApplied();
        imgFilter_4.setImageBitmap(filterBitmap);

        if(isThisGif){
            gifIndex = 0; gifCount = GlobalUploadBitmapImage.bitmapList.size();

            mTask = new TimerTask() {
                @Override
                public void run() {
                    gifIndex = gifIndex % (gifCount-1);

                    Filtering.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (GlobalUploadBitmapImage.filteredBitmapList.size() > 0)
                                imageView.setImageBitmap(GlobalUploadBitmapImage.filteredBitmapList.get(gifIndex));
                        }
                    });
                    gifIndex++;
                }
            };

            mTimer = new Timer();
            mTimer.schedule(mTask, 0, 200);
        }

        gpuImage.setImage(bitmap);

        filterImageContainerOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    for (int i = 0; i < GlobalUploadBitmapImage.bitmapList.size(); i++) {
                        GlobalUploadBitmapImage.filteredBitmapList.set(i, GlobalUploadBitmapImage.bitmapList.get(i));
                    }
//                    selectedFilterIndex = 0;
//                    if (threadForGifFiltered.isAlive()){
//                        threadForGifFiltered.interrupt();
//                    }
//                    threadForGifFiltered.start();
                } else {
                    bitmap = GlobalUploadBitmapImage.bitmap;
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });

        filterImageContainer_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 1;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[0]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                }

                //imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            }
        });

        filterImageContainer_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 2;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[1]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }


            }
        });

        filterImageContainer_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 3;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[2]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }



            }
        });

        filterImageContainer_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 4;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[3]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }


            }
        });

        filterImageContainer_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 5;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[4]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }



            }
        });

        filterImageContainer_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 6;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[5]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }

            }
        });

        filterImageContainer_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 7;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[6]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });

        filterImageContainer_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif) {
                    selectedFilterIndex = 8;
                    makeGifFilteringThread();
                }else{
                    gpuImage.setFilter(filterArray[7]);
                    bitmap = gpuImage.getBitmapWithFilterApplied();
                    imageView.setImageBitmap(bitmap);
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });

        btnRate_1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRate_1_1.setVisibility(View.INVISIBLE);
                btnRate_4_3.setVisibility(View.VISIBLE);
                transparentBoxTop.setVisibility(View.VISIBLE);
                transparentBoxBottom.setVisibility(View.VISIBLE);
                filterRateFlag = 1;
            }
        });

        btnRate_4_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRate_1_1.setVisibility(View.VISIBLE);
                btnRate_4_3.setVisibility(View.INVISIBLE);
                transparentBoxTop.setVisibility(View.INVISIBLE);
                transparentBoxBottom.setVisibility(View.INVISIBLE);
                filterRateFlag = 0;

            }
        });

        btnRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bmp = bitmap;
                bitmap = imgRotate();
                imageView.setImageBitmap(bitmap);
                GlobalUploadBitmapImage.bitmapCopy = bitmap;

            }
        });

        btn_next.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView title = (TextView) findViewById(R.id.btn_filtering_title);
        title.setText(getResources().getString(R.string.filter_title_select_filter));

//        btn_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //사본에 저장하고 그리기 작업
//                GlobalUploadBitmapImage.bitmapCopy = bitmap;
//
//                //사진을 4:3 비율로 자른다.
//                if (filterRateFlag == 1 && rateFlag == 0) {
//
//                    GlobalUploadBitmapImage.bitmapCopy = Bitmap.createBitmap(GlobalUploadBitmapImage.bitmapCopy, 0, GlobalUploadBitmapImage.bitmapCopy.getWidth()/8,
//                            GlobalUploadBitmapImage.bitmap.getWidth(), GlobalUploadBitmapImage.bitmap.getWidth() - GlobalUploadBitmapImage.bitmapCopy.getWidth()/4);
//                }
//
//                Intent intent = new Intent(Filtering.this, InputContents.class);
//
//                intent.putExtra("MEDIA_TYPE", "image");
//
//                startActivity(intent);
//            }
//        });
        btn_next_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisGif){
                    wentToInputContents = true;
                    Intent intent = new Intent(Filtering.this, InputContents.class);
                    String activityName = "UploadPhotoActivity";

                    intent.putExtra("ACTIVITY_NAME", activityName);
                    intent.putExtra("TITLE_HEIGHT",  intent.getExtras().getString("TITLE_HEIGHT"));
                    intent.putExtra("MEDIA_TYPE", "gif");
                    intent.putExtra("RATE", rateFlag);
                    //intent.putExtra("filePath", file)

                    startActivity(intent);
                }else{
                    //사본에 저장하고 그리기 작업
                    GlobalUploadBitmapImage.bitmapCopy = bitmap;

                    //사진을 4:3 비율로 자른다.
                    if (filterRateFlag == 1) {

                        GlobalUploadBitmapImage.bitmapCopy = Bitmap.createBitmap(GlobalUploadBitmapImage.bitmapCopy, 0, GlobalUploadBitmapImage.bitmapCopy.getWidth()/8,
                                GlobalUploadBitmapImage.bitmap.getWidth(), GlobalUploadBitmapImage.bitmap.getWidth() - GlobalUploadBitmapImage.bitmapCopy.getWidth()/4);
                    }

                    Intent intent = new Intent(Filtering.this, InputContents.class);

                    intent.putExtra("MEDIA_TYPE", "image");

                    startActivity(intent);
                }
            }
        });

        btn_cancel_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FILTERING", "CANCEL");
                if (isThisGif == true)
                    if (mTimer != null)
                        mTimer.cancel();
                finish();
            }
        });
    }

    Thread filterThread_1 = new Thread("Filter Thread") {

        public void run() {
            filterBitmap_1 = filterBitmap;
            GPUImage mGPUImage = new GPUImage(Filtering.this);
            mGPUImage.setImage(filterBitmap_1);
            mGPUImage.setFilter(new IFHudsonFilter(Filtering.this));
            filterBitmap_1 = mGPUImage.getBitmapWithFilterApplied();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imgFilter_6.setImageBitmap(filterBitmap_1);
                }
            });
        }
    };

    Thread filterThread_2 = new Thread("Filter Thread 2") {

        public void run() {
            filterBitmap_2 = filterBitmap;
            GPUImage mGPUImage = new GPUImage(Filtering.this);
            mGPUImage.setImage(filterBitmap_2);
            mGPUImage.setFilter(new IFInkwellFilter(Filtering.this));
            filterBitmap_2 = mGPUImage.getBitmapWithFilterApplied();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imgFilter_7.setImageBitmap(filterBitmap_2);
                }
            });
        }
    };

    Thread filterThread_3 = new Thread("Filter Thread 3") {

        public void run() {
            filterBitmap_3 = filterBitmap;
            GPUImage mGPUImage = new GPUImage(Filtering.this);
            mGPUImage.setImage(filterBitmap_3);
            mGPUImage.setFilter(new IFLomoFilter(Filtering.this));
            filterBitmap_3 = mGPUImage.getBitmapWithFilterApplied();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imgFilter_8.setImageBitmap(filterBitmap_3);
                }
            });
        }
    };

    Thread filterThread_4 = new Thread("Filter Thread 4") {

        public void run() {
            filterBitmap_4 = filterBitmap;
            GPUImage mGPUImage = new GPUImage(Filtering.this);
            mGPUImage.setImage(filterBitmap_4);
            mGPUImage.setFilter(new IFLordKelvinFilter(Filtering.this));
            filterBitmap_4 = mGPUImage.getBitmapWithFilterApplied();

            mHandler.post(new Runnable() {
                @Override
                public void run() {


                    imgFilter_9.setImageBitmap(filterBitmap_4);
                }
            });
        }
    };

    Thread filterThread_5 = new Thread("Filter Thread 5") {

        public void run() {
            filterBitmap_5 = filterBitmap;
            GPUImage mGPUImage = new GPUImage(Filtering.this);
            mGPUImage.setImage(filterBitmap_5);
            mGPUImage.setFilter(new IFHefeFilter(Filtering.this));
            filterBitmap_5 = mGPUImage.getBitmapWithFilterApplied();

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    imgFilter_5.setImageBitmap(filterBitmap_5);
                }
            });
        }
    };

    private void makeGifFilteringThread(){

        threadForGifFiltered = new Thread("GIF Filtering Thread") {
            public void run() {
//                Filtering.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressDialog.show();
//                    }
//                });

                if (selectedFilterIndex != 0){
//                    ArrayList<Bitmap> tempFilteredBitmapList = new ArrayList<Bitmap>();
                    gpuImage.setFilter(filterArray[selectedFilterIndex - 1]);
                    for (int i = 0; i < GlobalUploadBitmapImage.bitmapList.size(); i++) {
                        gpuImage.setImage(GlobalUploadBitmapImage.bitmapList.get(i));
//                        tempFilteredBitmapList.add(gpuImage.getBitmapWithFilterApplied());
                        GlobalUploadBitmapImage.filteredBitmapList.set(i, gpuImage.getBitmapWithFilterApplied());
                    }

//                    GlobalUploadBitmapImage.filteredBitmapList = tempFilteredBitmapList;
                }
//                Filtering.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressDialog.hide();
//                    }
//                });
            }
        };
        threadForGifFiltered.start();
    }

    private Bitmap imgRotate() {

        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        return resizedBitmap;

    }

    //전면카메라로 촬영을 하면 오른쪽으로 회전이 되어 일단 임시조치로 강제회전
    private Bitmap imgRotate(boolean cameraFront) {

        int width = GlobalUploadBitmapImage.bitmap.getWidth();
        int height = GlobalUploadBitmapImage.bitmap.getHeight();

        Matrix matrix = new Matrix();

        if(cameraFront)
            matrix.postRotate(270);
        else {
            matrix.postRotate(90);
        }

        Bitmap resizedBitmap = Bitmap.createBitmap(GlobalUploadBitmapImage.bitmap, 0, 0, width, height, matrix, true);

        return resizedBitmap;

    }

    private Bitmap reverse(Bitmap bitmap) {

        Matrix sideInversion = new Matrix();
        sideInversion.setScale(-1, 1);
        Bitmap sideInversionImg = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), sideInversion, false);

        return sideInversionImg;

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (wentToInputContents == true)
//            if (isThisGif)
//                if (mTimer != null) {
//                    wentToInputContents = false;
//                    mTimer.schedule(mTask, 0, 200);
//                }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();

        if (mTimer != null)
            mTimer.cancel();

        RecycleUtils.recursiveRecycle(imageView);
        RecycleUtils.recursiveRecycle(imgFilter_2);
        RecycleUtils.recursiveRecycle(imgFilter_3);
        RecycleUtils.recursiveRecycle(imgFilter_4);
        RecycleUtils.recursiveRecycle(imgFilter_5);
        RecycleUtils.recursiveRecycle(imgFilter_6);
        RecycleUtils.recursiveRecycle(imgFilter_7);
        RecycleUtils.recursiveRecycle(imgFilter_8);
        RecycleUtils.recursiveRecycle(imgFilter_9);
        RecycleUtils.recursiveRecycle(imgFilterOriginal);
        RecycleUtils.recursiveRecycle(btnBrightness);
        RecycleUtils.recursiveRecycle(btnRate_1_1);
        RecycleUtils.recursiveRecycle(btnRate_4_3);
        bitmap.recycle();

    }

    class LoadFilterImage extends AsyncTask<GPUImageFilter, Integer, Void> {

        GPUImage mGPUImage = new GPUImage(Filtering.this);

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            mGPUImage.setImage(filterBitmap);

        }
        @Override
        protected Void doInBackground(GPUImageFilter... params) {
            //setProgressBarIndeterminateVisibility(true);

            int totalCount = params.length;

            for(int i=0; i<totalCount; i++) {

                mGPUImage.setFilter(params[i]);
                filterBitmap = mGPUImage.getBitmapWithFilterApplied();

                publishProgress(i, totalCount);

            }
            return null;
        }

        @Override
        public void onProgressUpdate(Integer... filters) {

            //Toast.makeText(Filtering.this, Integer.toString(filters[0]), Toast.LENGTH_SHORT).show();

            switch (filters[0]) {
                case 0:
                    imgFilter_2.setImageBitmap(filterBitmap);
                    imgFilter_2.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;

                case 1:
                    imgFilter_3.setImageBitmap(filterBitmap);
                    break;
                case 2:
                    imgFilter_4.setImageBitmap(filterBitmap);
                    break;
                case 3:
                    imgFilter_5.setImageBitmap(filterBitmap);
                    break;
                case 4:
                    imgFilter_6.setImageBitmap(filterBitmap);
                    break;
                case 5:
                    imgFilter_7.setImageBitmap(filterBitmap);
                    break;
                case 6:
                    imgFilter_8.setImageBitmap(filterBitmap);
                    break;
                case 7:
                    imgFilter_9.setImageBitmap(filterBitmap);
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {


        }
    }

    public void startThread() {

        filterThread_5.start();
        filterThread_1.start();
        filterThread_2.start();
        filterThread_3.start();
        filterThread_4.start();

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

}
