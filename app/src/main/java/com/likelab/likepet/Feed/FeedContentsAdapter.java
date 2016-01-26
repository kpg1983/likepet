package com.likelab.likepet.Feed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.likelab.likepet.CircleTransform;
import com.likelab.likepet.CommentBtnClickListener;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RoundedAvatarDrawable;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.yourPage.YourPageActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-09-16.
 */
public class FeedContentsAdapter extends BaseAdapter{

    Activity context;
    LayoutInflater inflater;
    ArrayList<FeedContents> contentsArrayList;
    int layout;

    RequestQueue queue;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    MainActivity mainActivity;
    private static final int RESULT_CODE = 1;

    CommentBtnClickListener commentBtnClickListener;

    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();


    public FeedContentsAdapter(Activity mContext, int mLayout, ArrayList<FeedContents> contentsArray, MainActivity mainActivity) {
        context = mContext;
        inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        contentsArrayList = contentsArray;
        layout = mLayout;
        this.mainActivity = mainActivity;

        queue = AppController.getInstance().getRequestQueue();

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
    public FeedContents getItem(int position) {

        return contentsArrayList.get(position);
    }

    class ViewHolder{

        RelativeLayout userProfileContainer;

        ImageView mainContents;

        TextView userName;
        TextView registerTime;
        TextView numberOfComment;

        //댓글 버튼과 공유하기 버튼
        ImageButton btn_share;
        ImageView btn_play;

        RelativeLayout comment_container;
        RelativeLayout userInfoContainer;

        ImageView imgGifIcon;

        TextView txtCaption;

        ImageView imgCommentBtn;


        TextView txtComment[] = new TextView[3];
        ImageView imgCommentDeco[] = new ImageView[3];
        RelativeLayout noCommentContainer[] = new RelativeLayout[3];
        ImageView imgNoComment[] = new ImageView[3];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View itemLayout = convertView;

        ViewHolder viewHolder = new ViewHolder();

        final ImageButton btn_like;

        if(itemLayout == null) {

            itemLayout = inflater.inflate(layout, null);


            viewHolder.userProfileContainer = (RelativeLayout)itemLayout.findViewById(R.id.feed_contents_date_container);
            viewHolder.mainContents = (ImageView) itemLayout.findViewById(R.id.feed_img_main_contents);
            viewHolder.userName = (TextView) itemLayout.findViewById(R.id.feed_txt_profile_name);
            viewHolder.registerTime = (TextView) itemLayout.findViewById(R.id.feed_contents_date);
            viewHolder.numberOfComment = (TextView) itemLayout.findViewById(R.id.feed_number_of_comment);

            //일반 글자 댓글
            viewHolder.txtComment[0] = (TextView) itemLayout.findViewById(R.id.feed_text_comment_1);
            viewHolder.txtComment[1] = (TextView) itemLayout.findViewById(R.id.feed_text_comment_2);
            viewHolder.txtComment[2] = (TextView) itemLayout.findViewById(R.id.feed_text_comment_3);

            //베스트 댓글 상단 장식
            viewHolder.imgCommentDeco[0] = (ImageView) itemLayout.findViewById(R.id.feed_comment_image_01_n);
            viewHolder.imgCommentDeco[1] = (ImageView) itemLayout.findViewById(R.id.feed_comment_image_02_n);
            viewHolder.imgCommentDeco[2] = (ImageView) itemLayout.findViewById(R.id.feed_comment_image_03_n);

            //베댓을 기다리고 있다멍 이미지
            viewHolder.imgNoComment[0] = (ImageView)itemLayout.findViewById(R.id.feed_img_no_comment_1);
            viewHolder.imgNoComment[1] = (ImageView)itemLayout.findViewById(R.id.feed_img_no_comment_2);
            viewHolder.imgNoComment[2] = (ImageView)itemLayout.findViewById(R.id.feed_img_no_comment_3);

            viewHolder.btn_share = (ImageButton)itemLayout.findViewById(R.id.feed_button_share);
            viewHolder.btn_play = (ImageView)itemLayout.findViewById(R.id.feed_img_play);

            viewHolder.comment_container = (RelativeLayout)itemLayout.findViewById(R.id.feed_comment_container);

            //배댓이 없는 나머지 두개를 위한 컨테이너
            viewHolder.noCommentContainer[0] = (RelativeLayout) itemLayout.findViewById(R.id.feed_comment_no_comment_container_1);
            viewHolder.noCommentContainer[1] = (RelativeLayout) itemLayout.findViewById(R.id.feed_comment_no_comment_container_2);
            viewHolder.noCommentContainer[2] = (RelativeLayout) itemLayout.findViewById(R.id.feed_comment_no_comment_container_3);

            viewHolder.userInfoContainer = (RelativeLayout)itemLayout.findViewById(R.id.feed_contents_date_container);
            viewHolder.imgGifIcon = (ImageView)itemLayout.findViewById(R.id.feed_img_gif_icon);
            viewHolder.txtCaption = (TextView)itemLayout.findViewById(R.id.feed_txt_caption);
            viewHolder.imgCommentBtn = (ImageView)itemLayout.findViewById(R.id.feed_button_comment);

            itemLayout.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder)itemLayout.getTag();

        }


        final ImageView imgComment[] = new ImageView[3];
        imgComment[0] = (ImageView) itemLayout.findViewById(R.id.feed_image_comment_1);
        imgComment[1] = (ImageView) itemLayout.findViewById(R.id.feed_image_comment_2);
        imgComment[2] = (ImageView) itemLayout.findViewById(R.id.feed_image_comment_3);

        final ImageView imgBoxLine[] = new ImageView[3];
        imgBoxLine[0] = (ImageView) itemLayout.findViewById(R.id.img_box_line_1);
        imgBoxLine[1] = (ImageView) itemLayout.findViewById(R.id.img_box_line_2);
        imgBoxLine[2] = (ImageView) itemLayout.findViewById(R.id.img_box_line_3);

        final TextView numberOfLike = (TextView) itemLayout.findViewById(R.id.feed_number_of_like);
        final ImageView imgUserProfile = (ImageView)itemLayout.findViewById(R.id.feed_img_profile);

        btn_like = (ImageButton)itemLayout.findViewById(R.id.feed_button_like);

        viewHolder.imgCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewActivity.class);
                intent.putExtra("IS_COMMENT_PRESSED", "ok");

                int contentType;
                int imageContent;
                int numberOfLike;
                int blackFlag;      //해당 컨텐츠가 신고로 인하여 블락처리 되어있는지 확인한다.
                int numberOfComment;

                imageContent = contentsArrayList.get(position).mainContent;
                numberOfLike = contentsArrayList.get(position).likeCount;
                blackFlag = contentsArrayList.get(position).blackFlag;
                numberOfComment = contentsArrayList.get(position).commentCount;
                String description = contentsArrayList.get(position).description;
                String contentId = contentsArrayList.get(position).contentId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;
                String profileImageUrl = contentsArrayList.get(position).profileImageUrl;
                String userName = contentsArrayList.get(position).name;
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;


                if (contentsArrayList.get(position).contentType.matches(".*image.*")) {
                    contentType = 1;
                    if (contentsArrayList.get(position).contentType.matches(".*gif.*")) {
                        contentType = 3;
                    }

                } else {
                    contentType = 2;
                }

                intent.putExtra("CONTENT_ID", contentId);
                intent.putExtra("DESC", description);
                intent.putExtra("TYPE", contentType);
                intent.putExtra("IMAGE", imageContent);
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
                intent.putExtra("REPORT_COUNT", reportCount);
                intent.putExtra("STATUS", status);

                context.startActivityForResult(intent, RESULT_CODE);
            }
        });


        int mediaWidth = contentsArrayList.get(position).mediaWidth;
        int mediaHeight = contentsArrayList.get(position).mediaHeight;

        try {
            //컨텐츠 타입: 비디오, 사진 확인 후 어떤 화면을 뿌려줄지 결정한다
            if (contentsArrayList.get(position).contentType.matches(".*image.*")) {
                Picasso.with(context).load(contentsArrayList.get(position).contentUrl)
                        .placeholder(R.drawable.place_holder_960)
                        .resize(mediaWidth, mediaHeight)
                        .into(viewHolder.mainContents);

            } else if(contentsArrayList.get(position).contentType.matches(".*video.*")){
                Picasso.with(context).load(contentsArrayList.get(position).videoScreenshotUrl)
                        .placeholder(R.drawable.place_holder_960)
                        .into(viewHolder.mainContents);
            }
        }catch (OutOfMemoryError e) {

        }


        //gif 아이콘 표시
        if(contentsArrayList.get(position).contentType.matches(".*gif.*")) {
            viewHolder.imgGifIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgGifIcon.setVisibility(View.INVISIBLE);
            viewHolder.imgGifIcon.setImageDrawable(null);
        }

        String likeCount = Integer.toString(contentsArrayList.get(position).likeCount);
        String commentCount = Integer.toString(contentsArrayList.get(position).commentCount);

        numberOfLike.setText(likeCount);
        viewHolder.numberOfComment.setText(commentCount);

        //한줄 캡션 설정
        String descriptions[] = contentsArrayList.get(position).description.split("\n");
        if(descriptions.length > 1) {
            viewHolder.txtCaption.setText(descriptions[0]);
        } else {
            viewHolder.txtCaption.setText(contentsArrayList.get(position).description);
        }


        viewHolder.userName.setText(contentsArrayList.get(position).name);
        viewHolder.registerTime.setText(contentsArrayList.get(position).registryDate);

        //유저의 프로필 이미지 설정
        //유저의 프로필 이미지 정보가 없는 경우 기본 종족 이미지를 출력한다.
        if(contentsArrayList.get(position).clan.equals("0")) {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_dog).resize(160, 160).transform(new CircleTransform()).
                    into(imgUserProfile);

        } else if(contentsArrayList.get(position).clan.equals("1")) {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_cat).resize(160, 160).transform(new CircleTransform()).
                    into(imgUserProfile);

        } else {

            Picasso.with(context).load(contentsArrayList.get(position).profileImageUrl).
                    placeholder(R.drawable.more_img_06_01_human).resize(160, 160).transform(new CircleTransform()).
                    into(imgUserProfile);
        }

        /*
        //유저의 프로필 이미지 설정
        //유저의 프로필 이미지 정보가 없는 경우 기본 종족 이미지를 출력한다.
        imageLoader.get(contentsArrayList.get(position).profileImageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                if(response.getBitmap() != null) {
                    //imgUserProfile.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                    imgUserProfile.setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 1));
                } else {
                    String clan = contentsArrayList.get(position).clan;
                    if (clan.equals("0")) {
                        imgUserProfile.setImageResource(R.drawable.more_img_06_01_dog);
                    } else if (clan.equals("1")) {
                        imgUserProfile.setImageResource(R.drawable.more_img_06_01_cat);
                    } else if (clan.equals("2")) {
                        imgUserProfile.setImageResource(R.drawable.more_img_06_01_human);
                    }
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        */


        //콘텐츠의 타입을 확인 후 동영상은 플레이 버튼 표시
        if(contentsArrayList.get(position).contentType.matches(".*video.*")) {
            viewHolder.btn_play.setVisibility(View.VISIBLE);
            viewHolder.btn_play.setImageResource(R.drawable.mypage_btn_play);
        } else {
            viewHolder.btn_play.setVisibility(View.INVISIBLE);
            viewHolder.btn_play.setImageDrawable(null);
        }


        //유저의 프로필을 선택하면 상대방의 페이지르 이동한다
        viewHolder.userInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourPageActivity.class);
                String userId = contentsArrayList.get(position).userId;
                String name = contentsArrayList.get(position).name;
                String profileImageUrl = contentsArrayList.get(position).profileImageUrl;
                String clan = contentsArrayList.get(position).clan;

                intent.putExtra("USER_ID", userId);
                intent.putExtra("CLAN", clan);
                intent.putExtra("PROFILE_IMAGE", profileImageUrl);
                intent.putExtra("NAME", name);

                ((Activity) context).startActivity(intent);
            }
        });


        int numberOfBestComment = contentsArrayList.get(position).numberOfBestComment;

        //베스트 댓글 숫자 확인 후 베스트 댓글 갯수만큼 리스트 화면에 뿌려준다
        if(numberOfBestComment > 0) {

            viewHolder.comment_container.setVisibility(View.VISIBLE);
            for (int i = 0; i < numberOfBestComment; i++) {

                viewHolder.noCommentContainer[i].setVisibility(View.INVISIBLE);
                viewHolder.imgNoComment[i].setImageDrawable(null);
                viewHolder.imgCommentDeco[i].setVisibility(View.VISIBLE);

                if(contentsArrayList.get(position).bestCommentUrl[i].contains("http")) {
                    imgBoxLine[i].setVisibility(View.INVISIBLE);
                    imgBoxLine[i].setImageDrawable(null);

                    imgComment[i].setVisibility(View.VISIBLE);

                    final int index = i;    //inner method에서는 final 변수만 사용할 수 있다

                    imageLoader.get(contentsArrayList.get(position).bestCommentUrl[i], new ImageLoader.ImageListener() {

                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                imgComment[index].startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                                imgComment[index].setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 2));
                            } else {
                                imgComment[index].setImageResource(R.drawable.place_holder_960);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //To change body of implemented methods use File | Settings | File Templates.
                        }
                    });
                } else  {
                    imgComment[i].setVisibility(View.INVISIBLE);
                    imgComment[i].setImageBitmap(null);

                    imgBoxLine[i].setVisibility(View.VISIBLE);
                    imgBoxLine[i].setImageResource(R.drawable.img_boxline);
                    viewHolder.txtComment[i].setText(contentsArrayList.get(position).bestCommentDescription[i]);

                }
            }

            for(int i=numberOfBestComment; i<3; i++) {
                viewHolder.noCommentContainer[i].setVisibility(View.VISIBLE);
                imgComment[i].setVisibility(View.INVISIBLE);

                if(i == 1) {
                    viewHolder.imgNoComment[i].setImageResource(R.drawable.mypage_comment_no_comment_03);
                } else if (i == 2) {
                    viewHolder.imgNoComment[i].setImageResource(R.drawable.mypage_comment_no_comment_01);
                }
            }

        } else {
            //베스트 댓글이 하나도 없는 경우
            for(int i=0; i<3; i++) {
                viewHolder.imgNoComment[i].setImageDrawable(null);
            }
            viewHolder.comment_container.setVisibility(View.GONE);

        }


        //좋아여 클릭 이벤트
        if (contentsArrayList.get(position).iLikeThis.equals("0")) {
            btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_n);
        } else {
            btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
        }

        //좋아요 버튼 클릭 이벤트
        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //로그인 상태일 경우만 좋아여 클릭 이벤트가 동작한다.
                if (GlobalSharedPreference.getAppPreferences(context, "login").equals("login")) {
                    final PopupWindow popupWindow = new PopupWindow(v);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView;

                    //iLikeThis 변수 값에 따라 출력되는 팝업 모양이 다르다
                    if (contentsArrayList.get(position).iLikeThis.equals("0")) {
                        popupView = inflater.inflate(R.layout.like_btn_popup_window_layout, null);
                    } else {
                        popupView = inflater.inflate(R.layout.like_cancel_btn_popup_window, null);
                    }

                    popupWindow.setContentView(popupView);
                    popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setTouchable(true);
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    //팝업 윈도우 위치 조정
                    popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - popupWindow.getHeight() - 200);

                    ImageButton btn_like_1 = (ImageButton) popupView.findViewById(R.id.btn_like_1);
                    ImageButton btn_like_2 = (ImageButton) popupView.findViewById(R.id.btn_like_2);
                    ImageButton btn_like_3 = (ImageButton) popupView.findViewById(R.id.btn_like_3);
                    ImageButton btn_like_4 = (ImageButton) popupView.findViewById(R.id.btn_like_4);

                    ImageButton btn_cancel_like = (ImageButton) popupView.findViewById(R.id.btn_like_cancel);


                    if (contentsArrayList.get(position).iLikeThis.equals("0")) {
                        //좋아요 캐릭터를 클릭하면 좋아요 버튼이 빨간색으로 바뀐다.
                        btn_like_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                contentsArrayList.get(position).iLikeThis = "1";
                                btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
                                registryEmotionRequest(contentsArrayList.get(position).contentId, 0);
                                contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount + 1;
                                numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));

                            }
                        });
                        btn_like_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                contentsArrayList.get(position).iLikeThis = "1";
                                btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
                                registryEmotionRequest(contentsArrayList.get(position).contentId, 1);
                                contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount + 1;
                                numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                            }
                        });
                        btn_like_3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                contentsArrayList.get(position).iLikeThis = "1";
                                btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
                                registryEmotionRequest(contentsArrayList.get(position).contentId, 2);
                                contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount + 1;
                                numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                            }
                        });
                        btn_like_4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                contentsArrayList.get(position).iLikeThis = "1";
                                btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
                                registryEmotionRequest(contentsArrayList.get(position).contentId, 3);
                                contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount + 1;
                                numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                            }
                        });
                    } else {
                        //좋아요 취소 이벤트
                        btn_cancel_like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                contentsArrayList.get(position).iLikeThis = "0";
                                btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_n);
                                deleteEmotionRequest(contentsArrayList.get(position).contentId);
                                contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount - 1;
                                numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                            }
                        });
                    }

                } else {
                    loginPopupRequest(v);
                }
            }

        });


        viewHolder.btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContents(context, "Here comes LikePet http://www.likelab.co.kr/share.php");
            }
        });


        return itemLayout;
    }

    //감정표현 취소 리퀘스트
    public void deleteEmotionRequest(final String contentId) {

        String endPoint = "/contents/" + contentId +"/feel";

        //Toast.makeText(JoinMemberBeginActivity.this, token, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
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

    //감정표현 좋아요 리퀘스트
    public void registryEmotionRequest(final String contentId, int likeType) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("likeType", likeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String endPoint = "/contents/" + contentId +"/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
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

    public void shareContents(Context context, String content) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }


    //로그 아웃일 경우 로그인 팝업을 표시한다
    private void loginPopupRequest(View v) {


        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.recommend_join_member_popup_windown, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        mainActivity.overlay.setVisibility(View.VISIBLE);

        RelativeLayout joinLater = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_later_container);
        RelativeLayout joinNow = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_now_container);

        joinLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        joinNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(context, JoinMemberBeginActivity.class);
                context.startActivity(intent);

            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mainActivity.overlay.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void setCommentBtnClickListener(CommentBtnClickListener commentBtnClickListener) {
        this.commentBtnClickListener = commentBtnClickListener;
    }


}
