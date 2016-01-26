package com.likelab.likepet.follow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class FollowerUserListActivity extends Activity {

    ArrayList<FollowingContents> contentsArrayList;
    ListView contentsList;

    private RelativeLayout cancelContainer;

    private TextView txtNumberOfLike_1;
    private TextView txtNumberOfLike_2;
    private TextView txtNumberOfLike_3;

    private TextView txtTitle;

    private RequestQueue queue;
    String follow;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    int currentPage=0;
    int maxPage;
    int adapterFlag = 0;

    private boolean lockListView;   //리스트뷰가 갱신되는 동안 재요청을 방지 하기 위한 변수
    FollowerContentsAdapter adapter;

    //데이터 불러오기 로드 화면
    View footer;
    RelativeLayout listViewLoaderContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following_user_list);

        lockListView = false;

        queue = AppController.getInstance().getRequestQueue();

        cancelContainer = (RelativeLayout)findViewById(R.id.following_user_cancel_container);

        txtNumberOfLike_1 = (TextView)findViewById(R.id.following_user_txt_like_1);
        txtNumberOfLike_2 = (TextView)findViewById(R.id.following_user_txt_like_2);
        txtNumberOfLike_3 = (TextView)findViewById(R.id.following_user_txt_like_3);
        txtTitle = (TextView)findViewById(R.id.following_user_txt_number_of_like);

        contentsArrayList = new ArrayList<FollowingContents>();

        adapter = new FollowerContentsAdapter(FollowerUserListActivity.this, R.layout.following_user_list_view, contentsArrayList);

        contentsList = (ListView)findViewById(R.id.following_user_list_view);

        footer = getLayoutInflater().inflate(R.layout.listview_load_footer, null, false);
        listViewLoaderContainer = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);
        contentsList.addFooterView(footer);

        final Intent intent = getIntent();

        follow = intent.getExtras().getString("FOLLOW");
        final String whosPage = intent.getExtras().getString("PAGE");

        if(whosPage.equals("MY_PAGE")) {
            loadFollowingRequest(currentPage);
        } else {
            String userId = intent.getExtras().getString("USER_ID");
            loadOtherFollowingRequest(userId, currentPage);

        }

        if(follow.equals("following"))
            txtTitle.setText("Following");
        else if(follow.equals("follower"))
            txtTitle.setText("Follower");


        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //리스트 맨 마지악으로 내려갈 경우 리스트를 추가로 불러 온다
        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView == false) {

                    lockListView = true;

                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        listViewLoaderContainer.setVisibility(View.VISIBLE);

                        if (whosPage.equals("MY_PAGE")) {
                            loadFollowingRequest(currentPage);
                        } else {
                            String userId = intent.getStringExtra("USER_ID");
                            loadOtherFollowingRequest(userId, currentPage);

                        }

                    } else {
                        listViewLoaderContainer.setVisibility(View.GONE);
                    }
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(adapter != null) {
            adapter.recycle();
        }

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();


    }
    public void loadFollowingRequest(int pageNo) {

        lockListView = true;

        String endPoint = "/mypage/follower";
        String parameter = "?pageNo=" + pageNo;

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                JSONObject following;

                                following = response.getJSONObject("follower");
                                JSONArray users = following.getJSONArray("items");

                                JSONObject pages = following.getJSONObject("pages");
                                JSONObject total = following.getJSONObject("total");


                                maxPage = pages.getInt("max");

                                int clanDog = total.getInt("clan0count");
                                int clanCat = total.getInt("clan1count");
                                int clanPerson = total.getInt("clan2count");

                                for(int i=0; i< users.length(); i++) {
                                    String userId;

                                    userId = users.getJSONObject(i).getString("followerUserId");
                                    String name = users.getJSONObject(i).getString("name");
                                    String profileImageUrl = users.getJSONObject(i).getString("profileImageUrl");
                                    String clan = users.getJSONObject(i).getString("clan");
                                    String gender = users.getJSONObject(i).getString("sex");
                                    String crossFollow = users.getJSONObject(i).getString("crossFollow");

                                    FollowingContents contents = new FollowingContents(userId, profileImageUrl, name, clan, gender, crossFollow);
                                    contentsArrayList.add(contents);

                                }

                                txtNumberOfLike_1.setText(Integer.toString(clanPerson));
                                txtNumberOfLike_2.setText(Integer.toString(clanDog));
                                txtNumberOfLike_3.setText(Integer.toString(clanCat));


                                //setAdapter은 1번만 실행하기 위함
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
                                        }, 2000);

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        listViewLoaderContainer.setVisibility(View.GONE);
                                    }
                                });
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
                if(GlobalSharedPreference.getAppPreferences(FollowerUserListActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(FollowerUserListActivity.this, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void loadOtherFollowingRequest(String userId, int pageNo) {



        lockListView = true;

        String endPoint = "/otherpage/"+ userId + "/follower";
        String parameter = "?pageNo=" + pageNo;

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                JSONObject following;

                                following = response.getJSONObject("follower");
                                JSONArray users = following.getJSONArray("items");

                                JSONObject pages = following.getJSONObject("pages");
                                JSONObject total = following.getJSONObject("total");


                                maxPage = pages.getInt("max");

                                int clanDog = total.getInt("clan0count");
                                int clanCat = total.getInt("clan1count");
                                int clanPerson = total.getInt("clan2count");

                                for(int i=0; i< users.length(); i++) {
                                    String userId;

                                    userId = users.getJSONObject(i).getString("followerUserId");
                                    String name = users.getJSONObject(i).getString("name");
                                    String profileImageUrl = users.getJSONObject(i).getString("profileImageUrl");
                                    String clan = users.getJSONObject(i).getString("clan");
                                    String gender = users.getJSONObject(i).getString("sex");
                                    String crossFollow;
                                    if(GlobalSharedPreference.getAppPreferences(FollowerUserListActivity.this, "login").equals("login"))
                                       crossFollow = users.getJSONObject(i).getString("crossFollow");
                                    else
                                        crossFollow = "1";

                                    FollowingContents contents = new FollowingContents(userId, profileImageUrl, name, clan, gender, crossFollow);
                                    contentsArrayList.add(contents);

                                }

                                listViewLoaderContainer.setVisibility(View.GONE);

                                txtNumberOfLike_1.setText(Integer.toString(clanPerson));
                                txtNumberOfLike_2.setText(Integer.toString(clanDog));
                                txtNumberOfLike_3.setText(Integer.toString(clanCat));

                                //FollowerContentsAdapter adapter = new FollowerContentsAdapter(FollowerUserListActivity.this, R.layout.following_user_list_view, contentsArrayList);

                                //contentsList = (ListView)findViewById(R.id.following_user_list_view);
                                //contentsList = (ListView)findViewById(R.id.following_user_list_view);
                                //setAdapter은 1번만 실행하기 위함
                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("runOn", "1");
                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                    }
                                });

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
                if(GlobalSharedPreference.getAppPreferences(FollowerUserListActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(FollowerUserListActivity.this, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Follower";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
