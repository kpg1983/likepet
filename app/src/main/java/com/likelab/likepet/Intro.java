package com.likelab.likepet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kr.co.fingerpush.android.GCMFingerPushManager;
import kr.co.fingerpush.android.NetworkUtility;

/**
 * Created by kpg1983 on 2016-02-01.
 */

public class Intro extends Activity{

    ImageView imgIntro;
    RelativeLayout overlay;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        imgIntro = (ImageView)findViewById(R.id.intro_img_splash);
        overlay = (RelativeLayout)findViewById(R.id.intro_overlay);
        
        if(GlobalSharedPreference.getAppPreferences(Intro.this, "loginType").equals("sns")) {

            String accountId = GlobalSharedPreference.getAppPreferences(Intro.this, "accountId");
            String email = GlobalSharedPreference.getAppPreferences(Intro.this, "email");

            snsLoginRequest(email, accountId);

        } else if(GlobalSharedPreference.getAppPreferences(Intro.this, "loginType").equals("email")) {

            String password = GlobalSharedPreference.getAppPreferences(Intro.this, "password");
            String email = GlobalSharedPreference.getAppPreferences(Intro.this, "email");

            emailLoginRequest(email, password);

        } else  {

            //로그인 정보가 전혀 없는 경우 로그아웃 프로세스 진행
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "userId");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "name");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "email");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "gender");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "clan");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "status");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "parentUserId");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "profileImageUrl");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "national");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "ownerType");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "birthday");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "registryDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "modifyDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "termServiceDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "privacyTermDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "withdrawReqDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "withdrawDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "mailAuthConfirmDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "lastLoginDate");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "mailAuth");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "useNoticeAddedFriend");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "useNoticeReply");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "useNoticeSystem");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "sid");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "linkageFacebook");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "linkageTwitter");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "linkageGoogle");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "accountId");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "password");
            GlobalSharedPreference.deleteAppPreferences(Intro.this, "loginType");

            GlobalSharedPreference.setAppPreferences(Intro.this, "login", "logout");
        }


        GCMFingerPushManager.getInstance(this).getAppReport(
                new NetworkUtility.NetworkObjectListener() { // 비동기 이벤트 리스너

                    @Override
                    public void onError(String code, String errorMessage) {
                        // TODO Auto-generated method stub
                        Log.d("", "onError : code : " + code + ", message : " + errorMessage);
                    }

                    @Override
                    public void onComplete(String code, String resultMessage, JSONObject ObjectData, Integer TotalArticleCount, Integer CurrentPageNo) {
                        // TODO Auto-generated method stub
                        Log.d("", "onComplete : code : " + code + ", message : " + resultMessage);

                        String AppKey = ObjectData.optString("appid");
                        String AppName = ObjectData.optString("app_name");
                        String User_Id = ObjectData.optString("user_id");
                        String Icon = ObjectData.optString("icon");
                        String Category = ObjectData.optString("category");
                        String Environments = ObjectData.optString("environments");
                        String BeAndroid = ObjectData.optString("beandroid");
                        String Version = ObjectData.optString("android_version");
                        String AndroidUpdateLink = ObjectData.optString("android_upd_link");
                        String BeUpdateLink = ObjectData.optString("beupdalert_a");
                        String UpdateDate = ObjectData.optString("ver_update_date_a");

                        Log.d("version", Version);
                        Log.d("beAndroid", BeAndroid);
                        Log.d("androidLink", AndroidUpdateLink);
                        Log.d("beUpdateLine", BeUpdateLink);
                        Log.d("updateDate", UpdateDate);
                    }

                    @Override
                    public void onCancel() {
                        // TODO Auto-generated method stub

                    }
                }
        );


        //신규 업데이트 체크
        //업데이트 체크 후 강제 인지 아닌지 구분한다.
        //강제 업데이트일 경우 업데이트를 하지 않으면 앱을 강제 종료한다.
        checkUpdateRequest();


    }

    @Override
    public void onStart() {
        super.onStart();

        imgIntro.setImageResource(R.drawable.splash);
    }

    @Override
    public void onStop() {
        super.onStop();

        imgIntro.setImageDrawable(null);

    }

    public void checkUpdateRequest() {

        String endPoint = "/version/last";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            int urgent = response.getInt("urgent_android");
                            //int urgent = 0;
                            String version = response.getString("ver_android");
                            //String version = "1.0.0";
                            String message_ko = response.getString("update_message_ko");
                            String message_en = response.getString("update_message_en");
                            String updateUrl = response.getString("update_url_android");

                            PackageInfo pi = null;
                            try {
                                pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            String myVersion = pi.versionName;

                            Locale mLocale = getResources().getConfiguration().locale;
                            String deviceLanguage = mLocale.getLanguage();

                            String message;
                            if (deviceLanguage.contains("ko")) {
                                message = message_ko;
                            } else {
                                message = message_en;
                            }

                            //긴급 업데이트
                            if (urgent == 1) {
                                urgentUpdatePopupRequest(imgIntro, message, updateUrl);

                                //현재 버전이 최신 버전이 아닌 경우
                            } else if (!version.equals(myVersion)) {
                                recommendUpdatePopupRequest(imgIntro, message, updateUrl);

                            } else {

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(Intro.this, MainActivity.class));
                                        finish();
                                    }
                                }, 1500);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(Intro.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    private void recommendUpdatePopupRequest(View v, String message, final String url) {

        final PopupWindow popupWindow = new PopupWindow(Intro.this);
        LayoutInflater inflater = (LayoutInflater) Intro.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.recommend_update_popup_window, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(imgIntro, Gravity.CENTER, 0, 0);
            }
        }, 100L);

        overlay.setVisibility(View.VISIBLE);
        overlay.setBackgroundColor(Color.parseColor("#70000000"));

        RelativeLayout updateLater = (RelativeLayout) popupView.findViewById(R.id.recommend_update_later_container);
        RelativeLayout updateNow = (RelativeLayout) popupView.findViewById(R.id.recommend_update_now_container);
        TextView txtMessage = (TextView)popupView.findViewById(R.id.recommend_update_txt);

        txtMessage.setText(message);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

                overlay.setVisibility(View.GONE);
                overlay.setBackground(null);
                finish();
            }
        });

        updateLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                Intent intent = new Intent(Intro.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                Uri uriUrl = Uri.parse(url);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                finish();
            }
        });
    }

    private void urgentUpdatePopupRequest(View v, String message, final String url) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) Intro.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.urgent_update_popup_window, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        overlay.setVisibility(View.VISIBLE);
        overlay.setBackgroundColor(Color.parseColor("#70000000"));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(imgIntro, Gravity.CENTER, 0, 0);
            }
        }, 100L);


        RelativeLayout updateNow = (RelativeLayout) popupView.findViewById(R.id.urgent_update_now_container);
        TextView txtMessage = (TextView)popupView.findViewById(R.id.urgent_update_txt);

        txtMessage.setText(message);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

                overlay.setVisibility(View.GONE);
                overlay.setBackground(null);
                finish();
            }
        });

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                Uri uriUrl = Uri.parse(url);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                finish();

            }
        });
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
                            GlobalSharedPreference.setAppPreferences(Intro.this, "email", email);
                            GlobalSharedPreference.setAppPreferences(Intro.this, "accountId", id);
                            GlobalSharedPreference.setAppPreferences(Intro.this, "loginType", "sns");
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(Intro.this, error.toString(), Toast.LENGTH_LONG).show();
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

                GlobalSharedPreference.setAppPreferences(Intro.this, "sid", sid);

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
                            Toast.makeText(Intro.this, Integer.toString(responseCode), Toast.LENGTH_LONG);

                            if (responseCode == 200) {

                                GlobalSharedPreference.setAppPreferences(Intro.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "password", password);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "loginType", "email");
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
                    GlobalSharedPreference.setAppPreferences(Intro.this, "sid", sid);

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
                                GlobalSharedPreference.setAppPreferences(Intro.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(Intro.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(Intro.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(Intro.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(Intro.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(Intro.this, "login", "login");

                                Log.d("userId", userId);


                                if(!GlobalSharedPreference.getAppPreferences(getApplicationContext(), "isPushRegistered").equals("true")) {

                                    GCMFingerPushManager.getInstance(Intro.this).setIdentity(
                                            userId,  // 식별자 값
                                            new NetworkUtility.NetworkObjectListener() { // 비동기 이벤트 리스너

                                                @Override
                                                public void onError(String code, String errorMessage) {
                                                    // TODO Auto-generated method stub
                                                    Log.d("", "onError : code : " + code + ", message : " + errorMessage);
                                                }

                                                @Override
                                                public void onComplete(String code, String resultMessage, JSONObject ObjectData, Integer TotalArticleCount, Integer CurrentPageNo) {
                                                    // TODO Auto-generated method stub
                                                    Log.d("", "onComplete : code : " + code + ", message : " + resultMessage);

                                                    GlobalSharedPreference.setAppPreferences(getApplicationContext(), "isPushRegistered", "true");
                                                    Toast.makeText(getApplicationContext(), "푸쉬서버에 등록 완료", Toast.LENGTH_SHORT).show();

                                                }

                                                @Override
                                                public void onCancel() {
                                                    // TODO Auto-generated method stub

                                                }
                                            }
                                    );

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
                        //Toast.makeText(Intro.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String sid = GlobalSharedPreference.getAppPreferences(Intro.this, "sid");
                params.put("sessionId", sid);

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

}
