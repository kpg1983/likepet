package com.likelab.likepet.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alexbbb.uploadservice.MultipartUploadRequest;
import com.alexbbb.uploadservice.UploadServiceBroadcastReceiver;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.likelab.likepet.CircleTransform;
import com.likelab.likepet.R;
import com.likelab.likepet.UploadContents;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.global.GlobalVariable;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.likeUser.LikeUserListActivity;
import com.likelab.likepet.likeUser.LikeUserListContents;
import com.likelab.likepet.player.DemoPlayer;
import com.likelab.likepet.player.EventLogger;
import com.likelab.likepet.player.HlsRendererBuilder;
import com.likelab.likepet.singIn.JoinMemberBeginActivity;
import com.likelab.likepet.upload.InputContents;
import com.likelab.likepet.volleryCustom.AppController;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kpg1983 on 2015-10-12.
 */
public class ViewActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, SurfaceHolder.Callback,
        DemoPlayer.Listener, DemoPlayer.Id3MetadataListener, AudioCapabilitiesReceiver.Listener {

    ArrayList<ViewContents> contentsArrayList;
    ArrayList<LikeUserListContents> likeArrayList;
    int mContentType;

    ListView contentsListView;

    TextView txtUserProfileName;
    TextView txtContents;
    TextView txtNumberOfLike;

    ImageButton btnMoreLike;
    ImageButton btnPlay;
    ImageButton btnCamera;
    ImageButton btnLike;
    ImageButton btnMore;
    ImageButton btnDeleteImageComment;

    TextView txtMoreLike;

    ImageView imgMainContents;
    ImageView gifMainContents;
    ImageView imgUserProfile;
    ImageView imgComment;

    RelativeLayout btnBackContainer;
    RelativeLayout btnMoreContainer;
    RelativeLayout viewMainImageContainerPhoto;
    RelativeLayout videoContents;
    RelativeLayout btnSend;
    RelativeLayout imgCommentContainer;
    RelativeLayout header;

    EditText editComment;

    Bitmap selectedImage = null;   //사진 앨범에서 가져온 댓글 이미지

    ViewContentsAdapter adapter;

    RelativeLayout overlay; //팝업뷰를 띄울때 사용되는 검정색 반투명 배경화면
    String descriptionTag;

    ImageView imgLikeUser[] = new ImageView[5];

    File imageFilePart;

    //댓글이 0개일때 footer에 들어가게 될 뷰들
    RelativeLayout noCommentContainer;
    ImageView imgNoComment;
    TextView txtNoComment;

    private static final String TEMP_PHOTO_FILE = "temp.jpg";       // 임시 저장파일
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final int REQ_CODE_MODIFY_CONTENTS = 1;

    //onActivityResult에 사용될 result 변수
    private static final int RESULT_MODIFY_CONTENT_SUMMARY = 5;
    private static final int RESULT_DELETE_CONTENT = 7;

    View footer;

    String contentId = null;
    String commentPart;
    String imageURL;
    String sid;
    String userId;
    String filePath;

    int position;
    int likeCount;
    int commentCount;
    int numberOfLike;
    int numberOfComment;
    int currentPage = 0;
    int maxPage;
    int adapterFlag = 0;
    int likeFlag = 0;

    Intent likeIntent = new Intent();

    ArrayList<TextView> txtLikeList;

    private boolean lockListView = false;

    boolean sendRefreshFlag = false;

    private static final String TAG = "PlayerActivity";
    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private MediaController mediaController;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;

    private Uri contentUri;

    private BroadcastReceiver mReceiver;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    static String momentsAgo;
    static String minutesAgo;
    static String hoursAgo;
    static String daysAgo;
    static String monthAgo;
    static String yearsAgo;

    long mCurrentPosition = 0;
    boolean screenOff;

    View footerListLoader;
    RelativeLayout listViewLoaderContainer;

    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private RequestQueue queue = AppController.getInstance().getRequestQueue();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity);

        //멤버 변수 초기화
        initView();

        //뷰에 나타낼 컨텐츠들 표시
        inflateContents();

        //데이터 리퀘스트
        //컨텐츠 읽음 카운트를 1개 증가 시킨다.
        readContentsRequest(contentId);

        //베스트 코멘트를 호출
        //리퀘스트가 완료되면 전체 댓글들이 호출된다.
        loadBestCommentRequest(contentId);

        Log.d("contentId", contentId);


        //감정표현 리퀘스트 요청
        //로그인이 되어 있는 상태에서만 콘텐츠에 대한 감정 표현 유무가 표시된다.
        //if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
            contentEmotionUserListRequest(contentId);
       // }


        //페이지 더 불러오기 표시 listview footer
        footerListLoader = getLayoutInflater().inflate(R.layout.listview_load_footer, null, false);
        listViewLoaderContainer = (RelativeLayout) footerListLoader.findViewById(R.id.listview_load_indicator);

        contentsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int count = totalItemCount - visibleItemCount;

                //댓글을 등록했을때 onScroll이 예상치 못하게 호출되어서 중복 호출을 방지하기 위하여 설정한 변수
                //차후에 다시 확인해 보자
                if (sendRefreshFlag == true) {
                    currentPage = 0;
                }

                if (firstVisibleItem >= count && totalItemCount != 0 && lockListView == false && sendRefreshFlag == false) {

                    currentPage = currentPage + 1;

                    if (currentPage < maxPage) {
                        loadAllCommentRequest(contentId, currentPage);

                    }
                }

                sendRefreshFlag = false;
            }

        });


        final InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);



        editComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {

                } else {
                    InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                    editComment.clearFocus();

                    loginPopupRequest(v);
                }
            }
        });

        contentsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int tempPosition, long id) {

                final int position = tempPosition - 1;

                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {

                    final PopupWindow popupWindow = new PopupWindow();
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View popupView;

                    overlay.setVisibility(View.VISIBLE);

                    RelativeLayout deleteConfirmContainer = null;
                    RelativeLayout deleteCancelContainer = null;

                    RelativeLayout reportConfirmContainer = null;
                    RelativeLayout reportCancelContainer = null;

                    //나의 댓글일 경우, 삭제하기 기능
                    if (contentsArrayList.get(position).userId.equals(GlobalSharedPreference.getAppPreferences(ViewActivity.this, "userId"))) {
                        popupView = inflater.inflate(R.layout.comment_delete_popup_window, null);

                        deleteConfirmContainer = (RelativeLayout) popupView.findViewById(R.id.comment_delete_confirm_container);
                        deleteCancelContainer = (RelativeLayout) popupView.findViewById(R.id.comment_delete_cancel_container);


                        deleteConfirmContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteCommentRequest(contentsArrayList.get(position).contentId, contentsArrayList.get(position).commentId, position);
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
                                overlay.setVisibility(View.INVISIBLE);
                            }
                        });

                        //상대방 댓글일 경우 신고하기
                    } else {
                        popupView = inflater.inflate(R.layout.comment_report_popup_window, null);

                        reportConfirmContainer = (RelativeLayout) popupView.findViewById(R.id.comment_report_confirm_container);
                        reportCancelContainer = (RelativeLayout) popupView.findViewById(R.id.comment_report_cancel_container);

                        overlay.setVisibility(View.VISIBLE);

                        reportConfirmContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                reportCommentRequest(contentsArrayList.get(position).contentId, contentsArrayList.get(position).commentId);
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
                                overlay.setVisibility(View.INVISIBLE);
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
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                } else {
                    loginPopupRequest(view);
                }

                return false;
            }
        });

        //키보드 이외부분을 터치하면
        contentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                editComment.clearFocus();

            }
        });

        //이미지 및 gif 컨텐츠를 클릭하면 키보드를 감춘다
        if(mContentType == 1 || mContentType == 3) {

            imgMainContents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                    editComment.clearFocus();
                }
            });
            gifMainContents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                    editComment.clearFocus();
                }
            });
        }


        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {

                    final PopupWindow popupWindow = new PopupWindow(v);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView;

                    if (likeFlag == 0) {
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


                    if (likeFlag == 0) {

                        //좋아요 캐릭터를 클릭하면 좋아요 버튼이 빨간색으로 바뀐다.
                        btn_like_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                clickLike();    //감정 표현 아이콘 셋팅, 좋아요  + 1
                                //좋아요  + 셋팅

                                registryEmotionRequest(contentId, 0, position);

                            }
                        });
                        btn_like_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                clickLike();
                                registryEmotionRequest(contentId, 1, position);

                            }
                        });
                        btn_like_3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                clickLike();
                                registryEmotionRequest(contentId, 2, position);

                            }
                        });
                        btn_like_4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                clickLike();
                                registryEmotionRequest(contentId, 3, position);

                            }
                        });
                    } else {
                        btn_cancel_like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();

                                likeFlag = 0;
                                btnLike.setImageResource(R.drawable.mypage_btn_bottom_like_n);
                                numberOfLike = numberOfLike - 1;
                                txtNumberOfLike.setText(Integer.toString(numberOfLike));
                                deleteEmotionRequest(contentId, position);

                            }
                        });
                    }

                } else {
                    loginPopupRequest(v);
                }
            }
        });


        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length

                //텍스트 내용이 200자를 넘어갈 경우, 마지막 글자 삭제후 커서를 맨뒤로 이동
                if (editComment.getText().length() > 150) {
                    Toast.makeText(ViewActivity.this, getResources().getString(R.string.input_contents_toast_too_long),
                            Toast.LENGTH_SHORT).show();

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editComment.setText(editComment.getText().toString().substring(0, 150));
                    editComment.setSelection(editComment.getText().toString().length());
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        editComment.addTextChangedListener(mTextEditorWatcher);

        //동영상 재생중일 경우 전화 또는 화면일 꺼졌을때 처리하기 위함
        //리시버 등록
        MyPhoneStateListener phoneListener = new MyPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    screenOff = true;
                    if(player != null && player.getPlayerControl().isPlaying()) {
                        player.getPlayerControl().pause();
                        mCurrentPosition = player.getCurrentPosition();
                    }

                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    screenOff = false;
                } else if(action.equals(Intent.ACTION_USER_PRESENT)) {
                    onResume();
                }
            }
        };
        registerReceiver(mReceiver, filter);

    }

    public void initView() {

        //시간을 며칠 전 식으로 변경하기 위험
        momentsAgo = getResources().getString(R.string.now);
        minutesAgo = getResources().getString(R.string.minute_ago);
        hoursAgo = getResources().getString(R.string.hour_ago);
        daysAgo = getResources().getString(R.string.day_ago);
        monthAgo = getResources().getString(R.string.month_ago);
        yearsAgo = getResources().getString(R.string.year_ago);

        sid = GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid");

        likeArrayList = new ArrayList<LikeUserListContents>();
        txtLikeList = new ArrayList<TextView>();

        //팝업윈도우를 띄울때 사용되는 검정색 반투명 배경
        overlay = (RelativeLayout) findViewById(R.id.view_overlay);

        btnBackContainer = (RelativeLayout) findViewById(R.id.view_back_key_container);
        btnMoreContainer = (RelativeLayout) findViewById(R.id.view_btn_more_container);

        btnBackContainer.setOnClickListener(this);
        btnMoreContainer.setOnClickListener(this);

        imgUserProfile = (ImageView) findViewById(R.id.view_img_user_profile);
        txtUserProfileName = (TextView) findViewById(R.id.view_txt_user_name);
        btnMore = (ImageButton) findViewById(R.id.view_btn_more);

        btnCamera = (ImageButton) findViewById(R.id.view_btn_camera);
        editComment = (EditText) findViewById(R.id.view_edit_comment);
        btnSend = (RelativeLayout) findViewById(R.id.view_send_container);

        imgCommentContainer = (RelativeLayout) findViewById(R.id.view_img_comment_container);
        imgComment = (ImageView) findViewById(R.id.view_img_comment);
        btnDeleteImageComment = (ImageButton) findViewById(R.id.view_img_cancel);

        btnMore.setImageResource(R.drawable.view_more_btn);
        btnCamera.setImageResource(R.drawable.view_btn_photo);

        btnMore.setScaleType(ImageView.ScaleType.FIT_XY);
        btnCamera.setScaleType(ImageView.ScaleType.FIT_XY);

        footer = getLayoutInflater().inflate(R.layout.view_listview_footer, null, false);
        noCommentContainer = (RelativeLayout) footer.findViewById(R.id.view_no_comment_container);
        imgNoComment = (ImageView) footer.findViewById(R.id.view_img_no_comment);
        txtNoComment = (TextView) footer.findViewById(R.id.view_txt_no_comment);

        Intent intent = getIntent();
        int reportCount = intent.getExtras().getInt("REPORT_COUNT");
        String status = intent.getExtras().getString("STATUS");
        mContentType = intent.getExtras().getInt("TYPE");

        if (mContentType == 1 || mContentType == 3 || status.equals("1") || reportCount > 10) { //1 = 사진, 3 = 움짤
            header = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_listview_header_photo, null, false);
            imgMainContents = (ImageView) header.findViewById(R.id.view_img_main_contents);
            gifMainContents = (ImageView) header.findViewById(R.id.view_gif_main_contents);
            viewMainImageContainerPhoto = (RelativeLayout) header.findViewById(R.id.view_photo_img_contents_container);

        } else {
            header = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_listview_header_video, null, false);
            videoContents = (RelativeLayout) header.findViewById(R.id.video_video_view_container);

        }

        txtContents = (TextView) header.findViewById(R.id.view_txt_contents);
        txtNumberOfLike = (TextView) header.findViewById(R.id.view_txt_numberOfLike);
        txtMoreLike = (TextView) header.findViewById(R.id.view_txt_moreLike);
        btnMoreLike = (ImageButton) header.findViewById(R.id.view_btn_moreLike);


        btnLike = (ImageButton) header.findViewById(R.id.view_btn_like);
        imgLikeUser[0] = (ImageView) header.findViewById(R.id.view_img_likeUserProfile_1);
        imgLikeUser[1] = (ImageView) header.findViewById(R.id.view_img_likeUserProfile_2);
        imgLikeUser[2] = (ImageView) header.findViewById(R.id.view_img_likeUserProfile_3);
        imgLikeUser[3] = (ImageView) header.findViewById(R.id.view_img_likeUserProfile_4);
        imgLikeUser[4] = (ImageView) header.findViewById(R.id.view_img_likeUserProfile_5);

        btnCamera.setOnClickListener(this);
        btnDeleteImageComment.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        editComment.setOnClickListener(this);
        editComment.setOnFocusChangeListener(this);
        btnMoreLike.setOnClickListener(this);

        for(int i=0; i<5; i++) {
            imgLikeUser[i].setOnClickListener(this);
        }

        contentsListView = (ListView) findViewById(R.id.view_comments_list);
        contentsArrayList = new ArrayList<ViewContents>();
        adapter = new ViewContentsAdapter(getApplicationContext(), R.layout.view_listview, contentsArrayList, ViewActivity.this);
    }

    public void inflateContents() {

        Intent intent = getIntent();

        String iLikeThis = intent.getExtras().getString("ILIKETHIS");
        contentId = intent.getExtras().getString("CONTENT_ID");
        numberOfLike = intent.getExtras().getInt("LIKES");
        numberOfComment = intent.getExtras().getInt("NUMBER_OF_COMMENT");
        imageURL = intent.getExtras().getString("IMAGE_URL");
        descriptionTag = intent.getExtras().getString("DESC");
        String userName = intent.getExtras().getString("NAME");
        position = intent.getExtras().getInt("POSITION");
        likeCount = intent.getExtras().getInt("LIKE_COUNT");
        commentCount = intent.getExtras().getInt("COMMENT_COUNT");
        userId = intent.getExtras().getString("USER_ID");
        int reportCount = intent.getExtras().getInt("REPORT_COUNT");
        String status = intent.getExtras().getString("STATUS");
        String profileImageUrl = intent.getExtras().getString("PROFILE_IMAGE_URL");
        String clan = intent.getExtras().getString("CLAN");



        //감정표현 등록 유무 확인 후 하트 색깔을 다르게 표시한다
        likeFlag = Integer.parseInt(iLikeThis);
        if (likeFlag == 0) {
            btnLike.setImageResource(R.drawable.mypage_btn_bottom_like_n);
            btnLike.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            btnLike.setImageResource(R.drawable.mypage_btn_bottom_like_s);
            btnLike.setScaleType(ImageView.ScaleType.FIT_XY);
        }


        //게시글 유저의 프로필 이미지 설정
        //유저가 설정한 이미지가 없는 경우 종족에 따른 기본 이미지 셋팅팅
        if(clan.equals("0")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_01)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(imgUserProfile);
        } else if(clan.equals("1")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_02)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(imgUserProfile);
        } else if(clan.equals("2")) {
            Picasso.with(this)
                    .load(profileImageUrl).placeholder(R.drawable.feed_profile_noimage_03)
                    .resize(200, 200)
                    .transform(new CircleTransform()).into(imgUserProfile);
        }

        txtUserProfileName.setText(userName);
        txtNumberOfLike.setText(Integer.toString(numberOfLike));
        txtContents.setText(descriptionTag);

        btnDeleteImageComment.setImageResource(R.drawable.view_btn_photocancel);
        btnDeleteImageComment.setScaleType(ImageView.ScaleType.FIT_XY);

        contentsListView.addHeaderView(header);
        contentsListView.addFooterView(footer, null, false);    //footer은 클릭 이벤트 방지

        //블라인드 처리된 컨텐츠 들도 사진 뷰로 나타낸다
        if (mContentType == 1 || mContentType == 3 || status.equals("1") || reportCount > 10) { //1 = 사진, 3 = 움짤

            ImageView imgBlind = (ImageView) header.findViewById(R.id.view_img_blind);

            //상태 값이 1 이거나 report count 가 10보다 크면 블라인드 처리한다.
            if (status.equals("1") || reportCount > 10) {
                imgBlind.setVisibility(View.VISIBLE);
                imgBlind.setImageResource(R.drawable.mypage_view_contents_blind);

            } else {
                imgBlind.setVisibility(View.GONE);
                imgBlind.setImageBitmap(null);

                //이미지와 움짤의 경우 view를 다르게 설정함
                if (mContentType == 1) {
                    imgMainContents.setVisibility(View.VISIBLE);
                    Picasso.with(this).load(imageURL).into(imgMainContents);
                    //imgMainContents.setImageUrl(imageURL, imageLoader);

                } else {
                    gifMainContents.setVisibility(View.VISIBLE);
                    //Glide.with(this).load(imageURL).asGif().into(gifMainContents);
                    Ion.with(this).load(imageURL).intoImageView(gifMainContents);

                }
            }

            //동영상은 기본 videoView가 아닌 exoplayer을 사용한다.
        } else if (mContentType == 2) { //동영상

            View root = header.findViewById(R.id.upload_video_root);

            videoContents.setVisibility(View.VISIBLE);
            root.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        toggleControlsVisibility();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        view.performClick();
                    }
                    return true;
                }
            });
            root.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                            || keyCode == KeyEvent.KEYCODE_MENU) {
                        return false;
                    }
                    return mediaController.dispatchKeyEvent(event);
                }
            });

            videoFrame = (AspectRatioFrameLayout) header.findViewById(R.id.upload_video_frame);
            surfaceView = (SurfaceView) header.findViewById(R.id.upload_video_surface_view);
            surfaceView.getHolder().addCallback(this);

            mediaController = new KeyCompatibleMediaController(this);
            mediaController.setAnchorView(root);

            CookieHandler currentHandler = CookieHandler.getDefault();
            if (currentHandler != defaultCookieManager) {
                CookieHandler.setDefault(defaultCookieManager);
            }

            audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
            audioCapabilitiesReceiver.register();

            contentUri = Uri.parse(imageURL);

            if (player == null) {
                if (!maybeRequestPermission()) {
                    preparePlayer(true);
                }
            } else {
                player.setBackgrounded(false);
            }

            btnPlay = (ImageButton) header.findViewById(R.id.view_btn_play);
            btnPlay.setVisibility(View.INVISIBLE);

        }

        //댓글 버튼을 누르고 view 페이지로 들어온 경우 키보드를 나타낸다
        if (intent.hasExtra("IS_COMMENT_PRESSED")) {


            String isCommentPressed = intent.getStringExtra("IS_COMMENT_PRESSED");
            if(isCommentPressed.equals("ok") && GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editComment.requestFocus();
                                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.showSoftInput(editComment, 0);
                            }
                        });
                    }
                }, 1000);
            }
        }

        likeIntent.putExtra("POSITION", position);
        setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

    }

    //댓글 신고하기
    public void reportCommentRequest(String contentId, String commentId) {

        String endPoint = "/comment/" + contentId + "/" + commentId + "/report";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_report_content),
                                        Toast.LENGTH_SHORT).show();

                            } else if (responseCode == 409) {
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_report_content_already),
                                        Toast.LENGTH_SHORT).show();
                            } else if (responseCode == 401) {

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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    public void onFocusChange(View v, boolean hasFocus) {

        if (hasFocus == true) {

            switch (v.getId()) {

                //edit text를 터치했을때 로그인 상태가 아니면 회원 가입 팝업을 띄운다.
                case R.id.view_edit_comment: {
                    if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                        contentsListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (contentsListView.getLastVisiblePosition() == contentsListView.getCount()) {
                                    contentsListView.smoothScrollToPosition(contentsListView.getCount());
                                }
                                contentsListView.smoothScrollToPosition(contentsListView.getCount());

                            }
                        }, 100);
                        break;
                    } else {
                        loginPopupRequest(v);
                    }
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            //뒤로 가기 버튼. 플레이어를 릴리즈 한다.
            case R.id.view_back_key_container:
                if (mContentType == 3) {
                    gifMainContents.setImageDrawable(null);
                }

                releasePlayer();
                finish();
                break;

            case R.id.view_btn_more_container: {

                //상단의 더보기 버튼 클릭 이벤트
                //자신의 컨텐츠인 경우 삭제하기, 수정하기 기능
                if (userId.equals(GlobalSharedPreference.getAppPreferences(ViewActivity.this, "userId"))) {

                    final PopupWindow popupWindow = new PopupWindow();
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.view_more_popup_window_mine, null);

                    overlay.setVisibility(View.VISIBLE);

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

                    int[] location = new int[2];
                    v.getLocationOnScreen(location);

                    //팝업 윈도우 위치 조정
                    popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - popupWindow.getHeight() + 135);

                    final RelativeLayout delete = (RelativeLayout) popupView.findViewById(R.id.view_delete_container);
                    RelativeLayout modify = (RelativeLayout) popupView.findViewById(R.id.view_modify_container);

                    //삭제하기 버튼
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            popupWindow.dismiss();
                            overlay.setVisibility(View.INVISIBLE);
                            deleteContents();
                        }
                    });

                    //수정하기 버튼. 글쓰기 페이지로 이동한다.
                    modify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            overlay.setVisibility(View.INVISIBLE);
                            popupWindow.dismiss();

                            Intent intent = new Intent(ViewActivity.this, InputContents.class);

                            intent.putExtra("DESC", descriptionTag);
                            intent.putExtra("URL", imageURL);
                            intent.putExtra("CONTEND_ID", contentId);
                            intent.putExtra("CONTENT_TYPE", mContentType);

                            //글쓰기가 완료되면 view 액티비티로 돌아왔을때 내용이 반영이 되어야 한다
                            startActivityForResult(intent, REQ_CODE_MODIFY_CONTENTS);

                        }
                    });

                } else {

                    //타인의 컨텐츠인 경우 북마크, 신고하기 기능 등등
                    final PopupWindow popupWindow = new PopupWindow();
                    LayoutInflater inflater = (LayoutInflater) ViewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.view_more_popup_window, null);

                    overlay.setVisibility(View.VISIBLE);

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
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    //팝업 윈도우 위치 조정
                    popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - popupWindow.getHeight() + 135);

                    RelativeLayout report = (RelativeLayout) popupView.findViewById(R.id.view_report_container);
                    RelativeLayout share = (RelativeLayout) popupView.findViewById(R.id.view_share_container);
                    RelativeLayout bookmark = (RelativeLayout) popupView.findViewById(R.id.view_bookmark_container);


                    //즐겨찾기
                    bookmark.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                bookmarkContentsRequest(contentId);
                                popupWindow.dismiss();
                                overlay.setVisibility(View.INVISIBLE);
                            } else {
                                loginPopupRequest(v);
                            }
                        }
                    });

                    //신고하기
                    report.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                reportContentRequest(contentId);
                                popupWindow.dismiss();
                                overlay.setVisibility(View.INVISIBLE);
                            } else {
                                loginPopupRequest(v);
                            }
                        }
                    });


                    //공유하기
                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                shareContents(ViewActivity.this, "Here comes LikePet http://www.likelab.co.kr/share.php");
                                overlay.setVisibility(View.INVISIBLE);
                                popupWindow.dismiss();
                            } else {
                                loginPopupRequest(v);
                            }
                        }
                    });
                }

                break;
            }
            case R.id.view_btn_camera: {

                //이미지 댓글을 위한 버튼 이벤트
                //갤러리를 오픈한 후  정사각형 모양으로 crop 한다
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {

                    Intent intent = new Intent(
                            Intent.ACTION_PICK,      // 또는 ACTION_PICK
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");              // 모든 이미지
                    intent.putExtra("crop", "true");        // Crop기능 활성화\
                    intent.putExtra("aspectX", 1); //이걸 삭제한다
                    intent.putExtra("aspectY", 1); //이걸 삭제한다
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
                    intent.putExtra("outputFormat",         // 포맷방식
                            Bitmap.CompressFormat.JPEG.toString());

                    startActivityForResult(intent, REQ_CODE_PICK_IMAGE);

                } else {
                    loginPopupRequest(v);
                }
                break;
            }

            //사진 댓글을 취소 하려는 경우, 엑스 표시를 누르면 불러온 사진을 감춘다.
            case R.id.view_img_cancel: {
                imgCommentContainer.setVisibility(View.INVISIBLE);
                break;
            }

            //전송 버튼을 누르면 업로드 프로세스를 진행하면서
            //글쓰기 화면을 전부 초기화 시킨다
            case R.id.view_send_container: {

                String endPoint = "/comment/" + contentId;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                editComment.clearFocus();

                //사진과 내용이 둘다 있을 경우
                if (selectedImage != null && !editComment.getText().toString().equals("")) {

                    String txtComment = editComment.getText().toString();

                    imgCommentContainer.setVisibility(View.GONE);
                    selectedImage = null;

                    imageFilePart = new File(filePath);
                    commentPart = txtComment;
                    try {
                        upload(filePath, commentPart);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    editComment.setText(null);  //텍스트 내용을 지운다

                    //scrollMyListViewToBottom(); // 글 등록 시 리스트 뷰의 마지막 하단으로 이동
                    //글만 있을 경우
                } else if (selectedImage == null && !editComment.getText().toString().equals("")) {
                    String txtComment = editComment.getText().toString();
                    commentPart = txtComment;
                    editComment.setText(null);  //텍스트 내용을 지운다

                    Runnable runnable = new RunnableComment();
                    Thread thread = new Thread(runnable);
                    thread.start();
                    selectedImage = null;



                    //사진만 있을 경우
                } else if (selectedImage != null && editComment.getText().toString().equals("")) {
                    Runnable runnable = new RunnableImplements();
                    Thread thread = new Thread(runnable);
                    thread.start();

                    imgCommentContainer.setVisibility(View.GONE);
                    selectedImage = null;

                }
                break;
            }
            case R.id.view_btn_play: {

                btnPlay.setVisibility(View.INVISIBLE);
                break;
            }

            //감정표현 유저 리스트 페이지로 이동
            case R.id.view_btn_moreLike:
            case R.id.view_img_likeUserProfile_1:
            case R.id.view_img_likeUserProfile_2:
            case R.id.view_img_likeUserProfile_3:
            case R.id.view_img_likeUserProfile_4:
            case R.id.view_img_likeUserProfile_5: {

                Intent intent = new Intent(ViewActivity.this, LikeUserListActivity.class);
                intent.putExtra("LIKE_USERS", likeArrayList);
                intent.putExtra("CONTENT_ID", contentId);
                startActivity(intent);
                break;

            }

        }
    }

    /**
     * 임시 저장 파일의 경로를 반환
     */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /**
     * 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환
     */
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

    //컨텐츠(게시물) 삭제
    private void deleteContents() {

        final PopupWindow popupWindow = new PopupWindow();
        LayoutInflater inflater = (LayoutInflater) ViewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.delete_contents_popup_window, null);

        View v = inflater.inflate(R.layout.view_activity, null);

        overlay.setVisibility(View.VISIBLE);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        RelativeLayout deleteConfirmContainer = (RelativeLayout) popupView.findViewById(R.id.view_delete_confirm_container);
        RelativeLayout deleteCancelContainer = (RelativeLayout) popupView.findViewById(R.id.view_delete_cancel_container);

        //확인 버튼을 누르면 게시물을 삭제한다.
        deleteConfirmContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContentsRequest(contentId);
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
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);
            }

        });


        //팝업 윈도우 위치 조정
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

    }

    /**
     * SD카드가 마운트 되어 있는지 확인
     */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    /**
     * 다시 액티비티로 복귀하였을때 이미지를 셋팅
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                        selectedImage = BitmapFactory.decodeFile(filePath);
                        // temp.jpg파일을 Bitmap으로 디코딩한다.
                        imgComment.setImageBitmap(selectedImage);
                        // temp.jpg파일을 이미지뷰에 씌운다.
                        imgCommentContainer.setVisibility(View.VISIBLE);

                    }
                }
                break;
            //컨텐츠 수정
            case REQ_CODE_MODIFY_CONTENTS:

                if (resultCode == RESULT_OK) {

                    Toast.makeText(ViewActivity.this, getResources().getString(R.string.input_contents_btn_finish),
                            Toast.LENGTH_SHORT).show();

                    String description = imageData.getStringExtra("DESC");

                    if (!description.equals("null")) {
                        txtContents.setVisibility(View.VISIBLE);

                    }
                    txtContents.setText(description);

                    Intent intent = new Intent();
                    intent.putExtra("DESC", description);
                    intent.putExtra("POSITION", position);
                    setResult(RESULT_MODIFY_CONTENT_SUMMARY, intent);
                }
                break;


        }

    }


    //댓글 좋아요 이벤트
    private void clickLike() {
        likeFlag = 1;
        btnLike.setImageResource(R.drawable.mypage_btn_bottom_like_s);
        numberOfLike = numberOfLike + 1;
        txtNumberOfLike.setText(Integer.toString(numberOfLike));
    }


    //컨텐츠 읽음 리퀘스트
    public void readContentsRequest(String contentId) {

        String endPoint = "/contents/" + contentId + "/read";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            //성공을 하더라도 클라이언트에서 별다른 처리가 필요 없다
                            if (responseCode == 200) {

                            } else if (responseCode == 401) {


                            } else if (responseCode == 500) {

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                    params.put("sessionId", sid);
                }
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    //게시물 삭제 리퀘스트
    public void deleteContentsRequest(String contentId) {

        String endPoint = "/contents/" + contentId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            //게시물을 삭제하면 뷰페이지를 종료하면서
                            //이전 페이지에서는 리스트에서 게시물을 삭제한다.
                            if (responseCode == 200) {

                                Intent intent = new Intent();
                                intent.putExtra("POSITION", position);
                                setResult(RESULT_DELETE_CONTENT, intent);
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_delete_finish), Toast.LENGTH_LONG).show();
                                finish();

                            } else if (responseCode == 409) {


                            } else if (responseCode == 401) {


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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                    params.put("sessionId", sid);
                }
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }


    //즐겨찾기 추가 리퀘스트
    public void bookmarkContentsRequest(String contentId) {

        String endPoint = "/mypage/favorite/" + contentId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_add_bookmark),
                                        Toast.LENGTH_SHORT).show();

                            } else if (responseCode == 409) {

                                //이미 즐겨찾기에 추가된 경우
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_add_bookmark_already),
                                        Toast.LENGTH_SHORT).show();
                            } else if (responseCode == 401) {

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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", sid);

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    //댓글 삭제하기
    public void deleteCommentRequest(String contentId, String commentId, final int commentPosition) {

        String endPoint = "/comment/" + contentId + "/" + commentId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                likeIntent.putExtra("POSITION", position);
                                setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_delete_comment), Toast.LENGTH_LONG).show();
                                contentsArrayList.remove(commentPosition);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //댓글이 하나도 없는 경우 댓글 0개 사진 노출
                                        if (contentsArrayList.size() == 0) {
                                            noCommentContainer.setVisibility(View.VISIBLE);
                                            imgNoComment.setImageResource(R.drawable.view_img_no_comment_image);
                                            imgNoComment.setVisibility(View.VISIBLE);
                                        } else {
                                            noCommentContainer.setVisibility(View.GONE);
                                        }

                                        adapter.notifyDataSetChanged();
                                    }
                                });


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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));
                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //게시물 신고하기 리퀘스트
    public void reportContentRequest(String contentId) {

        String endPoint = "/contents/" + contentId + "/report";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_report_content), Toast.LENGTH_SHORT).show();

                            } else if (responseCode == 409) {
                                //이미 신고한 게시물인 경우
                                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_report_content_already), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", sid);

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    //감정표현 취소
    public void deleteEmotionRequest(final String contentId, final int position) {

        String endPoint = "/contents/" + contentId + "/feel";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {

                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                likeCount = likeCount - 1;
                                likeArrayList.clear();
                                contentEmotionUserListRequest(contentId);

                                likeIntent.putExtra("iLikeThis", "0");
                                likeIntent.putExtra("POSITION", position);
                                likeIntent.putExtra("LIKE_COUNT", likeCount);
                                setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //감정표현 등록 리퀘스트
    public void registryEmotionRequest(final String contentId, final int likeType, final int position) {

        JSONObject obj = new JSONObject();

        //감정 표현의 타입을 함께 전송한다.
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

                                //Toast.makeText(ViewActivity.this, "좋아용", Toast.LENGTH_SHORT).show();

                                likeArrayList.clear();
                                contentEmotionUserListRequest(contentId);

                                likeCount = likeCount + 1;

                                likeIntent.putExtra("iLikeThis", "1");
                                likeIntent.putExtra("POSITION", position);
                                likeIntent.putExtra("LIKE_COUNT", likeCount);

                                setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

                            }

                        } catch (JSONException e) {
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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //게시물을 좋아하는 유저들의 리스트
    public void contentEmotionUserListRequest(final String contentId) {

        String endPoint = "/contents/" + contentId + "/feel/users";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");
                            JSONObject comment = response.getJSONObject("feels");

                            if (responseCode == 200) {
                                JSONArray users = comment.getJSONArray("items");

                                for (int i = 0; i < users.length(); i++) {
                                    String userId = users.getJSONObject(i).getString("userId");
                                    String name = users.getJSONObject(i).getString("name");
                                    String gender = users.getJSONObject(i).getString("sex");
                                    String clan = users.getJSONObject(i).getString("clan");
                                    String profileImageUrl = users.getJSONObject(i).getString("profileImageUrl");
                                    int likeType = users.getJSONObject(i).getInt("likeType");

                                    String myFriend;

                                    if(GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                        myFriend = users.getJSONObject(i).getString("myFriend");
                                    } else {
                                        myFriend = "0";
                                    }

                                    LikeUserListContents contents = new LikeUserListContents(userId, name, gender, clan, profileImageUrl, likeType, myFriend);
                                    likeArrayList.add(contents);

                                }
                                likeContent();  //좋아요 유저 이미지
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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    //전체 댓글 불러오기 리퀘스트
    public void loadAllCommentRequest(final String contentId, int pageNo) {

        lockListView = true;

        String endPoint = "/comment/" + contentId + "?pageNo=" + pageNo;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                JSONObject comment = response.getJSONObject("comment");
                                JSONObject pages = comment.getJSONObject("pages");
                                maxPage = pages.getInt("max");
                                JSONArray best = comment.getJSONArray("items");


                                for (int i = 0; i < best.length(); i++) {
                                    String commentId = best.getJSONObject(i).getString("commentId");
                                    String description = best.getJSONObject(i).getString("descriptions");
                                    String commentUrl = best.getJSONObject(i).getString("commentUrl");
                                    String contentType = best.getJSONObject(i).getString("contentType");
                                    String registryDate = best.getJSONObject(i).getString("registryDate");

                                    Date date = null;
                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        String localTime = convertUtcToLocal(registryDate);
                                        date = format.parse(localTime);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    registryDate = formatTimeString(date);

                                    int likeCount = best.getJSONObject(i).getInt("likeCount");
                                    int reportCount = best.getJSONObject(i).getInt("reportCount");
                                    String name = best.getJSONObject(i).getString("name");
                                    String profileImageUrl = best.getJSONObject(i).getString("profileImageUrl");
                                    String gender = best.getJSONObject(i).getString("sex");
                                    String clan = best.getJSONObject(i).getString("clan");
                                    String iLikeThis;
                                    String userId = best.getJSONObject(i).getString("userId");

                                    //일반 댓글은 전부 플래그 값이 0이다.
                                    //flag는 베스트 댓글 장식을 표시하기 위하여 adapter class 에서 사용된다.
                                    int bestCommentFlag = 0;

                                    //로그인 되어 있는 경우, 댓글에 좋아요를 했는지 유무를 표시한다. 빨간색으로
                                    if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                        iLikeThis = best.getJSONObject(i).getString("ILikedThis");
                                    } else {
                                        iLikeThis = "0";
                                    }

                                    ViewContents contents = new ViewContents(commentId, description, commentUrl, contentType, registryDate, likeCount, reportCount, name,
                                            profileImageUrl, gender, clan, contentId, iLikeThis, userId, bestCommentFlag);
                                    contentsArrayList.add(contents);

                                }

                                //setAdapter 은 1번만 수행한다.
                                if (adapterFlag == 0) {
                                    contentsListView.setAdapter(adapter);
                                    adapterFlag = 1;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //댓글이 하나도 없는 경우 댓글 0개 사진 노출
                                        if (contentsArrayList.size() == 0) {
                                            noCommentContainer.setVisibility(View.VISIBLE);
                                            imgNoComment.setImageResource(R.drawable.view_img_no_comment_image);
                                            imgNoComment.setVisibility(View.VISIBLE);
                                        } else {
                                            imgNoComment.setImageDrawable(null);
                                            noCommentContainer.setVisibility(View.GONE);
                                        }

                                        adapter.notifyDataSetChanged();
                                        lockListView = false;

                                    }
                                });

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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }


    //베스트 댓글 리퀘스트
    public void loadBestCommentRequest(final String contentId) {

        String endPoint = "/comment/" + contentId + "/best";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GlobalUrl.BASE_URL + endPoint,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {
                                JSONArray best = response.getJSONArray("items");

                                for (int i = 0; i < best.length(); i++) {
                                    String commentId = best.getJSONObject(i).getString("commentId");
                                    String description = best.getJSONObject(i).getString("descriptions");

                                    String commentUrl = best.getJSONObject(i).getString("commentUrl");
                                    String contentType = best.getJSONObject(i).getString("contentType");
                                    String registryDate = best.getJSONObject(i).getString("registryDate");

                                    registryDate = registryDate.replaceAll("\\.", "-");
                                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                    Date date = null;

                                    //날짜를 조금전, 방금전, 4일전 식으로 변환한다
                                    try {
                                        date = format.parse(registryDate);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    registryDate = formatTimeString(date);

                                    int likeCount = best.getJSONObject(i).getInt("likeCount");
                                    int reportCount = best.getJSONObject(i).getInt("reportCount");
                                    String name = best.getJSONObject(i).getString("name");
                                    String profileImageUrl = best.getJSONObject(i).getString("profileImageUrl");
                                    String gender = best.getJSONObject(i).getString("sex");
                                    String clan = best.getJSONObject(i).getString("clan");

                                    String iLikeThis;

                                    //로그인 되어 있는 경우, 댓글에 좋아요를 했는지 유무를 표시한다. 빨간색으로
                                    if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login")) {
                                        iLikeThis = best.getJSONObject(i).getString("ILikedThis");
                                    } else {
                                        iLikeThis = "0";
                                    }
                                    String userId = best.getJSONObject(i).getString("userId");
                                    int bestCommentFlag = i + 1;    //베스트 코멘트 앞에 숫자를 붙여준다.  프로필 이미지에 장식을 붙여주기 위해서, 1: 골드, 2: 실버, 3: 브론즈 나머지 댓글들은 전부 0이다


                                    ViewContents contents = new ViewContents(commentId, description, commentUrl, contentType, registryDate, likeCount, reportCount, name, profileImageUrl,
                                            gender, clan, contentId, iLikeThis, userId, bestCommentFlag);
                                    contentsArrayList.add(contents);

                                }

                                //베스트 댓글 리퀘스트가 끝나면 곧바로 전체 댓글 리퀘스트를 실행한다.
                                loadAllCommentRequest(contentId, currentPage);

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
                if (GlobalSharedPreference.getAppPreferences(ViewActivity.this, "login").equals("login"))
                    params.put("sessionId", GlobalSharedPreference.getAppPreferences(ViewActivity.this, "sid"));

                params.put("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                        GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

                return params;

            }

        };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }


    private static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }


    public static String formatTimeString(Date tempDate) {

        long curTime = System.currentTimeMillis();
        long regTime = tempDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            // sec
            msg = momentsAgo;
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            // min
            msg = diffTime + minutesAgo;
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            // hour
            msg = (diffTime) + hoursAgo;
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            // day
            msg = (diffTime) + daysAgo;
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            // day
            msg = (diffTime) + monthAgo;
        } else {
            msg = (diffTime) + yearsAgo;
        }

        return msg;
    }

    //글만 업로드
    public void upload(String comment) throws Exception {

        String url = GlobalUrl.BASE_URL + "/comment/" + contentId;
        final MultipartUploadRequest request =
                new MultipartUploadRequest(ViewActivity.this, "custom-upload-id", url);


        request.addHeader("sessionId", sid);
        request.addHeader("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");

        request.addParameter("descriptions", comment);
        request.setCustomUserAgent("UploadServiceDemo/1.0");
        request.setMaxRetries(1);

        try {
            //Start upload service and display the notification
            request.startUpload();

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete upload request
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }

        //뷰페이지에서 뒤로 이동했을 경우 카운트를 추가 해주기 위함
        likeIntent.putExtra("POSITION", position);
        setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

    }

    //이미지, 텍스트 둘다 업로드
    public void upload(String filePath, String comment) throws Exception {

        String url = GlobalUrl.BASE_URL + "/comment/" + contentId;
        final MultipartUploadRequest request =
                new MultipartUploadRequest(ViewActivity.this, "custom-upload-id", url);

        request.addFileToUpload(filePath, "upfile", "temp.jpg", "image/jpeg");
        request.addHeader("sessionId", sid);
        request.addHeader("User-agent", "likepet/" + GlobalVariable.appVersion + "(" + GlobalVariable.deviceName + ";" +
                GlobalVariable.deviceOS + ";" + GlobalVariable.mnc + ";" + GlobalVariable.mcc +  ";" + GlobalVariable.countryCode + ")");
        request.addParameter("descriptions", comment);
        request.setCustomUserAgent("UploadServiceDemo/1.0");
        request.setMaxRetries(1);

        try {
            //Start upload service and display the notification
            request.startUpload();

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete upload request
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }

        //뷰페이지에서 뒤로 이동했을 경우 카운트를 추가 해주기 위함
        likeIntent.putExtra("POSITION", position);
        setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

    }

    //업로드 완료 브로드캐스트 리시버
    private final UploadServiceBroadcastReceiver uploadReceiver = new UploadServiceBroadcastReceiver() {

        // you can override this progress method if you want to get
        // the completion progress in percent (0 to 100)
        // or if you need to know exactly how many bytes have been transferred
        // override the method below this one
        @Override
        public void onProgress(String uploadId, int progress) {
            Log.i(TAG, "The progress of the upload with ID "
                    + uploadId + " is: " + progress);
        }

        @Override
        public void onProgress(final String uploadId,
                               final long uploadedBytes,
                               final long totalBytes) {
            Log.i(TAG, "Upload with ID "
                    + uploadId + " uploaded bytes: " + uploadedBytes
                    + ", total: " + totalBytes);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            Log.e(TAG, "Error in upload with ID: " + uploadId + ". "
                    + exception.getLocalizedMessage(), exception);
        }

        @Override
        public void onCompleted(String uploadId,
                                int serverResponseCode,
                                String serverResponseMessage) {
            Log.i(TAG, "Upload with ID " + uploadId
                    + " has been completed with HTTP " + serverResponseCode
                    + ". Response from server: " + serverResponseMessage);

            //댓글 쓰기가 완료되면 전체 댓글을 다시 로드한다.
            if (serverResponseCode == 200) {

                sendRefreshFlag = true;
                currentPage = 0;
                contentsArrayList.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetInvalidated();
                    }
                });
                loadBestCommentRequest(contentId);

                Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_registry_comment),
                        Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);

        header.setVisibility(View.VISIBLE);

        String pageName = "View";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        //비디오 플레이 중 화면이 꺼졌을 경우
        if (mContentType == 2) {
            if (screenOff == true) {
                if (player.getPlayerControl().isPlaying() && player != null) {
                    player.getPlayerControl().pause();
                }
            } else {
                if (mCurrentPosition != 0)
                    player.seekTo(mCurrentPosition);
                //mVideoView.start();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);

        if (mContentType == 2) {
            if (player != null) {
                if (player.getPlayerControl().isPlaying()) {
                    player.getPlayerControl().pause();
                    mCurrentPosition = player.getCurrentPosition();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //메모리 반환을 실시한다.
        if (audioCapabilitiesReceiver != null)
            audioCapabilitiesReceiver.unregister();
        releasePlayer();

        unregisterReceiver(mReceiver);

        if (mContentType == 3) {
            gifMainContents.setImageDrawable(null);

        }
        if (mContentType == 1) {
            imgMainContents.setImageDrawable(null);

        }

        if (adapter != null) {
            adapter.recycle();
        }

        RecycleUtils.recursiveRecycle(imgMainContents);
        RecycleUtils.recursiveRecycle(gifMainContents);
        RecycleUtils.recursiveRecycle(imgNoComment);
        RecycleUtils.recursiveRecycle(imgUserProfile);

        for (int i = 0; i < imgLikeUser.length; i++) {
            RecycleUtils.recursiveRecycle(imgLikeUser[i]);
        }

        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();

    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            contentsArrayList.clear();
            adapter.notifyDataSetInvalidated();
            loadBestCommentRequest(contentId);

        }
    };

    //사진만 업로드
    class RunnableImplements implements Runnable {

        String resultCode;

        public void run() {
            UploadContents upload = new UploadContents();

            String endPoint = "/comment/" + contentId;
            try {
                resultCode = upload.HttpImageCommentUpload(GlobalUrl.BASE_URL + endPoint, filePath, ViewActivity.this);

                JSONObject obj = new JSONObject(resultCode);

                String result = obj.getString("code");

                if (result.equals("200")) {
                    sendRefreshFlag = true;
                    currentPage = 0;

                    contentsArrayList.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetInvalidated();
                        }
                    });

                    loadBestCommentRequest(contentId);

                    likeIntent.putExtra("POSITION", position);
                    setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);

                    Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_registry_comment),
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //댓글만 업로드
    class RunnableComment implements Runnable {

        String resultCode;

        public void run() {
            UploadContents uploadContents = new UploadContents();

            String endPoint = "/comment/" + contentId;
            try {
                resultCode = uploadContents.HttpCommentUpload(GlobalUrl.BASE_URL + endPoint, commentPart, ViewActivity.this);
                JSONObject obj = new JSONObject(resultCode);
                String result = obj.getString("code");

                if (result.equals("200")) {
                    sendRefreshFlag = true;

                    currentPage = 0;
                    contentsArrayList.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetInvalidated();
                            Toast.makeText(ViewActivity.this, getResources().getString(R.string.view_toast_registry_comment),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    loadBestCommentRequest(contentId);

                    likeIntent.putExtra("POSITION", position);
                    setResult(RESULT_MODIFY_CONTENT_SUMMARY, likeIntent);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shareContents(Context context, String content) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }


    public void likeContent() {

        int likeUserCount = likeArrayList.size();

        if (likeUserCount < 5) {
            for (int i = likeUserCount; i < 5; i++) {
                imgLikeUser[i].setImageResource(R.drawable.view_btn_03_02);
            }
        }

        //게시물에 감정표현을 한 유저의 수만큼 회전하며 프로필 이미지를 표시한다
        for (int i = 0; i < likeUserCount; i++) {

            //메소드 안에 final 변수가 대입 가능하므로 j에 i 값 대입
            final int j = i;

            //뷰페이지에서는 기본 5개만 나타낸다
            if (i != 5) {

                //유저의 프로필 이미지를 셋팅한다.
                //유저가 설정한 프로필 이미지가 없다면 종족에 따른 기본 이미지를 표시한다.
                if(likeArrayList.get(j).clan.equals("0")) {
                    Picasso.with(ViewActivity.this)
                            .load(likeArrayList.get(j).profileImageUrl).placeholder(R.drawable.feed_profile_noimage_01)
                            .resize(90, 90)
                            .transform(new CircleTransform()).into(imgLikeUser[j]);
                } else if(likeArrayList.get(j).clan.equals("1")) {
                    Picasso.with(ViewActivity.this)
                            .load(likeArrayList.get(j).profileImageUrl).placeholder(R.drawable.feed_profile_noimage_02)
                            .resize(90, 90)
                            .transform(new CircleTransform()).into(imgLikeUser[j]);
                } else if(likeArrayList.get(j).clan.equals("2")) {
                    Picasso.with(ViewActivity.this)
                            .load(likeArrayList.get(j).profileImageUrl).placeholder(R.drawable.feed_profile_noimage_03)
                            .resize(90, 90)
                            .transform(new CircleTransform()).into(imgLikeUser[j]);
                }


            } else {
                break;
            }

        }

    }

    private void loginPopupRequest(View v) {

        final PopupWindow popupWindow = new PopupWindow(v);
        LayoutInflater inflater = (LayoutInflater) ViewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.view_activity, null);
        final View popupView = inflater.inflate(R.layout.recommend_join_member_popup_windown, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
        editComment.clearFocus();

        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

        RelativeLayout joinLater = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_later_container);
        RelativeLayout joinNow = (RelativeLayout) popupView.findViewById(R.id.recommend_join_member_now_container);

        overlay.setVisibility(View.VISIBLE);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                overlay.setVisibility(View.INVISIBLE);
            }
        });

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
                Intent intent = new Intent(ViewActivity.this, JoinMemberBeginActivity.class);
                //intent.setFlags(intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }

    // Permission request listener method

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            preparePlayer(true);
        } else {
//            Toast.makeText(getApplicationContext(), R.string.storage_permission_denied,
//                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Permission management methods

    /**
     * Checks whether it is necessary to ask for permission to read storage. If necessary, it also
     * requests permission.
     *
     * @return true if a permission request is made. False if it is not necessary.
     */
    @TargetApi(23)
    private boolean maybeRequestPermission() {
        if (requiresPermission(contentUri)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private boolean requiresPermission(Uri uri) {
        return Util.SDK_INT >= 23
                && Util.isLocalFileUri(uri)
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void preparePlayer(boolean playWhenReady) {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        if (player == null) {
            player = new DemoPlayer(new HlsRendererBuilder(this, userAgent, contentUri.toString()));
            player.addListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mContentType == 2) {
            if (player != null) {
                if (player.getPlayerControl().isPlaying()) {
                    mCurrentPosition = player.getCurrentPosition();
                }
            }
        } else if(mContentType == 3) {
            gifMainContents.setImageDrawable(null);
        } else {
            imgMainContents.setImageDrawable(null);
        }

        imgNoComment.setImageDrawable(null);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mContentType == 3) {
            Ion.with(this).load(imageURL).intoImageView(gifMainContents);
        } else if(mContentType == 1) {
            Picasso.with(this).load(imageURL).into(imgMainContents);
        }

        if(contentsArrayList.size() == 0) {
            imgNoComment.setImageResource(R.drawable.view_img_no_comment_image);
        } else {
            imgNoComment.setImageDrawable(null);
        }
    }


    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
    }

    @Override
    public void onError(Exception e) {

        playerNeedsPrepare = true;

        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }


    @Override
    public void onId3Metadata(Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (TxxxMetadata.TYPE.equals(entry.getKey())) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s",
                        TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value));
            } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
                PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s",
                        PrivMetadata.TYPE, privMetadata.owner));
            } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
                GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename,
                        geobMetadata.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", entry.getKey()));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            Log.d("finish", "finish");
            releasePlayer();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (mContentType == 2) {
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    player.seekTo(mCurrentPosition);
                    //mVideoView.start();
                } else if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (player.getPlayerControl().isPlaying()) {
                        player.getPlayerControl().pause();
                        mCurrentPosition = player.getCurrentPosition();

                    }
                }
            }
        }
    }

    //표준시간과 local 시간 변환
    private static String convertUtcToLocal(String utcTime) {

        String localTime = null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date dateUtcTime = dateFormat.parse(utcTime);

            long longUtcTime = dateUtcTime.getTime();

            TimeZone timeZone = TimeZone.getDefault();
            int offset = timeZone.getOffset(longUtcTime);
            long longLocalTime = longUtcTime + offset;

            Date dateLocalTime = new Date();
            dateLocalTime.setTime(longLocalTime);

            localTime = dateFormat.format(dateLocalTime);

        } catch (ParseException e) {
            e.printStackTrace();;
        }

        return localTime;
    }

}