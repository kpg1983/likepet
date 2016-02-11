package com.likelab.likepet.Main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexbbb.uploadservice.UploadService;
import com.facebook.FacebookSdk;
import com.likelab.likepet.BuildConfig;
import com.likelab.likepet.R;
import com.likelab.likepet.global.BackPressCloseHandler;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.more.MoreActivity;
import com.likelab.likepet.notification.AlarmActivity;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;
import com.likelab.likepet.upload.Upload;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kpg1983 on 2015-09-20.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private int numPages = 3;

    public final static int fragment_page_1 = 0;
    public final static int fragment_page_2 = 1;
    public final static int fragment_page_3 = 2;

    private ViewPager pager;

    private TextView txtTitle;

    private ImageButton btnHome;
    private ImageButton btnFeed;
    private ImageButton btnMypage;

    private ImageView btnMore;
    private ImageView btnAlarm;

    private RelativeLayout tabBarHome;
    private RelativeLayout tabBarFeed;
    private RelativeLayout tabBarMyPage;

    private RelativeLayout homeTabContainer;
    private RelativeLayout feedTabContainer;
    private RelativeLayout myPageTabContainer;

    private RelativeLayout btnMoreContainer;
    private RelativeLayout btnAlarmContainer;

    public RelativeLayout overlay;     //반투명 배경

    private static int REQ_UPLOAD_CONTENTS = 0;
    private static int REQ_LOGOUT = 5;
    private static int REQ_NO_LOGIN = 7;

    private BackPressCloseHandler backPressCloseHandler;    //뒤로 버튼을 두번 눌러 종료하기 위한 핸들러

    public String uploadTempFilePath;      //업르드 완료 후 임시 파일 경로 저장


    public String upload = "finish";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalVariable.backKeyPressed = getResources().getString(R.string.common_toast_finish_app);

        backPressCloseHandler = new BackPressCloseHandler(this);

        txtTitle = (TextView)findViewById(R.id.main_txt_title);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_SIGNATURES);

            for(Signature signature : pInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                //Log.d("keyHash")
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int versionCode = pInfo.versionCode;
        String versionName = pInfo.versionName;
        String strVersionCode = Integer.toString(versionCode);

        GlobalSharedPreference.setAppPreferences(this, "appVersionName", versionName);
        GlobalSharedPreference.setAppPreferences(this, "appVersionCode", strVersionCode);


        if(GlobalSharedPreference.getAppPreferences(this, "login").equals("")) {
            GlobalSharedPreference.setAppPreferences(this, "login", "logout");

        }

        FacebookSdk.sdkInitialize(getApplicationContext());

        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        // Or, you can define it manually.
        UploadService.NAMESPACE = "com.yourcompany.yourapp";


        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.likelab.likepet", PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        }catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        overlay = (RelativeLayout)findViewById(R.id.main_overlay);

        btnMore = (ImageView)findViewById(R.id.btn_more);
        btnMoreContainer = (RelativeLayout)findViewById(R.id.btn_more_container);

        ImageButton btn_upload = (ImageButton) findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(GlobalSharedPreference.getAppPreferences(MainActivity.this, "login").equals("login")) {
                    Intent intent = new Intent(MainActivity.this, Upload.class);

                    intent.setFlags(intent.FLAG_ACTIVITY_SINGLE_TOP | intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQ_UPLOAD_CONTENTS);
                } else {

                    loginPopupRequest(v);
                }
            }
        });

        btnAlarmContainer = (RelativeLayout)findViewById(R.id.btn_alarm_container);
        btnAlarmContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                startActivity(intent);
            }
        });

        btnAlarm = (ImageView)findViewById(R.id.btn_alarm);

        btnMore.setImageResource(R.drawable.mypage_btn_more);
        btn_upload.setImageResource(R.drawable.mypage_btn_upload);

        btnMore.setScaleType(ImageView.ScaleType.FIT_XY);
        btn_upload.setScaleType(ImageView.ScaleType.FIT_XY);

        btnMoreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, MoreActivity.class);
                //intent.setFlags(intent.FLAG_ACTIVITY_SINGLE_TOP | intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQ_LOGOUT);
            }
        });

        initLayout();

        btnHome.setOnClickListener(this);
        btnFeed.setOnClickListener(this);
        btnMypage.setOnClickListener(this);

        btnHome.setScaleType(ImageView.ScaleType.FIT_XY);
        btnFeed.setScaleType(ImageView.ScaleType.FIT_XY);
        btnMypage.setScaleType(ImageView.ScaleType.FIT_XY);

        homeTabContainer.setOnClickListener(this);
        feedTabContainer.setOnClickListener(this);
        myPageTabContainer.setOnClickListener(this);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_LOGOUT) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(MainActivity.this, "로그아웃 되었다냥", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }

        } else if(requestCode == REQ_NO_LOGIN) {
            if(resultCode == 0) {
                pager.setCurrentItem(fragment_page_1);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_my_page_moment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void initLayout() {

        //최초 실행시 3개의 Fragment 중 선택된 Fragment의 탭바 아이콘만 흰색으로 초기회 한다.
        btnMypage = (ImageButton)findViewById(R.id.btn_mypage);
        btnFeed = (ImageButton)findViewById(R.id.btn_feed);
        btnHome = (ImageButton)findViewById(R.id.btn_home);

        btnHome.setImageResource(R.drawable.mypage_top_btn_home_s);
        btnFeed.setImageResource(R.drawable.mypage_top_btn_feed_n);
        btnMypage.setImageResource(R.drawable.mypage_top_btn_mypage_n);

        tabBarHome = (RelativeLayout)findViewById(R.id.main_tabBar_home);
        tabBarFeed = (RelativeLayout)findViewById(R.id.main_tabBar_feed);
        tabBarMyPage = (RelativeLayout)findViewById(R.id.main_tabBar_myPage);

        homeTabContainer = (RelativeLayout)findViewById(R.id.main_home_tab_container);
        feedTabContainer = (RelativeLayout)findViewById(R.id.main_feed_tab_container);
        myPageTabContainer = (RelativeLayout)findViewById(R.id.main_mypage_tab_container);

        tabBarHome.setVisibility(View.VISIBLE);
        tabBarFeed.setVisibility(View.INVISIBLE);
        tabBarMyPage.setVisibility(View.INVISIBLE);


        pager = (ViewPager)findViewById(R.id.main_viewPager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(fragment_page_1);


        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                btnHome.setImageResource(R.drawable.mypage_top_btn_home_n);
                btnFeed.setImageResource(R.drawable.mypage_top_btn_feed_n);
                btnMypage.setImageResource(R.drawable.mypage_top_btn_mypage_n);

                tabBarHome.setVisibility(View.INVISIBLE);
                tabBarFeed.setVisibility(View.INVISIBLE);
                tabBarMyPage.setVisibility(View.INVISIBLE);

                btnHome.setScaleType(ImageView.ScaleType.FIT_XY);
                btnFeed.setScaleType(ImageView.ScaleType.FIT_XY);
                btnMypage.setScaleType(ImageView.ScaleType.FIT_XY);

                switch (position) {
                    case 0: {
                        btnHome.setImageResource(R.drawable.mypage_top_btn_home_s);
                        tabBarHome.setVisibility(View.VISIBLE);
                        txtTitle.setText("");
                        break;
                    }
                    case 1: {

                        btnFeed.setImageResource(R.drawable.mypage_top_btn_feed_s);
                        tabBarFeed.setVisibility(View.VISIBLE);

                        txtTitle.setText("Feed");

                        if(GlobalSharedPreference.getAppPreferences(MainActivity.this, "login").equals("login")) {

                        } else  {
                            Intent intent = new Intent(MainActivity.this, JoinMemberBeginActivity.class);
                            startActivityForResult(intent, REQ_NO_LOGIN);
                        }


                        break;
                    }
                    case 2: {
                        btnMypage.setImageResource(R.drawable.mypage_top_btn_mypage_s);
                        tabBarMyPage.setVisibility(View.VISIBLE);

                        txtTitle.setText("My Page");
                        break;
                    }
                }

                btnHome.setScaleType(ImageView.ScaleType.FIT_XY);
                btnFeed.setScaleType(ImageView.ScaleType.FIT_XY);
                btnMypage.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_home : {
                pager.setCurrentItem(fragment_page_1);
                break;
            }
            case R.id.btn_feed : {

                if(GlobalSharedPreference.getAppPreferences(MainActivity.this, "login").equals("login"))
                    pager.setCurrentItem(fragment_page_2);
                else {
                    startActivity(new Intent(MainActivity.this, JoinMemberBeginActivity.class));
                }
                break;
            }
            case R.id.btn_mypage : {
                pager.setCurrentItem(fragment_page_3);
                break;
            }
            case R.id.main_home_tab_container:
                pager.setCurrentItem(fragment_page_1);
                break;

            case R.id.main_feed_tab_container:
                if(GlobalSharedPreference.getAppPreferences(MainActivity.this, "login").equals("login"))
                    pager.setCurrentItem(fragment_page_2);
                else {
                    startActivity(new Intent(MainActivity.this, JoinMemberBeginActivity.class));
                }
                break;

            case R.id.main_mypage_tab_container:
                pager.setCurrentItem(fragment_page_3);
                break;
        }
    }

    private void loginPopupRequest(View v) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.recommend_join_member_popup_windown, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        RelativeLayout joinLater = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_later_container);
        RelativeLayout joinNow = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_now_container);

        //검투명 바탕색 뷰
        overlay.setVisibility(View.VISIBLE);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);

                pager.setCurrentItem(fragment_page_1);
            }
        });
        joinLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        joinNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(MainActivity.this, JoinMemberBeginActivity.class);
                startActivity(intent);

            }
        });


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //로그인 리퀘스트가 실행된 경우 마이페이지로 이동
        if (intent.hasExtra("LOGIN_REQUEST") || intent.hasExtra("UPLOAD_REQUEST") || intent.hasExtra("MYPAGE")) {

            if(intent.hasExtra("FILE_PATH")) {
                uploadTempFilePath = intent.getStringExtra("FILE_PATH");
            }

            if(intent.hasExtra("UPLOAD_REQUEST")) {
                upload = "uploading";

            }

            pager.getAdapter().notifyDataSetChanged();
            pager.setCurrentItem(fragment_page_3);
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
