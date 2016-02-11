package com.likelab.likepet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.singIn.SignInFlowActivityList;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class LoginActivity extends Activity {

    private Button btnFindPassword;

    private EditText editEmail;
    private EditText editPassword;

    private ImageButton btnDeleteEmail;
    private ImageButton btnDeletePassword;

    private RelativeLayout cancelContainer;

    private Button btnConfirm;

    private TextView txtLostEmail;
    private TextView txtLostPassword;

    private int blackLayoutFlag = 0;

    private RequestQueue queue;
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        queue = AppController.getInstance().getRequestQueue();

        final RelativeLayout layout = (RelativeLayout)getLayoutInflater().inflate(R.layout.login, null);

        btnConfirm = (Button)findViewById(R.id.login_btn_next);

        txtLostEmail = (TextView)findViewById(R.id.login_txt_lost_id);
        txtLostPassword = (TextView)findViewById(R.id.login_txt_lost_password);

        editEmail = (EditText)findViewById(R.id.login_edit_insert_email);
        editPassword = (EditText)findViewById(R.id.login_edit_insert_password);

        btnDeletePassword = (ImageButton)findViewById(R.id.login_insert_password_btn_delete);
        btnDeleteEmail = (ImageButton)findViewById(R.id.login_insert_email_btn_delete);

        cancelContainer = (RelativeLayout)findViewById(R.id.login_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email;
                String password;

                email = editEmail.getText().toString();
                password = editPassword.getText().toString();

                txtLostEmail.setVisibility(View.INVISIBLE);
                txtLostPassword.setVisibility(View.INVISIBLE);

                //아이디나 비밀번호란이 비어 있으면 진행이 되지 않는다
                if(email.length() != 0 || password.length() != 0) {

                    btnConfirm.setEnabled(false);
                    loginRequest(email, password);
                } else {
                    //Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnDeleteEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmail.setText("");
            }
        });

        btnDeletePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPassword.setText("");
            }
        });

        btnFindPassword = (Button)findViewById(R.id.join_member_begin_btn_lost_password);
        btnFindPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow popupWindow = new PopupWindow(v);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView;

                final RelativeLayout blackLayout = (RelativeLayout)findViewById(R.id.login_black_layout);
                final Button btnConfirmLogin = (Button)findViewById(R.id.login_btn_next);

                blackLayoutFlag = 1;

                popupView = inflater.inflate(R.layout.lost_password_popup_view, null);

                popupWindow.setContentView(popupView);
                popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                popupWindow.showAtLocation(layout, Gravity.CENTER, 0, -300);

                blackLayout.setVisibility(View.VISIBLE);
                btnConfirmLogin.setVisibility(View.INVISIBLE);

                RelativeLayout cancelContainer = (RelativeLayout) popupView.findViewById(R.id.lost_password_cancel_container);
                cancelContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                Button btnConfirm = (Button) popupView.findViewById(R.id.lost_password_btn_confirm);

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    public void onDismiss() {
                        blackLayout.setVisibility(View.INVISIBLE);
                        btnConfirmLogin.setVisibility(View.VISIBLE);
                    }

                });
            }
        });


        final TextWatcher editEmailWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                if(editEmail.getText().length() == 0) {
                    btnDeleteEmail.setVisibility(View.INVISIBLE);
                }
                else
                    btnDeleteEmail.setVisibility(View.VISIBLE);

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
                if(editPassword.getText().length() == 0) {
                    btnDeletePassword.setVisibility(View.INVISIBLE);
                }
                else
                    btnDeletePassword.setVisibility(View.VISIBLE);

            }

            public void afterTextChanged(Editable s) {
            }
        };

        editPassword.addTextChangedListener(editPasswordWatcher);

    }

    public void loginRequest(final String email, final String password) {

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
                            Toast.makeText(LoginActivity.this, Integer.toString(responseCode), Toast.LENGTH_LONG);

                            if (responseCode == 200) {

                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "password", password);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "loginType", "email");
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.join_insert_email_password_txt_login_complete),
                                        Toast.LENGTH_LONG).show();
                                loadUserInformation(email);
                                finish();


                            } else if(responseCode == 401) {
                                txtLostPassword.setVisibility(View.VISIBLE);
                                btnConfirm.setEnabled(true);

                            } else if(responseCode == 404) {
                                txtLostEmail.setVisibility(View.VISIBLE);
                                btnConfirm.setEnabled(true);
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
                    GlobalSharedPreference.setAppPreferences(LoginActivity.this, "sid", sid);

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
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(LoginActivity.this, "login", "login");


                                try {
                                    Thread.sleep(100);
                                }catch (Exception e) {

                                }

                                //로그인이 성공하면 메인 액티비티의 마이페이지로 이동한다
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("LOGIN_REQUEST", "login");

                                //쌓인 액티비티 청소
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
                String sid = GlobalSharedPreference.getAppPreferences(LoginActivity.this, "sid");
                params.put("sessionId", sid);
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

        String pageName = "Login";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
