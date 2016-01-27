package com.likelab.likepet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kpg1983 on 2016-01-27.
 */
public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {


            wasScreenOn = false;
        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            wasScreenOn = true;
        }
    }
}
