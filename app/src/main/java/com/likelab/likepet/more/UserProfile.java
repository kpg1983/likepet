package com.likelab.likepet.more;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.RoundedAvatarDrawable;
import com.likelab.likepet.UploadContents;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalUrl;
import com.likelab.likepet.volleryCustom.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by kpg1983 on 2015-10-27.
 */
public class UserProfile extends Activity {

    private RelativeLayout cancelContainer;
    private ImageView btnCancel;
    private ImageView imgSpeechBubble;
    private ImageView imgCat;
    private Button btnJoinMember;


    private Button btnEdit;
    private RelativeLayout guideContainer;
    private RelativeLayout guideCancelContainer;

    private RelativeLayout userPhotoContainer;
    private ImageView imgUserPhoto;
    private ImageView btnCamera;

    private ImageButton btnGender;
    private ImageButton btnBirth;
    private ImageButton btnState;

    private RelativeLayout inputGenderContainer;
    private RelativeLayout genderDisableContainer;
    private LinearLayout selectGenderContainer;

    private RelativeLayout inputBirthContainer;
    private RelativeLayout inputStateContainer;

    private RelativeLayout overlay;

    private TextView txtGenderMan;
    private TextView txtGenderWoman;
    private TextView txtGenderUniSex;

    private RelativeLayout genderManContainer;
    private RelativeLayout genderWomanContainer;
    private RelativeLayout genderUinContainer;

    private TextView txtChoiceBirth;
    private TextView txtBirth;

    private TextView txtChoiceState;
    private TextView txtSetState;

    private ImageView imgClan;
    private TextView txtUserName;

    private TextView txtGender;

    Bitmap selectedImage;

    int xmlFlag = 1;
    int stateFlag = 0;      //편집, 저장 상태를 나태낸다.
    int genderFlag = 0;

    int year, month, day;

    private static final String TEMP_PHOTO_FILE = "temp.jpg";       // 임시 저장파일
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final int REQ_LOAD_IMAGE = 1;
    private static final String GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos";

    private static final int REQUEST_PHOTO_FROM_GOOGLE_PHOTOS = 100;
    private static final int SELECT_PHOTO = 1000;

    OutputStream os;

    RequestQueue queue = AppController.getInstance().getRequestQueue();

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    String filePath;

    private static final String TAG = "AndroidUploadService";

    String gender = null;
    String birthDay = null;
    String statusMessage = null;

    private Tracker mTracker = AppController.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gender = GlobalSharedPreference.getAppPreferences(this, "gender");

        GregorianCalendar calendar = new GregorianCalendar();
        //회원 가입을 하지 않은 경우
        if (xmlFlag == 0) {
            setContentView(R.layout.more_profile_no_member);

            cancelContainer = (RelativeLayout) findViewById(R.id.no_profile_cancel_container);
            btnCancel = (ImageView) findViewById(R.id.no_profile_btn_cancel);
            imgSpeechBubble = (ImageView) findViewById(R.id.no_profile_img_speech_bubble);
            imgCat = (ImageView) findViewById(R.id.no_profile_img_cat);
            btnJoinMember = (Button) findViewById(R.id.no_profile_btn_join);
            imgSpeechBubble.setImageResource(R.drawable.more_img_07);
            imgCat.setImageResource(R.drawable.more_img_05);

        }
        //회원 가입이 된 경우
        else {
            setContentView(R.layout.more_profile);

            cancelContainer = (RelativeLayout) findViewById(R.id.profile_cancel_container);

            btnEdit = (Button) findViewById(R.id.profile_btn_edit);
            guideContainer = (RelativeLayout) findViewById(R.id.profile_guide_container);
            guideCancelContainer = (RelativeLayout) findViewById(R.id.profile_guide_cancel_container);
            imgUserPhoto = (ImageView) findViewById(R.id.profile_img_user_photo_main);

            txtGender = (TextView) findViewById(R.id.profile_txt_gender);

            txtUserName = (TextView) findViewById(R.id.profile_txt_user_name);

            txtUserName.setText(GlobalSharedPreference.getAppPreferences(this, "name"));

            Log.d("name", GlobalSharedPreference.getAppPreferences(this, "name"));
            imgClan = (ImageView) findViewById(R.id.profile_img_user_clan);

            final String clan = GlobalSharedPreference.getAppPreferences(this, "clan");

            if (clan.equals("0")) {
                imgClan.setImageResource(R.drawable.mypage_img_02);
            } else if (clan.equals("1")) {
                imgClan.setImageResource(R.drawable.mypage_img_01);
            } else if (clan.equals("2")) {
                imgClan.setImageResource(R.drawable.mypage_img_03);
            }

            userPhotoContainer = (RelativeLayout) findViewById(R.id.profile_user_photo_container);
            userPhotoContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");              // 모든 이미지
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
                    intent.putExtra("outputFormat",         // 포맷방식
                            Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("crop", "true");        // Crop기능 활성화\
                    intent.putExtra("aspectX", 1); //이걸 삭제한다
                    intent.putExtra("aspectY", 1); //이걸 삭제한다

                    startActivityForResult(intent, REQ_CODE_PICK_IMAGE);

                }
            });


            imageLoader.get(GlobalSharedPreference.getAppPreferences(UserProfile.this, "profileImageUrl"), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                    if (response.getBitmap() != null) {
                        imgUserPhoto.startAnimation(AnimationUtils.loadAnimation(UserProfile.this, android.R.anim.fade_in));
                        imgUserPhoto.setImageDrawable(new RoundedAvatarDrawable(response.getBitmap(), 1));
                    } else {

                        if (clan.equals("0")) {
                            imgUserPhoto.setImageResource(R.drawable.more_img_06_01_dog);
                        } else if (clan.equals("1")) {
                            imgUserPhoto.setImageResource(R.drawable.more_img_06_01_cat);
                        } else if (clan.equals("2")) {
                            imgUserPhoto.setImageResource(R.drawable.more_img_06_01_human);
                        }

                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            btnCamera = (ImageView) findViewById(R.id.profile_btn_camera);

            btnGender = (ImageButton) findViewById(R.id.profile_btn_gender);
            btnBirth = (ImageButton) findViewById(R.id.profile_btn_birth);
            btnState = (ImageButton) findViewById(R.id.profile_btn_state);

            inputGenderContainer = (RelativeLayout) findViewById(R.id.profile_input_gender);
            selectGenderContainer = (LinearLayout) findViewById(R.id.profile_select_gender_container);
            genderDisableContainer = (RelativeLayout) findViewById(R.id.profile_gender_disable_container);

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);

            inputBirthContainer = (RelativeLayout) findViewById(R.id.profile_input_birth);
            inputStateContainer = (RelativeLayout) findViewById(R.id.profile_input_state);

            txtGenderMan = (TextView) findViewById(R.id.profile_txt_gender_man);
            txtGenderWoman = (TextView) findViewById(R.id.profile_txt_gender_woman);
            txtGenderUniSex = (TextView) findViewById(R.id.profile_txt_gender_uni);

            genderManContainer = (RelativeLayout) findViewById(R.id.profile_gender_man_container);
            genderWomanContainer = (RelativeLayout) findViewById(R.id.profile_gender_woman_container);
            genderUinContainer = (RelativeLayout) findViewById(R.id.profile_gender_uni_container);

            txtBirth = (TextView) findViewById(R.id.profile_txt_set_birth);
            txtChoiceBirth = (TextView) findViewById(R.id.profile_txt_choice_birth);

            txtChoiceState = (TextView) findViewById(R.id.profile_txt_choice_state);
            txtSetState = (TextView) findViewById(R.id.profile_txt_set_state);

            overlay = (RelativeLayout) findViewById(R.id.profile_overlay);


            String gender = GlobalSharedPreference.getAppPreferences(this, "gender");
            if (gender.equals("0") || gender.equals("1") || gender.equals("2")) {

                if (gender.equals("0")) {
                    txtGender.setText(getResources().getString(R.string.profile_edit_gender_man));
                } else if (gender.equals("1")) {
                    txtGender.setText(getResources().getString(R.string.profile_edit_gender_woman));
                } else if (gender.equals("2")) {
                    txtGender.setText(getResources().getString(R.string.profile_edit_gender_uni));
                }

                genderDisableContainer.setVisibility(View.INVISIBLE);
                //selectGenderContainer.setVisibility(View.VISIBLE);

                if (gender.equals("0")) {
                    txtGenderMan.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderWoman.setTextColor(Color.parseColor("#494949"));
                    txtGenderUniSex.setTextColor(Color.parseColor("#494949"));

                } else if (gender.equals("1")) {
                    txtGenderWoman.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderMan.setTextColor(Color.parseColor("#494949"));
                    txtGenderUniSex.setTextColor(Color.parseColor("#494949"));

                } else if (gender.equals("2")) {
                    txtGenderUniSex.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderMan.setTextColor(Color.parseColor("#494949"));
                    txtGenderWoman.setTextColor(Color.parseColor("#494949"));
                }
            }

            String birthday = GlobalSharedPreference.getAppPreferences(this, "birthday");

            if (birthday.contains("-")) {
                txtChoiceBirth.setVisibility(View.INVISIBLE);
                txtBirth.setVisibility(View.VISIBLE);
                txtBirth.setText(birthday);

            }


            String statusMessage = GlobalSharedPreference.getAppPreferences(this, "status");
            if (!statusMessage.equals("null")) {
                txtChoiceState.setVisibility(View.INVISIBLE);

                if (statusMessage.equals("0")) {
                    txtSetState.setText(getResources().getString(R.string.profile_edit_status_date));
                } else if (statusMessage.equals("1")) {
                    txtSetState.setText(getResources().getString(R.string.profile_edit_status_single));
                } else if (statusMessage.equals("2")) {
                    txtSetState.setText(getResources().getString(R.string.profile_edit_status_marry));
                } else if (statusMessage.equals("3")) {
                    txtSetState.setText(getResources().getString(R.string.profile_edit_status_work));
                } else if (statusMessage.equals("4")) {
                    txtSetState.setText(getResources().getString(R.string.profile_edit_status_parent));
                }

            }

        }

        genderManContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateFlag == 1) {
                    txtGenderMan.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderWoman.setTextColor(Color.parseColor("#494949"));
                    txtGenderUniSex.setTextColor(Color.parseColor("#494949"));
                    gender = "0";

                }
            }
        });

        genderWomanContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateFlag == 1) {
                    txtGenderWoman.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderMan.setTextColor(Color.parseColor("#494949"));
                    txtGenderUniSex.setTextColor(Color.parseColor("#494949"));
                    gender = "1";
                }
            }
        });

        genderUinContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateFlag == 1) {
                    txtGenderUniSex.setTextColor(Color.parseColor("#f7c243"));
                    txtGenderMan.setTextColor(Color.parseColor("#494949"));
                    txtGenderWoman.setTextColor(Color.parseColor("#494949"));
                    gender = "2";
                }
            }
        });

        //상태메시지 팝업 윈도우
        inputStateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final PopupWindow popupWindow = new PopupWindow();
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView = inflater.inflate(R.layout.profile_state_popup_view, null);

                final PopupWindow popupWindow = new PopupWindow();

                if (stateFlag == 1) {

                    popupWindow.setContentView(popupView);
                    popupWindow.setWindowLayoutMode(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setTouchable(true);
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    RelativeLayout btnCancelContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_cancel_container);
                    ImageButton btnCancel = (ImageButton) popupView.findViewById(R.id.profile_state_popup_btn_cancel);

                    RelativeLayout dateContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_date_container);
                    RelativeLayout singleContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_single_container);
                    RelativeLayout marriedContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_married_container);
                    RelativeLayout walkContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_walk_container);
                    RelativeLayout lookMomContainer = (RelativeLayout) popupView.findViewById(R.id.profile_state_popup_look_mom_container);

                    final TextView txtDate = (TextView) popupView.findViewById(R.id.profile_state_txt_date);
                    final TextView txtSingle = (TextView) popupView.findViewById(R.id.profile_state_txt_single);
                    final TextView txtMarried = (TextView) popupView.findViewById(R.id.profile_state_txt_married);
                    final TextView txtWalk = (TextView) popupView.findViewById(R.id.profile_state_txt_walk);
                    final TextView txtLookMom = (TextView) popupView.findViewById(R.id.profile_state_txt_look_mom);

                    overlay.setVisibility(View.VISIBLE);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    btnCancelContainer.setOnClickListener(new View.OnClickListener() {
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

                    dateContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusMessage = "0";
                            popupWindow.dismiss();
                            txtSetState.setText(txtDate.getText());
                            txtChoiceState.setVisibility(View.INVISIBLE);


                        }
                    });
                    singleContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusMessage = "1";
                            popupWindow.dismiss();
                            txtSetState.setText(txtSingle.getText());
                            txtChoiceState.setVisibility(View.INVISIBLE);
                        }
                    });

                    marriedContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusMessage = "2";
                            popupWindow.dismiss();
                            txtSetState.setText(txtMarried.getText());
                            txtChoiceState.setVisibility(View.INVISIBLE);
                        }
                    });

                    walkContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusMessage = "3";
                            popupWindow.dismiss();
                            txtSetState.setText(txtWalk.getText());
                            txtChoiceState.setVisibility(View.INVISIBLE);
                        }
                    });

                    lookMomContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusMessage = "4";
                            popupWindow.dismiss();
                            txtSetState.setText(txtLookMom.getText());
                            txtChoiceState.setVisibility(View.INVISIBLE);
                        }
                    });


                    View layout = (View) findViewById(R.id.profile_layout);
                    //팝업 윈도우 위치 조정 화면
                    popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
                }
            }
        });

        inputBirthContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (stateFlag == 1) {
                    new DatePickerDialog(UserProfile.this, dateSetListener, year, month, day).show();

                    txtChoiceBirth.setVisibility(View.INVISIBLE);
                }

            }
        });

        inputGenderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (stateFlag == 1) {
                    if (genderFlag == 0) {
                        selectGenderContainer.setVisibility(View.VISIBLE);
                        genderDisableContainer.setVisibility(View.INVISIBLE);

                        genderFlag = 1;
                    } else {
                        selectGenderContainer.setVisibility(View.INVISIBLE);
                        genderDisableContainer.setVisibility(View.VISIBLE);

                        genderFlag = 0;
                    }
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (stateFlag == 0) {
                    genderDisableContainer.setVisibility(View.INVISIBLE);
                    btnEdit.setText(getResources().getString(R.string.profile_btn_save));
                    btnGender.setVisibility(View.VISIBLE);
                    btnBirth.setVisibility(View.VISIBLE);
                    btnState.setVisibility(View.VISIBLE);

                    txtGender.setVisibility(View.INVISIBLE);
                    selectGenderContainer.setVisibility(View.VISIBLE);

                    stateFlag = 1;
                } else if (stateFlag == 1) {
                    btnEdit.setText(getResources().getString(R.string.profile_btn_modify));
                    btnGender.setVisibility(View.INVISIBLE);
                    btnBirth.setVisibility(View.INVISIBLE);
                    btnState.setVisibility(View.INVISIBLE);

                    if (gender.equals("0")) {
                        txtGender.setText(getResources().getString(R.string.profile_edit_gender_man));
                    } else if (gender.equals("1")) {
                        txtGender.setText(getResources().getString(R.string.profile_edit_gender_woman));
                    } else if (gender.equals("2")) {
                        txtGender.setText(getResources().getString(R.string.profile_edit_gender_uni));
                    } else {
                        genderDisableContainer.setVisibility(View.VISIBLE);
                    }


                    txtGender.setVisibility(View.VISIBLE);
                    selectGenderContainer.setVisibility(View.INVISIBLE);

                    stateFlag = 0;

                    if (gender != null) {
                        modifyGenderRequest(gender);
                    }
                    if (birthDay != null) {
                        modifyBirthdayRequest(birthDay);
                    }
                    if (statusMessage != null) {
                        modifyStatusRequest(statusMessage);
                    }

                    Toast.makeText(UserProfile.this, getResources().getString(R.string.profile_toast_finish), Toast.LENGTH_SHORT).show();

                }

            }
        });


        //친구들에게 보여줄 프로필을 작성해보라 컨테이너 없애기
        guideCancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideContainer.setVisibility(View.GONE);
            }
        });

        cancelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }




    public void modifyBirthdayRequest(final String birthday) {

        String endPoint = "/users/user/birthday";

        JSONObject obj = new JSONObject();

        try {
            obj.put("birthday", birthday);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");


                            if (responseCode == 200) {
                                GlobalSharedPreference.setAppPreferences(UserProfile.this, "birthday", birthday);
                                Log.d("birthday", GlobalSharedPreference.getAppPreferences(UserProfile.this, "birthday"));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(UserProfile.this, "sid"));

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public void modifyStatusRequest(final String status) {

        String endPoint = "/users/user/status";

        JSONObject obj = new JSONObject();

        try {
            obj.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");


                            if (responseCode == 200) {
                                GlobalSharedPreference.setAppPreferences(UserProfile.this, "status", status);

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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(UserProfile.this, "sid"));

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    public void modifyGenderRequest(final String gender) {

        String endPoint = "/users/user/sex";

        JSONObject obj = new JSONObject();

        try {
            obj.put("sex", gender);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, GlobalUrl.BASE_URL + endPoint, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        int responseCode = 0;

                        try {
                            responseCode = response.getInt("code");


                            if (responseCode == 200) {
                                GlobalSharedPreference.setAppPreferences(UserProfile.this, "gender", gender);

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
                params.put("sessionId", GlobalSharedPreference.getAppPreferences(UserProfile.this, "sid"));

                return params;

            }

        };
        queue.add(jsonObjectRequest);
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            // TODO Auto-generated method stub
            String msg = String.format("%d / %d / %d", year, monthOfYear + 1, dayOfMonth);

            String strYear, strMonth, strDay;

            strYear = Integer.toString(year);
            strMonth = Integer.toString(monthOfYear + 1);
            strDay = Integer.toString(dayOfMonth);
            txtBirth.setVisibility(View.VISIBLE);
            txtBirth.setText(strYear + "-" + strMonth + "-" + strDay);

            txtChoiceBirth.setVisibility(View.INVISIBLE);

            birthDay = strYear + "-" + strMonth + "-" + strDay;

        }
    };

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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);


        switch (requestCode) {

            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                        if (filePath.contains(""))

                            System.out.println("path : " + Environment.getExternalStorageDirectory().getPath()); // logCat으로 경로확인.

                        selectedImage = BitmapFactory.decodeFile(filePath);
                        imgUserPhoto.setImageDrawable(new RoundedAvatarDrawable(selectedImage, 1));
                        // temp.jpg파일을 Bitmap으로 디코딩한다.


                        // temp.jpg파일을 이미지뷰에 씌운다.
                        //imgUserPhoto.setImageUrl();

                        Runnable runnable = new RunnableImplements();
                        Thread thread = new Thread(runnable);
                        thread.start();

                    }

                }
                break;

            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {

                        /*
                        Uri selectedImage = imageData.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = getContentResolver().query(
                                selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        cursor.close();

                        this.selectedImage = BitmapFactory.decodeFile(filePath);
                        this.filePath = filePath;

                        imgUserPhoto.setImageDrawable(new RoundedAvatarDrawable(this.selectedImage, 1));

                        Log.d("filePath", this.filePath);

                        */

                        filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                        Uri selectedImage = imageData.getData();
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                        Intent intent = new Intent("com.android.camera.action.CROP");
                        intent.setDataAndType(selectedImage, "image/*");
                        intent.putExtra("aspectX", 1); // crop 박스의 x축 비율
                        intent.putExtra("aspectY", 1); // crop 박스의 y축 비율
                        //intent.putExtra("scale", true);
                        //intent.putExtra("return-data", true);
                        startActivityForResult(intent, 50);


                        imgUserPhoto.setImageDrawable(new RoundedAvatarDrawable(yourSelectedImage, 1));

                        Runnable runnable = new RunnableImplements();
                        Thread thread = new Thread(runnable);
                        thread.start();


                    } catch (Exception e) {

                    }
                }
                break;

        }

    }

    class RunnableImplements implements Runnable {

        public void run() {
            UploadContents upload = new UploadContents();

            String endPoint = "/users/user/profile/image";
            try {
                upload.HttpFileUpload(GlobalUrl.BASE_URL + endPoint, filePath, getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pageName = "Profile";
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
