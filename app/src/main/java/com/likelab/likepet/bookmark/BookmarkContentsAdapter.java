package com.likelab.likepet.bookmark;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-12-03.
 */
public class BookmarkContentsAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<BookmarkContents> contentsArrayList;
    int layout;

    RequestQueue queue = AppController.getInstance().getRequestQueue();
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    //멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();


    public BookmarkContentsAdapter(Context context, int layout, ArrayList<BookmarkContents> contentsArrayList) {
        this.context = context;
        this.layout = layout;
        this.contentsArrayList = contentsArrayList;
        this.inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
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
    public BookmarkContents getItem(int position) {
        return contentsArrayList.get(position);
    }

    class ViewHolder {
        ImageView imgContents;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null) {
            convertView = inflater.inflate(layout, null);

            viewHolder.imgContents = (ImageView)convertView.findViewById(R.id.bookmark_img_contents);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(contentsArrayList.get(position).contentType.matches(".*image.*")) {
            Picasso.with(context).load(contentsArrayList
                    .get(position).contentUrl)
                    .placeholder(R.drawable.place_holder_960)
                    .resize(320, 320)
                    .centerCrop()
                    .into(viewHolder.imgContents);
        } else {
            Picasso.with(context).load(contentsArrayList
                    .get(position).videoScreenshotUrl)
                    .resize(320, 320)
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_960)
                    .into(viewHolder.imgContents);
        }

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                View layout = inflater.inflate(R.layout.bookmark_activity, null);

                final RelativeLayout overlay = (RelativeLayout)layout.findViewById(R.id.bookmark_overlay);

                overlay.setVisibility(View.VISIBLE);

                final PopupWindow popupWindow = new PopupWindow();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.bookmark_popup_window, null);

                popupWindow.setContentView(popupView);
                popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    public void onDismiss() {
                        overlay.setVisibility(View.INVISIBLE);
                    }

                });

                //팝업 윈도우 위치 조정
                popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

                RelativeLayout deleteContainer = (RelativeLayout)popupView.findViewById(R.id.bookmark_delete_confirm_container);
                RelativeLayout cancelContainer = (RelativeLayout)popupView.findViewById(R.id.bookmark_delete_cancel_container);

                deleteContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteBookmarkRequest(contentsArrayList.get(position).contentId, position);
                        popupWindow.dismiss();
                        overlay.setVisibility(View.INVISIBLE);
                    }
                });

                cancelContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        overlay.setVisibility(View.INVISIBLE);
                    }
                });

                return false;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int contentType;
                int numberOfLike;
                int blackFlag;      //해당 컨텐츠가 신고로 인하여 블락처리 되어있는지 확인한다.
                int numberOfComment;

                numberOfLike = contentsArrayList.get(position).likeCount;

                if (contentsArrayList.get(position).reportCount > 20) {
                    blackFlag = 1;
                } else {
                    blackFlag = 0;
                }

                numberOfLike = contentsArrayList.get(position).likeCount;
                numberOfComment = contentsArrayList.get(position).commentCount;
                String description = contentsArrayList.get(position).description;
                String contentId = contentsArrayList.get(position).contentId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;
                String profileImageUrl = contentsArrayList.get(position).profileImageUrl;
                String userName = contentsArrayList.get(position).writerName;
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;

                Intent intent = new Intent(context, ViewActivity.class);

                if (contentsArrayList.get(position).contentType.matches(".*image.*")) {
                    if (contentsArrayList.get(position).contentType.matches(".*gif.*")) {
                        contentType = 3;
                    } else {
                        contentType = 1;
                    }
                } else {
                    contentType = 2;
                }


                intent.putExtra("CONTENT_ID", contentId);
                intent.putExtra("DESC", description);
                intent.putExtra("TYPE", contentType);
                intent.putExtra("LIKES", numberOfLike);
                intent.putExtra("BLIND_FLAG", blackFlag);
                intent.putExtra("NUMBER_OF_COMMENT", numberOfComment);
                intent.putExtra("IMAGE_URL", contentsArrayList.get(position).contentUrl);
                intent.putExtra("ILIKETHIS", iLikeThis);
                intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl);
                intent.putExtra("NAME", userName);
                intent.putExtra("POSITION", position);
                intent.putExtra("LIKE_COUNT", likeCount);
                intent.putExtra("COMMENT_COUNT", commentCount);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("STATUS", status);
                intent.putExtra("REPORT_COUNT", reportCount);


                context.startActivity(intent);

            }
        });

        //메모리 해제할 View를 추가

        mRecycleList.add(new WeakReference<View>(viewHolder.imgContents));

        return convertView;
    }

    public void deleteBookmarkRequest(String contentId, final int position) {

        String endPoint = "/mypage/favorite/" + contentId;

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                Toast.makeText(context, context.getResources().getString(R.string.bookmark_toast_delete),
                                        Toast.LENGTH_LONG).show();
                                contentsArrayList.remove(position);
                                notifyDataSetChanged();
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


                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(context, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
}
