package com.likelab.likepet.Feed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.CommentBtnClickListener;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Feed extends Fragment implements CommentBtnClickListener {


    private static final int RESULT_CODE = 1;

    ArrayList<FeedContents> contentsArrayList;
    ListView contentsList;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    FeedContentsAdapter adapter;

    int currentPage=0;
    int maxPage;
    int adapterFlag = 0;

    private static final int RESULT_MODIFY_CONTENT_SUMMARY = 5;

    private SwipeRefreshLayout mSwipeRefresh;

    private boolean lockListView;   //리스트뷰가 갱신되는 동안 재요청을 방지 하기 위한 변수

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    static String momentsAgo;
    static String minutesAgo;
    static String hoursAgo;
    static String daysAgo;
    static String monthAgo;
    static String yearsAgo;

    RelativeLayout layout;
    View footer;
    RelativeLayout listViewLoadIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = (RelativeLayout)inflater.inflate(R.layout.feed, container, false);

        //변수 선언 및 초기화
        initView(inflater);

        adapter.setCommentBtnClickListener(this);


        //로그인 로그아웃 상태에 따른 리퀘스트 요청을 달리한다.
        if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
            feedRequest(currentPage);
        } else {
            operatorFeedRequest(currentPage);
        }

        //페이지 새로 고침
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                listViewLoadIndicator.setVisibility(View.GONE);

                //페이지를 새로 고침하면 첫변재 페이지부터 다시 리퀘스트 요청을 날린다
                currentPage = 0;
                adapterFlag = 0;

                adapter.notifyDataSetInvalidated();
                contentsArrayList.clear();

                //로그인 상태와 로그 아웃 상태에 따라 다른 페이지를 표시한다.
                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    feedRequest(currentPage);
                } else {
                    operatorFeedRequest(currentPage);
                }

            }
        });


        //페이지 더 불러오기
        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            //스크롤이 리스트뷰 가장 하단으로 내려갔을 경우 다음 페이지 리퀘스트를 요청한다
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if(firstVisibleItem >= count && totalItemCount != 0 && lockListView == false)
                {
                    lockListView = true;

                    currentPage = currentPage + 1;

                    if(currentPage < maxPage) {

                        if (adapterFlag == 1) {
                            listViewLoadIndicator.setVisibility(View.VISIBLE);
                            //Toast.makeText(getActivity(),  "데이터갱신", Toast.LENGTH_SHORT).show();
                        }

                        //로그인, 로그아웃 상태에 따른 다른 리퀘스트 요청
                        if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                            feedRequest(currentPage);
                        } else {
                            operatorFeedRequest(currentPage);
                        }

                    } else {

                    }
                }
            }

        });


        //리스트 항목 선택 시 뷰페이지로 이동
        contentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //뷰페이지로 이동
                startViewActivity(position, "no");

            }
        });


        return layout;
    }

    private void initView(LayoutInflater inflater) {

        momentsAgo = getResources().getString(R.string.now);
        minutesAgo = getResources().getString(R.string.minute_ago);
        hoursAgo = getResources().getString(R.string.hour_ago);
        daysAgo = getResources().getString(R.string.day_ago);
        monthAgo = getResources().getString(R.string.month_ago);
        yearsAgo = getResources().getString(R.string.year_ago);

        lockListView = false;
        contentsList = (ListView)layout.findViewById(R.id.feed_contents_list);
        footer = inflater.inflate(R.layout.listview_load_footer, null, false);

        contentsList.addFooterView(footer);
        contentsArrayList = new ArrayList<FeedContents>();
        adapter = new FeedContentsAdapter(getActivity(), R.layout.feed_listview, contentsArrayList, (MainActivity)getActivity());
        listViewLoadIndicator = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);
        mSwipeRefresh = (SwipeRefreshLayout)layout.findViewById(R.id.swype_layout);
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

                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(getContext(), "sid"));

                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }


    //운영자 피드
    //사용자가 로그인을 하지 않은 상태에서 노출된다.
    public void operatorFeedRequest(int pageNo) {

        lockListView = true;

        String endPoint = "/community";

        System.out.println("operatorRequest");

        //사용자의 언어정보를 확인해서 해당되는 언어의 컨텐츠를 보여준다
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
        } else {
            language = "en";
        }


        String parameter = "?pageNo=" + pageNo + "&language=" + language;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject feeds = response.getJSONObject("community");

                                JSONObject pages = feeds.getJSONObject("pages");
                                maxPage = pages.getInt("max");
                                JSONArray items = feeds.getJSONArray("items");

                                for (int i = 0; i < items.length(); i++) {

                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String descriptions = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");
                                    String contentUrl = items.getJSONObject(i).getString("contentUrl");
                                    int likeCount = items.getJSONObject(i).getInt("likeCount");
                                    int commentCount = items.getJSONObject(i).getInt("commentCount");
                                    int readCount = items.getJSONObject(i).getInt("readCount");
                                    int reportCount = items.getJSONObject(i).getInt("reportCount");
                                    String name = items.getJSONObject(i).getString("writerName");
                                    String status = items.getJSONObject(i).getString("status");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String clan = items.getJSONObject(i).getString("clan");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    String iLikeThis;
                                    String videoScreenshotUrl = items.getJSONObject(i).getString("videoScreenshotUrl");

                                    if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                                        iLikeThis = items.getJSONObject(i).getString("ILikedThis");
                                    } else {
                                        iLikeThis = "0";
                                    }

                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

                                    java.util.Date date = null;

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        date = format.parse(registryDate);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    registryDate = formatTimeString(date);

                                    //베스트 댓글이 없는 경우
                                    if (!items.getJSONObject(i).has("bestCommentItems")) {

                                        FeedContents contents = new FeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                readCount, reportCount, commentCount, 0, null, null, null, null, null, null, null, null, null, iLikeThis, videoScreenshotUrl, mediaWidth, mediaHeight);
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


                                        FeedContents contents = new FeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                readCount, reportCount, commentCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2], iLikeThis, videoScreenshotUrl, mediaWidth, mediaHeight);

                                        contentsArrayList.add(contents);

                                    }

                                }

                                //contentsList.setAdapter(adapter);

                                //setAdapter은 1번만 실행하기 위함
                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }


                                if(getActivity() == null) {
                                    return;
                                }

                                //리스트뷰를 새로 고침
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        }, 2000);

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        listViewLoadIndicator.setVisibility(View.GONE);
                                    }
                                });

                                //새로고침 아이콘을 없애준다
                                mSwipeRefresh.setRefreshing(false);


                            }
                        } catch (Exception e) {
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
                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(getContext(), "sid"));

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public void feedRequest(int pageNo) {

        String endPoint = "/feeds";

        System.out.println("feedRequest");

        String parameter = "?pageNo=" + pageNo;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject feeds = response.getJSONObject("feeds");
                                JSONObject pages = feeds.getJSONObject("pages");

                                maxPage = pages.getInt("max");
                                JSONArray items = feeds.getJSONArray("items");

                                for (int i = 0; i < items.length(); i++) {

                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String descriptions = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");
                                    String contentUrl = items.getJSONObject(i).getString("contentUrl");
                                    int likeCount = items.getJSONObject(i).getInt("likeCount");
                                    int commentCount = items.getJSONObject(i).getInt("commentCount");
                                    int readCount = items.getJSONObject(i).getInt("readCount");
                                    int reportCount = items.getJSONObject(i).getInt("reportCount");
                                    String name = items.getJSONObject(i).getString("writerName");
                                    String status = items.getJSONObject(i).getString("status");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String clan = items.getJSONObject(i).getString("clan");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    String iLikeThis = items.getJSONObject(i).getString("ILikedThis");
                                    String videoScreenshotUrl = items.getJSONObject(i).getString("videoScreenshotUrl");

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

                                    //베스트 댓글이 없는 경우
                                    if (!items.getJSONObject(i).has("bestCommentItems")) {

                                        FeedContents contents = new FeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                 readCount, reportCount, commentCount, 0, null, null, null, null, null, null, null, null, null, iLikeThis, videoScreenshotUrl, mediaWidth, mediaHeight);
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


                                        FeedContents contents = new FeedContents(contentId, userId, descriptions, contentType, registryDate, contentUrl, likeCount, name, status, profileImageUrl, clan, gender,
                                                 readCount, reportCount, commentCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2], iLikeThis, videoScreenshotUrl, mediaWidth, mediaHeight);

                                        contentsArrayList.add(contents);

                                    }

                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;

                                }


                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        //listViewLoadIndicator.setVisibility(View.GONE);
                                    }
                                });

                                mSwipeRefresh.setRefreshing(false);

                            }
                        } catch (Exception e) {
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
                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(getContext(), "sid"));

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

    public void updateList(ArrayList<FeedContents> newList) {

        adapter.notifyDataSetInvalidated();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();

        String pageName = "Feed";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    //comment 버튼을 클릭했을 경우
    //view 페이지로 이동하고 키보드를 자동으로 나태낸다
    @Override
    public void onCommentBtnClicked(int position) {
        startViewActivity(position, "ok");
    }


    private void startViewActivity(int position, String commentPressed) {

        Intent intent = new Intent(getActivity(), ViewActivity.class);
        intent.putExtra("IS_COMMENT_PRESSED", commentPressed);

        int contentType;
        int imageContent;
        int numberOfLike;
        int blackFlag;      //해당 컨텐츠가 신고로 인하여 블락처리 되어있는지 확인한다.
        int numberOfComment;

        imageContent = contentsArrayList.get(position).mainContent;
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
        String status = contentsArrayList.get(position).status;
        int reportCount = contentsArrayList.get(position).reportCount;


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
        intent.putExtra("IMAGE", imageContent);
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

}

