package com.likelab.likepet.bookmark;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.RelativeLayout;

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
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-12-03.
 */
public class BookmarkActivity extends Activity {

    ArrayList<BookmarkContents> contentsArrayList;

    GridView contentList;
    BookmarkContentsAdapter adapter;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    private RelativeLayout cancelContainer;
    private RelativeLayout imgBookmarkNoItemContainer;

    int bookmarkCount;
    int maxPage = 0;
    int currentPage = 0;
    int adapterFlag = 0;

    boolean lockListView = false;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.bookmark_activity);

        imgBookmarkNoItemContainer = (RelativeLayout)findViewById(R.id.bookmark_no_item_container);

        cancelContainer = (RelativeLayout)findViewById(R.id.bookmark_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        contentsArrayList = new ArrayList<BookmarkContents>();

        bookmarkRequest(currentPage);

        contentList = (GridView)findViewById(R.id.bookmark_grid_view);

        adapter = new BookmarkContentsAdapter(this, R.layout.bookmark_grid_view, contentsArrayList);

        //페이지 더 불러오기
        contentList.setOnScrollListener(new AbsListView.OnScrollListener() {
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

                        bookmarkRequest(currentPage);

                    }
                }
            }

        });

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



    public void bookmarkRequest(int pageNo) {

        String endPoint = "/mypage/favorite";

        String parameter = "?pageNo=" + pageNo;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject favorite = response.getJSONObject("favorite");
                                JSONArray items = favorite.getJSONArray("items");
                                JSONObject pages = favorite.getJSONObject("pages");
                                maxPage = pages.getInt("max");

                                for(int i=0; i<items.length(); i++) {
                                    bookmarkCount++;
                                    String contentId = items.getJSONObject(i).getString("contentId");
                                    String userId = items.getJSONObject(i).getString("userId");
                                    String description = items.getJSONObject(i).getString("descriptions");
                                    String contentType = items.getJSONObject(i).getString("contentType");
                                    String contentUrl = items.getJSONObject(i).getString("contentUrl");
                                    int likeCount = items.getJSONObject(i).getInt("likeCount");
                                    int commentCount = items.getJSONObject(i).getInt("commentCount");
                                    int readCount = items.getJSONObject(i).getInt("readCount");
                                    int reportCount = items.getJSONObject(i).getInt("reportCount");
                                    String writerName =  items.getJSONObject(i).getString("writerName");
                                    String status = items.getJSONObject(i).getString("status");
                                    String registryDate = items.getJSONObject(i).getString("registryDate");
                                    String recommendation = items.getJSONObject(i).getString("recommendation");
                                    String iLikeThis = items.getJSONObject(i).getString("ILikedThis");
                                    String gender = items.getJSONObject(i).getString("sex");
                                    String clan = items.getJSONObject(i).getString("clan");
                                    String profileImageUrl = items.getJSONObject(i).getString("profileImageUrl");
                                    String videoScreenshotUrl = items.getJSONObject(i).getString("videoScreenshotUrl");

                                    BookmarkContents contents = new BookmarkContents(contentId, userId, description, contentType, contentUrl, likeCount, commentCount, readCount, reportCount,
                                            writerName, status, registryDate, recommendation, iLikeThis, gender, clan, profileImageUrl, videoScreenshotUrl);

                                    contentsArrayList.add(contents);
                                }

                                if(bookmarkCount == 0) {
                                    imgBookmarkNoItemContainer.setVisibility(View.VISIBLE);
                                } else {
                                    imgBookmarkNoItemContainer.setVisibility(View.INVISIBLE);
                                }

                                if(adapterFlag == 0) {
                                    contentList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                    }
                                });
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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(BookmarkActivity.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Favorite";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
