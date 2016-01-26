package com.likelab.likepet.HomeFeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        String pageId = intent.getExtras().getString("PAGE_ID");
        int feedCount = intent.getExtras().getInt("FEED_COUNT");
        String tags = intent.getExtras().getString("TAGS");

        System.out.println("tags" + tags);

        Log.d("DESC: ", tags);
        String[] tagArray = tags.split("#");

        contentsArrayList = new ArrayList<HomeFeedContents>();


        //태그가 있는 경우, 없는 경우
        tag_1 = (TextView)findViewById(R.id.home_contents_txt_tag_1);
        tag_2 = (TextView)findViewById(R.id.home_contents_txt_tag_2);

        if(tagArray.length == 2) {
            Log.d("DESC: ", tagArray[0]);
            tag_1.setText("#"+tagArray[1]);
        } else if(tagArray.length == 3) {
            tag_1.setText("#"+tagArray[1]);
            tag_2.setText("#"+tagArray[2]);
        } else {
            tag_1.setText(tags);
        }

        //그룹 설명이 아예 없는 경우
        if(tags.equals(" ") || tags.length() == 0) {
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

        feedRequest(groupId, currentPage);

        adapter = new HomeFeedContentsAdapter(this, R.layout.home_contents_list_view, contentsArrayList, HomeFeedActivity.this);

        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swype_layout);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                currentPage = 0;
                adapterFlag  = 0;

                adapter.notifyDataSetInvalidated();
                contentsArrayList.clear();
                feedRequest(groupId, currentPage);


            }
        });

        contentsList = (ListView)findViewById(R.id.home_contents_list);


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

                Log.d("Home mediaSize", mediaSize);

                Log.d("iLikeThis", iLikeThis + ": " + Integer.toString(position));

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

                startActivityForResult(intent, RESULT_CODE);

            }
        });

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
            contentsInfoRequest(position, contentsArrayList.get(position).contentId);
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
                                String iLikeThis = item.getString("ILikedThis");
                                String descriptions = item.getString("descriptions");

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

                                JSONArray items = response.getJSONArray("items");

                                for(int i=0; i<items.length(); i++) {

                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String descriptions = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");

                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    java.util.Date date = null;

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        date = format.parse(registryDate);
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

                                contentsList.setAdapter(adapter);
                                mSwipeRefresh.setRefreshing(false);

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

                return params;

            }

        };
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
}
