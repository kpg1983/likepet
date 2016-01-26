package com.likelab.likepet.Main;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

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
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-09-21.
 */
public class Home extends Fragment {

    ArrayList<HomeContents> contentsArrayList;
    ListView contentsList;

    private int mPageNumber;
    private Button btnMemoryPopup;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    HomeContentsAdapter adapter;

    int currentPage = 0;
    int maxPage;
    int adapterFlag = 0;

    private SwipeRefreshLayout mSwipeRefresh;
    private boolean lockListView;
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();     //구글 트래커

    View footer;
    RelativeLayout listViewLoadIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPageNumber = getArguments().getInt("PAGE");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.home_fragment_layout, container, false);
        btnMemoryPopup = (Button) layout.findViewById(R.id.home_popup_menory);

        contentsArrayList = new ArrayList<HomeContents>();
        adapter = new HomeContentsAdapter(getActivity(), R.layout.home_even_listview, contentsArrayList);
        footer = inflater.inflate(R.layout.listview_load_footer, null, false);
        contentsList = (ListView) layout.findViewById(R.id.home_contents_list);
        contentsList.addFooterView(footer);

        listViewLoadIndicator = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);

        lockListView = false;

        homeContentsRequest(currentPage);

        //페이지 새로 고침
        mSwipeRefresh = (SwipeRefreshLayout)layout.findViewById(R.id.swype_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPage = 0;

                adapter.notifyDataSetInvalidated();
                contentsArrayList.clear();
                homeContentsRequest(currentPage);
                adapterFlag = 0;   //setAdapter 위한 변수, 0일때 1번만 setAdapter 수행
            }
        });


        //메모리 부족 팝업
        //버튼을 invisible 시켜놓아 화면에 보이지는 않는다.
        btnMemoryPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow popupWindow = new PopupWindow(v);
                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView;

                popupView = inflater.inflate(R.layout.out_of_memory_popup_view, null);

                popupWindow.setContentView(popupView);
                popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

                RelativeLayout okContainer = (RelativeLayout) popupView.findViewById(R.id.out_of_memory_ok_container);

                okContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

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

                    lockListView = true;

                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        //페이지 새로고침시 페이지 로딩 표시를 보여주지 않는다.
                        if(adapterFlag == 1)
                            listViewLoadIndicator.setVisibility(View.VISIBLE);

                        homeContentsRequest(currentPage);

                    }
                }
            }
        });

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(adapter != null) {
            adapter.recycle();
        }

        System.gc();


    }
    public void homeContentsRequest(int currentPage) {

        lockListView = true;

        String endPoint = "/home";

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

        String urlParameter = "?language=" + language+"&pageNo="+currentPage;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + urlParameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");


                            if (responseCode == 200) {

                                JSONObject homePageObject = response.getJSONObject("homepage");
                                JSONArray groupArray = homePageObject.getJSONArray("group");
                                JSONObject pages = homePageObject.getJSONObject("pages");

                                maxPage = pages.getInt("max");

                                for (int i = 0; i < groupArray.length(); i++) {
                                    String pageId = groupArray.getJSONObject(i).getString("pageId");
                                    String language = groupArray.getJSONObject(i).getString("language");
                                    String status = groupArray.getJSONObject(i).getString("status");
                                    String registryDate = groupArray.getJSONObject(i).getString("registryDate");
                                    String displayStartDate = groupArray.getJSONObject(i).getString("displayStartDate");
                                    String displayEndDate = groupArray.getJSONObject(i).getString("displayEndDate");

                                    JSONArray itemsArray = groupArray.getJSONObject(i).getJSONArray("items");

                                    int itemArrayLength = itemsArray.length();

                                    String groupId = null;
                                    int feedCount = 0;
                                    int readCount = 0;
                                    String groupRegistryDate = null;
                                    String ownerName = null;

                                    String description = null;
                                    String thumbnailUrl = null;

                                    String groupIdRight = null;
                                    int feedCountRight = 0;
                                    int readCountRight = 0;
                                    String groupRegistryDateRight = null;
                                    String ownerNameRight = null;
                                    String descriptionRight = null;
                                    String thumbnailUrlRight = null;
                                    String thumbnailType = null;

                                    int count = 0;  //1:1 홈컨텐츠를 왼쪽, 오른쪽으로 카운트 해주기 위하여 사용
                                    for (int j = 0; j < itemArrayLength; j++) {
                                        thumbnailType = itemsArray.getJSONObject(j).getString("thumbnailType");

                                        //썸네일 타입이 1이면 16:9 크기 컨텐츠
                                        if (thumbnailType.equals("1")) {

                                            groupId = itemsArray.getJSONObject(j).getString("groupId");
                                            feedCount = itemsArray.getJSONObject(j).getInt("feedCount");
                                            readCount = itemsArray.getJSONObject(j).getInt("readCount");

                                            groupRegistryDate = itemsArray.getJSONObject(j).getString("registryDate");
                                            ownerName = itemsArray.getJSONObject(j).getString("ownerName");
                                            thumbnailUrl = itemsArray.getJSONObject(j).getString("thumbnailUrl");
                                            description = itemsArray.getJSONObject(j).getString("descriptions");

                                            HomeContents contents = new HomeContents(pageId, language, status, registryDate, displayStartDate,
                                                    displayEndDate, groupId, feedCount, readCount, thumbnailType, groupRegistryDate, ownerName, thumbnailUrl,
                                                    description);

                                            contentsArrayList.add(contents);


                                        } else {
                                            //1:1 컨텐츠의 좌측
                                            if (count == 0) {

                                                groupId = itemsArray.getJSONObject(j).getString("groupId");
                                                feedCount = itemsArray.getJSONObject(j).getInt("feedCount");
                                                readCount = itemsArray.getJSONObject(j).getInt("readCount");

                                                groupRegistryDate = itemsArray.getJSONObject(j).getString("registryDate");
                                                ownerName = itemsArray.getJSONObject(j).getString("ownerName");
                                                thumbnailUrl = itemsArray.getJSONObject(j).getString("thumbnailUrl");
                                                description = itemsArray.getJSONObject(j).getString("descriptions");

                                                count++;

                                            } else {

                                                //1:1 컨텐츠 우측
                                                groupIdRight = itemsArray.getJSONObject(j).getString("groupId");
                                                feedCountRight = itemsArray.getJSONObject(j).getInt("feedCount");
                                                readCountRight = itemsArray.getJSONObject(j).getInt("readCount");

                                                groupRegistryDateRight = itemsArray.getJSONObject(j).getString("registryDate");
                                                ownerNameRight = itemsArray.getJSONObject(j).getString("ownerName");
                                                thumbnailUrlRight = itemsArray.getJSONObject(j).getString("thumbnailUrl");
                                                descriptionRight = itemsArray.getJSONObject(j).getString("descriptions");

                                                //1:1 컨텐츠는 한쌍이며, 우축에서 끝나기 때문에 우측에 들어왔을때 좌측과 우측을 한번에 리스트에 추가한다
                                                HomeContents contents = new HomeContents(pageId, language, status, registryDate, displayStartDate,
                                                        displayEndDate, groupId, feedCount, readCount, thumbnailType, groupRegistryDate, ownerName, thumbnailUrl,
                                                        description, pageId, language, status, registryDate, displayStartDate,
                                                        displayEndDate, groupIdRight, feedCountRight, readCountRight, thumbnailType, groupRegistryDateRight, ownerNameRight, thumbnailUrlRight,
                                                        descriptionRight);

                                                count = 0;

                                                contentsArrayList.add(contents);

                                            }

                                        }

                                    }

                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                if(getActivity() == null) {
                                    return;
                                }

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        listViewLoadIndicator.setVisibility(View.GONE);
                                    }
                                });

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
                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(getContext(), "sid"));

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onResume() {
        super.onResume();

        String pageName = "Home";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }
}
