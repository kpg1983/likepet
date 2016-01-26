package com.likelab.likepet.view;

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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.global.RoundedAvatarDrawable;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.volleryCustom.CustomNetworkImageView;
import com.likelab.likepet.yourPage.YourPageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kpg1983 on 2015-10-12.
 */
public class ViewContentsAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<ViewContents> viewContentsArrayList;
    int layout;

    ImageLoader imageLoader;
    RequestQueue queue;
    View viewLayout;
    ViewActivity viewActivity;
    private static final int RESULT_MODIFY_COMMENT = 8;

    //멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

    public ViewContentsAdapter(Context mContext, int mLayout, ArrayList<ViewContents> contentsArray, ViewActivity viewActivity) {
        context = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        viewContentsArrayList = contentsArray;
        layout = mLayout;
        imageLoader = AppController.getInstance().getImageLoader();
        queue = AppController.getInstance().getRequestQueue();
        this.viewActivity = viewActivity;

    }

    //onDestory에서 쉽게 해제할 수 있도록 메소드 생성
    public void recycle() {

        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());
        }

    }

    @Override
    public int getCount() {
        return viewContentsArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewContents getItem(int position) {

        return viewContentsArrayList.get(position);
    }

    class ViewHolder {

        TextView txtUserName;
        NetworkImageView imgComment;
        TextView txtComment;

        TextView txtDate;
        TextView txtLike;

        RelativeLayout likeContainer;

        ImageView imgBestCommentGold;
        ImageView imgBestCommentSilver;
        ImageView imgBestCommentBronze;

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View itemLayout = convertView;

        final ImageView imgLike;
        final TextView txtNumberOfLike;

        GlobalSharedPreference.getAppPreferences(context, "sid");

        ViewHolder viewHolder = new ViewHolder();

        if (itemLayout == null) {

            itemLayout = inflater.inflate(layout, null);

            viewHolder.txtUserName = (TextView) itemLayout.findViewById(R.id.comment_userName);
            viewHolder.imgComment = (NetworkImageView) itemLayout.findViewById(R.id.comment_img_comment);
            viewHolder.txtComment = (TextView) itemLayout.findViewById(R.id.comment_txt_comment);
            viewHolder.txtDate = (TextView) itemLayout.findViewById(R.id.comment_date);
            viewHolder.txtLike = (TextView) itemLayout.findViewById(R.id.comment_txt_like);
            viewHolder.likeContainer = (RelativeLayout) itemLayout.findViewById(R.id.comment_like_container);

            viewHolder.imgBestCommentGold = (ImageView) itemLayout.findViewById(R.id.comment_img_best_gold);
            viewHolder.imgBestCommentSilver = (ImageView) itemLayout.findViewById(R.id.comment_img_best_silver);
            viewHolder.imgBestCommentBronze = (ImageView) itemLayout.findViewById(R.id.comment_img_best_bronze);

            itemLayout.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) itemLayout.getTag();
        }

        final ImageView imgUserProfile = (ImageView) itemLayout.findViewById(R.id.comment_img_userProfile);
        //final CustomNetworkImageView imgCommentExpansion = (CustomNetworkImageView)


        imageLoader.get(viewContentsArrayList.get(position).profileImageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                if (response.getBitmap() != null) {
                    //imgUserProfile.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                    imgUserProfile.setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 1));
                } else {
                    String clan = viewContentsArrayList.get(position).clan;
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

            }
        });

        imgUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, YourPageActivity.class);
                String userId = viewContentsArrayList.get(position).userId;
                String name = viewContentsArrayList.get(position).mUserProfileName;
                String profileImageUrl = viewContentsArrayList.get(position).profileImageUrl;
                String clan = viewContentsArrayList.get(position).clan;


                intent.putExtra("USER_ID", userId);
                intent.putExtra("NAME", name);
                intent.putExtra("CLAN", clan);
                intent.putExtra("PROFILE_IMAGE", profileImageUrl);

                viewActivity.startActivity(intent);
            }
        });

        imgLike = (ImageView) itemLayout.findViewById(R.id.comment_img_like);

        viewHolder.txtUserName.setText(viewContentsArrayList.get(position).mUserProfileName);

        //텍스트에 내용이 달리지지 않았다면 댓글 화면에 표시하지 않는다.
        String txtFlag = viewContentsArrayList.get(position).mTxtComment;
        if (txtFlag.equals("") || txtFlag.equals("null")) {
            viewHolder.txtComment.setVisibility(View.GONE);
        } else {
            viewHolder.txtComment.setVisibility(View.VISIBLE);
            viewHolder.txtComment.setText(viewContentsArrayList.get(position).mTxtComment);
        }

        viewHolder.txtDate.setText(viewContentsArrayList.get(position).registryDate);

        //좋아요 카운트
        txtNumberOfLike = (TextView) itemLayout.findViewById(R.id.comment_numberOfLike);

        //좋아여 카운트 형변환 후 대입
        String numberOfLike = Integer.toString(viewContentsArrayList.get(position).likeCount);
        txtNumberOfLike.setText(numberOfLike);

        //베스트 댓글 장식 포함
        if (viewContentsArrayList.get(position).bestCommentFlag == 1) {
            viewHolder.imgBestCommentGold.setVisibility(View.VISIBLE);
            viewHolder.imgBestCommentGold.setImageResource(R.drawable.view_img_01_gold);

            viewHolder.imgBestCommentSilver.setVisibility(View.INVISIBLE);
            viewHolder.imgBestCommentBronze.setVisibility(View.INVISIBLE);

            viewHolder.imgBestCommentSilver.setImageBitmap(null);
            viewHolder.imgBestCommentBronze.setImageBitmap(null);

        } else if (viewContentsArrayList.get(position).bestCommentFlag == 2) {
            viewHolder.imgBestCommentSilver.setVisibility(View.VISIBLE);
            viewHolder.imgBestCommentSilver.setImageResource(R.drawable.view_img_02_silver);

            viewHolder.imgBestCommentGold.setVisibility(View.INVISIBLE);
            viewHolder.imgBestCommentBronze.setVisibility(View.INVISIBLE);

            viewHolder.imgBestCommentGold.setImageBitmap(null);
            viewHolder.imgBestCommentBronze.setImageBitmap(null);

        } else if (viewContentsArrayList.get(position).bestCommentFlag == 3) {
            viewHolder.imgBestCommentBronze.setVisibility(View.VISIBLE);
            viewHolder.imgBestCommentBronze.setImageResource(R.drawable.view_img_03_bronze);

            viewHolder.imgBestCommentSilver.setVisibility(View.INVISIBLE);
            viewHolder.imgBestCommentGold.setVisibility(View.INVISIBLE);

            viewHolder.imgBestCommentSilver.setImageBitmap(null);
            viewHolder.imgBestCommentGold.setImageBitmap(null);
        } else {
            viewHolder.imgBestCommentGold.setVisibility(View.INVISIBLE);
            viewHolder.imgBestCommentSilver.setVisibility(View.INVISIBLE);
            viewHolder.imgBestCommentBronze.setVisibility(View.INVISIBLE);

        }

        //댓글에 이미지 포함 유무 확인후 레이아웃 변경
        if (viewContentsArrayList.get(position).commentUrl == null || viewContentsArrayList.get(position).commentUrl.equals("null") || viewContentsArrayList.get(position).commentUrl.equals("")) {
            viewHolder.imgComment.setVisibility(View.GONE);

        } else {
            viewHolder.imgComment.setVisibility(View.VISIBLE);
            viewHolder.imgComment.setImageUrl(viewContentsArrayList.get(position).commentUrl, imageLoader);

        }

        //이미지 댓글을 터치하면 확대한다.
        viewHolder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageCommentPopupRequest(v, viewContentsArrayList.get(position).commentUrl);

            }
        });

        /*

        //사진 댓글을 롱클릭 했을 경우도 신고하기 또는 삭제하기 기능을 수행한다.
        viewHolder.imgComment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (GlobalSharedPreference.getAppPreferences(context, "login").equals("login")) {

                    final PopupWindow popupWindow = new PopupWindow(v);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View popupView;

                    viewActivity.overlay.setVisibility(View.VISIBLE);

                    RelativeLayout deleteConfirmContainer = null;
                    RelativeLayout deleteCancelContainer = null;

                    RelativeLayout reportConfirmContainer = null;
                    RelativeLayout reportCancelContainer = null;


                    //나의 댓글일 경우, 삭제하기 기능
                    if (viewContentsArrayList.get(position).userId.equals(GlobalSharedPreference.getAppPreferences(context, "userId"))) {
                        popupView = inflater.inflate(R.layout.comment_delete_popup_window, null);

                        deleteConfirmContainer = (RelativeLayout) popupView.findViewById(R.id.comment_delete_confirm_container);
                        deleteCancelContainer = (RelativeLayout) popupView.findViewById(R.id.comment_delete_cancel_container);


                        deleteConfirmContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteCommentRequest(viewContentsArrayList.get(position).contentId, viewContentsArrayList.get(position).commentId);

                                viewContentsArrayList.remove(position);

                                popupWindow.dismiss();
                            }
                        });

                        deleteCancelContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                viewActivity.overlay.setVisibility(View.INVISIBLE);
                            }
                        });

                        //상대방 댓글일 경우 신고하기
                    } else {
                        popupView = inflater.inflate(R.layout.comment_report_popup_window, null);

                        reportConfirmContainer = (RelativeLayout) popupView.findViewById(R.id.comment_report_confirm_container);
                        reportCancelContainer = (RelativeLayout) popupView.findViewById(R.id.comment_report_cancel_container);

                        viewActivity.overlay.setVisibility(View.VISIBLE);

                        reportConfirmContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                reportCommentRequest(viewContentsArrayList.get(position).contentId, viewContentsArrayList.get(position).commentId);
                                popupWindow.dismiss();
                            }
                        });

                        reportCancelContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                viewActivity.overlay.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    popupWindow.setContentView(popupView);
                    popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setTouchable(true);
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    //팝업 윈도우 위치 조정
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                } else {
                    loginPopupRequest(v);
                }


                return false;
            }
        });
        */


        //댓글의 좋아요 유무로 하트 색상을 다르게 표시한다.
        if (viewContentsArrayList.get(position).iLikeThis.equals("0")) {
            imgLike.setImageResource(R.drawable.view_btn_04_like_n);
            imgLike.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imgLike.setImageResource(R.drawable.view_btn_04_like_s);
            imgLike.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        //댓글에 좋아여 이벤트 처리
        viewHolder.likeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(context, "login").equals("login")) {

                    //좋아요 버튼이 눌리고 취소될 때 마다 좋아요 숫자가 변경된다.
                    int likeCount;
                    likeCount = viewContentsArrayList.get(position).likeCount;

                    //좋아요
                    if (viewContentsArrayList.get(position).iLikeThis.equals("0")) {
                        imgLike.setImageResource(R.drawable.view_btn_04_like_s);
                        imgLike.setScaleType(ImageView.ScaleType.FIT_XY);
                        viewContentsArrayList.get(position).iLikeThis = "1";

                        iLikeThisRequest(viewContentsArrayList.get(position).contentId, viewContentsArrayList.get(position).commentId, position);

                        likeCount = viewContentsArrayList.get(position).likeCount;
                        likeCount = likeCount + 1;

                    }
                    //좋아여 취소
                    else {
                        imgLike.setImageResource(R.drawable.view_btn_04_like_n);
                        imgLike.setScaleType(ImageView.ScaleType.FIT_XY);
                        viewContentsArrayList.get(position).iLikeThis = "0";
                        iLikeCancelRequest(viewContentsArrayList.get(position).contentId, viewContentsArrayList.get(position).commentId, position);

                        likeCount = viewContentsArrayList.get(position).likeCount;
                        likeCount = likeCount - 1;
                    }

                    viewContentsArrayList.get(position).likeCount = likeCount;
                    txtNumberOfLike.setText(Integer.toString(likeCount));
                } else {
                    loginPopupRequest(v);
                }

            }
        });

        //메모리 해제할 View를 추가
        mRecycleList.add(new WeakReference<View>(imgUserProfile));
        mRecycleList.add(new WeakReference<View>(imgLike));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgBestCommentBronze));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgBestCommentGold));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgBestCommentSilver));
        mRecycleList.add(new WeakReference<View>(viewHolder.imgComment));

        return itemLayout;
    }

    //댓글 좋아요 이벤트
    public void iLikeThisRequest(String contentId, String commentId, final int position) {

        String endPoint = "/comment/" + contentId + "/" + commentId + "/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                for (int i = 0; i < viewContentsArrayList.size(); i++) {
                                    //리퀘스트가 완료되면 commentID를 확인 후 iLikeThis의 값과 likeCount의 값을 수정해 준다.
                                    if (viewContentsArrayList.get(i).commentId.equals(viewContentsArrayList.get(position).commentId)) {
                                        if (i == position) {
                                            continue;
                                        }
                                        viewContentsArrayList.get(i).iLikeThis = viewContentsArrayList.get(position).iLikeThis;
                                        viewContentsArrayList.get(i).likeCount = viewContentsArrayList.get(position).likeCount;

                                    }
                                }
                                notifyDataSetChanged();
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

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //댓글 좋아요 취소
    public void iLikeCancelRequest(String contentId, String commentId, final int position) {

        String endPoint = "/comment/" + contentId + "/" + commentId + "/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                for (int i = 0; i < viewContentsArrayList.size(); i++) {
                                    if (viewContentsArrayList.get(i).commentId.equals(viewContentsArrayList.get(position).commentId)) {
                                        if (i == position) {
                                            continue;
                                        }
                                        viewContentsArrayList.get(i).iLikeThis = viewContentsArrayList.get(position).iLikeThis;
                                        viewContentsArrayList.get(i).likeCount = viewContentsArrayList.get(position).likeCount;

                                    }
                                }

                                notifyDataSetChanged();
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

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //댓글 이미지를 클릭할 경우 확대 표시한다
    private void ImageCommentPopupRequest(View v, String imageUrl) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.view_image_comment_popup_window, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        //팝업 화면을 띄울때 반투명 검정색 배경을 화면에 표시한다.
        viewActivity.overlay.setVisibility(View.VISIBLE);

        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        CustomNetworkImageView imgCommentExpansion = (CustomNetworkImageView) popupView.findViewById(R.id.view_img_comment_expansion);
        imgCommentExpansion.setImageUrl(imageUrl, imageLoader);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                viewActivity.overlay.setVisibility(View.INVISIBLE);
            }
        });

    }

    //회원가입 팝업
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

        //팝업 화면을 띄울때 반투명 검정색 배경을 화면에 표시한다.
        viewActivity.overlay.setVisibility(View.VISIBLE);

        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

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
                viewActivity.overlay.setVisibility(View.INVISIBLE);
            }
        });
    }

}
