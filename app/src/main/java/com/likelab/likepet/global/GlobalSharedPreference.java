package com.likelab.likepet.global;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kpg1983 on 2015-11-16.
 */
public class GlobalSharedPreference {

    public static void setAppPreferences(Context context, String key, String value)
    {
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("sid", 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(key, value);

        prefEditor.commit();
    }

    public static String getAppPreferences(Context context, String key)
    {
        String returnValue = null;
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("sid", 0);
        returnValue = pref.getString(key, "");

        return returnValue;
    }


    public static void deleteAppPreferences(Context context, String key) {
        SharedPreferences.Editor pref;
        pref = context.getSharedPreferences("sid", Context.MODE_PRIVATE).edit();
        pref.remove(key);
        pref.commit();

    }
}
