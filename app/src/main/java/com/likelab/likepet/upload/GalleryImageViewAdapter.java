package com.likelab.likepet.upload;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.likelab.likepet.R;
import com.likelab.likepet.global.RecycleUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GalleryImageViewAdapter extends BaseAdapter {
    private Handler mHandler;

    private Context mContext;
    private ArrayList<UploadGalleryMediaItem> mediaItemsArrayList;
    private DisplayMetrics dm;
    private LayoutInflater inflater;
    private float scale;

    //멤버변수로 해제할 Set을 생성

    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();


    public GalleryImageViewAdapter(Context context, ArrayList<UploadGalleryMediaItem> _mediaItemsArrayList) {
        mContext = context;
        mHandler = new Handler();
        mediaItemsArrayList = _mediaItemsArrayList;
        dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
        inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        scale = mContext.getResources().getDisplayMetrics().density;
    }

    public void addMediaItem(UploadGalleryMediaItem mediaItem) {

    }

    //onDestory에서 쉽게 해제할 수 있도록 메소드 생성

    public void recycle() {

        for (WeakReference<View> ref : mRecycleList) {

            RecycleUtils.recursiveRecycle(ref.get());

        }

    }


    public int getCount() {
        return mediaItemsArrayList.size();
    }

    public Object getItem(int position) {
        return mediaItemsArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemLayoutView;

        if (convertView == null) {
            itemLayoutView  = inflater.inflate(R.layout.upload_gallery_gridview_item, parent, false);
            int width = dm.widthPixels;
            itemLayoutView.setLayoutParams(new GridView.LayoutParams( (width / 4) - (int)(1 * scale + 0.5f), width / 4));
        } else {
            itemLayoutView = (View) convertView;
        }

        if(position < mediaItemsArrayList.size()){
            UploadGalleryMediaItem item = mediaItemsArrayList.get(position);
            ImageView imageView = (ImageView) itemLayoutView.findViewById(R.id.upload_gallery_gridview_item_imageview);
            imageView.setImageBitmap(mediaItemsArrayList.get(position).getThumbnailBitmap());
            TextView durationTextView = (TextView) itemLayoutView.findViewById(R.id.upload_gallery_gridview_item_video_duration_label);
            View backgroundView = itemLayoutView.findViewById(R.id.upload_gallery_gridview_item_selected_view);

            if (mediaItemsArrayList.get(position).getIsSelected()){
                backgroundView.setVisibility(View.VISIBLE);
            }else{
                backgroundView.setVisibility(View.INVISIBLE);
            }

            if (item.getContentsType() == 1 ){ // if this content is video
                durationTextView.setText(mediaItemsArrayList.get(position).getDurationString());
            }else{
                durationTextView.setText("");
            }

            mRecycleList.add(new WeakReference<View>(imageView));
        }




        return itemLayoutView;
    }
}