package com.likelab.likepet.upload;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.likelab.likepet.upload.UploadGallery;
import com.likelab.likepet.upload.UploadPhoto;

/**
 * Created by kpg1983 on 2015-09-24.
 */
public class UploadPagerAdapter extends FragmentStatePagerAdapter{

    public UploadPagerAdapter(FragmentManager fm) {

        super(fm);

    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return new UploadGallery();
            }
            case 1: {
                return new UploadPhoto();
            }

            default: {
                return null;
            }

        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position >= getCount()) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }

    }


    @Override
    public int getCount() {
        return 2;
    }
}
