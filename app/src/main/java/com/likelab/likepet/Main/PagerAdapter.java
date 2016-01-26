package com.likelab.likepet.Main;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.likelab.likepet.Feed.Feed;
import com.likelab.likepet.Main.Home;
import com.likelab.likepet.mypage.MyPageMoment;

/**
 * Created by kpg1983 on 2015-09-20.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return new Home();
            }
            case 1: {
                return new Feed();
            }
            case 2: {
                return new MyPageMoment();
            }
            default: {
                return null;
            }

        }
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }



}