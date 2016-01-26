package com.likelab.likepet.singIn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.LoginActivity;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        // [END customize_button]
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            GoogleSignInAccount client = result.getSignInAccount();

            if(client != null) {
                Log.d("email", client.getEmail());

                checkEmailRequest("google", client.getEmail(), client.getId());
            }

        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);


        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mGoogleApiClient.isConnected()) {
            signOut();
            revokeAccess();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
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

    public void checkMemberRequest(String social, final String email, final String accountId, final boolean isAlreadyMember) {
        String endPoint = "/users/id/friendly/duplicate?login="+ social + "&accountId=" + accountId;

        System.out.println(endPoint);

        Log.d("accountId", accountId);

        //queue.add(new StringRequest(detail_info_request_url+endPoint, successListener, errorListener));
        //String parameter = null;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    int code;

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            code = response.getInt("code");

                            Log.d("checkMember", Integer.toString(code));
                            //Toast.makeText(JoinMemberBeginActivity.this, Integer.toString(code), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (code == 404) {


                            //이미 이메일로 가입되어 있는 회원이 소셜을 통해 가입하려는 경우 로그인 페이지로 이동시킨다.
                            if(isAlreadyMember == true) {
                                Toast.makeText(SignInActivity.this, "이미 이메일로 가입된 회원입니다. 로그인 페이지로 이동합니다.", Toast.LENGTH_LONG).show();

                                try {
                                    Thread.sleep(1500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {

                                //소셜을 통한 회원가입 프로세스 SignInActivity
                                Intent intent = new Intent(SignInActivity.this, ChooseCharacterActivity.class);

                                intent.putExtra("SOCIAL", 1);   //변수 값이 1이면 소셜을 통해 가입
                                intent.putExtra("EMAIL", email);
                                intent.putExtra("ID", accountId);
                                intent.putExtra("SOCIAL_TYPE", "google");
                                startActivity(intent);
                            }

                        } else if (code == 409) {
                            //이미 등록된 사용자는 로그인 프로세스 진행
                            loginRequest(email, accountId);
                            GlobalSharedPreference.setAppPreferences(SignInActivity.this, "linkageGoogle", "1");

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
                            Toast.makeText(SignInActivity.this, "로그인 되었습니다", Toast.LENGTH_LONG).show();
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
                setAppPreferences(SignInActivity.this, "sid", sid);
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

                                //로그인
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "userId", userId);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "name", name);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "email", email);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "gender", gender);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "clan", clan);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "status", status);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "parentUserId", parentUserId);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "profileImageUrl", profileImageUrl);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "national", national);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "ownerType", ownerType);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "birthday", birthday);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "registryDate", registryDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "modifyDate", modifyDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "termServiceDate", termServiceDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "privacyTermDate", privacyTermDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "withdrawReqDate", withdrawReqDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "withdrawDate", withdrawDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "mailAuthConfirmDate", mailAuthConfirmDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "lastLoginDate", lastLoginDate);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "mailAuth", mailAuth);
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "useNotice", "1");
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "useNoticeAddedFriend", "1");
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "useNoticeReply", "1");
                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "useNoticeSystem", "1");

                                GlobalSharedPreference.setAppPreferences(SignInActivity.this, "login", "login");


                                //로그인이 성공하면 메인 액티비티의 마이페이지로 이동한다
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
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
                String sid = GlobalSharedPreference.getAppPreferences(SignInActivity.this, "sid");
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


}
