package com.likelab.likepet.notification;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.likelab.likepet.CircleTransform;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.yourPage.YourPageActivity;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class AlarmContentsAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<AlarmContents> contentsArrayList;
    int layout;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    //멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

    public AlarmContentsAdapter(Context mContext, int mLayout, ArrayList<AlarmContents> contentsArray) {
        context = mContext;
        inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        contentsArrayList = contentsArray;
        layout = mLayout;
    }

    //onDestory에서 쉽게 해제할 수 있도록 메소드 생성
    public void recycle() {

        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());
        }
    }

    @Override
    public int getCount() {
        return contentsArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public AlarmContents getItem(int position) {

        return contentsArrayList.get(position);
    }

    class ViewHolder {

        TextView txtDate;
        TextView txtContents;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View itemLayout = convertView;
        ViewHolder viewHolder = new ViewHolder();

        if(itemLayout == null) {

            itemLayout = inflater.inflate(layout, null);

            viewHolder.txtDate = (TextView)itemLayout.findViewById(R.id.alarm_date);
            viewHolder.txtContents = (TextView)itemLayout.findViewById(R.id.alarm_txt_contents);

            itemLayout.setTag(viewHolder);

        }

        else  {
            viewHolder = (ViewHolder)itemLayout.getTag();
        }

        final ImageView imgProfileImage = (ImageView)itemLayout.findViewById(R.id.alarm_img_userProfile);

        if(contentsArrayList.get(position).clan.equals("0")) {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_dog).resize(120, 120).transform(new CircleTransform()).
                    into(imgProfileImage);

        } else if(contentsArrayList.get(position).clan.equals("1")) {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_cat).resize(120, 120).transform(new CircleTransform()).
                    into(imgProfileImage);

        } else {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_human).resize(120, 120).transform(new CircleTransform()).
                    into(imgProfileImage);
        }

        String actUserName = contentsArrayList.get(position).actUserName;
        String userName = GlobalSharedPreference.getAppPreferences(context, "name");

        String message = context.getResources().getString(R.string.notification_like_moment);

        if(contentsArrayList.get(position).notifyType.equals("0")) {
            viewHolder.txtContents.setText( String.format(context.getResources().getString(R.string.notification_like_moment), actUserName, userName));
        } else if(contentsArrayList.get(position).notifyType.equals("1")) {

            viewHolder.txtContents.setText(String.format(context.getResources().getString(R.string.notification_tell_moment), actUserName, userName));
        } else if(contentsArrayList.get(position).notifyType.equals("2")) {

            viewHolder.txtContents.setText(String.format(context.getResources().getString(R.string.notification_like_comment), actUserName, userName));
        }


        imgProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourPageActivity.class);
                intent.putExtra("USER_ID", contentsArrayList.get(position).actUserId);
                intent.putExtra("NAME", contentsArrayList.get(position).actUserName);
                intent.putExtra("CLAN", contentsArrayList.get(position).clan);
                intent.putExtra("PROFILE_IMAGE", contentsArrayList.get(position).profileImageUrl);

                context.startActivity(intent);
            }
        });

        viewHolder.txtDate.setText(contentsArrayList.get(position).registryDate);

        //메모리 해제할 View를 추가
        mRecycleList.add(new WeakReference<View>(imgProfileImage));

        return itemLayout;
    }
}
