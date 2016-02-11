package com.likelab.likepet.HomeFeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kpg1983 on 2015-11-01.
 */
public class HomeFeedActivity extends Activity {


    private static final int RESULT_CODE = 1;

    ArrayList<HomeFeedContents> contentsArrayList;
    ListView contentsList;

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private TextView tag_1;
    private TextView tag_2;

    HomeFeedContentsAdapter adapter;
    RequestQueue queue = AppController.getInstance().getRequestQueue();

    private static final int RESULT_MODIFY_CONTENT_SUMMARY = 5;

    private SwipeRefreshLayout mSwipeRefresh;
    private boolean lockListView;
    boolean refreshLock = false;

    int currentPage=0;
    int maxPage;
    int adapterFlag = 0;

    RelativeLayout overlay;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    static String momentsAgo;
    static String minutesAgo;
    static String hoursAgo;
    static String daysAgo;
    static String monthAgo;
    static String yearsAgo;

    String newStory;
    View footer;

    RelativeLayout listViewLoadIndicator;

    String groupId;

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.home_contents_activity);

        momentsAgo = getResources().getString(R.string.now);
        minutesAgo = getResources().getString(R.string.minute_ago);
        hoursAgo = getResources().getString(R.string.hour_ago);
        daysAgo = getResources().getString(R.string.day_ago);
        monthAgo = getResources().getString(R.string.month_ago);
        yearsAgo = getResources().getString(R.string.year_ago);

        lockListView = false;

        Intent intent = getIntent();

        final String groupId = intent.getExtras().getString("GROUP_ID");
        this.groupId = groupId;
        String pageId = intent.getExtras().getString("PAGE_ID");
        int feedCount = intent.getExtras().getInt("FEED_COUNT");
        String tags = intent.getExtras().getString("TAGS");
        newStory = intent.getExtras().getString("NEW_STORY");

        String[] tagArray = tags.split("#");

        contentsArrayList = new ArrayList<HomeFeedContents>();


        //태그가 있는 경우, 없는 경우
        tag_1 = (TextView)findViewById(R.id.home_contents_txt_tag_1);
        tag_2 = (TextView)findViewById(R.id.home_contents_txt_tag_2);

        if(tagArray.length == 2) {

            tag_1.setText("#"+tagArray[1]);
        } else if(tagArray.length == 3) {
            tag_1.setText("#"+tagArray[1]);
            tag_2.setText("#"+tagArray[2]);
        } else {
            tag_1.setText(tags);
        }

        //그룹 설명이 아예 없는 경우는 새로운 이야기
        if(newStory.equals("Y")) {
            tag_1.setText(getResources().getString(R.string.home_feed_txt_new_story_title));

        } else if(tags.equals(" ") || tags.length() == 0 ) {
            tag_1.setText("Editor's Picks");
        }

        overlay = (RelativeLayout)findViewById(R.id.home_overlay);

        cancelContainer = (RelativeLayout)findViewById(R.id.home_contents_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.home_contents_btn_cancel);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        adapter = new HomeFeedContentsAdapter(this, R.layout.home_contents_list_view, contentsArrayList, HomeFeedActivity.this);

        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swype_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                refreshLock = true;
                listViewLoadIndicator.setVisibility(View.GONE);
                adapter.notifyDataSetInvalidated();
                contentsArrayList.clear();

                currentPage = 0;
                adapterFlag = 0;

                if (newStory.equals("Y")) {
                    newStoryRequest(groupId, currentPage);
                } else {
                    feedRequest(groupId, currentPage);
                }

            }
        });

        contentsList = (ListView) findViewById(R.id.home_contents_list);
        footer = getLayoutInflater().inflate(R.layout.listview_load_footer, null, false);
        contentsList.addFooterView(footer);
        listViewLoadIndicator = (RelativeLayout) footer.findViewById(R.id.listview_load_indicator);

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

                        lockListView = true;

                        if (newStory.equals("Y")) {
                            newStoryRequest(groupId, currentPage);
                        } else {
                            feedRequest(groupId, currentPage);
                        }
                    }
                }
            }

        });

        contentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                int contentType;
                int numberOfLike;
                int blackFlag;      //해당 컨텐츠가 신고로 인하여 블락처리 되어있는지 확인한다.
                int numberOfComment;


                numberOfLike = contentsArrayList.get(position).likeCount;
                blackFlag = contentsArrayList.get(position).blackFlag;
                numberOfComment = contentsArrayList.get(position).commentCount;
                String description = contentsArrayList.get(position).description;
                String contentId = contentsArrayList.get(position).contentId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;
                String profileImageUrl = contentsArrayList.get(position).profileImageUrl;
                String userName = contentsArrayList.get(position).name;
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String mediaSize = contentsArrayList.get(position).mediaSize;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;
                String clan = contentsArrayList.get(position).clan;


                Intent intent = new Intent(HomeFeedActivity.this, ViewActivity.class);

                if (contentsArrayList.get(position).contentType.matches(".*image.*")) {
                    contentType = 1;
                    if (contentsArrayList.get(position).contentType.matches(".*gif.*")) {
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
                intent.putExtra("IMAGE_URL", contentsArrayList.get(position).contentUrl);
                intent.putExtra("ILIKETHIS", iLikeThis);
                intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl);
                intent.putExtra("NAME", userName);
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

        if(newStory.equals("Y")) {
            newStoryRequest(groupId, currentPage);
        } else {
            feedRequest(groupId, currentPage);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adapter != null)
            adapter.recycle();

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_MODIFY_CONTENT_SUMMARY){
            int position = data.getExtras().getInt("POSITION");
            try {
                contentsInfoRequest(position, contentsArrayList.get(position).contentId);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                                String iLikeThis;
                                String descriptions = item.getString("descriptions");

                                //로그인 상태일 경우만 감정표현 유무를 표시한다
                                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login")) {
                                    iLikeThis = item.getString("ILikedThis");

                                } else {
                                    iLikeThis = "0";
                                }

                                contentsArrayList.get(position).likeCount = likeCount;
                                contentsArrayList.get(position).commentCount = commentCount;
                                contentsArrayList.get(position).iLikeThis = iLikeThis;
                                contentsArrayList.get(position).description = descriptions;

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

                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }

    public void feedRequest(String groupId, int pageNo) {

        lockListView = true;

        String endPoint = "/home/pages/feeds/"+ groupId;

        String parameter = "?pageNo=" + pageNo;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                adapter.notifyDataSetInvalidated();
                                JSONObject newStory = response.getJSONObject("feeds");
                                JSONObject pages = newStory.getJSONObject("pages");
                                maxPage = pages.getInt("max");
                                JSONArray items = newStory.getJSONArray("items");

                                for(int i=0; i<items.length(); i++) {

                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String descriptions = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");

                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date = null;

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        String localTime = convertUtcToLocal(registryDate);
                                        date = format.parse(localTime);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    registryDate = formatTimeString(date);


                                    String contentUrl = items.getJSONObject(i).getString("contentUrl");
                                    int likeCount = items.getJSONObject(i).getInt("likeCount");
                                    String name = items.getJSONObject(i).getString("writerName");
                                    String status = items.getJSONObject(i).getString("status");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String clan = items.getJSONObject(i).getString("clan");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    int commentCount = items.getJSONObject(i).getInt("commentCount");
                                    String videoScreenshotUrl  = items.getJSONObject(i).getString("videoScreenshotUrl");
                                    String iLikeThis;

                                    //로그인을 한 상태에서만 나의 감정 표현 유무가 표시된다
                                    if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login"))
                                        iLikeThis = items.getJSONObject(i).getString("ILikedThis");
                                    else{
                                        iLikeThis = "0";
                                    }

                                    //미디어 사이즈를 확인하고 저장한다
                                    String mediaSize = items.getJSONObject(i).getString("mediaSize");
                                    String mediaSizeArr[] = mediaSize.split(",");

                                    int mediaWidth;
                                    int mediaHeight;

                                    //비디오는 크기가 현재 고정이다
                                    if(contentType.contains("video")) {
                                        mediaWidth = 960;
                                        mediaHeight = 720;

                                    } else {

                                        mediaWidth = Integer.parseInt(mediaSizeArr[0]);
                                        mediaHeight = Integer.parseInt(mediaSizeArr[1]);
                                    }

                                    if (!items.getJSONObject(i).has("bestCommentItems")) {


                                        HomeFeedContents contents = new HomeFeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl,
                                                likeCount, name, status, profileImageUrl, clan, gender, commentCount, 0, null, null, null, "text",
                                                "text", "text", null, null, null, iLikeThis, videoScreenshotUrl, mediaSize, mediaWidth, mediaHeight);
                                        contentsArrayList.add(contents);


                                    } else {

                                        JSONArray commentJSONArray = items.getJSONObject(i).getJSONArray("bestCommentItems");
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

                                        HomeFeedContents contents = new HomeFeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                commentCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2],
                                                iLikeThis, videoScreenshotUrl, mediaSize, mediaWidth, mediaHeight);

                                        contentsArrayList.add(contents);

                                    }

                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;

                                }

                                //리스트뷰를 새로 고침
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

                            }

                            else if(responseCode == 401) {

                                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "loginType").equals("sns")) {

                                    String accountId = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "accountId");
                                    String email = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "email");

                                    snsLoginRequest(email, accountId);

                                } else if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "loginType").equals("email")) {

                                    String password = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "password");
                                    String email = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "email");

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
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public void newStoryRequest(String groupId, final int pageNo) {

        lockListView = true;

        Locale mLocale = getResources().getConfiguration().locale;
        String deviceLanguage = mLocale.getLanguage();
        String language = null;

        if(deviceLanguage.contains("ko")) {
            language = "ko";
        } else if(deviceLanguage.contains("en")) {
            language = "en";
        } else if(deviceLanguage.contains("es")) {
            language = "es";
        } else if(deviceLanguage.contains("ja")) {
            language = "ja";
        } else if(deviceLanguage.contains("pt")) {
            language = "pt";
        } else {
            language = "en";
        }

        String endPoint = "/home/pages/newstory/" + groupId + "?language=" + language;
        String pageParameter = "&pageNo=" + pageNo;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + pageParameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                adapter.notifyDataSetInvalidated();

                                JSONObject newStory = response.getJSONObject("newstory");

                                JSONObject pages = newStory.getJSONObject("pages");
                                maxPage = pages.getInt("max");
                                JSONArray items = newStory.getJSONArray("items");

                                for(int i=0; i<items.length(); i++) {

                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String descriptions = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");

                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date = null;

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        String localTime = convertUtcToLocal(registryDate);
                                        date = format.parse(localTime);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    registryDate = formatTimeString(date);


                                    String contentUrl = items.getJSONObject(i).getString("contentUrl");
                                    int likeCount = items.getJSONObject(i).getInt("likeCount");
                                    String name = items.getJSONObject(i).getString("writerName");
                                    String status = items.getJSONObject(i).getString("status");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String clan = items.getJSONObject(i).getString("clan");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    int commentCount = items.getJSONObject(i).getInt("commentCount");
                                    String videoScreenshotUrl  = items.getJSONObject(i).getString("videoScreenshotUrl");
                                    String iLikeThis;

                                    //로그인을 한 상태에서만 나의 감정 표현 유무가 표시된다
                                    if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login"))
                                        iLikeThis = items.getJSONObject(i).getString("ILikedThis");
                                    else{
                                        iLikeThis = "0";
                                    }

                                    //미디어 사이즈를 확인하고 저장한다
                                    String mediaSize = items.getJSONObject(i).getString("mediaSize");

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

                                    if (!items.getJSONObject(i).has("bestCommentItems")) {


                                        HomeFeedContents contents = new HomeFeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl,
                                                likeCount, name, status, profileImageUrl, clan, gender, commentCount, 0, null, null, null, "text",
                                                "text", "text", null, null, null, iLikeThis, videoScreenshotUrl, mediaSize, mediaWidth, mediaHeight);
                                        contentsArrayList.add(contents);


                                    } else {

                                        JSONArray commentJSONArray = items.getJSONObject(i).getJSONArray("bestCommentItems");
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

                                        HomeFeedContents contents = new HomeFeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                commentCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2],
                                                iLikeThis, videoScreenshotUrl, mediaSize, mediaWidth, mediaHeight);

                                        contentsArrayList.add(contents);
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

                                            }
                                        }, 200);

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        listViewLoadIndicator.setVisibility(View.GONE);
                                        refreshLock = false;
                                    }
                                });

                                mSwipeRefresh.setRefreshing(false);
                            } else if(responseCode == 401) {

                                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "loginType").equals("sns")) {

                                    String accountId = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "accountId");
                                    String email = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "email");

                                    snsLoginRequest(email, accountId);

                                } else if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "loginType").equals("email")) {

                                    String password = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "password");
                                    String email = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "email");

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
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                if(GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "sid"));

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
                            GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "email", email);
                            GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "accountId", id);
                            GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "loginType", "sns");
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(HomeFeedActivity.this, error.toString(), Toast.LENGTH_LONG).show();
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

                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "sid", sid);

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
                            Toast.makeText(HomeFeedActivity.this, Integer.toString(responseCode), Toast.LENGTH_LONG);

                            if (responseCode == 200) {

                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "password", password);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "loginType", "email");
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
                    GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "sid", sid);

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
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(HomeFeedActivity.this, "login", "login");

                                adapter.notifyDataSetInvalidated();
                                contentsArrayList.clear();

                                currentPage = 0;
                                adapterFlag = 0;

                                if (newStory.equals("Y")) {
                                    newStoryRequest(groupId, currentPage);
                                } else {
                                    feedRequest(groupId, currentPage);
                                }

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
                        //Toast.makeText(HomeFeedActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String sid = GlobalSharedPreference.getAppPreferences(HomeFeedActivity.this, "sid");
                params.put("sessionId", sid);

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

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

    public void updateList(ArrayList<HomeFeedContents> newList) {

        adapter.notifyDataSetInvalidated();
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName;
        if(GlobalSharedPreference.getAppPreferences(this, "login").equals("login")) {
            pageName = "RecoFeed";
        } else {
            pageName = "PreRecoFeed";
        }

        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
