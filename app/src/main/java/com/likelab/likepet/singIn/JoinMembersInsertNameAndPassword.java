package com.likelab.likepet.singIn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class JoinMembersInsertNameAndPassword extends Activity {

    private EditText editEmail;
    private EditText editPassword;

    private ImageView imgCheckEmail;
    private ImageView imgCheckPassword;

    private RelativeLayout cancelContainer;
    private Button btnConfirm;
    private RequestQueue queue;

    private TextView txtEmailErrorMessage;
    private TextView txtPasswordErrorMessage;
    private TextView txtPasswordErrorMessageSpecialCharacter;
    private TextView txtPasswordErrorMessageNoSpace;

    String clan;
    String name;

    boolean isEmailOK = false;
    boolean isPasswordOK = false;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_member_insert_email_password);

        queue = AppController.getInstance().getRequestQueue();

        Intent intent = getIntent();

        clan = intent.getStringExtra("CLAN");
        name = intent.getStringExtra("NAME");

        txtEmailErrorMessage = (TextView)findViewById(R.id.join_member_txt_error_message_email);
        txtPasswordErrorMessage = (TextView)findViewById(R.id.join_member_txt_error_message_password);
        txtPasswordErrorMessageSpecialCharacter = (TextView)findViewById(R.id.join_member_txt_error_message_password_special_character);
        txtPasswordErrorMessageNoSpace = (TextView)findViewById(R.id.join_member_txt_error_message_password_no_space);

        editEmail = (EditText)findViewById(R.id.join_member_edit_insert_email);
        editPassword = (EditText)findViewById(R.id.join_member_edit_insert_password);

        imgCheckEmail = (ImageView)findViewById(R.id.join_member_insert_email_img_check);
        imgCheckPassword = (ImageView)findViewById(R.id.join_member_insert_password_img_check);

        cancelContainer = (RelativeLayout)findViewById(R.id.join_member_insert_email_password_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirm = (Button)findViewById(R.id.join_member_insert_email_password_btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();

                if(isEmailOK == true && isPasswordOK == true) {
                    checkMember(email, password);
                }
                else if(isEmailOK == false) {
                    txtEmailErrorMessage.setVisibility(View.VISIBLE);

                } else if(isPasswordOK == false) {

                    if(editPassword.getText().length() < 6) {

                        txtPasswordErrorMessage.setVisibility(View.VISIBLE);
                    } else if(!isPasswordValidate(editPassword.getText().toString())) {
                        txtPasswordErrorMessageSpecialCharacter.setVisibility(View.VISIBLE);

                    } else if(editPassword.getText().toString().contains(" ")){
                        txtPasswordErrorMessageNoSpace.setVisibility(View.VISIBLE);
                    }

                }

            }
        });

        final TextWatcher editEmailWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                txtEmailErrorMessage.setVisibility(View.INVISIBLE);
                if(isValidEmail(editEmail.getText())) {
                    txtEmailErrorMessage.setVisibility(View.INVISIBLE);
                    imgCheckEmail.setVisibility(View.VISIBLE);
                    isEmailOK = true;
                }
                else {
                    imgCheckEmail.setVisibility(View.INVISIBLE);
                    isEmailOK = false;
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        editEmail.addTextChangedListener(editEmailWatcher);

        final TextWatcher editPasswordWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                txtPasswordErrorMessage.setVisibility(View.INVISIBLE);
                txtPasswordErrorMessageSpecialCharacter.setVisibility(View.INVISIBLE);
                txtPasswordErrorMessageNoSpace.setVisibility(View.INVISIBLE);

                String tempPassword = editPassword.getText().toString();
                int passwordLength = tempPassword.length();

                if(isPasswordValidate(editPassword.getText().toString()) && passwordLength > 5 && passwordLength < 21 && !editPassword.getText().toString().contains(" ")) {
                    imgCheckPassword.setVisibility(View.VISIBLE);
                    isPasswordOK = true;
                }
                else {
                    isPasswordOK = false;
                    imgCheckPassword.setVisibility(View.INVISIBLE);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        editPassword.addTextChangedListener(editPasswordWatcher);
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }

    }


    private static final String PasswordPATTERN = "^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{6,20}$";

    public boolean isPasswordValidate(final String hex) {

        Pattern pattern = Pattern.compile(PasswordPATTERN);

        Matcher matcher = pattern.matcher(hex);

        return matcher.matches();

    }




    public void checkMember(final String email, final String password) {


        String endPoint = "/users/duplicate/" + email;


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL+ endPoint,
                new Response.Listener<JSONObject>() {

                    int code;

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            code = response.getInt("code");
                            //Toast.makeText(JoinMemberBeginActivity.this, Integer.toString(code), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (code == 404) {
                            singInRequest(email, password);

                        } else if (code == 409) {
                            Toast.makeText(JoinMembersInsertNameAndPassword.this, getResources().getString(R.string.join_insert_email_password_toast_duplicate_email),
                                    Toast.LENGTH_LONG).show();

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

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public void singInRequest(final String email, final String password) {

        String endPoint = "/users/user";

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
            obj.put("password", password);
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
                        System.out.println(response);

                        int responseCode;

                        try {
                            responseCode = response.getInt("code");
                            //Toast.makeText(JoinMemberBeginActivity.this, Integer.toString(responseCode), Toast.LENGTH_LONG).show();

                            if (responseCode == 200) {
                                Toast.makeText(JoinMembersInsertNameAndPassword.this, getResources().getString(R.string.join_insert_email_password_toast_finish),
                                        Toast.LENGTH_SHORT).show();

                                loginRequest(email, password);

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
                }){
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

    public void loginRequest(final String email, final String password) {

        String endPoint = "/login/" + email;

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

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

                            if (responseCode == 200) {
                                Toast.makeText(JoinMembersInsertNameAndPassword.this, getResources().getString(R.string.join_insert_email_password_txt_login_complete),
                                        Toast.LENGTH_LONG).show();
                                loadUserInformation(email);
                                finish();


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
                    GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "sid", sid);

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

                                //로그인
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "useNoticeSystem", "1");

                                //로그인 처리를 해준다
                                GlobalSharedPreference.setAppPreferences(JoinMembersInsertNameAndPassword.this, "login", "login");

                                try {
                                    Thread.sleep(100);
                                }catch (Exception e) {

                                }

                                //로그인이 성공하면 메인 액티비티의 마이페이지로 이동한다
                                Intent intent = new Intent(JoinMembersInsertNameAndPassword.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("LOGIN_REQUEST", "login");

                                for(int i=0; i<SignInFlowActivityList.activityArrayList.size(); i++) {
                                    SignInFlowActivityList.activityArrayList.get(i).finish();
                                }

                                SignInFlowActivityList.activityArrayList.clear();

                                startActivity(intent);
                                finish();

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
                String sid = GlobalSharedPreference.getAppPreferences(JoinMembersInsertNameAndPassword.this, "sid");
                params.put("sessionId", sid);
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

        String pageName = "setEmail";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
