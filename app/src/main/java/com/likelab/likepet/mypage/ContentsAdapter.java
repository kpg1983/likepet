package com.likelab.likepet.mypage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.likelab.likepet.CommentBtnClickListener;
import com.likelab.likepet.R;
import com.likelab.likepet.RoundedCornerTransformation;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.volleryCustom.LruMemoryDiskBitmapCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
public class ContentsAdapter extends BaseAdapter {

    private static final int RESULT_CODE = 0;

    Activity context;
    LayoutInflater inflater;
    ArrayList<Contents> contentsArrayList;
    int layout;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private LruMemoryDiskBitmapCache cache;

    //멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

    RequestQueue queue;
    private CommentBtnClickListener commentBtnClickListener;

    public ContentsAdapter(Activity mContext, int mLayout, ArrayList<Contents> contentsArray) {
        context = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        contentsArrayList = contentsArray;
        layout = mLayout;

        cache = new LruMemoryDiskBitmapCache(context);
        queue = AppController.getInstance().getRequestQueue();

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
    public Contents getItem(int position) {

        return contentsArrayList.get(position);
    }

    class ViewHolder {

        ImageView mainContents;
        ImageView imgBlind;

        TextView registerTime;
        TextView numberOfComment;
        TextView txtComment[] = new TextView[3];

        ImageView imgCommentDeco[] = new ImageView[3];

        //댓글 버튼과 공유하기 버튼
        ImageButton btn_share;
        ImageView btn_play;

        RelativeLayout comment_container;

        RelativeLayout noCommentContainer[] = new RelativeLayout[3];

        TextView txtCaption;

        ImageView imgGifICON;
        ImageView btnComment;

        ImageView imgNoComment[] = new ImageView[3];

        ImageView imgCommentBtn;

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View itemLayout = convertView;

        ViewHolder viewHolder = new ViewHolder();

        final ImageButton btn_like;


        if (itemLayout == null) {

            itemLayout = inflater.inflate(layout, null);

            viewHolder.mainContents = (ImageView) itemLayout.findViewById(R.id.content_main_image);
            viewHolder.registerTime = (TextView) itemLayout.findViewById(R.id.contents_time);
            viewHolder.numberOfComment = (TextView) itemLayout.findViewById(R.id.number_of_comment);

            //일반 글자 댓글
            viewHolder.txtComment[0] = (TextView) itemLayout.findViewById(R.id.text_comment_1);
            viewHolder.txtComment[1] = (TextView) itemLayout.findViewById(R.id.text_comment_2);
            viewHolder.txtComment[2] = (TextView) itemLayout.findViewById(R.id.text_comment_3);

            //베스트 댓글 상단 장식
            viewHolder.imgCommentDeco[0] = (ImageView) itemLayout.findViewById(R.id.mypage_comment_image_01_n);
            viewHolder.imgCommentDeco[1] = (ImageView) itemLayout.findViewById(R.id.mypage_comment_image_02_n);
            viewHolder.imgCommentDeco[2] = (ImageView) itemLayout.findViewById(R.id.mypage_comment_image_03_n);

            //베댓을 기다리고 있다멍 이미지
            viewHolder.imgNoComment[0] = (ImageView)itemLayout.findViewById(R.id.mypage_img_no_comment_1);
            viewHolder.imgNoComment[1] = (ImageView)itemLayout.findViewById(R.id.mypage_img_no_comment_2);
            viewHolder.imgNoComment[2] = (ImageView)itemLayout.findViewById(R.id.mypage_img_no_comment_3);

            viewHolder.btn_share = (ImageButton) itemLayout.findViewById(R.id.button_share);
            viewHolder.btn_play = (ImageView) itemLayout.findViewById(R.id.mypage_btn_play);

            viewHolder.comment_container = (RelativeLayout) itemLayout.findViewById(R.id.comment_container);

            //배댓이 없는 나머지 두개를 위한 컨테이너
            viewHolder.noCommentContainer[0] = (RelativeLayout) itemLayout.findViewById(R.id.mypage_comment_no_comment_container_1);
            viewHolder.noCommentContainer[1] = (RelativeLayout) itemLayout.findViewById(R.id.mypage_comment_no_comment_container_2);
            viewHolder.noCommentContainer[2] = (RelativeLayout) itemLayout.findViewById(R.id.mypage_comment_no_comment_container_3);


            viewHolder.imgGifICON = (ImageView)itemLayout.findViewById(R.id.mypage_img_gif_icon);
            viewHolder.txtCaption = (TextView)itemLayout.findViewById(R.id.mypage_txt_caption);
            viewHolder.btnComment = (ImageView) itemLayout.findViewById(R.id.button_comment);
            viewHolder.imgBlind = (ImageView) itemLayout.findViewById(R.id.mypage_img_blind);

            viewHolder.imgCommentBtn = (ImageView) itemLayout.findViewById(R.id.button_comment);

            itemLayout.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) itemLayout.getTag();
        }

        final ImageView imgComment[] = new ImageView[3];
        imgComment[0] = (ImageView) itemLayout.findViewById(R.id.image_comment_1);
        imgComment[1] = (ImageView) itemLayout.findViewById(R.id.image_comment_2);
        imgComment[2] = (ImageView) itemLayout.findViewById(R.id.image_comment_3);

        final ImageView imgBoxLine[] = new ImageView[3];
        imgBoxLine[0] = (ImageView) itemLayout.findViewById(R.id.img_box_line_1);
        imgBoxLine[1] = (ImageView) itemLayout.findViewById(R.id.img_box_line_2);
        imgBoxLine[2] = (ImageView) itemLayout.findViewById(R.id.img_box_line_3);

        final TextView numberOfLike = (TextView) itemLayout.findViewById(R.id.number_of_like);

        btn_like = (ImageButton) itemLayout.findViewById(R.id.button_like);

        if(contentsArrayList.get(position).contentsType.matches(".*gif.*")) {
            viewHolder.imgGifICON.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgGifICON.setVisibility(View.INVISIBLE);
            //viewHolder.imgGifICON.setImageDrawable(null);
        }

        if(contentsArrayList.get(position).status.equals("1") || contentsArrayList.get(position).reportCount > 10) {
            viewHolder.imgBlind.setVisibility(View.VISIBLE);
            viewHolder.imgBlind.setImageResource(R.drawable.mypage_view_contents_blind);
            viewHolder.mainContents.setVisibility(View.GONE);

        } else {

            viewHolder.imgBlind.setImageBitmap(null);
            viewHolder.imgBlind.setVisibility(View.GONE);
            viewHolder.mainContents.setVisibility(View.VISIBLE);

            int mediaWidth = contentsArrayList.get(position).mediaWidth;
            int mediaHeight = contentsArrayList.get(position).mediaHeight;


            if (contentsArrayList.get(position).contentsType.matches(".*image.*")) {
                Picasso.with(context)
                        .load(contentsArrayList.get(position).contentsUrl)
                        .placeholder(R.drawable.place_holder_960)
                        .resize(mediaWidth, mediaHeight)
                        .into(viewHolder.mainContents);

            } else if(contentsArrayList.get(position).contentsType.matches(".*video.*")){
                Picasso.with(context)
                        .load(contentsArrayList.get(position).videoScreenShotUrl)
                        .placeholder(R.drawable.place_holder_960)
                        .into(viewHolder.mainContents);
            }

        }

        viewHolder.registerTime.setText(contentsArrayList.get(position).registryDate);

        String likeCount = Integer.toString(contentsArrayList.get(position).likeCount);
        numberOfLike.setText(likeCount);
        viewHolder.numberOfComment.setText(Integer.toString(contentsArrayList.get(position).commentCount));

        String descriptions[] = contentsArrayList.get(position).descriptionTag.split("\n");

        if(descriptions.length > 1) {
            viewHolder.txtCaption.setText(descriptions[0]);
        } else {
            viewHolder.txtCaption.setText(contentsArrayList.get(position).descriptionTag);
        }

        //댓글 버튼을 누를 경우 뷰페이지에서 바로 키보드를 활성화 시킨다.

        viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentBtnClickListener.onCommentBtnClicked(position);
            }
        });



        //콘텐츠의 타입을 확인 후 동영상은 플레이 버튼 표시
        if(contentsArrayList.get(position).contentsType.matches(".*video.*")) {
            viewHolder.btn_play.setVisibility(View.VISIBLE);
            viewHolder.btn_play.setImageResource(R.drawable.mypage_btn_play);
        } else {
            viewHolder.btn_play.setVisibility(View.INVISIBLE);
            viewHolder.btn_play.setImageBitmap(null);
        }


        int numberOfBestComment = contentsArrayList.get(position).numberOfBestComment;

        //베스트 댓글 숫자 확인 후 베스트 댓글 갯수만큼 리스트 화면에 뿌려준다
        if(numberOfBestComment > 0) {

            viewHolder.comment_container.setVisibility(View.VISIBLE);
            for (int i = 0; i < numberOfBestComment; i++) {

                viewHolder.noCommentContainer[i].setVisibility(View.INVISIBLE);
                viewHolder.imgNoComment[i].setImageDrawable(null);
                viewHolder.imgCommentDeco[i].setVisibility(View.VISIBLE);

                //이미지 comment
                if(contentsArrayList.get(position).bestCommentUrl[i].contains("http")) {

                    //텍스트 comment 입력시 필요한 배경들은 보이지 않게 처리한다.
                    imgBoxLine[i].setVisibility(View.INVISIBLE);
                    imgBoxLine[i].setImageDrawable(null);

                    //이미지 comment 컨테이너
                    imgComment[i].setVisibility(View.VISIBLE);

                    final Transformation transFormation = new RoundedCornerTransformation(10, 0);
                    Picasso.with(context)
                            .load(contentsArrayList.get(position).bestCommentUrl[i])
                            .resize(300, 300)
                            .transform(transFormation)
                            .into(imgComment[i]);


                } else  {
                    //텍스트 comment
                    imgComment[i].setVisibility(View.INVISIBLE);
                    imgComment[i].setImageBitmap(null);

                    imgBoxLine[i].setVisibility(View.VISIBLE);
                    imgBoxLine[i].setImageResource(R.drawable.img_boxline);
                    viewHolder.txtComment[i].setText(contentsArrayList.get(position).bestCommentDescription[i]);

                }
            }

            //베스트 comment가 3개 미만을때는 나머지 항목을 채워준다
            for(int i=numberOfBestComment; i<3; i++) {
                viewHolder.noCommentContainer[i].setVisibility(View.VISIBLE);
                imgComment[i].setVisibility(View.INVISIBLE);

                //베스트 댓글을 기다리고 있다멍
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
                String description = contentsArrayList.get(position).descriptionTag;
                String contentId = contentsArrayList.get(position).contentsId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;
                String profileImageUrl = GlobalSharedPreference.getAppPreferences(context, "profileImageUrl");
                String userName = GlobalSharedPreference.getAppPreferences(context, "name");
                String clan = GlobalSharedPreference.getAppPreferences(context, "clan");
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;


                if (contentsArrayList.get(position).contentsType.matches(".*image.*")) {
                    contentType = 1;
                    if (contentsArrayList.get(position).contentsType.matches(".*gif.*")) {
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
                intent.putExtra("IMAGE_URL", contentsArrayList.get(position).contentsUrl);
                intent.putExtra("ILIKETHIS", iLikeThis);
                intent.putExtra("PROFILE_IMAGE_URL", profileImageUrl);
                intent.putExtra("NAME", userName);
                intent.putExtra("POSITION", position);
                intent.putExtra("LIKE_COUNT", likeCount);
                intent.putExtra("COMMENT_COUNT", commentCount);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("REPORT_COUNT", reportCount);
                intent.putExtra("STATUS", status);
                intent.putExtra("CLAN", clan);


                context.startActivityForResult(intent, RESULT_CODE);
            }
        });


        //좋아여 클릭 이벤트
        if (contentsArrayList.get(position).iLikeThis.equals("0")) {
            btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_n);
            btn_like.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_s);
            btn_like.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        ImageButton btn_share = (ImageButton) itemLayout.findViewById(R.id.button_share);
        btn_share.setScaleType(ImageView.ScaleType.FIT_XY);


        //좋아요 버튼 클릭 이벤트
        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupWindow popupWindow = new PopupWindow(v);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView;

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
                            registryEmotionRequest(contentsArrayList.get(position).contentsId, 0);
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
                            registryEmotionRequest(contentsArrayList.get(position).contentsId, 1);
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
                            registryEmotionRequest(contentsArrayList.get(position).contentsId, 2);
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
                            registryEmotionRequest(contentsArrayList.get(position).contentsId, 3);
                            contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount + 1;
                            numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                        }
                    });
                } else {
                    btn_cancel_like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            contentsArrayList.get(position).iLikeThis = "0";
                            btn_like.setImageResource(R.drawable.mypage_btn_bottom_like_n);
                            deleteEmotionRequest(contentsArrayList.get(position).contentsId);
                            contentsArrayList.get(position).likeCount = contentsArrayList.get(position).likeCount - 1;
                            numberOfLike.setText(Integer.toString(contentsArrayList.get(position).likeCount));
                        }
                    });
                }

            }

        });


        viewHolder.btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContents(context, "Here comes LikePet http://www.likelab.co.kr/share.php");
            }
        });

        //메모리 해제할 View를 추가
        mRecycleList.add(new WeakReference<View>(viewHolder.btn_play));
        mRecycleList.add(new WeakReference<View>(viewHolder.btn_share));
        mRecycleList.add(new WeakReference<View>(viewHolder.btnComment));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgBlind));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgGifICON));
        mRecycleList.add(new WeakReference<View>(viewHolder.mainContents));

        for(int i=0; i<3; i++) {
            mRecycleList.add(new WeakReference<View>(viewHolder.imgNoComment[i]));
            mRecycleList.add(new WeakReference<View>(viewHolder.imgCommentDeco[i]));
            mRecycleList.add(new WeakReference<View>(imgBoxLine[i]));
            mRecycleList.add(new WeakReference<View>(imgComment[i]));
        }



        return itemLayout;
    }


    public void deleteEmotionRequest(final String contentId) {

        String endPoint = "/contents/" + contentId + "/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {

                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                            }

                        } catch (JSONException e) {
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
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void registryEmotionRequest(final String contentId, int likeType) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("likeType", likeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String endPoint = "/contents/" + contentId + "/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {

                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                            }

                        } catch (JSONException e) {
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
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

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

    public void setCommentBtnClickListener(CommentBtnClickListener commentBtnClickListener) {
        this.commentBtnClickListener = commentBtnClickListener;
    }


}
