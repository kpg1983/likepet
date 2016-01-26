package com.likelab.likepet.more;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.likelab.likepet.R;
import com.likelab.likepet.account.Account;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.notice.Notice;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;

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

                if(GlobalSharedPreference.getAppPreferences(MoreActivity.this, "login").equals("login"))
                {
                    Intent intent = new Intent(MoreActivity.this, UserProfile.class);
                    startActivity(intent);
                } else {
                    loginPopupRequest(v);
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
                if (GlobalSharedPreference.getAppPreferences(MoreActivity.this, "login").equals("login")) {
                    Intent intent = new Intent(MoreActivity.this, Account.class);
                    startActivityForResult(intent, REQ_LOGOUT);
                } else {
                    loginPopupRequest(v);
                }
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

    private void loginPopupRequest(View v) {


        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) MoreActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.recommend_join_member_popup_windown, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        overlay.setVisibility(View.VISIBLE);

        RelativeLayout joinLater = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_later_container);
        RelativeLayout joinNow = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_now_container);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);
            }
        });

        joinLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        joinNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(MoreActivity.this, JoinMemberBeginActivity.class);
                finish();
                MoreActivity.this.startActivity(intent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_LOGOUT) {
            if(resultCode == RESULT_OK) {
                Log.d("MoreActivity", "로그아웃");
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
