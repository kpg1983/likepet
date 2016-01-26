package com.likelab.likepet.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.alexbbb.uploadservice.MultipartUploadRequest;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.likelab.likepet.global.ActivityList;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUploadBitmapImage;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.Main.MainActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.utils.AnimatedGifEncoder;
import com.likelab.likepet.volleryCustom.AppController;
import com.likelab.likepet.volleryCustom.TinyDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by kpg1983 on 2015-09-24.
 */
public class InputContents extends Activity {

    private static final int REQ_UPLOAD_CONTENTS = 0;

    private TextView numberOfText;
    private RelativeLayout layCancel;

    private RelativeLayout btnSharpContainer;
    private LinearLayout recommendTagContainer[] = new LinearLayout[5];



    private TextView txtRecommendTag_1;    //추천태그
    private TextView txtRecommendTag_2;
    private TextView txtRecommendTag_3;
    private TextView txtRecommendTag_4;
    private TextView txtRecommendTag_5;

    private TextView txtRecommendTag[] = new TextView[5];

    private ScrollView scrollView;
    private RelativeLayout blankLayout;
    RelativeLayout layout;

    ImageView imgContents;

    ViewGroup vg;

    EditText mEditText;
    String contentId;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    RequestQueue queue = AppController.getInstance().getRequestQueue();
    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    public static int REQ_MODIFY_CONTENTS = 1;
    private static final int RESULT_LIKE = 5;
    private static final int RESULT_MODIFY_CONTENT = 6;

    private static final String TEMP_PHOTO_FILE = "temp.jpg";
    Button btn_finish;
    String filePath, videoContentsFilePath;
    String description;

    TinyDB tinyDb;
    ArrayList<String> tagList;

    private static final String TAG = "AndroidUploadService";
    private Boolean isThisImage, isThisGif, isThisVideo, isGifCreated, isUploadButtonTouched;
    int mediaType;
    GifDrawable gifImage;
    int rateFlag;

    private Intent intent;
    private FrameLayout videoViewContainer;
    private VideoView videoView;
    private MediaController mediaControls;

    private TimerTask mTask;
    private Timer mTimer;
    private int gifIndex, gifCount;

    Bitmap videoThumbnail;

    private boolean finishUploadFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_contents);

        intent = getIntent();
        isThisImage = false;
        isThisGif = false;
        isThisVideo = false;
        isGifCreated = false;
        isUploadButtonTouched = false;

        videoView = (VideoView)findViewById(R.id.upload_videoView);

        if ( intent.getExtras().getString("MEDIA_TYPE") != null ){
            if( intent.getExtras().getString("MEDIA_TYPE").equals("image") ){
                isThisImage = true;
                mediaType = 0;
//                videoViewContainer = (FrameLayout)findViewById(R.id.upload_videoView_container);
//                videoViewContainer.setVisibility(View.INVISIBLE);
            }
            else if( intent.getExtras().getString("MEDIA_TYPE").equals("gif") ){
                isThisGif = true;
                mediaType = 1;
//                videoViewContainer = (FrameLayout) findViewById(R.id.upload_videoView_container);
//                videoViewContainer.setVisibility(View.INVISIBLE);
            }
            else if( intent.getExtras().getString("MEDIA_TYPE").equals("video") ){
                isThisVideo = true;
                mediaType = 2;
                videoContentsFilePath = intent.getExtras().getString("VIDEO_CONTENTS_FILE_PATH");
//                videoViewContainer = (FrameLayout) findViewById(R.id.upload_videoView_container);

                videoThumbnail = ThumbnailUtils.createVideoThumbnail(videoContentsFilePath, MediaStore.Video.Thumbnails.MINI_KIND);



            }
        }

        tinyDb = new TinyDB(this);
        tagList = new ArrayList<String>(5);
        tagList = tinyDb.getListString("tags");

        vg = (ViewGroup) findViewById(R.id.input_relative);

        layout = (RelativeLayout) findViewById(R.id.upload_keyboard_tag_container);

        LayoutInflater inflater = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE));

        final Intent intent = getIntent();

        btnSharpContainer = (RelativeLayout) findViewById(R.id.upload_img_sharp_container);
        recommendTagContainer[0] = (LinearLayout) findViewById(R.id.upload_tag_container_1);
        recommendTagContainer[1] = (LinearLayout) findViewById(R.id.upload_tag_container_2);
        recommendTagContainer[2] = (LinearLayout) findViewById(R.id.upload_tag_container_3);
        recommendTagContainer[3]= (LinearLayout) findViewById(R.id.upload_tag_container_4);
        recommendTagContainer[4] = (LinearLayout) findViewById(R.id.upload_tag_container_5);

        txtRecommendTag_1 = (TextView) findViewById(R.id.upload_txt_tag_1);
        txtRecommendTag_2 = (TextView) findViewById(R.id.upload_txt_tag_2);
        txtRecommendTag_3 = (TextView) findViewById(R.id.upload_txt_tag_3);
        txtRecommendTag_4 = (TextView) findViewById(R.id.upload_txt_tag_4);
        txtRecommendTag_5 = (TextView) findViewById(R.id.upload_txt_tag_5);

        txtRecommendTag[0] = (TextView) findViewById(R.id.upload_txt_tag_1);
        txtRecommendTag[1] = (TextView) findViewById(R.id.upload_txt_tag_2);
        txtRecommendTag[2] = (TextView) findViewById(R.id.upload_txt_tag_3);
        txtRecommendTag[3] = (TextView) findViewById(R.id.upload_txt_tag_4);
        txtRecommendTag[4] = (TextView) findViewById(R.id.upload_txt_tag_5);

        int count = 0;

        Log.d("listSize", Integer.toString(tagList.size()));


        //테그를 설정한다
        if (tagList.size() != 0) {
            for (int i = 0; i < tagList.size(); i++) {

                if (count == 5) {
                    break;
                }
                txtRecommendTag[i].setText("#" + tagList.get(i));
                Log.d("TAGS", tagList.get(i));
                count++;
            }
        }

        //테크의 배경화면을 설정한다 테크가 있는 숫자만큼만
        for(int i=0; i<tagList.size() ; i++) {

            if(i >= 5)
                break;

            recommendTagContainer[i].setBackgroundColor(Color.parseColor("#263034"));
        }

        scrollView = (ScrollView) findViewById(R.id.input_scrollview);
        btn_finish = (Button) findViewById(R.id.btn_contents_input_finish);


        final InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        layCancel = (RelativeLayout) findViewById(R.id.input_contents_cancel_container);
        numberOfText = (TextView) findViewById(R.id.input_text_count);
        mEditText = (EditText) findViewById(R.id.upload_input_edit_text);

        ImageView btn_back = (ImageView) findViewById(R.id.btn_contents_input_cancel);
        btn_back.setImageResource(R.drawable.upload_btn_back);

        ImageView text_count = (ImageView) findViewById(R.id.upload_count_box);
        text_count.setImageResource(R.drawable.upload_img_countbox);

        btn_back.setScaleType(ImageView.ScaleType.FIT_XY);
        text_count.setScaleType(ImageView.ScaleType.FIT_XY);

        imgContents = (ImageView) findViewById(R.id.upload_image_contents);

        //편집 모드
        if (intent.hasExtra("DESC")) {
            String imageUrl = intent.getStringExtra("URL");
            String description = intent.getStringExtra("DESC");
            contentId = intent.getStringExtra("CONTEND_ID");
            int contentType = intent.getIntExtra("CONTENT_TYPE", -1);
            imgContents.setVisibility(View.VISIBLE);

            //사진
            if(contentType == 1) {

                imageLoader.get(imageUrl, new ImageLoader.ImageListener() {

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null) {
                            imgContents.setImageBitmap(response.getBitmap());

                        } else {
                            imgContents.setImageResource(R.drawable.main_image);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });

                //gif
            } else if(contentType == 3) {
                //Ion.with(this).load(imageUrl).intoImageView(imgContents);
                Ion.with(this).load(imageUrl).intoImageView(imgContents);

                //비디오
            } else {

                if (mediaControls == null) {
                    mediaControls = new MediaController(this);
                }

                videoView.setVisibility(View.VISIBLE);
                videoView.setMediaController(mediaControls);

                videoView.setVideoURI(Uri.parse(imageUrl));
                videoView.start();

            }

            mEditText.setText(description);



            //업로드 모드
        }else{
            if ( isThisImage ) {
                imgContents.setVisibility(View.VISIBLE);
                imgContents.setImageBitmap(GlobalUploadBitmapImage.bitmapCopy);
            }
            else if ( isThisGif ) {

                imgContents.setVisibility(View.VISIBLE);

                gifIndex = 0; gifCount = GlobalUploadBitmapImage.bitmapList.size();

                mTask = new TimerTask() {
                    @Override
                    public void run() {
                        gifIndex = gifIndex % (gifCount-1);

                        InputContents.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (GlobalUploadBitmapImage.filteredBitmapList.size() > 0)
                                    imgContents.setImageBitmap(GlobalUploadBitmapImage.filteredBitmapList.get(gifIndex));
                            }
                        });
                        gifIndex++;
                    }
                };

                mTimer = new Timer();
                mTimer.schedule(mTask, 0, 200);

                mediaType = 1;

                try {
                    generateGifFile(new Callback());

                } catch (Exception e) {

                }
            }
            else if ( isThisVideo ){

                imgContents.setVisibility(View.VISIBLE);
                imgContents.setImageBitmap(videoThumbnail);
                //videoView.setVideoPath(videoContentsFilePath);
                //videoView.start();
            }
        }

        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                numberOfText.setText(String.valueOf(s.length()));

                //텍스트 내용이 200자를 넘어갈 경우, 마지막 글자 삭제후 커서를 맨뒤로 이동
                if(mEditText.getText().length() > 200) {
                    Toast.makeText(InputContents.this, getResources().getString(R.string.input_contents_toast_too_long),
                            Toast.LENGTH_SHORT).show();

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mEditText.setText(mEditText.getText().toString().substring(0, 200));
                    mEditText.setSelection(mEditText.getText().toString().length());
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        mEditText.addTextChangedListener(mTextEditorWatcher);

        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //edit text에 포커스가 없을때
                if (!mEditText.hasFocus()) {
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollBy(0, scrollView.getBottom());
                        }
                    }, 100);
                }
            }
        });

        imgContents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mEditText.clearFocus();
            }
        });

        btn_finish.setVisibility(View.VISIBLE);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mEditText.getText().toString().length() != 0) {

                    btn_finish.setEnabled(false);

                    //편집모드
                    if (intent.hasExtra("DESC")) {
                        String description = mEditText.getText().toString();
                        modifyContentsRequest(contentId, description);

                    } else {
                        if (isThisImage) {
                            //getTempUri();
                            filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";
                            SaveBitmapToFileCache(GlobalUploadBitmapImage.bitmapCopy, filePath);


                        } else if (isThisGif) {
                            if (mTimer != null)
                                mTimer.cancel();
                            mediaType = 1;

                            isUploadButtonTouched = true;


                        } else if (isThisVideo) {
                            mediaType = 2;
                        }

                        description = mEditText.getText().toString();

                        //이 부분에서 문제가 발생하여 사진이 자꾸 동영사응로 인식되었다
                        //나중에 이유 파악을 한번 해보자꾸나...
                        //사진만 업로드
                        if (description.equals("null")) {
                            //Toast.makeText(InputContents.this, "filePath" + filePath, Toast.LENGTH_SHORT).show();

                        }
                        //사진과 글 다 업로드
                        //실제로는 사진만 업로드 하여도 이부분이 실행된다.
                        else {
                            try {
                                upload(description);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //테그를 저장하는 작업 후 업로드 작업을 완료한다.
                        insertTags(mEditText.getText().toString());
                        finishUpload();
                    }


                } else {

                    Toast.makeText(InputContents.this, getResources().getString(R.string.input_contents_toast_no_comment),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        layCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (videoView != null){
                    if (videoView.isPlaying()) {
                        videoView.pause();
                    }
                }
                if (isThisGif == true)
                    if (mTimer != null)
                        mTimer.cancel();

                finish();
            }
        });
        //Ion.with(this).load(filePath).intoImageView(imgContents);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        RecycleUtils.recursiveRecycle(imgContents);
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();

    }


    public interface OnTaskCompleted{
        void onTaskCompleted();
    }

    public class Callback implements OnTaskCompleted{
        @Override
        public void onTaskCompleted() {

            isGifCreated = true;
            if (isUploadButtonTouched == true){
                try {
                    upload(mEditText.getText().toString());
                }catch (Exception e) {

                }
            }
            // do something with result here!
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Ion.with(InputContents.this).load(filePath).intoImageView(imgContents);
//                }
//            });
        }
    }

    private void generateGifFile(final OnTaskCompleted listener) throws IOException {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();

                encoder.setDelay(200);
//                encoder.setSize(512, 512);
                encoder.setQuality(10);
                encoder.start(baos);

                int length = GlobalUploadBitmapImage.filteredBitmapList.size();
                for (int i = 0; i < length; i++) {
                    encoder.addFrame(GlobalUploadBitmapImage.filteredBitmapList.get(i));
                }

                encoder.finish();
                byte[] gifAsByteArr = baos.toByteArray();

                try {
                    filePath = getTempFile().getPath();
                    FileOutputStream fos = new FileOutputStream(filePath);
                    fos.write(gifAsByteArr);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                listener.onTaskCompleted();
            }
        });
        thread.setPriority(10);
        thread.start();

    }

    public void onClick(View v) {

        //추천태그 글자 색상을 변경한다.

        switch (v.getId()) {
            case R.id.upload_tag_container_1: {
                SpannableStringBuilder sps = new SpannableStringBuilder(txtRecommendTag_1.getText());
                //sps.setSpan(new ForegroundColorSpan(Color.parseColor("#f7c243")), 0, sps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEditText.append(sps);

                break;
            }
            case R.id.upload_tag_container_2: {
                SpannableStringBuilder sps = new SpannableStringBuilder(txtRecommendTag_2.getText());
                //sps.setSpan(new ForegroundColorSpan(Color.parseColor("#f7c243")), 0, sps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEditText.append(sps);
                break;
            }
            case R.id.upload_tag_container_3: {
                SpannableStringBuilder sps = new SpannableStringBuilder(txtRecommendTag_3.getText());
                //sps.setSpan(new ForegroundColorSpan(Color.parseColor("#f7c243")), 0, sps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                mEditText.append(sps);
                break;
            }
            case R.id.upload_tag_container_4: {
                SpannableStringBuilder sps = new SpannableStringBuilder(txtRecommendTag_4.getText());
                //sps.setSpan(new ForegroundColorSpan(Color.parseColor("#f7c243")), 0, sps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEditText.append(sps);
                break;
            }
            case R.id.upload_tag_container_5: {
                SpannableStringBuilder sps = new SpannableStringBuilder(txtRecommendTag_5.getText());
                //sps.setSpan(new ForegroundColorSpan(Color.parseColor("#f7c243")), 0, sps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEditText.append(sps);
                break;
            }
            case R.id.upload_img_sharp_container: {
                mEditText.append("#");
                break;
            }
        }

    }

    public void modifyContentsRequest(String contentId, final String description) {

        String endPoint = "/contents/" + contentId;

        JSONObject obj = new JSONObject();

        try {
            obj.put("descriptions", description);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("url", GlobalUrl.BASE_URL + endPoint);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode;

                        try {
                            responseCode = response.getInt("code");

                            if (responseCode == 200) {

                                //테크 설정
                                insertTags(description);

                                Intent intent = new Intent();
                                intent.putExtra("DESC", description);
                                setResult(RESULT_OK, intent);
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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(InputContents.this, "sid"));

                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 임시 저장 파일의 경로를 반환
     */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /*
     * 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환
     */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f;

            if(mediaType == 0 ){
                f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                        System.currentTimeMillis() + TEMP_PHOTO_FILE);
            }else if (mediaType == 1){
                f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                        System.currentTimeMillis() + "_temp.gif");
            }else{
                f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                        System.currentTimeMillis() + "_temp.mp4");
            }

            try {
                f.createNewFile();      // 외장메모리에 temp.jpg 파일 생성
            } catch (IOException e) {

            }

            return f;
        } else
            return null;
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

    //사진과 글 모두 업로드 시 사용
    public void upload(String description) throws Exception {

        String url = GlobalUrl.BASE_URL + "/contents/content";
        final MultipartUploadRequest request =
                new MultipartUploadRequest(InputContents.this, "custom-upload-id", url);

        request.addHeader("sessionId", GlobalSharedPreference.getAppPreferences(InputContents.this, "sid"));
        request.addParameter("descriptions", description);
        //사진
        if(mediaType == 0) {
            request.addFileToUpload(filePath, "upfile", "temp.jpeg", "image/jpeg");
        }
        //gif
        else if(mediaType == 1){
            request.addFileToUpload(filePath, "upfile", "temp.gif", "image/gif");

            //비디오
        } else if(mediaType == 2){
            request.addFileToUpload(videoContentsFilePath, "upfile", "temp.mp4", "video/mp4");
        }


        request.setCustomUserAgent("UploadServiceDemo/1.0");
        request.setMaxRetries(1);

        try {
            //Start upload service and display the notification
            request.startUpload();

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete upload request
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //uploadReceiver.register(this);

        String pageName = "Notice";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //uploadReceiver.unregister(this);
    }

    private void finishUpload() {

        finishUploadFlag = true;

        //업로드 테스크와 관련된 액티비티들 모두 삭제
        for (int i = 0; i < ActivityList.activityArrayList.size(); i++) {
            ActivityList.activityArrayList.get(i).finish();
        }

        ActivityList.activityArrayList.clear();


        Intent intent = new Intent(InputContents.this, MainActivity.class);
        intent.putExtra("UPLOAD_REQUEST", "upload");

        //업로드를 완료 하였을경우 임시 파일을 삭제한다.
        if(mediaType == 0 || mediaType == 1) {
            intent.putExtra("FILE_PATH", filePath);
        }

        intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void insertTags(String description) {

        String[] tags = description.split("\\#");

        int count = 0;
        if (tags.length > 1) {
            for (int i = 1; i < tags.length; i++) {

                if (count == 5)
                    break;

                String temp = null;
                if (tags[i].contains(" ")) {
                    temp = tags[i].substring(0, tags[i].indexOf(" "));
                } else {
                    temp = tags[i];
                }

                //엔터문자를 삭제한다
                if(temp.contains("\n")) {
                    temp = temp.replaceAll("\n", "");
                }

                //저장되어 있는 태그가 하나도 없는 경우는 첫번째태크를 무조건 저장
                if(tagList.size() == 0) {

                    tagList.add(0, temp);

                }
                else {
                    boolean tagDifference = false;
                    for (int j = 0; j < tagList.size(); j++) {

                        if (tagList.get(j).equals(temp)) {
                            tagDifference = true;
                        }

                    }

                    if(tagDifference == false) {
                        tagList.add(0, temp);
                    }
                }

            }
        }
        tinyDb.putListString("tags", tagList);

    }

    @Override
    protected void onStop() {
        super.onStop();


        //업로드를 완료하지 않고 액티비티를 종료하면, 임시파일을 삭제한다.
        if(finishUploadFlag == false) {

            if(mediaType == 0 && filePath != null) {
                File files = new File(filePath);
                if (files.exists()) {
                    files.delete();
                }
            } else if(mediaType == 2 && videoContentsFilePath != null){
                File files = new File(videoContentsFilePath);
                if (files.exists()) {
                    files.delete();
                }
            }
        }

    }

}