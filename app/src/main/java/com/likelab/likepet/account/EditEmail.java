package com.likelab.likepet.account;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by kpg1983 on 2015-10-29.
 */
public class EditEmail extends Activity {

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;
    private TextView txtOriginalEmail;
    private EditText editNewEmail;

    private TextView txtEmailErrorMessage;
    private ImageView imgCheckEmail;

    boolean isEmailOK = false;

    private Button btnConfirm;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.edit_email_activity);

        cancelContainer = (RelativeLayout) findViewById(R.id.edit_email_cancel_container);
        btnCancel = (ImageButton) findViewById(R.id.edit_email_btn_cancel);

        txtOriginalEmail = (TextView) findViewById(R.id.edit_email_txt_setting_before_email);
        txtOriginalEmail.setText(GlobalSharedPreference.getAppPreferences(this, "email"));

        editNewEmail = (EditText) findViewById(R.id.edit_email_edit_setting_new_email);

        txtEmailErrorMessage = (TextView) findViewById(R.id.edit_email_txt_error_message_email);
        imgCheckEmail = (ImageView) findViewById(R.id.edit_email_img_check);

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

        //edit text의 변화를 감지한다.
        final TextWatcher editEmailWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                txtEmailErrorMessage.setVisibility(View.INVISIBLE);
                //이메일 유효성 체크 OK
                if (isValidEmail(editNewEmail.getText())) {
                    txtEmailErrorMessage.setVisibility(View.INVISIBLE);
                    imgCheckEmail.setVisibility(View.VISIBLE);
                    isEmailOK = true;
                } else {
                    imgCheckEmail.setVisibility(View.INVISIBLE);
                    isEmailOK = false;
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };


        editNewEmail.addTextChangedListener(editEmailWatcher);

        //확인버튼
        btnConfirm = (Button)findViewById(R.id.edit_email_btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이매일 변경 조건이 적합한경우
                if(isEmailOK == true) {
                    String newEmail = editNewEmail.getText().toString();
                    editEmailRequest(newEmail);

                } else {
                    txtEmailErrorMessage.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    public void editEmailRequest(final String newEmail) {

        String endPoint = "/users/user/email";

        JSONObject obj = new JSONObject();
        try {
            obj.put("email", newEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");


                            if (responseCode == 200) {
                                Toast.makeText(EditEmail.this, getResources().getString(R.string.modify_email_toast_complited),
                                        Toast.LENGTH_LONG).show();
                                GlobalSharedPreference.setAppPreferences(EditEmail.this, "email", newEmail);
                                finish();
                            }
                            else if(responseCode == 404) {

                            }
                            else if(responseCode == 401) {
                                Toast.makeText(EditEmail.this, getResources().getString(R.string.modify_password_toast_wrong),
                                        Toast.LENGTH_LONG).show();
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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(EditEmail.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }

    }
}
