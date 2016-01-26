package com.likelab.likepet.more;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.likelab.likepet.R;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;

/**
 * Created by kpg1983 on 2015-11-05.
 */
public class UpdateActivity extends Activity {

    private RelativeLayout btnCancel;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    ImageView imgUpdate;
    TextView txtOldVersion;
    TextView txtNewVersion;

    Button btnUpdate;
    boolean updateFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);

        updateFlag = false;

        imgUpdate = (ImageView)findViewById(R.id.update_img_contents);
        txtOldVersion = (TextView)findViewById(R.id.update_txt_current_version);
        txtNewVersion = (TextView)findViewById(R.id.update_txt_latest_version);

        btnUpdate = (Button)findViewById(R.id.update_btn_update);
        Intent intent = getIntent();
        String oldVersion = intent.getStringExtra("OLD_VERSION");
        String newVersion = intent.getStringExtra("NEW_VERSION");
        String updateImageUrl = intent.getStringExtra("UPDATE_IMAGE_URL");

        txtNewVersion.setText(newVersion);
        txtOldVersion.setText(oldVersion);

        if(newVersion != null && oldVersion != null) {
            if (!newVersion.equals(oldVersion)) {

                updateFlag = true;

                if (updateImageUrl != null)
                {
                    imageLoader.get(updateImageUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                            if (response.getBitmap() != null) {
                                imgUpdate.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
                                imgUpdate.setImageBitmap(response.getBitmap());

                            } else {
                               imgUpdate.setImageResource(R.drawable.setup_img_update_end);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                }
                else {
                    imgUpdate.setImageResource(R.drawable.setup_img_update);

                }

                btnUpdate.setBackgroundColor(Color.parseColor("#f7c243"));
            }
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //업데이트 변경 사항이 있을 경우우
                if (updateFlag) {

                    Uri uriUrl = Uri.parse("https://play.google.com/store/apps/details?id=com.likelab.likepet");
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
           }
        });

        btnCancel = (RelativeLayout) findViewById(R.id.update_cancel_container);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RecycleUtils.recursiveRecycle(imgUpdate);
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }
}
