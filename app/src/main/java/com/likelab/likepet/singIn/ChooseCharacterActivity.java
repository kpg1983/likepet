package com.likelab.likepet.singIn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.volleryCustom.AppController;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class ChooseCharacterActivity extends Activity {

    private RelativeLayout cancelContainer;

    private ImageButton btnDog;
    private ImageButton btnCat;
    private ImageButton btnHuman;

    private ImageView imgDog;
    private ImageView imgCat;
    private ImageView imgHuman;

    private Button btnNext;

    int socialFlag=0;

    String userId;
    String email;
    String clan = "0";
    String socialType;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_member_choose_character);

        SignInFlowActivityList.activityArrayList.add(this);

        Intent intent = getIntent();

        if(intent.hasExtra("SOCIAL")) {
            if(intent.getExtras().getInt("SOCIAL") == 1) {
                socialFlag = 1;
                userId = intent.getExtras().getString("ID");
                email = intent.getExtras().getString("EMAIL");
                socialType = intent.getExtras().getString("SOCIAL_TYPE");
            }
        }

        cancelContainer = (RelativeLayout)findViewById(R.id.join_member_choose_character_cancel_container);
        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDog = (ImageButton)findViewById(R.id.join_member_choose_btn_dog);
        btnCat = (ImageButton)findViewById(R.id.join_member_choose_btn_cat);
        btnHuman = (ImageButton)findViewById(R.id.join_member_choose_btn_man);

        imgHuman = (ImageView)findViewById(R.id.join_member_choose_img_check_man);
        imgDog = (ImageView)findViewById(R.id.join_member_choose_img_check_dog);
        imgCat = (ImageView)findViewById(R.id.join_member_choose_img_check_cat);

        btnNext = (Button)findViewById(R.id.join_member_choose_character_btn_next);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChooseCharacterActivity.this, InsertUserNameActivity.class);

                if(socialFlag == 1) {
                    intent.putExtra("SOCIAL", socialFlag);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("ID", userId);
                    intent.putExtra("CLAN", clan);
                    intent.putExtra("SOCIAL_TYPE", socialType);
                } else {
                    intent.putExtra("CLAN", clan);
                }
                startActivity(intent);
            }
        });

        btnDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgDog.setVisibility(View.VISIBLE);
                imgCat.setVisibility(View.INVISIBLE);
                imgHuman.setVisibility(View.INVISIBLE);
                SignInUserInformation.character = "dog";
                clan = "0";
            }
        });

        btnCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgDog.setVisibility(View.INVISIBLE);
                imgCat.setVisibility(View.VISIBLE);
                imgHuman.setVisibility(View.INVISIBLE);
                SignInUserInformation.character = "cat";
                clan = "1";
            }
        });

        btnHuman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgDog.setVisibility(View.INVISIBLE);
                imgCat.setVisibility(View.INVISIBLE);
                imgHuman.setVisibility(View.VISIBLE);
                SignInUserInformation.character = "human";
                clan = "2";
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "setChar";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
