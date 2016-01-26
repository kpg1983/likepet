package com.likelab.likepet.singIn;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.LoginActivity;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.volleryCustom.AppController;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class JoinMemberBeginActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    private RelativeLayout joinEmail;
    private RelativeLayout cancelContainer;

    private Button btnLogin;

    private LoginButton btnLoginFacebook;

    private CallbackManager callbackManager;

    private RelativeLayout facebookLoginContainer;
    private RelativeLayout twitterLoginContainer;
    private RelativeLayout googleLoginContainer;

    private static final int REQ_LOGIN = 4;

    private static final int REQ_FACEBOOK_LOGIN = 1;
    private static final int REQ_GOOGLE_LOGIN = 2;
    private static final int REQ_TWITTER_LOGIN = 3;


    private String CALLBACK_URL = "callback://tweeter";


    String id;
    String name;
    String email;

    public AccessTokenTracker accessTokenTracker;

    private static final String TAG = "SignInActivity";

    private RequestQueue queue;
    private String mFacebookAccessToken;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    TwitterAuthClient twitterAuthClient = new TwitterAuthClient();

    private static boolean isTwitterLogin = false;
    private static boolean isFacebookLogin = false;
    private static boolean isGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_member_begin);

        queue = AppController.getInstance().getRequestQueue();

        mFacebookAccessToken = getAppPreferences(this, "ACCESS_TOKEN");

        SignInFlowActivityList.activityArrayList.add(this);


        //국가, 언어 정보
        Locale systemLocale = getResources().getConfiguration().locale;
        String strDisplayCountry = systemLocale.getDisplayCountry();
        String strCountry = systemLocale.getCountry();
        String strLanguage = systemLocale.getLanguage();


        FacebookSdk.sdkInitialize(getApplicationContext());

        twitterLoginContainer = (RelativeLayout)findViewById(R.id.join_member_begin_twitter_container);
        twitterLoginContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isTwitterLogin = true;
                isFacebookLogin = false;
                isGoogleLogin = false;

                email = null;
                id = null;
                name = null;

                twitterAuthClient.authorize(JoinMemberBeginActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

                    @Override
                    public void success(Result<TwitterSession> twitterSessionResult) {
                        TwitterSession session = Twitter.getSessionManager().getActiveSession();
                        TwitterAuthToken authToken = session.getAuthToken();
                        String token = authToken.token;
                        String secret = authToken.secret;

                        //트위터로부터 이메일 정보를 가져온다
                        twitterAuthClient.requestEmail(session, new Callback<String>() {
                            @Override
                            public void success(Result<String> result) {

                                String email = result.data;

                                setTwitterEmail(email);

                                //이메일 주소를 가져오는데 성공하면, 회원가입 또는 로그인 프로세스를 진행한다.
                                //첫번째로 중복되어 있는 이메일인지 확인한다.

                                if(email != null) {
                                    checkEmailRequest("twitter", email, id);
                                } else {
                                    Toast.makeText(JoinMemberBeginActivity.this, getResources().getString(R.string.join_incorrect_twitter_email),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void failure(TwitterException e) {

                                e.printStackTrace();
                            }
                        });

                        Twitter.getApiClient(session).getAccountService().verifyCredentials(true, false, new Callback<User>() {
                            @Override
                            public void success(Result<User> result) {
                                User user = result.data;

                                long id = user.id;
                                setTwitterUserId(id);

                            }

                            @Override
                            public void failure(TwitterException e) {

                                e.printStackTrace();

                            }
                        });
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                    }
                });

            }
        });


        googleLoginContainer = (RelativeLayout) findViewById(R.id.join_member_begin_google_container);
        googleLoginContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinMemberBeginActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        twitterLoginContainer = (RelativeLayout) findViewById(R.id.join_member_begin_twitter_container);

        // View Initialization
        // FaceBook Access Token Track
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // App code
                Log.i("페이스북", "Current Token : " + currentAccessToken);
                if (currentAccessToken == null) {

                }
            }
        };


        facebookLoginContainer = (RelativeLayout) findViewById(R.id.join_member_begin_facebook_container);
        facebookLoginContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFacebookLogin = true;
                isTwitterLogin = false;
                isGoogleLogin = false;

                email = null;
                id = null;
                name = null;
                facebookLogin();
            }
        });

        cancelContainer = (RelativeLayout) findViewById(R.id.join_member_begin_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        joinEmail = (RelativeLayout) findViewById(R.id.join_member_begin_email_container);
        joinEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinMemberBeginActivity.this, ChooseCharacterActivity.class);
                startActivity(intent);
            }
        });


        btnLogin = (Button) findViewById(R.id.join_member_begin_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinMemberBeginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(isFacebookLogin == true)
            callbackManager.onActivityResult(requestCode, resultCode, data);

        if(isTwitterLogin == true) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);

        }

    }

    public void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();


        LoginManager.getInstance().logOut();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {//로그인이 성공되었을때 호출

                        final GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {

                                    //토큰 값
                                    String token = loginResult.getAccessToken().getToken();

                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        // Application code
                                        try {

                                            mFacebookAccessToken = getAppPreferences(JoinMemberBeginActivity.this, "Facebook_Token");

                                            if (mFacebookAccessToken.equals("") || mFacebookAccessToken == null) {

                                                mFacebookAccessToken = token;
                                                setAppPreferences(JoinMemberBeginActivity.this, "Facebook_Token", mFacebookAccessToken);
                                            }

                                            id = (String) response.getJSONObject().get("id");//페이스북 아이디값
                                            name = (String) response.getJSONObject().get("name");//페이스북 이름String email = (String) response.getJSONObject().get("email");//이메일
                                            email = (String) response.getJSONObject().get("email");//이메일

                                        } catch (JSONException e) {

                                            // TODO Auto-generated catch block

                                            e.printStackTrace();

                                        }
                                        // new joinTask().execute(); //자신의 서버에서 로그인 처리를 해줍니다
                                        Log.d("accountId", id);

                                        //queue.add(new StringRequest(detail_info_request_url+endPoint, successListener, errorListener));
                                        //String parameter = null;

                                        //소셜을 통한 회원 가입 프로세스 중 중복체크
                                        if(email != null) {
                                            checkEmailRequest("facebook", email, id);
                                        } else {
                                            Toast.makeText(JoinMemberBeginActivity.this, getResources().getString(R.string.join_incorrect_facebook_email), Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                });


                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    public void checkEmailRequest(final String social, final String email, final String accountId) {


        String endPoint = "/users/duplicate/" + email;


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL+ endPoint,
                new Response.Listener<JSONObject>() {

                    int code;
                    boolean isAlreadyMember;

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            code = response.getInt("code");
                            //Toast.makeText(JoinMemberBeginActivity.this, Integer.toString(code), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (code == 404) {
                            isAlreadyMember = false;    //이메일 정보가 없는경우 -> 회원가입이 되어 있지 않음
                            checkMemberRequest(social, email, accountId, isAlreadyMember);


                        } else if (code == 409) {
                            isAlreadyMember = true; //이메일 정보가 있는 경우 -> 소셜 또는 이메일로 회원가입이 되어 있음
                            checkMemberRequest(social, email, accountId, isAlreadyMember);

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }


    public void checkMemberRequest(final String social, final String email, final String accountId, final boolean isAlreadyMember) {
        String endPoint = "/users/id/friendly/duplicate?login="+ social + "&accountId=" + accountId;

        System.out.println(endPoint);

       // Log.d("accountId", accountId);

        //queue.add(new StringRequest(detail_info_request_url+endPoint, successListener, errorListener));
        //String parameter = null;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
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


                            //이미 이메일 또는 다른 소셜을 통해서 가입되어 있는 회원이 소셜을 통해 가입하려는 경우 로그인 페이지로 이동시킨다.
                            if(isAlreadyMember == true) {
                                Toast.makeText(JoinMemberBeginActivity.this, "이미 가입된 이메일 주소입니다.", Toast.LENGTH_SHORT).show();

                            } else {

                                //소셜을 통한 회원가입 프로세스 진행
                                Intent intent = new Intent(JoinMemberBeginActivity.this, ChooseCharacterActivity.class);

                                intent.putExtra("SOCIAL", 1);   //변수 값이 1이면 소셜을 통해 가입
                                intent.putExtra("EMAIL", email);
                                intent.putExtra("ID", accountId);
                                intent.putExtra("SOCIAL_TYPE", social);
                                startActivity(intent);
                            }

                        } else if (code == 409) {
                            //이미 등록된 사용자는 로그인 프로세스 진행
                                loginRequest(email, accountId);

                                //이메일로 가입되어 있는 회원은 로그인 페이지로 이동한다.

                            //후에 회원계정 정보에서 어떠한 소셜과 연동되어 있는지 확인해준다.
                            if(social.equals("twitter")) {
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "linkageTwitter", "1");
                            } else if(social.equals("facebook")) {
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "linkageFacebook", "1");
                            } else {
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "linkageGoogle", "1");

                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }




    public void loginRequest(final String email, final String id) {

        String endPoint = "/login/friendly/" + email;

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

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
                            Toast.makeText(JoinMemberBeginActivity.this, getResources().getString(R.string.join_insert_email_password_txt_login_complete),
                                    Toast.LENGTH_LONG).show();
                            loadUserInformation(email);
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

                return params;

            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                Map<String, String> responseHeaders = response.headers;
                String sid = responseHeaders.get("sessionID");


                Log.d("SID", sid);
                setAppPreferences(JoinMemberBeginActivity.this, "sid", sid);
                System.out.println("sid:" + sid);

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
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(JoinMemberBeginActivity.this, "login", "login");

                                try {
                                    Thread.sleep(100);
                                }catch (Exception e) {

                                }


                                //로그인이 성공하면 메인 액티비티의 마이페이지로 이동한다
                                Intent intent = new Intent(JoinMemberBeginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("LOGIN_REQUEST", "login");

                                for(int i=0; i<SignInFlowActivityList.activityArrayList.size(); i++) {
                                    SignInFlowActivityList.activityArrayList.get(i).finish();
                                }

                                SignInFlowActivityList.activityArrayList.clear();

                                startActivity(intent);
                                finish();

                                Log.d("userId", userId);
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
                String sid = GlobalSharedPreference.getAppPreferences(JoinMemberBeginActivity.this, "sid");
                params.put("sessionId", sid);

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

    public void setTwitterEmail(String email) {
        this.email = email;
    }

    public void setTwitterUserId(long userId) {

        this.id = Long.toString(userId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "SignIn";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


}
