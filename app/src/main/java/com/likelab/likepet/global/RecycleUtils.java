package com.likelab.likepet.global;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by kpg1983 on 2016-01-08.
 */
public class RecycleUtils {

    private RecycleUtils(){};

    public static void recursiveRecycle(View root) {

        if (root == null)

            return;

        root.setBackgroundDrawable(null);

        if (root instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) root;

            int count = group.getChildCount();

            for (int i = 0; i < count; i++) {

                recursiveRecycle(group.getChildAt(i));

            }

            if (!(root instanceof AdapterView)) {

                group.removeAllViews();

            }

        }


        if (root instanceof ImageView) {

            ((ImageView)root).setImageDrawable(null);

        }

        root = null;

        return;

    }

    public static void recursiveRecycle(List<WeakReference<View>> recycleList) {

        for (WeakReference<View> ref : recycleList)

            recursiveRecycle(ref.get());

    }


}