package com.likelab.likepet.more;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-10-28.
 */
public class Setting extends Activity {

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private RelativeLayout appVersionContainer;

    ImageView imgNewVersion;
    TextView txtNewVersion;

    String oldVersion;
    String newVersion;
    String updateImageUrl;

    Switch swAllAlarm;
    Switch swFriend;
    Switch swComment;
    Switch swLikePet;

    String useNotice;
    String useNoticeAddedFriend;
    String useNoticeReply;
    String useNoticeSystem;

    RequestQueue queue = AppController.getInstance().getRequestQueue();
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_activity);

        useNotice = GlobalSharedPreference.getAppPreferences(this, "useNotice");
        useNoticeSystem = GlobalSharedPreference.getAppPreferences(this, "useNoticeSystem");
        useNoticeReply = GlobalSharedPreference.getAppPreferences(this, "useNoticeReply");
        useNoticeAddedFriend = GlobalSharedPreference.getAppPreferences(this, "useNoticeAddedFriend");

        cancelContainer = (RelativeLayout)findViewById(R.id.setting_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.setting_btn_cancel);

        imgNewVersion = (ImageView)findViewById(R.id.setting_img_new_version);
        txtNewVersion = (TextView)findViewById(R.id.setting_txt_new_version);

        swAllAlarm = (Switch)findViewById(R.id.setting_sw_alarm);
        swFriend = (Switch)findViewById(R.id.setting_sw_friend_alarm);
        swComment = (Switch)findViewById(R.id.setting_sw_comment_alarm);
        swLikePet = (Switch)findViewById(R.id.setting_sw_like_pet_alarm);

        PackageInfo pi = null;
        try {

            pi = getPackageManager().getPackageInfo(getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {


            e.printStackTrace();

        }

        String version = pi.versionName;
        txtNewVersion.setText(version);

        checkVersionRequest();


        //알람 설정이 꺼져 있으면 나머지도 전부 끄고 기능도  OFF 시킨다
       if(useNotice.equals("1")) {
            swAllAlarm.setChecked(true);
        } else {
            swAllAlarm.setChecked(false);

            swFriend.setClickable(false);
            swComment.setClickable(false);
            swLikePet.setClickable(false);
        }

        if(useNoticeAddedFriend.equals("1")) {
            swFriend.setChecked(true);
        } else {
            swFriend.setChecked(false);
        }

        if(useNoticeReply.equals("1")) {
            swComment.setChecked(true);
        } else {
            swComment.setChecked(false);
        }

        if(useNoticeSystem.equals("1")) {
            swLikePet.setChecked(true);
        } else {
            swLikePet.setChecked(false);
        }

        swAllAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked == false) {
                    useNotice = "0";

                    swFriend.setChecked(false);
                    swLikePet.setChecked(false);
                    swComment.setChecked(false);

                    swFriend.setClickable(false);
                    swComment.setClickable(false);
                    swLikePet.setClickable(false);

                } else {

                    useNotice = "1";

                    swFriend.setChecked(true);
                    swLikePet.setChecked(true);
                    swComment.setChecked(true);

                    swFriend.setClickable(true);
                    swComment.setClickable(true);
                    swLikePet.setClickable(true);
                }
            }
        });

        swLikePet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {

                    useNoticeSystem = "1";

                } else {
                    useNoticeSystem = "0";
                }

            }
        });

        swComment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {

                    useNoticeReply = "1";

                } else {
                    useNoticeReply = "0";
                }

            }
        });

        swFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    useNoticeAddedFriend = "1";

                } else {
                    useNoticeAddedFriend = "0";
                }

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

        appVersionContainer = (RelativeLayout)findViewById(R.id.setting_version_container);
        appVersionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setting.this, UpdateActivity.class);

                intent.putExtra("OLD_VERSION", oldVersion);
                intent.putExtra("NEW_VERSION", newVersion);
                intent.putExtra("UPDATE_IMAGE_URL", updateImageUrl);

                startActivity(intent);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();

        //alarmModifyRequestRequest();
    }

    public void checkVersionRequest() {

        String endPoint = "/version/last";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                JSONObject versionObj = response.getJSONObject("version");

                                if(versionObj.has("version")) {
                                    String versionId = versionObj.getString("versionId");
                                    String version = versionObj.getString("version");
                                    String versionImageUrl = versionObj.getString("versionImageUrl");
                                    String registryDate = versionObj.getString("registryDate");
                                    int updateCount = versionObj.getInt("updateCount");

                                    String strMyUpdateCount = GlobalSharedPreference.getAppPreferences(Setting.this, "appVersionCode");
                                    int myUpdateCount = Integer.parseInt(strMyUpdateCount);

                                    oldVersion = GlobalSharedPreference.getAppPreferences(Setting.this, "appVersionName");
                                    txtNewVersion.setText(oldVersion);

                                    if(myUpdateCount < updateCount) {
                                        imgNewVersion.setVisibility(View.VISIBLE);
                                        txtNewVersion.setText(version);

                                        oldVersion = GlobalSharedPreference.getAppPreferences(Setting.this, "appVersionName");
                                        newVersion = version;
                                        updateImageUrl = versionImageUrl;

                                    }else  {

                                    }

                                } else {

                                    oldVersion = GlobalSharedPreference.getAppPreferences(Setting.this, "appVersionName");
                                    newVersion = null;
                                    updateImageUrl = null;

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
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }


                });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //지금은 사용되지 않은 메소드
    public void alarmModifyRequestRequest() {

        String endPoint = "/users/user/notice";

        JSONObject obj = new JSONObject();

        try{

            obj.put("useNotice", useNotice);
            obj.put("useNoticeSystem", useNoticeSystem);
            obj.put("useNoticeAddedFriend", useNoticeAddedFriend);
            obj.put("useNoticeReply", useNoticeReply);

        }catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            Log.e("response", Integer.toString(responseCode));

                            if (responseCode == 200) {
                                GlobalSharedPreference.setAppPreferences(Setting.this, "useNotice", useNotice);
                                GlobalSharedPreference.setAppPreferences(Setting.this, "useNoticeReply", useNoticeReply);
                                GlobalSharedPreference.setAppPreferences(Setting.this, "useNoticeAddedFriend", useNoticeAddedFriend);
                                GlobalSharedPreference.setAppPreferences(Setting.this, "useNoticeSystem", useNoticeSystem);

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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(Setting.this, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Setting";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
