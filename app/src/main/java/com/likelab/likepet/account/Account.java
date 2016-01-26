package com.likelab.likepet.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.likelab.likepet.R;

/**
 * Created by kpg1983 on 2015-10-28.
 */
public class Account extends Activity{

    private RelativeLayout cancelContainer;
    private ImageButton btnCancel;

    private RelativeLayout myAccountContainer;
    private RelativeLayout inviteFriendContainer;

    private static int REQ_LOGOUT = 5;

    private TextView txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        txtEmail = (TextView)findViewById(R.id.account_setting_edit_email);
        //txtEmail.setText(GlobalSharedPreference.getAppPreferences(this, "email"));

        myAccountContainer = (RelativeLayout)findViewById(R.id.account_my_account_container);
        inviteFriendContainer = (RelativeLayout)findViewById(R.id.account_invite_friend_container);

        inviteFriendContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContents(Account.this, "Here comes LikePet http://www.likelab.co.kr/share.php");
            }
        });

        myAccountContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Account.this, AccountSetting.class);
                startActivityForResult(intent, REQ_LOGOUT);
            }
        });

        cancelContainer = (RelativeLayout)findViewById(R.id.account_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCancel = (ImageButton)findViewById(R.id.account_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    public void shareContents(Context context, String content) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public void shareContentsImage(Context context, String imageUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUrl));
        context.startActivity(sendIntent);
    }
}
