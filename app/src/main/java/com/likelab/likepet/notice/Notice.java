package com.likelab.likepet.notice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kpg1983 on 2015-10-29.
 */
public class Notice extends Activity {

    ArrayList<NoticeContents> contentsArrayList;
    ListView contentsListView;
    NoticeContentsAdapter noticeContentsAdapter;

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    RequestQueue queue = AppController.getInstance().getRequestQueue();
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.notice_activity);

        cancelContainer = (RelativeLayout)findViewById(R.id.notice_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.notice_btn_cancel);

        contentsArrayList = new ArrayList<NoticeContents>();

        noticeRequest();

        noticeContentsAdapter = new NoticeContentsAdapter(this, R.layout.notice_listview, contentsArrayList);
        contentsListView = (ListView)findViewById(R.id.notice_list_view);


        contentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(Notice.this, Integer.toString(position), Toast.LENGTH_SHORT).show();
            }
        });

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

    }


    public void noticeRequest() {

        String endPoint = "/notice?filter=1";

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

        String parameter = "&language="+ language;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject notice = response.getJSONObject("notice");
                                JSONArray items = notice.getJSONArray("items");

                                for(int i=0; i<items.length(); i++) {

                                    String noticeId = items.getJSONObject(i).getString("noticeId");
                                    String title = items.getJSONObject(i).getString("title");
                                    String description = items.getJSONObject(i).getString("descriptions");
                                    int readCount = items.getJSONObject(i).getInt("readCount");
                                    String noticeType = items.getJSONObject(i).getString("noticeType");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");


                                    registryDate = registryDate.replaceAll("\\.", "-");

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    String localTime = convertUtcToLocal(registryDate);
                                    //localTime = localTime.replaceFirst("-", ".");
                                    registryDate = localTime.substring(0, localTime.indexOf(" "));
                                    registryDate = registryDate.replaceAll("-", "/");

                                    String userId = items.getJSONObject(i).getString("userId");
                                    String writerName = items.getJSONObject(i).getString("writerName");
                                    String modifyDate = items.getJSONObject(i).getString("modifyDate");
                                    String language = items.getJSONObject(i).getString("language");

                                    description = description.replaceAll("<br>", "\r\n");

                                    NoticeContents noticeContents = new NoticeContents(noticeId, title, description, readCount, noticeType, registryDate, userId, writerName, modifyDate, language);
                                    contentsArrayList.add(noticeContents);

                                }

                                contentsListView.setAdapter(noticeContentsAdapter);
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
                        System.out.println(error.toString());
                    }


                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(Notice.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Notice";
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
