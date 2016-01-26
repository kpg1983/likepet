package com.likelab.likepet.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.R;
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
 * Created by kpg1983 on 2015-11-06.
 */
public class AlarmActivity extends Activity {

    ArrayList<AlarmContents> contentsArrayList;
    ListView contentsList;

    AlarmContentsAdapter adapter;
    private RelativeLayout cancelContainer;
    RequestQueue queue = AppController.getInstance().getRequestQueue();

    int currentPage = 0;
    int maxPage;
    int adapterFlag = 0;
    int itemCount = 0;

    private TextView txtNoAlarmItems;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    static String momentsAgo;
    static String minutesAgo;
    static String hoursAgo;
    static String daysAgo;
    static String monthAgo;
    static String yearsAgo;

    private boolean lockListView;   //리스트뷰가 갱신되는 동안 재요청을 방지 하기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_list);

        lockListView = false;

        momentsAgo = getResources().getString(R.string.now);
        minutesAgo = getResources().getString(R.string.minute_ago);
        hoursAgo = getResources().getString(R.string.hour_ago);
        daysAgo = getResources().getString(R.string.day_ago);
        monthAgo = getResources().getString(R.string.month_ago);
        yearsAgo = getResources().getString(R.string.year_ago);

        cancelContainer = (RelativeLayout)findViewById(R.id.alarm_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contentsArrayList = new ArrayList<AlarmContents>();

        txtNoAlarmItems = (TextView)findViewById(R.id.alarm_txt_no_items);


        adapter = new AlarmContentsAdapter(this, R.layout.alarm_list_view, contentsArrayList);
        contentsList = (ListView)findViewById(R.id.alarm_list_view);

        contentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contentId = contentsArrayList.get(position).contentId;

                contentRequest(contentId, position);
            }
        });

        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView ==false) {

                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        Log.d("currentPage", Integer.toString(currentPage));

                        alarmRequest(currentPage);

                    }
                }
            }

        });

        alarmRequest(currentPage);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        //Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
        if (adapter != null)
            adapter.recycle();

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());

        System.gc();


    }


    public void alarmRequest(int pageNo) {

        lockListView = true;

        String endPoint = "/notify";

        String parameter = "?pageNo=" + pageNo;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject notice = response.getJSONObject("notice");
                                JSONObject pages = notice.getJSONObject("pages");
                                maxPage = pages.getInt("max");

                                JSONArray items = notice.getJSONArray("items");

                                System.out.println("Alarm Items: " + items.length());

                                for(int i=0; i<items.length(); i++) {
                                    itemCount++;    //알림 갯수 카운트

                                    String noticeId = items.getJSONObject(i).getString("noticeId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String commentId = items.getJSONObject(i).getString("commentId");
                                    String actUserId = items.getJSONObject(i).getString("actUserId");
                                    String actUserName = items.getJSONObject(i).getString("actUserName");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");
                                    String notifyType = items.getJSONObject(i).getString("notifyType");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    String clan = items.getJSONObject(i).getString("clan");

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


                                    AlarmContents contents = new AlarmContents(noticeId, userId,contentId, commentId, actUserId, actUserName,
                                            registryDate, notifyType, profileImageUrl, gender, clan);

                                    contentsArrayList.add(contents);

                                }

                                if(itemCount == 0) {
                                    txtNoAlarmItems.setVisibility(View.INVISIBLE);
                                } else {
                                    txtNoAlarmItems.setVisibility(View.VISIBLE);
                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        adapter.notifyDataSetChanged();

                                        if(itemCount == 0) {
                                            txtNoAlarmItems.setVisibility(View.VISIBLE);
                                        } else {
                                            txtNoAlarmItems.setVisibility(View.INVISIBLE);
                                        }

                                        lockListView = false;
                                    }
                                });

                            } else if (responseCode == 401) {


                            } else if (responseCode == 500) {


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

                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(AlarmActivity.this, "sid"));


                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public void contentRequest(String contentId, final int position) {

        String endPoint = "/contents/" + contentId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject item = response.getJSONObject("item");

                                String contentId = item.getString("contentId");
                                String userId = item.getString("userId");
                                String descriptions = item.getString("descriptions");
                                String contentType = item.getString("contentType");
                                String contentUrl = item.getString("contentUrl");
                                int likeCount = item.getInt("likeCount");
                                int commentCount = item.getInt("commentCount");
                                int reportCount = item.getInt("reportCount");
                                String name = item.getString("writerName");
                                String status = item.getString("status");
                                String profileImageUrl = item.getString("profileImageUrl");
                                String iLikeThis = item.getString("ILikedThis");



                                int blackFlag;

                                if(reportCount > 10) {
                                    blackFlag = 1;
                                } else {
                                    blackFlag = 0;
                                }
                                Intent intent = new Intent(AlarmActivity.this, ViewActivity.class);

                                int intContentType;
                                if (contentType.matches(".*image.*")) {
                                    intContentType = 1;
                                } else {
                                    intContentType = 2;
                                }

                                intent.putExtra("CONTENT_ID", contentId);
                                intent.putExtra("DESC", descriptions);
                                intent.putExtra("TYPE", intContentType);
                                intent.putExtra("LIKES", likeCount);
                                intent.putExtra("BLIND_FLAG", blackFlag);
                                intent.putExtra("NUMBER_OF_COMMENT", commentCount);
                                intent.putExtra("IMAGE_URL", contentUrl);
                                intent.putExtra("ILIKETHIS", iLikeThis);
                                intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl);
                                intent.putExtra("NAME", name);
                                intent.putExtra("POSITION", position);
                                intent.putExtra("LIKE_COUNT", likeCount);
                                intent.putExtra("COMMENT_COUNT", commentCount);
                                intent.putExtra("USER_ID", userId);
                                intent.putExtra("REPORT_COUNT", reportCount);
                                intent.putExtra("STATUS", status);

                                startActivity(intent);


                            } else if (responseCode == 401) {


                            } else if (responseCode == 500) {


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

                params.put("sessionId", GlobalSharedPreference.getAppPreferences(AlarmActivity.this, "sid"));


                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onResume() {
        super.onResume();

        String pageName = "Notification";

        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
}
