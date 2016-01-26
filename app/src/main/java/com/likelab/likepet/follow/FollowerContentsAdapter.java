package com.likelab.likepet.follow;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.CircleTransform;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.yourPage.YourPageActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-11-06.
 */
public class FollowerContentsAdapter extends BaseAdapter{

    Context context;
    LayoutInflater inflater;
    ArrayList<FollowingContents> contentsArrayList;
    int layout;

    ImageLoader imageLoader;
    RequestQueue queue = AppController.getInstance().getRequestQueue();
//멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();


    public FollowerContentsAdapter (Context mContext,int mLayout, ArrayList<FollowingContents> contentsArrayList) {

        context = mContext;
        inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        this.contentsArrayList = contentsArrayList;
        layout = mLayout;
        imageLoader = AppController.getInstance().getImageLoader();

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
    public FollowingContents getItem(int position) {
        return contentsArrayList.get(position);
    }

    class ViewHolder {

        TextView txtLikeUserName;
        ImageView imgClan;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View itemLayout = convertView;

        ViewHolder viewHolder = new ViewHolder();

        if(itemLayout == null) {

            itemLayout = inflater.inflate(layout, null);

            viewHolder.txtLikeUserName = (TextView)itemLayout.findViewById(R.id.following_user_txt_name);
            viewHolder.imgClan = (ImageView)itemLayout.findViewById(R.id.following_img_clan);


            itemLayout.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder)itemLayout.getTag();
        }

        final ImageView imgLikeUserProfileImage = (ImageView)itemLayout.findViewById(R.id.following_user_img_profile);
        final ImageButton btnFriendAdd = (ImageButton)itemLayout.findViewById(R.id.following_user_img_add);

        if(contentsArrayList.get(position).clan.equals("0")) {

            Picasso.with(context).load(contentsArrayList.get(position).userProfileImage).
                    placeholder(R.drawable.more_img_06_01_dog).resize(120, 120).transform(new CircleTransform()).
                    into(imgLikeUserProfileImage);

        } else if(contentsArrayList.get(position).clan.equals("1")) {

            Picasso.with(context).load(contentsArrayList.get(position).userProfileImage).
                    placeholder(R.drawable.more_img_06_01_cat).resize(120, 120).transform(new CircleTransform()).
                    into(imgLikeUserProfileImage);

        } else {

            Picasso.with(context).load(contentsArrayList.get(position).userProfileImage).
                    placeholder(R.drawable.more_img_06_01_human).resize(120, 120).transform(new CircleTransform()).
                    into(imgLikeUserProfileImage);
        }


        /*
        //유저의 프로필 이미지 설정
        imageLoader.get(contentsArrayList.get(position).userProfileImage, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                if (response.getBitmap() != null) {
                    //imgLikeUserProfileImage.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                    imgLikeUserProfileImage.setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 1));
                } else {
                    if (contentsArrayList.get(position).clan.equals("0")) {
                        imgLikeUserProfileImage.setImageResource(R.drawable.more_img_06_01_dog);
                    } else if (contentsArrayList.get(position).clan.equals("1")) {
                        imgLikeUserProfileImage.setImageResource(R.drawable.more_img_06_01_cat);
                    } else if (contentsArrayList.get(position).clan.equals("2")) {
                        imgLikeUserProfileImage.setImageResource(R.drawable.more_img_06_01_human);
                    }
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        */

        imgLikeUserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourPageActivity.class);
                intent.putExtra("USER_ID", contentsArrayList.get(position).userId);
                intent.putExtra("NAME", contentsArrayList.get(position).userName);
                intent.putExtra("CLAN", contentsArrayList.get(position).clan);
                intent.putExtra("PROFILE_IMAGE", contentsArrayList.get(position).userProfileImage);

                context.startActivity(intent);
            }
        });

        viewHolder.txtLikeUserName.setText(contentsArrayList.get(position).userName);

        if(contentsArrayList.get(position).clan.equals("0")) {
            viewHolder.imgClan.setImageResource(R.drawable.mypage_img_02);
        } else if(contentsArrayList.get(position).clan.equals("1")) {
            viewHolder.imgClan.setImageResource(R.drawable.mypage_img_01);
        } else if(contentsArrayList.get(position).clan.equals("2")) {
            viewHolder.imgClan.setImageResource(R.drawable.mypage_img_03);
        } else {

        }

    //로그인 상태가 아닐경우 친구 추가 기능이 필요 없다
        if(!GlobalSharedPreference.getAppPreferences(context, "login").equals("login")) {
            btnFriendAdd.setVisibility(View.INVISIBLE);
        }


        if(GlobalSharedPreference.getAppPreferences(context, "userId").equals(contentsArrayList.get(position).userId)) {
            btnFriendAdd.setVisibility(View.INVISIBLE);
        } else {
            btnFriendAdd.setVisibility(View.VISIBLE);

            if(contentsArrayList.get(position).crossFollow.equals("1")) {
                btnFriendAdd.setImageResource(R.drawable.view_img_like_ok);
            } else {
                btnFriendAdd.setImageResource(R.drawable.view_img_like_plus);
            }
        }

        btnFriendAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (contentsArrayList.get(position).crossFollow.equals("1")) {
                    contentsArrayList.get(position).crossFollow = "0";
                    btnFriendAdd.setImageResource(R.drawable.view_img_like_plus);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String endPoint = "/follow/following";
                                String parameter = "followingUserId=" + contentsArrayList.get(position).userId;

                                URL url = new URL(GlobalUrl.BASE_URL + endPoint + "?" + parameter);

                                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                                httpCon.setDoOutput(true);
                                httpCon.setRequestProperty(
                                        "Content-Type", "application/x-www-form-urlencoded");
                                httpCon.setRequestMethod("DELETE");
                                httpCon.setRequestProperty("charset", "utf-8");
                                if (GlobalSharedPreference.getAppPreferences(context, "login").equals("login")) {
                                    httpCon.setRequestProperty("sessionId", GlobalSharedPreference.getAppPreferences(context, "sid"));
                                }
                                //httpCon.setRequestProperty("followingUserId", contentsArrayList.get(position).userId);

                                httpCon.connect();

                                int responseCode = httpCon.getResponseCode();

                                Log.d("responseCode", Integer.toString(responseCode));

                                if (responseCode == 200) {
                                    //Toast.makeText(context, "팔로잉 삭제", Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                } else {
                    contentsArrayList.get(position).crossFollow = "1";
                    btnFriendAdd.setImageResource(R.drawable.view_img_like_ok);
                    addFollowingRequest(contentsArrayList.get(position).userId);
                }


            }
        });

        //메모리 해제할 View를 추가
        mRecycleList.add(new WeakReference<View>(viewHolder.imgClan));
        mRecycleList.add(new WeakReference<View>(imgLikeUserProfileImage));
        mRecycleList.add(new WeakReference<View>(btnFriendAdd));


        return itemLayout;
    }

    public void addFollowingRequest(String userId) {

        String endPoint = "/follow/following";
        JSONObject obj = new JSONObject();

        try {
            obj.put("followingUserId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode=0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                //Toast.makeText(context, "팔로잉 추가", Toast.LENGTH_SHORT).show();

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
                if (GlobalSharedPreference.getAppPreferences(context, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(context, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
}
