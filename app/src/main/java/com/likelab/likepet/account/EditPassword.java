package com.likelab.likepet.account;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kpg1983 on 2015-10-29.
 */
public class EditPassword extends Activity{

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private Button btnConfirm;

    private EditText editNewPassword;
    private ImageView imgCheckPassword;

    private EditText editOldPassword;

    private RequestQueue queue;
    private final String detail_info_request_url = "http://54.169.212.108:8080";
    private String sid;

    private TextView txtPasswordErrorMessage;
    private TextView txtPasswordErrorMessageSpecialCharacter;

    boolean isPasswordOK = false;

    @Override
    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.edit_password_activity);

        sid = getAppPreferences(EditPassword.this, "sid");
        queue = AppController.getInstance().getRequestQueue();

        txtPasswordErrorMessage = (TextView)findViewById(R.id.edit_password_txt_error_message_password);
        txtPasswordErrorMessageSpecialCharacter = (TextView)findViewById(R.id.edit_password_txt_error_message_password_special_character);

        cancelContainer = (RelativeLayout)findViewById(R.id.edit_password_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.edit_password_btn_cancel);
        imgCheckPassword = (ImageView)findViewById(R.id.edit_password_img_check);

        editOldPassword = (EditText)findViewById(R.id.edit_password_edit_setting_before_password);

        btnConfirm = (Button)findViewById(R.id.edit_password_btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editOldPassword.getText().toString())) {
                    Toast.makeText(EditPassword.this, getResources().getString(R.string.modify_password_toast_current), Toast.LENGTH_LONG).show();
                    return;

                } else {
                    if(isPasswordOK == true) {
                        editPasswordRequest();
                    } else  {

                        if(editNewPassword.getText().length() < 6) {
                            txtPasswordErrorMessage.setVisibility(View.VISIBLE);

                        } else if(!isPasswordValidate(editNewPassword.getText().toString())) {
                            txtPasswordErrorMessageSpecialCharacter.setVisibility(View.VISIBLE);
                        }
                    }
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

        editNewPassword = (EditText)findViewById(R.id.edit_password_edit_setting_new_password);

        final TextWatcher editPasswordWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                txtPasswordErrorMessage.setVisibility(View.INVISIBLE);
                txtPasswordErrorMessageSpecialCharacter.setVisibility(View.INVISIBLE);

                String tempPassword = editNewPassword.getText().toString();
                int passwordLength = tempPassword.length();

                if(isPasswordValidate(editNewPassword.getText().toString()) && passwordLength > 5 && passwordLength < 21) {
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

        editNewPassword.addTextChangedListener(editPasswordWatcher);

    }

    private static final String PasswordPATTERN = "^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{6,20}$";

    public boolean isPasswordValidate(final String hex) {

        Pattern pattern = Pattern.compile(PasswordPATTERN);

        Matcher matcher = pattern.matcher(hex);

        return matcher.matches();

    }

    public void editPasswordRequest() {

        String endPoint = "/users/user/password";

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JSONObject obj = new JSONObject();
        try {
            obj.put("oldPassword", editOldPassword.getText().toString());
            obj.put("newPassword", editNewPassword.getText().toString());
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
                                Toast.makeText(EditPassword.this, getResources().getString(R.string.modify_password_toast_complited),
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else if(responseCode == 404) {

                            }
                            else if(responseCode == 401) {
                                Toast.makeText(EditPassword.this, getResources().getString(R.string.modify_password_toast_wrong),
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
                params.put("sessionId", sid);

                return params;

            }

        };
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
