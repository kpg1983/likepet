package com.likelab.likepet.mypage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadServiceBroadcastReceiver;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.CommentBtnClickListener;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.UploadContents;
import com.likelab.likepet.bookmark.BookmarkActivity;
import com.likelab.likepet.follow.FollowerUserListActivity;
import com.likelab.likepet.follow.FollowingUserListActivity;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUploadBitmapImage;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.global.RoundedAvatarDrawable;
import com.likelab.likepet.more.UserProfile;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;
import com.likelab.likepet.view.ViewActivity;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.volleryCustom.CustomNetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyPageMoment extends Fragment implements CommentBtnClickListener{

    RelativeLayout layout;

    ArrayList<Contents> contentsArrayList;
    ListView contentsList;

    ProgressBar progressUpload;

    TextView profileName;
    ImageButton btn_bookmark;
    ImageButton btn_setting;
    ImageView btn_camera;

    ImageView mainProfileImage;

    ImageView imgNoMoment;

    TextView txtFollower;
    TextView txtFollowing;
    TextView txtMoment;

    RelativeLayout followingInfoContainer;
    RelativeLayout followerInfoContainer;

    RelativeLayout userProfileImageContainer;

    ImageView imgClan;
    ImageView contentStart;

    View header;

    String sid;

    private RequestQueue queue = AppController.getInstance().getRequestQueue();;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    ContentsAdapter adapter;

    private static final String TEMP_PHOTO_FILE = "temp.jpg";       // 임시 저장파일
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final int REQ_CODE_MODIFY_CONTENT_INFO = 1;
    private static final int REQ_LOGIN = 4;

    private static final int RESULT_MODIFY_CONTENT_SUMMARY = 5;
    private static final int RESULT_DELETE_CONTENT = 7;

    String filePath;
    Bitmap selectedImage;

    private RelativeLayout loginContainer;
    private Button btnJoin;

    private RelativeLayout overlay;

    private SwipeRefreshLayout mSwipeRefresh;

    private RelativeLayout noMomentContainer;
    private TextView txtNoMomentTime;

    private boolean lockListView;   //리스트뷰 데이터 추가 시 중복 요청을 방지하기 위한 변수

    private int currentPage = 0;
    private int maxPage;
    private int adapterFlag = 0;

    private static final String TAG = "AndroidUploadService";

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    private View footer;
    private RelativeLayout listViewLoadIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = (RelativeLayout) inflater.inflate(R.layout.activity_my_page_moment, container, false);

        //변수 초기화
        initView(inflater);

        //뷰들을 화면에 셋팅
        inflateLayout();

        //회원 가입을 하거나 로그인을 하였을 경우 마이페이지를 새로 고침하여 사용자 화면을 보여준다.

        //페이지 새로 고침
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                listViewLoadIndicator.setVisibility(View.GONE);

                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {

                    currentPage = 0;
                    adapter.notifyDataSetInvalidated();
                    contentsArrayList.clear();
                    adapterFlag = 0;
                    mypageRequest(currentPage);

                } else {
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });

        //회원가입 버튼 이벤트
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), JoinMemberBeginActivity.class);
                startActivityForResult(intent, REQ_LOGIN);

            }
        });

        //즐겨찾기 이동
        btn_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                    startActivity(intent);
                } else {

                }
            }
        });

        //팔로잉 유저 목록
        followingInfoContainer = (RelativeLayout) header.findViewById(R.id.following_info_container);
        followingInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String follow = "following";


                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    Intent intent = new Intent(getActivity(), FollowingUserListActivity.class);
                    intent.putExtra("FOLLOW", follow);
                    intent.putExtra("PAGE", "MY_PAGE");
                    startActivity(intent);
                }
            }
        });

        //팔로우 유저 목록
        followerInfoContainer = (RelativeLayout) header.findViewById(R.id.follower_info_container);
        followerInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String follow = "follower";

                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    Intent intent = new Intent(getActivity(), FollowerUserListActivity.class);
                    intent.putExtra("FOLLOW", follow);
                    intent.putExtra("PAGE", "MY_PAGE");
                    startActivity(intent);
                }
            }
        });



        //유저의 프로필 설정
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    Intent intent = new Intent(getActivity(), UserProfile.class);
                    startActivity(intent);
                } else {


                }
            }
        });

        mainProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //imageUrl이 있어야만 보이게 한다
                if (GlobalSharedPreference.getAppPreferences(getActivity(), "profileImageUrl").contains("http"))
                    ProfileImagePopup(v, GlobalSharedPreference.getAppPreferences(getActivity(), "profileImageUrl"));
            }
        });

        //카메라 버튼 클릭시 프로필 이미지 선택 화면으로 전환한다
       btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");              // 모든 이미지
                    intent.putExtra("crop", "true");        // Crop기능 활성화
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                    startActivityForResult(intent, REQ_CODE_PICK_IMAGE);

                } else {

                }
            }
        });

        adapter.setCommentBtnClickListener(this);

        //리스트뷰에 데이터를 추가한다.
        //리스트뷰의 가장 하단 데이터가 화면에 보일경우 리퀘스트를 날린다.
        contentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView == false) {
                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {

                        if (adapterFlag == 1) {
                            listViewLoadIndicator.setVisibility(View.VISIBLE);
                        }

                        lockListView = true;
                        mypageRequest(currentPage);

                    }
                }
            }

        });


        //리스트뷰를 선택했을 경우 해당 아이템의 뷰 페이지로 이동한다.
        contentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position_1, long id) {

                int contentType;
                int imageContent;
                int numberOfLike;
                int numberOfComment;
                int position = position_1 - 1;

                imageContent = contentsArrayList.get(position).mainContent;
                numberOfLike = contentsArrayList.get(position).likeCount;
                numberOfComment = contentsArrayList.get(position).commentCount;
                String description = contentsArrayList.get(position).descriptionTag;
                String contentId = contentsArrayList.get(position).contentsId;
                String iLikeThis = contentsArrayList.get(position).iLikeThis;

                //마이페이지 이므로 핸드폰에 저장되어 있는 유저의 정보를 불러온다
                String profileImageUrl = GlobalSharedPreference.getAppPreferences(getContext(), "profileImageUrl");
                String userName = GlobalSharedPreference.getAppPreferences(getContext(), "name");
                int likeCount = contentsArrayList.get(position).likeCount;
                int commentCount = contentsArrayList.get(position).commentCount;
                String userId = contentsArrayList.get(position).userId;
                String status = contentsArrayList.get(position).status;
                int reportCount = contentsArrayList.get(position).reportCount;

                Intent intent = new Intent(getContext(), ViewActivity.class);

                //이미지, gif, 동영상의 컨텐츠 타입을 확인
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

                //컨텐츠를 선택하고 다시 마이페이지로 돌아왔을때 view 페이지에서 좋아요 한 이벤트를 처리하기 위함
                startActivityForResult(intent, REQ_CODE_MODIFY_CONTENT_INFO);
            }
        });

        return layout;
    }

    private void initView(LayoutInflater inflater) {

        lockListView = false;
        sid = getAppPreferences(getActivity(), "sid");
        overlay = (RelativeLayout)layout.findViewById(R.id.mypage_overlay);

        //페이지 새로고침
        mSwipeRefresh = (SwipeRefreshLayout)layout.findViewById(R.id.swype_layout);
        header = inflater.inflate(R.layout.user_profile_list, null, false);
        footer = inflater.inflate(R.layout.listview_load_footer, null, false);

        profileName = (TextView) header.findViewById(R.id.user_name);
        btn_bookmark = (ImageButton) header.findViewById(R.id.button_bookmark);
        btn_setting = (ImageButton) header.findViewById(R.id.button_setting);
        btn_camera = (ImageView) header.findViewById(R.id.mypage_btn_camera);
        mainProfileImage = (ImageView) header.findViewById(R.id.main_profile_image);

        txtFollower = (TextView) header.findViewById(R.id.number_of_follower);
        txtFollowing = (TextView) header.findViewById(R.id.number_of_following);
        txtMoment = (TextView) header.findViewById(R.id.number_of_moments);
        userProfileImageContainer = (RelativeLayout)header.findViewById(R.id.user_profile_container);
        loginContainer = (RelativeLayout) header.findViewById(R.id.mypage_join_container);
        btnJoin = (Button)header.findViewById(R.id.mypage_btn_join);
        noMomentContainer = (RelativeLayout)header.findViewById(R.id.mypage_no_moment_container);
        txtNoMomentTime = (TextView)header.findViewById(R.id.contents_time);
        imgNoMoment = (ImageView)header.findViewById(R.id.mypage_img_noMoment_image);
        progressUpload = (ProgressBar)header.findViewById(R.id.mypage_upload_indicator);
        contentStart = (ImageView)header.findViewById(R.id.contents_start);
        imgClan = (ImageView)header.findViewById(R.id.img_user_clan);

        listViewLoadIndicator = (RelativeLayout)footer.findViewById(R.id.listview_load_indicator);

        contentsArrayList = new ArrayList<Contents>();

        adapter = new ContentsAdapter(getActivity(), R.layout.mypage_listview, contentsArrayList);

        contentsList = (ListView) layout.findViewById(R.id.contents_list);
        contentsList.addHeaderView(header);
        contentsList.addFooterView(footer);
    }

    private void inflateLayout() {

        final String clan = GlobalSharedPreference.getAppPreferences(getContext(), "clan");
        //유저의 종족
        if(clan.equals("0")) {
            imgClan.setImageResource(R.drawable.mypage_img_02);
        } else if(clan.equals("1")) {
            imgClan.setImageResource(R.drawable.mypage_img_01);
        } else if(clan.equals("2")) {
            imgClan.setImageResource(R.drawable.mypage_img_03);
        }

        profileName.setText(GlobalSharedPreference.getAppPreferences(getContext(), "name"));


        btn_bookmark.setImageResource(R.drawable.mypage_profile_btn_bookmark);
        btn_bookmark.setScaleType(ImageView.ScaleType.FIT_XY);

        btn_setting.setImageResource(R.drawable.mypage_profile_btn_setup);
        btn_setting.setScaleType(ImageView.ScaleType.FIT_XY);

        btn_camera.setImageResource(R.drawable.mypage_profile_btn_photo);
        btn_camera.setScaleType(ImageView.ScaleType.FIT_XY);

        //유저가 로그인 상태일때 프로필 이미지를 표시한다.
        //로그 아웃 상태일때는 기본 이미지를 교시한다.
        if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
            imageLoader.get(GlobalSharedPreference.getAppPreferences(getContext(), "profileImageUrl"), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                    if (response.getBitmap() != null) {
                        mainProfileImage.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                        mainProfileImage.setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 1));

                    } else {
                        String clan = GlobalSharedPreference.getAppPreferences(getContext(), "clan");
                        if (clan.equals("0")) {
                            mainProfileImage.setImageResource(R.drawable.more_img_06_01_dog);
                        } else if (clan.equals("1")) {
                            mainProfileImage.setImageResource(R.drawable.more_img_06_01_cat);
                        } else if (clan.equals("2")) {
                            mainProfileImage.setImageResource(R.drawable.more_img_06_01_human);
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        } else {
            mainProfileImage.setImageResource(R.drawable.more_img_profile_human);
        }

        //로그 아웃 상태이면 회원가입 페이지를 노출한다.
        ImageView imgJoin = (ImageView)header.findViewById(R.id.mypage_img_join);

        //로그인 상태이면 유저의 마이페이지 정보를 노출한다
        if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login")) {
            mypageRequest(currentPage);
            loginContainer.setVisibility(View.GONE);
            imgJoin.setImageBitmap(null);

        }
        //로그 아웃 상태일 경우 초기 기본 정보를 노출한다.
        else if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("logout")) {

            contentsList.setAdapter(adapter);
            loginContainer.setVisibility(View.VISIBLE);
            imgJoin.setImageResource(R.drawable.membership_img_01);

            txtMoment.setText("0");
            txtFollower.setText("0");
            txtFollowing.setText("0");
            btn_camera.setVisibility(View.INVISIBLE);
            imgClan.setImageResource(R.drawable.mypage_img_03);
            profileName.setText(getResources().getString(R.string.mypage_name_likepet));

        }

    }

    //프로필 이미지를 선택했을때 확대 화면으로 표시한다.
    private void ProfileImagePopup(View v, String imageUrl) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.view_image_comment_popup_window, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        //팝업 화면을 띄울때 반투명 검정색 배경을 화면에 표시한다.
        overlay.setVisibility(View.VISIBLE);

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        CustomNetworkImageView imgCommentExpansion = (CustomNetworkImageView)popupView.findViewById(R.id.view_img_comment_expansion);
        imgCommentExpansion.setImageUrl(imageUrl, imageLoader);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);
            }
        });

    }

    //컨텐츠 업로드 후 진행 상태 표시 및 완료 상태를 나타내기 위한 브로드캐스트 리시버
    private final UploadServiceBroadcastReceiver uploadReceiver =
            new UploadServiceBroadcastReceiver() {

                // you can override this progress method if you want to get
                // the completion progress in percent (0 to 100)
                // or if you need to know exactly how many bytes have been transferred
                // override the method below this one
                @Override
                public void onProgress(String uploadId, int progress) {
                    Log.i(TAG, "The progress of the upload with ID "
                            + uploadId + " is: " + progress);
                }

                //진행상태
                @Override
                public void onProgress(final String uploadId, final long uploadedBytes, final long totalBytes) {
                    Log.i(TAG, "Upload with ID " + uploadId + " uploaded bytes: " + uploadedBytes + ", total: " + totalBytes);

                    //업로드 상태 표시
                    noMomentContainer.setVisibility(View.VISIBLE);
                    imgNoMoment.setVisibility(View.GONE);
                    progressUpload.setVisibility(View.VISIBLE);

                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String time = sdfNow.format(new Date(System.currentTimeMillis()));

                    time = time.substring(0, time.indexOf(" "));
                    time = time.replaceFirst("\\/", "년 ");
                    time = time.replaceFirst("\\/", "월 ");
                    time = time + "일";

                    txtNoMomentTime.setText(time);
                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    Log.e(TAG, "Error in upload with ID: " + uploadId + ". "
                            + exception.getLocalizedMessage(), exception);
                }

                //업로드 완료 후
                @Override
                public void onCompleted(String uploadId,
                                        int serverResponseCode,
                                        String serverResponseMessage) {

                    if(serverResponseCode == 200) {

                        noMomentContainer.setVisibility(View.GONE);
                        imgNoMoment.setVisibility(View.GONE);
                        progressUpload.setVisibility(View.GONE);

                        currentPage = 0;
                        adapter.notifyDataSetInvalidated();
                        contentsArrayList.clear();
                        adapterFlag = 0;

                        //업로드가 완료되면 다시 마이페이지 리퀘스트 요청 -> 페이지 새로 고침
                        myPageSummaryRequest();
                        mypageRequest(currentPage);

                        Toast.makeText(getActivity(), getContext().getResources().getString(R.string.mypage_toast_registry_contents), Toast.LENGTH_SHORT).show();

                        //업로드가 완료되면 기존의 임시파일은 삭제한다
                        String tempFilePath = ((MainActivity)getActivity()).uploadTempFilePath;
                        if(tempFilePath != null) {
                            File files = new File(tempFilePath);
                            if (files.exists()) {
                                files.delete();
                            }
                        }

                        //업로드때 생성 되었던 비트맵들을 전부 초기화 시켜 메모리 확보
                        GlobalUploadBitmapImage.bitmap = null;
                        GlobalUploadBitmapImage.bitmapCopy = null;

                        for(int i=0; i<GlobalUploadBitmapImage.bitmapArray.length; i++) {
                            GlobalUploadBitmapImage.bitmapArray[i] = null;
                        }

                        GlobalUploadBitmapImage.bitmapList.clear();
                        GlobalUploadBitmapImage.filteredBitmapList.clear();
                    }

                    Log.i(TAG, "Upload with ID " + uploadId
                            + " has been completed with HTTP " + serverResponseCode
                            + ". Response from server: " + serverResponseMessage);

                }
            };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //프로필 사진 변경경
        if(requestCode == REQ_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                    selectedImage = BitmapFactory.decodeFile(filePath);
                    mainProfileImage.setImageDrawable(new RoundedAvatarDrawable(selectedImage, 1));
                    // temp.jpg파일을 Bitmap으로 디코딩한다.
                    // temp.jpg파일을 이미지뷰에 씌운다.
                    //imgUserPhoto.setImageUrl();

                    Runnable runnable = new RunnableImplements();
                    Thread thread = new Thread(runnable);
                    thread.start();

                }

            }
        }
        //좋아요 이벤트
        else if(requestCode == REQ_CODE_MODIFY_CONTENT_INFO){

            //컨텐츠가 삭제된 경우
            if(resultCode == RESULT_DELETE_CONTENT) {
                int position = data.getExtras().getInt("POSITION");
                contentsArrayList.remove(position);
                updateList(contentsArrayList);
                return;

                //좋아요, 댓글 카운트가 변경된 경우
            } else if(resultCode == RESULT_MODIFY_CONTENT_SUMMARY){
                int position = data.getExtras().getInt("POSITION");
                contentsInfoRequest(position, contentsArrayList.get(position).contentsId);

            }

        }
        else if(requestCode == REQ_LOGIN) {
            if(resultCode == Activity.RESULT_OK) {

                Fragment frg = null;
                frg = getActivity().getSupportFragmentManager().findFragmentByTag("Your_Fragment_TAG");
                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();

            }
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if(adapter != null) {
            adapter.recycle();
        }

        RecycleUtils.recursiveRecycle(imgClan);
        RecycleUtils.recursiveRecycle(imgNoMoment);
        RecycleUtils.recursiveRecycle(btn_bookmark);
        RecycleUtils.recursiveRecycle(btn_setting);
        RecycleUtils.recursiveRecycle(btn_camera);
        RecycleUtils.recursiveRecycle(mainProfileImage);
        RecycleUtils.recursiveRecycle(contentStart);


    }

    private String getAppPreferences(Activity context, String key) {
        String returnValue = null;
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("sid", 0);
        returnValue = pref.getString(key, "");

        return returnValue;
    }

    public void contentsInfoRequest(final int position, String contentId) {

        String endPoint = "/contents/" + contentId;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (responseCode == 200) {
                            try {
                                JSONObject item = response.getJSONObject("item");
                                final int likeCount = item.getInt("likeCount");
                                final int commentCount = item.getInt("commentCount");
                                final String iLikeThis = item.getString("ILikedThis");
                                final String descriptions = item.getString("descriptions");

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        contentsArrayList.get(position).likeCount = likeCount;
                                        contentsArrayList.get(position).commentCount = commentCount;
                                        contentsArrayList.get(position).iLikeThis = iLikeThis;
                                        contentsArrayList.get(position).descriptionTag = descriptions;
                                    }
                                });

                                updateList(contentsArrayList);

                            } catch (Exception e) {
                                e.printStackTrace();

                            }
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

                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", sid);

                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }


    public void mypageRequest(int pageNo) {

        lockListView = true;

        String endPoint = "/mypage";

        String parameter = "?pageNo=" + pageNo;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint + parameter,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (responseCode == 200) {

                            try {
                                JSONObject mypageJsonObject = response.getJSONObject("mypage");
                                JSONObject pages = mypageJsonObject.getJSONObject("pages");
                                maxPage = pages.getInt("max");
                                JSONArray jsonArray = mypageJsonObject.getJSONArray("items");

                                adapter.notifyDataSetInvalidated();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String userId = jsonArray.getJSONObject(i).getString("userId");
                                    String contentType = jsonArray.getJSONObject(i).getString("contentType");
                                    String registryDate = jsonArray.getJSONObject(i).getString("registryDate");
                                    String descriptions = jsonArray.getJSONObject(i).getString("descriptions");
                                    String contentId = jsonArray.getJSONObject(i).getString("contentId");
                                    String iLikeThis = jsonArray.getJSONObject(i).getString("ILikedThis");
                                    String videoScreenshotUrl = jsonArray.getJSONObject(i).getString("videoScreenshotUrl");
                                    String status = jsonArray.getJSONObject(i).getString("status");
                                    int reportCount = jsonArray.getJSONObject(i).getInt("reportCount");

                                    registryDate = registryDate.substring(0, registryDate.indexOf(" "));
                                    //registryDate = registryDate.replaceFirst("\\.", "년 ");
                                    //registryDate = registryDate.replaceFirst("\\.", "월 ");
                                    //registryDate = registryDate + "일";

                                    String contentUrl = jsonArray.getJSONObject(i).getString("contentUrl");
                                    int likeCount = jsonArray.getJSONObject(i).getInt("likeCount");
                                    int commentCount = jsonArray.getJSONObject(i).getInt("commentCount");

                                    //미디어 사이즈를 확인하고 저장한다
                                    String mediaSize = jsonArray.getJSONObject(i).getString("mediaSize");
                                    String mediaSizeArr[] = mediaSize.split(",");

                                    int mediaWidth;
                                    int mediaHeight;

                                    if(contentType.contains("video")) {
                                        mediaWidth = 960;
                                        mediaHeight = 720;

                                    } else {

                                        mediaWidth = Integer.parseInt(mediaSizeArr[0]);
                                        mediaHeight = Integer.parseInt(mediaSizeArr[1]);
                                    }

                                    //베스트 댓글이 없는 경우
                                    if (!jsonArray.getJSONObject(i).has("bestCommentItems")) {
                                        Contents content = new Contents(contentUrl, contentType, registryDate, likeCount, 0, null, null, null, null,
                                                null, null, null, null, null, commentCount, 0, contentId, iLikeThis, descriptions, userId, videoScreenshotUrl, status, reportCount,
                                                mediaWidth, mediaHeight);

                                        contentsArrayList.add(content);

                                    } else {

                                        JSONArray commentJSONArray = jsonArray.getJSONObject(i).getJSONArray("bestCommentItems");
                                        int numberOfBestComment = commentJSONArray.length();

                                        String commentUrl[] = new String[3];
                                        String commentDescription[] = new String[3];
                                        String commentType[] = new String[3];

                                        //베스트 댓글 갯수만큼 회전후 정보 저장
                                        for(int j=0; j<numberOfBestComment; j++) {
                                            commentUrl[j] = commentJSONArray.getJSONObject(j).getString("commentUrl");
                                            commentType[j] = commentJSONArray.getJSONObject(j).getString("contentType");
                                            commentDescription[j] = commentJSONArray.getJSONObject(j).getString("descriptions");
                                        }

                                        Contents content = new Contents(contentUrl, contentType, registryDate, likeCount, numberOfBestComment, commentUrl[0], commentUrl[1], commentUrl[2], commentType[0],
                                                commentType[1], commentType[2], commentDescription[0], commentDescription[1], commentDescription[2], commentCount, 0, contentId, iLikeThis, descriptions, userId,
                                                videoScreenshotUrl, status, reportCount, mediaWidth, mediaHeight);

                                        contentsArrayList.add(content);

                                    }

                                }

                                if(adapterFlag == 0) {
                                    contentsList.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                if(getActivity() == null) {
                                    return;
                                }

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        }, 2000);

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;
                                        listViewLoadIndicator.setVisibility(View.GONE);
                                    }
                                });

                                mSwipeRefresh.setRefreshing(false);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                //Toast.makeText(getActivity(), "JSONException", Toast.LENGTH_LONG).show();
                            }
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

                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", sid);

                Log.d("mypageSid", GlobalSharedPreference.getAppPreferences(getActivity(), "sid"));
                return params;

            }
        };
        queue.add(jsonObjectRequest);
    }


    public void myPageSummaryRequest() {

        String endPoint = "/mypage/summary";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if(responseCode == 200) {

                                JSONObject jsonObject = response.getJSONObject("summary");
                                int followerCount = jsonObject.getInt("followerCount");
                                int followingCount = jsonObject.getInt("followingCount");
                                int contentCount = jsonObject.getInt("contentCount");

                                txtFollowing.setText(Integer.toString(followingCount));
                                txtFollower.setText(Integer.toString(followerCount));
                                txtMoment.setText(Integer.toString(contentCount));

                                //콘텐츠가 하나도 없을 경우 기본 이미지를 출력한다.
                                if (contentCount == 0) {
                                    noMomentContainer.setVisibility(View.VISIBLE);
                                    imgNoMoment.setImageResource(R.drawable.img_no_moment_01_960x960_02);
                                    contentStart.setImageResource(R.drawable.mypage_img_01);

                                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    String time = sdfNow.format(new Date(System.currentTimeMillis()));

                                    time = time.substring(0, time.indexOf(" "));
                                    time = time.replaceFirst("\\/", ".");
                                    time = time.replaceFirst("\\/", ".");

                                    txtNoMomentTime.setText(time);

                                } else {
                                    noMomentContainer.setVisibility(View.GONE);
                                }

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
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
                    params.put("sessionId", sid);

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }


    public void updateList(ArrayList<Contents> newList) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetInvalidated();
                adapter.notifyDataSetChanged();

            }
        });

    }

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    TEMP_PHOTO_FILE);
            try {
                f.createNewFile();      // 외장메모리에 temp.jpg 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    class RunnableImplements implements Runnable {

        public void run() {
            UploadContents upload = new UploadContents();

            String endPoint = "/users/user/profile/image";
            try {
                upload.HttpFileUpload(GlobalUrl.BASE_URL + endPoint, filePath, getContext());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        uploadReceiver.register(getActivity());
        //currentPage = 0;
        //adapterFlag = 0;

        if(GlobalSharedPreference.getAppPreferences(getActivity(), "login").equals("login"))
            myPageSummaryRequest();

        String pageName = "Mypage";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        uploadReceiver.unregister(getActivity());
    }

    @Override
    public void onCommentBtnClicked(int position) {
        int contentType;
        int imageContent;
        int numberOfLike;
        int numberOfComment;

        imageContent = contentsArrayList.get(position).mainContent;
        numberOfLike = contentsArrayList.get(position).likeCount;
        numberOfComment = contentsArrayList.get(position).commentCount;
        String description = contentsArrayList.get(position).descriptionTag;
        String contentId = contentsArrayList.get(position).contentsId;
        String iLikeThis = contentsArrayList.get(position).iLikeThis;
        String profileImageUrl = GlobalSharedPreference.getAppPreferences(getContext(), "profileImageUrl");
        String userName = GlobalSharedPreference.getAppPreferences(getContext(), "name");
        int likeCount = contentsArrayList.get(position).likeCount;
        int commentCount = contentsArrayList.get(position).commentCount;
        String userId = contentsArrayList.get(position).userId;
        String status = contentsArrayList.get(position).status;
        int reportCount = contentsArrayList.get(position).reportCount;

        Intent intent = new Intent(getContext(), ViewActivity.class);

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

        startActivityForResult(intent, REQ_CODE_MODIFY_CONTENT_INFO);
    }

}

