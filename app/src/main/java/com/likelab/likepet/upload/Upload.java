package com.likelab.likepet.upload;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.likelab.likepet.R;
import com.likelab.likepet.global.ActivityList;
import com.likelab.likepet.global.GlobalUploadBitmapImage;
import com.likelab.likepet.global.RecycleUtils;

/**
 * Created by kpg1983 on 2015-09-23.
 */
public class Upload extends AppCompatActivity implements View.OnClickListener{

    public final static int fragment_page_1 = 0;
    public final static int fragment_page_2 = 1;

    private static ViewPager pager;

    private TextView txt_gallery;
    private TextView txt_photo;
    private TextView txt_movie;

    private RelativeLayout tabGallery;
    private RelativeLayout tabPhoto;
    private RelativeLayout tabMovie;

    private RelativeLayout tabGalleryContainer;
    private RelativeLayout tabPhotoContainer;
    private RelativeLayout tabMovieContainer;

    public static TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivityList.activityArrayList.add(this);

        ImageView btn_cancel_upload = (ImageView)findViewById(R.id.btn_upload_cancel);
        final ImageView btn_next = (ImageView)findViewById(R.id.btn_upload_next);

        btn_cancel_upload.setImageResource(R.drawable.upload_btn_cancel);
        btn_next.setImageResource(R.drawable.upload_btn_next);
        btn_next.setScaleType(ImageView.ScaleType.FIT_XY);
        btn_cancel_upload.setScaleType(ImageView.ScaleType.FIT_XY);

        title = (TextView)findViewById(R.id.btn_upload_title);
        pager = (ViewPager)findViewById(R.id.upload_viewPager);

        initLayout();

        txt_movie.setOnClickListener(this);
        txt_photo.setOnClickListener(this);
        txt_gallery.setOnClickListener(this);

        tabGalleryContainer.setOnClickListener(this);
        tabPhotoContainer.setOnClickListener(this);
        tabMovieContainer.setOnClickListener(this);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {

                    case 0: {
                        txt_gallery.setTextColor(Color.parseColor("#f7c243"));
                        txt_photo.setTextColor(Color.parseColor("#ffffff"));
                        txt_movie.setTextColor(Color.parseColor("#ffffff"));
                        title.setText(getResources().getString(R.string.upload_title_select_picture));

                        tabGallery.setVisibility(View.VISIBLE);
                        tabPhoto.setVisibility(View.INVISIBLE);
                        tabMovie.setVisibility(View.INVISIBLE);
                        btn_next.setVisibility(View.VISIBLE);
                        btn_next.setClickable(true);
                        break;
                    }

                    case 1: {
                        txt_gallery.setTextColor(Color.parseColor("#ffffff"));
                        txt_photo.setTextColor(Color.parseColor("#f7c243"));
                        txt_movie.setTextColor(Color.parseColor("#ffffff"));
                        title.setText(getResources().getString(R.string.upload_title_take_picture));

                        tabGallery.setVisibility(View.INVISIBLE);
                        tabPhoto.setVisibility(View.VISIBLE);
                        tabMovie.setVisibility(View.INVISIBLE);

                        //사진 촬영 모드에서는 상단의 next 버튼을 비활성화 시킨다.
                       btn_next.setVisibility(View.GONE);
                        btn_next.setClickable(false);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    protected void initLayout() {

        txt_gallery = (TextView)findViewById(R.id.btn_gallery_bar);
        txt_photo = (TextView)findViewById(R.id.btn_photo_bar);
        txt_movie = (TextView)findViewById(R.id.btn_movie_bar);

        tabGallery = (RelativeLayout)findViewById(R.id.upload_tab_gallery);
        tabPhoto = (RelativeLayout)findViewById(R.id.upload_tab_photo);
        tabMovie = (RelativeLayout)findViewById(R.id.upload_tab_video);

        tabGalleryContainer = (RelativeLayout)findViewById(R.id.upload_gallery_bar_container);
        tabPhotoContainer = (RelativeLayout)findViewById(R.id.upload_photo_bar_container);
        tabMovieContainer = (RelativeLayout)findViewById(R.id.upload_movie_bar_container);

        pager.setAdapter(new UploadPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(fragment_page_1);
        txt_gallery.setTextColor(Color.parseColor("#f7c243"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        for (int i=0; i<GlobalUploadBitmapImage.fileList.size(); i++) {
            if(GlobalUploadBitmapImage.fileList.get(i).exists()) {
                GlobalUploadBitmapImage.fileList.get(i).delete();
            }
        }

        GlobalUploadBitmapImage.fileList.clear();


        //GlobalUploadBitmapImage.filteredBitmapList.clear();
        //GlobalUploadBitmapImage.bitmapList.clear();
        //GlobalUploadBitmapImage.filteredBitmapListCopy.clear();
        //GlobalUploadBitmapImage.filteredBitmapList.clear();

        for (int i=0; i<GlobalUploadBitmapImage.bitmapArray.length; i++) {
            GlobalUploadBitmapImage.bitmapArray[i] = null;
        }

        GlobalUploadBitmapImage.bitmap = null;
        GlobalUploadBitmapImage.bitmapCopy = null;

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.btn_gallery_bar : {
                pager.setCurrentItem(fragment_page_1);
                title.setText("사진 선택");
                txt_gallery.setTextColor(Color.parseColor("#f7c243"));
                txt_photo.setTextColor(Color.parseColor("#ffffff"));
                txt_movie.setTextColor(Color.parseColor("#ffffff"));

                tabGallery.setVisibility(View.VISIBLE);
                tabPhoto.setVisibility(View.INVISIBLE);
                tabMovie.setVisibility(View.INVISIBLE);

                break;
            }
            case R.id.btn_photo_bar : {
                pager.setCurrentItem(fragment_page_2);
                title.setText("사진 촬영");
                UploadPhoto.fragmentState = 1;
                txt_gallery.setTextColor(Color.parseColor("#ffffff"));
                txt_photo.setTextColor(Color.parseColor("#f7c243"));
                txt_movie.setTextColor(Color.parseColor("#ffffff"));

                tabGallery.setVisibility(View.INVISIBLE);
                tabPhoto.setVisibility(View.VISIBLE);
                tabMovie.setVisibility(View.INVISIBLE);
                break;
            }

            case R.id.btn_movie_bar: {
                pager.setCurrentItem(fragment_page_2);
                title.setText("동영상 촬영");
                UploadPhoto.fragmentState = 2;
                txt_gallery.setTextColor(Color.parseColor("#ffffff"));
                txt_photo.setTextColor(Color.parseColor("#ffffff"));
                txt_movie.setTextColor(Color.parseColor("#f7c243"));

                tabGallery.setVisibility(View.INVISIBLE);
                tabPhoto.setVisibility(View.INVISIBLE);
                tabMovie.setVisibility(View.VISIBLE);
                break;
            }

        }

    }

    public static void startFragment()
    {
        pager.setCurrentItem(fragment_page_1);
    }
}
