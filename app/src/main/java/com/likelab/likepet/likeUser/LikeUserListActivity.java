package com.likelab.likepet.likeUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
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
public class LikeUserListActivity extends Activity {

    ArrayList<LikeUserListContents> contentsArrayList;
    ListView contentsList;

    private RelativeLayout cancelContainer;
    private TextView numberOfLike;

    private ImageView imgLikeType_1;
    private ImageView imgLikeType_2;
    private ImageView imgLikeType_3;
    private ImageView imgLikeType_4;

    private TextView txtNumberOfLike_1;
    private TextView txtNumberOfLike_2;
    private TextView txtNumberOfLike_3;
    private TextView txtNumberOfLike_4;

    LikeUserListContentsAdapter adapter;

    RequestQueue queue =AppController.getInstance().getRequestQueue();

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    int currentPage=0;
    int maxPage;
    int adapterFlag = 0;

    private boolean lockListView;   //리스트뷰가 갱신되는 동안 재요청을 방지 하기 위한 변수

    //데이터 불러오기 로드 화면
    View footer;
    RelativeLayout listViewLoaderContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.like_user_list);

        lockListView = false;

        final Intent intent = getIntent();
        contentsArrayList = new ArrayList<LikeUserListContents>();


        cancelContainer = (RelativeLayout)findViewById(R.id.like_user_cancel_container);
        numberOfLike = (TextView)findViewById(R.id.like_user_txt_number_of_like);
        imgLikeType_1 = (ImageView)findViewById(R.id.like_user_img_like_1);
        imgLikeType_2 = (ImageView)findViewById(R.id.like_user_img_like_2);
        imgLikeType_3 = (ImageView)findViewById(R.id.like_user_img_like_3);
        imgLikeType_4 = (ImageView)findViewById(R.id.like_user_img_like_4);

        txtNumberOfLike_1 = (TextView)findViewById(R.id.like_user_txt_like_1);
        txtNumberOfLike_2 = (TextView)findViewById(R.id.like_user_txt_like_2);
        txtNumberOfLike_3 = (TextView)findViewById(R.id.like_user_txt_like_3);
        txtNumberOfLike_4 = (TextView)findViewById(R.id.like_user_txt_like_4);

        imgLikeType_1.setImageResource(R.drawable.view_img_like_small_01);
        imgLikeType_2.setImageResource(R.drawable.view_img_like_small_02);
        imgLikeType_3.setImageResource(R.drawable.view_img_like_small_03);
        imgLikeType_4.setImageResource(R.drawable.view_img_like_small_04);

        int likeType_1 = 0;
        int likeType_2 = 0;
        int likeType_3 = 0;
        int likeType_4 = 0;

        final String contentId = intent.getStringExtra("CONTENT_ID");

        contentEmotionRequest(contentId, currentPage);  //좋아요 사용자 리퀘스트

        txtNumberOfLike_1.setText(Integer.toString(likeType_1));
        txtNumberOfLike_2.setText(Integer.toString(likeType_2));
        txtNumberOfLike_3.setText(Integer.toString(likeType_3));
        txtNumberOfLike_4.setText(Integer.toString(likeType_4));

        adapter = new LikeUserListContentsAdapter(this, R.layout.like_user_listview, contentsArrayList);
        contentsList = (ListView)findViewById(R.id.like_user_list_view);

        //리스트뷰 데이터 로딩화면
        footer = getLayoutInflater().inflate(R.layout.listview_load_footer, null, false);
        listViewLoaderContainer = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);
        contentsList.addFooterView(footer);

        contentsList.setAdapter(adapter);

        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView == false) {
                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        listViewLoaderContainer.setVisibility(View.VISIBLE);
                        contentEmotionRequest(contentId, currentPage);

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


    public void contentEmotionRequest(final String contentId, int pageNo) {

        lockListView = true;

        String parameter = "?pageNo=" + pageNo;

        String endPoint = "/contents/" + contentId + "/feel/users";

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject feels = response.getJSONObject("feels");
                                JSONObject pages = feels.getJSONObject("pages");

                                maxPage = pages.getInt("max");

                                JSONObject total = feels.getJSONObject("total");

                                int likeTypeCount_0 = total.getInt("likeType0count");
                                int likeTypeCount_1 = total.getInt("likeType1count");
                                int likeTypeCount_2 = total.getInt("likeType2count");
                                int likeTypeCount_3 = total.getInt("likeType3count");

                                int totalCount = likeTypeCount_0 + likeTypeCount_1 + likeTypeCount_2 + likeTypeCount_3;
                                numberOfLike.setText(Integer.toString(totalCount) + " " + getResources().getString(R.string.like_user_people));

                                txtNumberOfLike_1.setText(Integer.toString(likeTypeCount_0));
                                txtNumberOfLike_2.setText(Integer.toString(likeTypeCount_1));
                                txtNumberOfLike_3.setText(Integer.toString(likeTypeCount_2));
                                txtNumberOfLike_4.setText(Integer.toString(likeTypeCount_3));

                                JSONArray users = feels.getJSONArray("items");

                                for (int i = 0; i < users.length(); i++) {
                                    String userId = users.getJSONObject(i).getString("userId");
                                    String name = users.getJSONObject(i).getString("name");
                                    String gender = users.getJSONObject(i).getString("sex");
                                    String clan = users.getJSONObject(i).getString("clan");
                                    String profileImageUrl = users.getJSONObject(i).getString("profileImageUrl");
                                    int likeType = users.getJSONObject(i).getInt("likeType");
                                    String myFriend;

                                    if(GlobalSharedPreference.getAppPreferences(LikeUserListActivity.this, "login").equals("login")) {
                                        myFriend = users.getJSONObject(i).getString("myFriend");
                                    } else {
                                        myFriend = "0";
                                    }

                                    LikeUserListContents contents = new LikeUserListContents(userId, name, gender, clan, profileImageUrl, likeType, myFriend);
                                    contentsArrayList.add(contents);

                                }

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

                        } catch (JSONException e) {
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
                if(GlobalSharedPreference.getAppPreferences(LikeUserListActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(LikeUserListActivity.this, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }



    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "symUserList";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
