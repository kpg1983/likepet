package com.likelab.likepet.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.likelab.likepet.R;
import com.likelab.likepet.account.Account;
import com.likelab.likepet.notice.Notice;

/**
 * Created by kpg1983 on 2015-10-23.
 */
public class MoreActivity extends Activity {

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private RelativeLayout profileContainer;
    private RelativeLayout settingContainer;
    private RelativeLayout noticeContainer;
    private RelativeLayout accountContainer;

    private  RelativeLayout overlay;

    private static int REQ_LOGOUT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.more_activity);

        cancelContainer = (RelativeLayout)findViewById(R.id.more_cancel_container);
        btnCancel = (ImageButton)findViewById(R.id.more_btn_cancel);

        profileContainer = (RelativeLayout)findViewById(R.id.more_profile_container);
        settingContainer = (RelativeLayout)findViewById(R.id.more_setting_container);
        noticeContainer = (RelativeLayout)findViewById(R.id.more_notice_container);
        accountContainer = (RelativeLayout)findViewById(R.id.more_account_container);
        overlay = (RelativeLayout)findViewById(R.id.more_overlay);

        profileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    Intent intent = new Intent(MoreActivity.this, UserProfile.class);
                    startActivity(intent);

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

        settingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MoreActivity.this, Setting.class);
                startActivity(intent);

            }
        });

        accountContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MoreActivity.this, Account.class);
                startActivityForResult(intent, REQ_LOGOUT);

            }
        });

        noticeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreActivity.this, Notice.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_LOGOUT) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
