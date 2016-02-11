package com.likelab.likepet.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.volleryCustom.AppController;

/**
 * Created by kpg1983 on 2015-10-28.
 */
public class AccountSetting extends Activity{

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private ImageButton btnEditEmail;
    private ImageButton btnEditPassword;

    private ImageButton btnFacebook;
    private ImageButton btnTwitter;
    private ImageButton btnGoogle;

    private Button btnLogout;

    private TextView txtEmail;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    protected void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.account_setting_activity);

        cancelContainer = (RelativeLayout)findViewById(R.id.account_setting_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.account_setting_btn_cancel);
        btnEditEmail = (ImageButton)findViewById(R.id.account_setting_btn_email);
        btnEditPassword = (ImageButton)findViewById(R.id.account_setting_btn_password);

        btnFacebook = (ImageButton)findViewById(R.id.account_setting_btn_facebook);
        btnTwitter = (ImageButton)findViewById(R.id.account_setting_btn_twitter);
        btnGoogle = (ImageButton)findViewById(R.id.account_setting_btn_google);

        txtEmail = (TextView)findViewById(R.id.account_setting_edit_email);

        btnLogout =(Button)findViewById(R.id.account_btn_logout);

        if(GlobalSharedPreference.getAppPreferences(this, "linkageFacebook").equals("1")) {
            btnFacebook.setImageResource(R.drawable.more_img_06_05);
        }

        if(GlobalSharedPreference.getAppPreferences(this, "linkageTwitter").equals("1")) {
            btnTwitter.setImageResource(R.drawable.more_img_06_06);
        }

        if(GlobalSharedPreference.getAppPreferences(this, "linkageGoogle").equals("1")) {
            btnGoogle.setImageResource(R.drawable.more_img_06_07);
        }

        if(GlobalSharedPreference.getAppPreferences(this, "linkageFacebook").equals("1") ||
                GlobalSharedPreference.getAppPreferences(this, "linkageTwitter").equals("1") ||
                GlobalSharedPreference.getAppPreferences(this, "linkageGoogle").equals("1")) {
            btnEditEmail.setVisibility(View.GONE);
            btnEditPassword.setVisibility(View.GONE);
            btnEditPassword.setEnabled(false);
            btnEditEmail.setEnabled(false);
        }

        //로그아웃
        //기기에 저장되어 있는 사용자의 정보를 삭제한다
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "userId");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "name");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "email");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "gender");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "clan");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "status");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "parentUserId");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "profileImageUrl");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "national");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "ownerType");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "birthday");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "registryDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "modifyDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "termServiceDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "privacyTermDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "withdrawReqDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "withdrawDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "mailAuthConfirmDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "lastLoginDate");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "mailAuth");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "useNoticeAddedFriend");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "useNoticeReply");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "useNoticeSystem");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "sid");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "linkageFacebook");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "linkageTwitter");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "linkageGoogle");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "accountId");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "password");
                GlobalSharedPreference.deleteAppPreferences(AccountSetting.this, "loginType");

                GlobalSharedPreference.setAppPreferences(AccountSetting.this, "login", "logout");

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
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

        btnEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AccountSetting.this, EditEmail.class);
                startActivity(intent);
            }
        });

        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AccountSetting.this, EditPassword.class);
                startActivity(intent);

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Account";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        txtEmail.setText(GlobalSharedPreference.getAppPreferences(this, "email"));
    }
}
