package com.likelab.likepet.yourPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.CircleTransform;
import com.likelab.likepet.R;
import com.likelab.likepet.UploadContents;
import com.likelab.likepet.bookmark.BookmarkActivity;
import com.likelab.likepet.follow.FollowerUserListActivity;
import com.likelab.likepet.follow.FollowingUserListActivity;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.global.RoundedAvatarDrawable;
import com.likelab.likepet.more.UserProfile;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kpg1983 on 2015-11-30.
 */
public class YourPageActivity extends Activity {

    private static final int RESULT_CODE = 1;

    ArrayList<YourPageContents> contentsArrayList;
    ListView contentsList;

    TextView profileName;
    ImageButton btn_bookmark;
    ImageButton btn_setting;
    ImageView btnFollow;

    ImageView mainProfileImage;

    ImageView imgNoMoment;

    TextView txtFollower;
    TextView txtFollowing;
    TextView txtMoment;

    RelativeLayout followingInfoContainer;
    RelativeLayout followerInfoContainer;

    RelativeLayout menuBarContainer;
    RelativeLayout userProfileSummaryContainer; //유저의 닉네임과 종족 이미지 컨테이너

    RelativeLayout cancelContainer;
    RelativeLayout profileImageContainer;

    public RelativeLayout overlay;

    private TextView txtNoMomentTime;

    ImageView contentStart;

    View header;

    String sid;

    private RequestQueue queue = AppController.getInstance().getRequestQueue();
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private YourPageContentsAdapter adapter;
    private String userId;

    int followingFlag = 0;

    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final int REQ_CODE_MODIFY_CONTENT_INFO = 1;

    private static final int RESULT_MODIFY_CONTENT_SUMMARY = 5;
    private static final int RESULT_DELETE_CONTENT = 7;

    int currentPage = 0;
    int maxPage;
    int adapterFlag = 0;

    private SwipeRefreshLayout mSwipeRefresh;

    private boolean lockListView;

    Bitmap selectedImage;
    String filePath;
    private static final String TEMP_PHOTO_FILE = "temp.jpg";       // 임시 저장파일

    private RelativeLayout noMomentContainer;

    private View footer;
    private RelativeLayout listViewLoadIndicator;

    boolean refreshLock = false;

    static String momentsAgo;
    static String minutesAgo;
    static String hoursAgo;
    static String daysAgo;
    static String monthAgo;
    static String yearsAgo;

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_my_page_moment);

        Intent intent = getIntent();
        userId = intent.getExtras().getString("USER_ID");
        final String name = intent.getExtras().getString("NAME");
        final String clan = intent.getExtras().getString("CLAN");
        final String profileImageUrl = intent.getExtras().getString("PROFILE_IMAGE");

        //변수 초기화
        initView();

        //페이지 셋팅
        inflateLayout(name, profileImageUrl, clan);

        //데이터 리퀘스트
        mypageRequest(userId, currentPage);


        //페이지 새로 고침
        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swype_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshLock = true;
                listViewLoadIndicator.setVisibility(View.GONE);

                currentPage = 0;

                adapter.notifyDataSetInvalidated();
                contentsArrayList.clear();
                mypageRequest(userId, currentPage);
                adapterFlag = 0;
            }
        });

        //프로필 이미지를 선택하면 확대 표시한다
        mainProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(profileImageUrl.contains("http")) {
                    ProfileImagePopup(v, profileImageUrl, clan);
                }
            }
        });

        //팔로우 하기
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userId.equals(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "userId"))) {

                    Intent intent = new Intent(
                            Intent.ACTION_PICK,      // 또는 ACTION_PICK
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");              // 모든 이미지
                    intent.putExtra("crop", "true");        // Crop기능 활성화\
                    intent.putExtra("aspectX", 1); //이걸 삭제한다
                    intent.putExtra("aspectY", 1); //이걸 삭제한다
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
                    intent.putExtra("outputFormat",         // 포맷방식
                            Bitmap.CompressFormat.JPEG.toString());

                    startActivityForResult(intent, REQ_CODE_PICK_IMAGE);


                } else {
                    if (followingFlag == 0) {
                        addFollowingRequest(userId);

                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String endPoint = "/follow/following";
                                    String parameter = "followingUserId=" + userId;

                                    URL url = new URL(GlobalUrl.BASE_URL + endPoint + "?" + parameter);

                                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                                    httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                    httpCon.setRequestMethod("DELETE");
                                    httpCon.setRequestProperty("sessionId", GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid"));
                                    httpCon.setRequestProperty("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                                            GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");
                                    int responseCode = httpCon.getResponseCode();

                                    if (responseCode == 200) {
                                        followingFlag = 0;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btnFollow.setImageResource(R.drawable.mypage_profile_btn_plus);
                                            }
                                        });

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                }

            }
        });

        cancelContainer = (RelativeLayout)findViewById(R.id.mypage_back_key_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login")) {
                    Intent intent = new Intent(YourPageActivity.this, BookmarkActivity.class);
                    startActivity(intent);
                } else {

                }
            }
        });

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login")) {
                    Intent intent = new Intent(YourPageActivity.this, UserProfile.class);
                    startActivity(intent);
                } else {


                }
            }
        });

        followingInfoContainer = (RelativeLayout) header.findViewById(R.id.following_info_container);
        followingInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String follow = "following";

                Intent intent = new Intent(YourPageActivity.this, FollowingUserListActivity.class);
                intent.putExtra("FOLLOW", follow);
                intent.putExtra("PAGE", "YOUR_PAGE");
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        followerInfoContainer = (RelativeLayout) header.findViewById(R.id.follower_info_container);
        followerInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String follow = "follower";

                Intent intent = new Intent(YourPageActivity.this, FollowerUserListActivity.class);
                intent.putExtra("FOLLOW", follow);
                intent.putExtra("PAGE", "YOUR_PAGE");
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });


        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView == false && refreshLock == false) {

                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        if (adapterFlag == 1) {
                            listViewLoadIndicator.setVisibility(View.VISIBLE);
                        }

                        mypageRequest(userId, currentPage);

                    }
                }
            }

        });

        contentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position_1, long id) {

                int contentType;
                int numberOfLike;
                int blackFlag;      //해당 컨텐츠가 신고로 인하여 블락처리 되어있는지 확인한다.
                int numberOfComment;
                int position = position_1 - 1;

                numberOfLike = contentsArrayList.get(position).likeCount;
                blackFlag = contentsArrayList.get(position).blackFlag;
                numberOfComment = contentsArrayList.get(position).commentCount;
                String description = contentsArrayList.get(position).descriptionTag;
                String contentId = contentsArrayList.get(position).contentsId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;
                String clan = contentsArrayList.get(position).clan;


                Intent intent = new Intent(YourPageActivity.this, ViewActivity.class);

                if (contentsArrayList.get(position).contentsType.matches(".*image.*")) {
                    contentType = 1;
                    if (contentsArrayList.get(position).contentsType.matches(".*gif.*")) {
                        contentType = 3;
                    }
                } else {
                    contentType = 2;
                }

                intent.putExtra("CONTENT_ID", contentId);
                intent.putExtra("DESC", description);
                intent.putExtra("TYPE", contentType);
                intent.putExtra("LIKES", numberOfLike);
                intent.putExtra("BLIND_FLAG", blackFlag);
                intent.putExtra("NUMBER_OF_COMMENT", numberOfComment);
                intent.putExtra("IMAGE_URL", contentsArrayList.get(position).contentsUrl);
                intent.putExtra("ILIKETHIS", iLikeThis);
                intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl);
                intent.putExtra("NAME", name);
                intent.putExtra("POSITION", position);
                intent.putExtra("LIKE_COUNT", likeCount);
                intent.putExtra("COMMENT_COUNT", commentCount);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("REPORT_COUNT", reportCount);
                intent.putExtra("STATUS", status);
                intent.putExtra("CLAN", clan);

                startActivityForResult(intent, RESULT_CODE);

            }
        });
    }

    private void initView() {


        //시간을 며칠 전 식으로 변경하기 위험
        momentsAgo = getResources().getString(R.string.now);
        minutesAgo = getResources().getString(R.string.minute_ago);
        hoursAgo = getResources().getString(R.string.hour_ago);
        daysAgo = getResources().getString(R.string.day_ago);
        monthAgo = getResources().getString(R.string.month_ago);
        yearsAgo = getResources().getString(R.string.year_ago);

        overlay = (RelativeLayout)findViewById(R.id.mypage_overlay);
        lockListView = false;
        contentsArrayList = new ArrayList<YourPageContents>();

        sid = GlobalSharedPreference.getAppPreferences(this, "sid");

        header = getLayoutInflater().inflate(R.layout.user_profile_list, null, false);
        footer = getLayoutInflater().inflate(R.layout.listview_load_footer, null, false);

        listViewLoadIndicator = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);

        profileName = (TextView) findViewById(R.id.mypage_txt_user_name);
        btn_bookmark = (ImageButton) header.findViewById(R.id.button_bookmark);
        btn_setting = (ImageButton) header.findViewById(R.id.button_setting);
        btnFollow = (ImageView) header.findViewById(R.id.mypage_btn_camera);
        mainProfileImage = (ImageView) header.findViewById(R.id.main_profile_image);

        txtFollower = (TextView) header.findViewById(R.id.number_of_follower);
        txtFollowing = (TextView) header.findViewById(R.id.number_of_following);
        txtMoment = (TextView) header.findViewById(R.id.number_of_moments);
        userProfileSummaryContainer = (RelativeLayout)header.findViewById(R.id.mypage_user_profile_summary_container);
        profileImageContainer = (RelativeLayout)header.findViewById(R.id.user_profile_container);
        noMomentContainer = (RelativeLayout)header.findViewById(R.id.mypage_no_moment_container);
        imgNoMoment = (ImageView)header.findViewById(R.id.mypage_img_noMoment_image);
        txtNoMomentTime = (TextView)header.findViewById(R.id.contents_time);
        contentStart = (ImageView)header.findViewById(R.id.contents_start);

        menuBarContainer = (RelativeLayout)findViewById(R.id.mypage_menu_tab);
        menuBarContainer.setVisibility(View.VISIBLE);

        adapter = new YourPageContentsAdapter(YourPageActivity.this, R.layout.mypage_listview, contentsArrayList, YourPageActivity.this);

        contentsList = (ListView)findViewById(R.id.contents_list);
        contentsList.addHeaderView(header);
        contentsList.addFooterView(footer);
    }

    private void inflateLayout(String name, String profileImageUrl, final String clan) {

        //다른 사람의 페이지인 경우 즐겨찾기, 프로필 수정 페이지 항목을 감춘다.
        if(GlobalSharedPreference.getAppPreferences(this, "login").equals("login")) {
            if(!userId.equals(GlobalSharedPreference.getAppPreferences(this, "userId"))) {
                btn_bookmark.setVisibility(View.GONE);
                btn_setting.setVisibility(View.GONE);
            }

            else {
                // 나의 페이지인 경우 즐겨찾기 항목을 보여주고 카메라 버튼을 노출훈다
                btnFollow.setImageResource(R.drawable.mypage_profile_btn_photo);
            }

        }

        userProfileSummaryContainer.setVisibility(View.GONE);

        profileName.setText(name);

        if(clan.equals("0")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_01)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(mainProfileImage);
        } else if(clan.equals("1")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_02)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(mainProfileImage);
        } else if(clan.equals("2")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_03)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(mainProfileImage);
        }


        btn_bookmark.setImageResource(R.drawable.mypage_profile_btn_bookmark);
        btn_bookmark.setScaleType(ImageView.ScaleType.FIT_XY);

        btn_setting.setImageResource(R.drawable.mypage_profile_btn_setup);
        btn_setting.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    //사용자 이미지를 누르면 확대한다
    private void ProfileImagePopup(View v, String profileImageUrl, final String clan) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.view_image_comment_popup_window, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        //팝업 화면을 띄울때 반투명 검정색 배경을 화면에 표시한다.
        overlay.setVisibility(View.VISIBLE);

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        final ImageView profileImageExpansion = (ImageView)popupView.findViewById(R.id.view_img_comment_expansion);
        Picasso.with(this).load(profileImageUrl).resize(960, 960).into(profileImageExpansion);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);
                profileImageExpansion.setImageDrawable(null);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(adapter != null) {
            adapter.recycle();
        }
        RecycleUtils.recursiveRecycle(imgNoMoment);
        RecycleUtils.recursiveRecycle(btn_bookmark);
        RecycleUtils.recursiveRecycle(btn_setting);
        RecycleUtils.recursiveRecycle(btnFollow);
        RecycleUtils.recursiveRecycle(mainProfileImage);

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }

    public void contentsInfoRequest(final int position, String contentId) {

        String endPoint = "/contents/" + contentId;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (responseCode == 200) {
                            try {
                                JSONObject item = response.getJSONObject("item");
                                int likeCount = item.getInt("likeCount");
                                int commentCount = item.getInt("commentCount");
                                String iLikeThis = item.getString("ILikedThis");
                                String descriptions = item.getString("descriptions");

                                contentsArrayList.get(position).likeCount = likeCount;
                                contentsArrayList.get(position).commentCount = commentCount;
                                contentsArrayList.get(position).iLikeThis = iLikeThis;
                                contentsArrayList.get(position).descriptionTag = descriptions;

                                updateList(contentsArrayList);


                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }

    public void addFollowingRequest(String userId) {

        String endPoint = "/follow/following";
        JSONObject obj = new JSONObject();

        try {
            obj.put("followingUserId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                followingFlag = 1;
                                btnFollow.setImageResource(R.drawable.mypage_profile_btn_minus);

                            }

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        System.out.println(error.toString());
                    }


                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void mypageRequest(String userId, int pageNo) {

        lockListView = true;

        String endPoint = "/otherpage/"+ userId;

        String parameter = "?pageNo=" + pageNo;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            String temp = response.toString();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (responseCode == 200) {

                            try {
                                JSONObject otherJsonObject = response.getJSONObject("otherpage");
                                JSONObject pages = otherJsonObject.getJSONObject("pages");

                                JSONObject other = otherJsonObject.getJSONObject("other");
                                String profileImageUrl = other.getString("profileImageUrl");
                                String name = other.getString("name");
                                String clan = other.getString("clan");

                                maxPage = pages.getInt("max");

                                JSONArray jsonArray = otherJsonObject.getJSONArray("items");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String userId = jsonArray.getJSONObject(i).getString("userId");
                                    String contentType = jsonArray.getJSONObject(i).getString("contentType");
                                    String registryDate = jsonArray.getJSONObject(i).getString("registryDate");
                                    String descriptions = jsonArray.getJSONObject(i).getString("descriptions");
                                    String contentId = jsonArray.getJSONObject(i).getString("contentId");
                                    String videoScreenshotUrl = jsonArray.getJSONObject(i).getString("videoScreenshotUrl");
                                    String iLikeThis;
                                    String status = jsonArray.getJSONObject(i).getString("status");
                                    int reportCount = jsonArray.getJSONObject(i).getInt("reportCount");

                                    //로그인 상태일 경우만 감정표현 유무를 표시한다
                                    if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login")) {
                                        iLikeThis = jsonArray.getJSONObject(i).getString("ILikedThis");

                                    } else {
                                        iLikeThis = "0";
                                    }

                                    //미디어 사이즈를 확인하고 저장한다
                                    String mediaSize = jsonArray.getJSONObject(i).getString("mediaSize");
                                    String mediaSizeArr[] = mediaSize.split(",");

                                    int mediaWidth;
                                    int mediaHeight;

                                    if(contentType.contains("video")) {
                                        mediaWidth = 960;
                                        mediaHeight = 720;

                                    } else {

                                        mediaWidth = Integer.parseInt(mediaSizeArr[0]);
                                        mediaHeight = Integer.parseInt(mediaSizeArr[1]);
                                    }

                                    Date date = null;
                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        String localTime = convertUtcToLocal(registryDate);
                                        date = format.parse(localTime);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    registryDate = formatTimeString(date);


                                    String contentUrl = jsonArray.getJSONObject(i).getString("contentUrl");
                                    int likeCount = jsonArray.getJSONObject(i).getInt("likeCount");
                                    int commentCount = jsonArray.getJSONObject(i).getInt("commentCount");


                                    //베스트 댓글이 없는 경우
                                    if (!jsonArray.getJSONObject(i).has("bestCommentItems")) {
                                        YourPageContents content = new YourPageContents(contentUrl, contentType, registryDate, likeCount, 0, null, null, null, null,
                                                null, null, null, null, null, commentCount, 0, contentId, iLikeThis, descriptions, userId, videoScreenshotUrl, status, reportCount,
                                                profileImageUrl, name, mediaWidth, mediaHeight, clan);

                                        contentsArrayList.add(content);

                                    } else {


                                        JSONArray commentJSONArray = jsonArray.getJSONObject(i).getJSONArray("bestCommentItems");
                                        int numberOfBestComment = commentJSONArray.length();


                                        String commentUrl[] = new String[3];
                                        String commentDescription[] = new String[3];
                                        String commentType[] = new String[3];

                                        //베스트 댓글 갯수만큼 회전후 정보 저장
                                        for(int j=0; j<numberOfBestComment; j++) {
                                            commentUrl[j] = commentJSONArray.getJSONObject(j).getString("commentUrl");
                                            commentType[j] = commentJSONArray.getJSONObject(j).getString("contentType");
                                            commentDescription[j] = commentJSONArray.getJSONObject(j).getString("descriptions");
                                        }

                                        YourPageContents content = new YourPageContents(contentUrl, contentType, registryDate, likeCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2], commentCount, 0, contentId, iLikeThis, descriptions, userId
                                        , videoScreenshotUrl, status, reportCount, profileImageUrl, name, mediaWidth, mediaHeight, clan);

                                        contentsArrayList.add(content);
                                    }

                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        }, 200);

                                        lockListView = false;
                                        listViewLoadIndicator.setVisibility(View.GONE);
                                        refreshLock = false;
                                    }
                                });

                                mSwipeRefresh.setRefreshing(false);

                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }

    public void myPageSummaryRequest(final String userId) {

        String endPoint = "/otherpage/"+ userId + "/summary";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {


                                JSONObject summary = response.getJSONObject("summary");
                                int followerCount = summary.getInt("followerCount");
                                int followingCount = summary.getInt("followingCount");
                                int contentCount = summary.getInt("contentCount");

                                //콘텐츠가 하나도 없을 경우 기본 이미지를 출력한다.
                                if(contentCount == 0) {
                                    noMomentContainer.setVisibility(View.VISIBLE);
                                    imgNoMoment.setImageResource(R.drawable.img_no_moment_01_960x960_02);
                                    contentStart.setImageResource(R.drawable.mypage_img_01);

                                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    String time = sdfNow.format(new Date(System.currentTimeMillis()));

                                    time = time.substring(0, time.indexOf(" "));
                                    time = time.replaceFirst("\\/", ".");
                                    time = time.replaceFirst("\\/", ".");

                                    txtNoMomentTime.setText(time);

                                } else {
                                    noMomentContainer.setVisibility(View.GONE);
                                    imgNoMoment.setImageDrawable(null);
                                }
                                String crossFollow;

                                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login")) {
                                    crossFollow = summary.getString("crossFollow");

                                    //현재 이 페이지가 나의 페이지이면 +, - 버튼 대신 카메라 사진을 보여준다
                                    if(userId.equals(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "userId"))) {
                                        btnFollow.setImageResource(R.drawable.mypage_profile_btn_photo);

                                        //타인의 페이지이면 플러스, 마이너스 버튼을 보여준다
                                    } else {
                                        if (crossFollow.equals("1")) {
                                            followingFlag = 1;
                                            btnFollow.setImageResource(R.drawable.mypage_profile_btn_minus);
                                        } else {
                                            followingFlag = 0;
                                            btnFollow.setImageResource(R.drawable.mypage_profile_btn_plus);
                                        }
                                    }

                                } else {
                                    crossFollow = "0";
                                }

                                txtFollowing.setText(Integer.toString(followingCount));
                                txtFollower.setText(Integer.toString(followerCount));
                                txtMoment.setText(Integer.toString(contentCount));

                            }
                            else if(responseCode == 401) {

                                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "loginType").equals("sns")) {

                                    String accountId = GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "accountId");
                                    String email = GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "email");

                                    snsLoginRequest(email, accountId);

                                } else if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "loginType").equals("email")) {

                                    String password = GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "password");
                                    String email = GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "email");

                                    emailLoginRequest(email, password);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if(GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }


    public void snsLoginRequest(final String email, final String id) {

        String endPoint = "/login/friendly/" + email;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (responseCode == 200) {
                            loadUserInformation(email);
                            GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "email", email);
                            GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "accountId", id);
                            GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "loginType", "sns");
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(YourPageActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }


                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", id);

                return params;

            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                Map<String, String> responseHeaders = response.headers;
                String sid = responseHeaders.get("sessionID");

                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "sid", sid);

                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void emailLoginRequest(final String email, final String password) {

        String endPoint = "/login/" + email;

        JSONObject obj = new JSONObject();

        try {
            obj.put("password", password);
            obj.put("contentApi", true);
        } catch(JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");
                            Toast.makeText(YourPageActivity.this, Integer.toString(responseCode), Toast.LENGTH_LONG);

                            if (responseCode == 200) {

                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "password", password);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "loginType", "email");
                                loadUserInformation(email);


                            } else if(responseCode == 401) {


                            } else if(responseCode == 404) {

                            }

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                Map<String, String> responseHeaders = response.headers;
                String sid = responseHeaders.get("sessionId");

                if(sid != null) {
                    Log.d("SID", sid);
                    GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "sid", sid);

                }
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }

            }

        };
        queue.add(jsonObjectRequest);
    }


    public void loadUserInformation(String email) {

        String endPoint = "/users/user/" + email;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject jsonObject = response.getJSONObject("item");

                                String userId = jsonObject.getString("userId");
                                String name = jsonObject.getString("name");
                                String email = jsonObject.getString("email");
                                String gender = jsonObject.getString("sex");
                                String clan = jsonObject.getString("clan");
                                String status = jsonObject.getString("status");
                                String parentUserId = jsonObject.getString("parentUserId");
                                String profileImageUrl = jsonObject.getString("profileImageUrl");
                                String national = jsonObject.getString("national");
                                String ownerType = jsonObject.getString("ownerType");
                                String birthday = jsonObject.getString("birthday");
                                String registryDate = jsonObject.getString("registryDate");
                                String modifyDate = jsonObject.getString("modifyDate");
                                String termServiceDate = jsonObject.getString("termServiceDate");
                                String privacyTermDate = jsonObject.getString("privacyTermDate");
                                String withdrawReqDate = jsonObject.getString("withdrawReqDate");
                                String withdrawDate = jsonObject.getString("withdrawDate");
                                String mailAuthConfirmDate = jsonObject.getString("mailAuthConfirmDate");
                                String lastLoginDate = jsonObject.getString("lastLoginDate");
                                String mailAuth = jsonObject.getString("mailAuth");

                                //로그인을 하면 서버로부터 사용자의 정보를 받아와 기기에 저장한다.
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(YourPageActivity.this, "login", "login");

                                adapter.notifyDataSetInvalidated();
                                contentsArrayList.clear();

                                currentPage = 0;
                                adapterFlag = 0;

                                mypageRequest(YourPageActivity.this.userId, currentPage);
                                myPageSummaryRequest(YourPageActivity.this.userId);

                            }

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(YourPageActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String sid = GlobalSharedPreference.getAppPreferences(YourPageActivity.this, "sid");
                params.put("sessionId", sid);

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //프로필 사진 변경경
        if(requestCode == REQ_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                    selectedImage = BitmapFactory.decodeFile(filePath);
                    mainProfileImage.setImageDrawable(new RoundedAvatarDrawable(selectedImage, 1));
                    // temp.jpg파일을 Bitmap으로 디코딩한다.
                    // temp.jpg파일을 이미지뷰에 씌운다.
                    //imgUserPhoto.setImageUrl();

                    Runnable runnable = new RunnableImplements();
                    Thread thread = new Thread(runnable);
                    thread.start();

                }

            }
        }
        //좋아요 이벤트
        else if(requestCode == REQ_CODE_MODIFY_CONTENT_INFO){

            //컨텐츠가 삭제된 경우
            if(resultCode == RESULT_DELETE_CONTENT) {
                int position = data.getExtras().getInt("POSITION");
                contentsArrayList.remove(position);
                updateList(contentsArrayList);
                return;


                //좋아요, 댓글 카운트가 변경된 경우
            } else if(resultCode == RESULT_MODIFY_CONTENT_SUMMARY){
                int position = data.getExtras().getInt("POSITION");
                try {
                    contentsInfoRequest(position, contentsArrayList.get(position).contentsId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void updateList(ArrayList<YourPageContents> newList) {

        adapter.notifyDataSetInvalidated();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();

        myPageSummaryRequest(userId);
    }


    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    TEMP_PHOTO_FILE);
            try {
                f.createNewFile();      // 외장메모리에 temp.jpg 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    //유저의 이미지를 업로드한다.
    class RunnableImplements implements Runnable {

        public void run() {
            UploadContents upload = new UploadContents();

            String endPoint = "/users/user/profile/image";
            try {
                upload.HttpFileUpload(GlobalUrl.BASE_URL + endPoint, filePath, getApplicationContext());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }


    public static String formatTimeString(Date tempDate) {

        long curTime = System.currentTimeMillis();
        long regTime = tempDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            // sec
            msg = momentsAgo;
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            // min
            msg = diffTime + minutesAgo;
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            // hour
            msg = (diffTime) + hoursAgo;
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            // day
            msg = (diffTime) + daysAgo;
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            // day
            msg = (diffTime) + monthAgo;
        } else {
            msg = (diffTime) + yearsAgo;
        }

        return msg;
    }

    //표준시간과 local 시간 변환
    private static String convertUtcToLocal(String utcTime) {

        String localTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date dateUtcTime = dateFormat.parse(utcTime);

            long longUtcTime = dateUtcTime.getTime();

            TimeZone timeZone = TimeZone.getDefault();
            int offset = timeZone.getOffset(longUtcTime);
            long longLocalTime = longUtcTime + offset;

            Date dateLocalTime = new Date();
            dateLocalTime.setTime(longLocalTime);

            localTime = dateFormat.format(dateLocalTime);

        } catch (ParseException e) {
            e.printStackTrace();;
        }

        return localTime;
    }

}
