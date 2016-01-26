package com.likelab.likepet.notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kpg1983 on 2015-10-29.
 */
public class NoticeContentsAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    int layout;
    ArrayList<NoticeContents> contentsArrayList;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    RequestQueue queue = AppController.getInstance().getRequestQueue();

    public NoticeContentsAdapter (Context context, int layout, ArrayList<NoticeContents> noticeContentsArrayList) {

        mContext = context;
        this.layout = layout;
        contentsArrayList = noticeContentsArrayList;
        inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
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
    public NoticeContents getItem(int position) {
        return contentsArrayList.get(position);
    }

    class ViewHolder {
        TextView txtTitle;
        TextView txtContents;
        ImageView imgContents;
        TextView txtDate;

        RelativeLayout noticeContents;

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View itemLayout = convertView;
        ViewHolder viewHolder = new ViewHolder();

        if(itemLayout == null) {
            itemLayout = inflater.inflate(layout, null);

            viewHolder.txtTitle = (TextView)itemLayout.findViewById(R.id.notice_txt_title);
            viewHolder.txtContents = (TextView)itemLayout.findViewById(R.id.notice_txt_contents);
            viewHolder.imgContents = (ImageView)itemLayout.findViewById(R.id.notice_img_contents);
            viewHolder.noticeContents = (RelativeLayout)itemLayout.findViewById(R.id.notice_contents);
            viewHolder.txtDate = (TextView)itemLayout.findViewById(R.id.notice_txt_date);

            itemLayout.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)itemLayout.getTag();
        }

        final ImageButton btnNotice = (ImageButton)itemLayout.findViewById(R.id.notice_btn);

        viewHolder.txtTitle.setText(contentsArrayList.get(position).title);
        viewHolder.txtContents.setText(contentsArrayList.get(position).description);

        btnNotice.setImageResource(R.drawable.more_btn_05_02);
        viewHolder.txtDate.setText(contentsArrayList.get(position).registryDate);

        final RelativeLayout noticeContentsContainer = (RelativeLayout)itemLayout.findViewById(R.id.notice_contents);

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (contentsArrayList.get(position).flag == 0) {
                    noticeContentsContainer.setVisibility(View.VISIBLE);
                    btnNotice.setImageResource(R.drawable.more_btn_05_01);
                    contentsArrayList.get(position).flag = 1;
                    readNoticeRequest(contentsArrayList.get(position).noticeId);

                } else if (contentsArrayList.get(position).flag == 1) {
                    noticeContentsContainer.setVisibility(View.GONE);
                    contentsArrayList.get(position).flag = 0;
                    btnNotice.setImageResource(R.drawable.more_btn_05_02);

                }

            }

        });

        //공지사항의 세부 내용을 터치했을때 컨텐츠가 닫히지 않도록 이벤트를 막아 놓는다.
        noticeContentsContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        btnNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contentsArrayList.get(position).flag == 0) {
                    noticeContentsContainer.setVisibility(View.VISIBLE);
                    btnNotice.setImageResource(R.drawable.more_btn_05_01);
                    contentsArrayList.get(position).flag = 1;
                    readNoticeRequest(contentsArrayList.get(position).noticeId);

                } else if (contentsArrayList.get(position).flag == 1) {
                    noticeContentsContainer.setVisibility(View.GONE);
                    contentsArrayList.get(position).flag = 0;
                    btnNotice.setImageResource(R.drawable.more_btn_05_02);

                }
            }
        });

        return itemLayout;
    }

    public void readNoticeRequest(String noticeId) {

        String endPoint = "/notice/" + noticeId + "/read";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                            }

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(JoinMemberBeginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println(error.toString());
                    }


                });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

}
