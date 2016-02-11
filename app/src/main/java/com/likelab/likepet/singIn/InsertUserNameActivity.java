package com.likelab.likepet.singIn;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.MccTable;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class InsertUserNameActivity extends Activity {

    private EditText editName;

    private RelativeLayout cancelContainer;

    private Button btnConfirm;

    private TextView txtErrorMessageTwoWord;
    private TextView txtErrorMessage15Word;
    private TextView txtErrorMessageNoSpace;

    int socialFlag = 0;

    String clan;
    String email;
    String userId;
    String name;
    String socialType;  //페이스북, 구글, 트위터 중 하나

    RequestQueue queue = AppController.getInstance().getRequestQueue();
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    private WebView mWebView;

    private TextView txtWebServiceTerms;
    private TextView txtWebPersonalInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_member_insert_name);


        mWebView = (WebView)findViewById(R.id.join_member_web_view);

        mWebView.getSettings().setJavaScriptEnabled(true);

        SignInFlowActivityList.activityArrayList.add(this);

        Intent intent = getIntent();

        clan = intent.getExtras().getString("CLAN");

        if(intent.hasExtra("SOCIAL")) {
            socialFlag = 1;

            userId = intent.getExtras().getString("ID");
            email = intent.getExtras().getString("EMAIL");
            socialType = intent.getExtras().getString("SOCIAL_TYPE");
        }

        txtWebServiceTerms = (TextView)findViewById(R.id.join_member_service_term);
        txtWebPersonalInfo = (TextView)findViewById(R.id.join_member_personal_info);

        txtWebServiceTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.service_term)));
                startActivity(intent);
            }
        });

        txtWebPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.personal_info)));
                startActivity(intent);
            }
        });


        txtErrorMessageTwoWord = (TextView)findViewById(R.id.join_member_txt_error_message_1);
        txtErrorMessage15Word = (TextView)findViewById(R.id.join_member_txt_error_message_2);
        txtErrorMessageNoSpace = (TextView)findViewById(R.id.join_member_txt_error_message_3);

        cancelContainer = (RelativeLayout)findViewById(R.id.join_member_choose_name_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editName = (EditText)findViewById(R.id.join_member_edit_insert_name);

        btnConfirm = (Button) findViewById(R.id.join_member_choose_name_btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editName.getText().length() < 2) {
                    txtErrorMessageTwoWord.setVisibility(View.VISIBLE);
                } else if (editName.getText().length() > 15) {
                    txtErrorMessage15Word.setVisibility(View.VISIBLE);
                } else if(editName.getText().toString().contains(" ")) {

                    txtErrorMessageNoSpace.setVisibility(View.VISIBLE);
                } else {

                    name = editName.getText().toString();
                    checkDuplicateNameRequest(name, socialFlag);

                }

            }
        });

        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                txtErrorMessageTwoWord.setVisibility(View.INVISIBLE);
                txtErrorMessage15Word.setVisibility(View.INVISIBLE);
                txtErrorMessageNoSpace.setVisibility(View.INVISIBLE);
            }

            public void afterTextChanged(Editable s) {
            }
        };

        editName.addTextChangedListener(mTextEditorWatcher);

    }


    public void checkDuplicateNameRequest(final String name, final int socialFlag) {


        String utf8Name = null;

        try {
             utf8Name = URLEncoder.encode(name, "UTF-8");
        }catch (Exception e) {

        }


        final String endPoint = "/users/name/duplicate/" + utf8Name;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 409) {

                                Toast.makeText(InsertUserNameActivity.this,
                                        getResources().getString(R.string.join_insert_userName_txt_overlap), Toast.LENGTH_SHORT).show();

                                //사용 가능한 이름
                            } else if(responseCode == 404) {


                                //이메일 가입
                                if(socialFlag == 0) {
                                    Intent intent = new Intent(InsertUserNameActivity.this, JoinMembersInsertNameAndPassword.class);
                                    SignInUserInformation.userName = editName.getText().toString();

                                    intent.putExtra("CLAN", clan);
                                    intent.putExtra("NAME", editName.getText().toString());

                                    startActivity(intent);

                                    //소셜 아이디를 통한 가입
                                } else {
                                    singInRequest(userId, email, socialType);
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

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void singInRequest(final String id, final String email, final String socialType) {

        String endPoint = "/users/friendly";

        JSONObject obj = new JSONObject();

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
        } else if(deviceLanguage.contains("pt")) {
            language = "pt";
        } else {
            language = "en";
        }

        String country;
        if(GlobalVariable.mcc.equals("null")) {

            country = GlobalVariable.countryCode;
        } else {
            int mcc = Integer.parseInt(GlobalSharedPreference.getAppPreferences(this, "mcc"));
            country = MccTable.countryCodeForMcc(mcc);
        }

        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String appVersion = pi.versionName;
        String osVersion = Build.VERSION.RELEASE;
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        try {

            obj.put("email", email);
            obj.put("termService", 1);
            obj.put("privacyTerm", 1);
            obj.put("phoneUUID", uuid);
            obj.put("PhoneOS", osVersion);
            obj.put("appVersion", appVersion);
            obj.put("language", language);
            obj.put("login", socialType);
            obj.put("accountId", id);
            obj.put("name", name);
            obj.put("clan", clan);
            obj.put("country", country);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                Toast.makeText(InsertUserNameActivity.this,
                                        getResources().getString(R.string.join_insert_email_password_toast_finish), Toast.LENGTH_SHORT).show();

                                //후에 회원계정 정보에서 어떠한 소셜과 연동되어 있는지 확인해준다.
                                if(socialType.equals("twitter")) {
                                    GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "linkageTwitter", "1");
                                } else if(socialType.equals("facebook")) {
                                    GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "linkageFacebook", "1");
                                } else {
                                    GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "linkageGoogle", "1");

                                }

                                loginRequest(email, id);


                            } else if (responseCode == 409) {
                                loginRequest(email, id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void loginRequest(final String email, final String id) {

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
                            Toast.makeText(InsertUserNameActivity.this,
                                    getResources().getString(R.string.join_insert_email_password_txt_login_complete), Toast.LENGTH_LONG).show();
                            loadUserInformation(email);
                            GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "email", email);
                            GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "accountId", id);
                            GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "loginType", "sns");

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
                params.put("accountId", id);
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                Map<String, String> responseHeaders = response.headers;
                String sid = responseHeaders.get("sessionID");

                setAppPreferences(InsertUserNameActivity.this, "sid", sid);

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

    public void loadUserInformation(final String email) {

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

                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "useNoticeSystem", "1");
                                GlobalSharedPreference.setAppPreferences(InsertUserNameActivity.this, "login", "login");

                                try {
                                    Thread.sleep(100);
                                }catch (Exception e) {

                                }

                                for(int i=0; i<SignInFlowActivityList.activityArrayList.size(); i++) {
                                    SignInFlowActivityList.activityArrayList.get(i).finish();
                                }

                                finish();

                                Intent intent = new Intent(InsertUserNameActivity.this, MainActivity.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.putExtra("LOGIN_REQUEST", "1");

                                startActivity(intent);


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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(InsertUserNameActivity.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    private void setAppPreferences(Activity context, String key, String value)
    {
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("sid", 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(key, value);

        prefEditor.commit();
    }

    private String getAppPreferences(Activity context, String key)
    {
        String returnValue = null;
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("sid", 0);
        returnValue = pref.getString(key, "");

        return returnValue;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "setName";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}

