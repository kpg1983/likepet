package com.likelab.likepet.global;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by kpg1983 on 2015-12-23.
 */
public class BackPressCloseHandler {


    //뒤로가기 버튼 연속으로 2번 누르면 앱 종료
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity,
                GlobalVariable.backKeyPressed, Toast.LENGTH_SHORT);
        toast.show();
    }
}
