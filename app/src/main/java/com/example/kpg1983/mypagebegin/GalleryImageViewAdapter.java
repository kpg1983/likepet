package com.example.kpg1983.mypagebegin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.kpg1983.mypagebegin.UploadGallery;

import java.util.ArrayList;

public class GalleryImageViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<UploadGallery.LoadedImage> photos = new ArrayList<UploadGallery.LoadedImage>();

    public GalleryImageViewAdapter(Context context) {
        mContext = context;
    }

    public void addPhoto(UploadGallery.LoadedImage photo) {
        photos.add(photo);
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int position) {
        return photos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new GridView.LayoutParams(270, 270));
        imageView.setImageBitmap(photos.get(position).getBitmap());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }
}