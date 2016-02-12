package com.likelab.likepet;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.util.Log;

import com.likelab.likepet.Main.MainActivity;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import kr.co.fingerpush.android.FingerPushIntentService;
import kr.co.fingerpush.android.GCMConstants;
import kr.co.fingerpush.android.GCMFingerPushManager;
import kr.co.fingerpush.android.NetworkUtility.NetworkBitmapListener;

public class GCMIntentService extends FingerPushIntentService {

	private static final String TAG = "GCMIntentService";	

	private GCMFingerPushManager manager;
	
	private String mode = "";
	private String message = "";
	private String sound = "";
	private String badge = "";
	private String img = "";
	private String imageURL = "";
	private String webLink = "";
	private String msgTag = "";
	private String custom1 = "";
	private String custom2 = "";
	private String custom3 = "";
	
	@Override
	protected void onError(Context context, String errorid) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onError ::: errorid:" + errorid);
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.gcm.GCMBaseIntentService#onMessage(android.content.Context, android.content.Intent)
	 * 
	 * data.message : 메세지 내용
	 * data.sound : 메세지 수신음
	 * data.badge : 메세지 뱃지 수
	 * data.imgUrl : 이미지 경로
	 * data.msgTag : 메세지 번호
	 * data.custom1 : 커스텀 필드 키(홈페이지에서 입력한 키를 입력)
	 * data.custom2 : 커스텀 필드 키(홈페이지에서 입력한 키를 입력)
	 * data.custom3 : 커스텀 필드 키(홈페이지에서 입력한 키를 입력)
	 */
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		super.onMessage(context, intent, new onMessageListener() {
			
			@Override
			public void onMessage(final Context context, Intent intent) {
				// TODO Auto-generated method stub
				// 메세지 처리 부분.

				Bundle b = intent.getExtras();

				Iterator<String> iterator = b.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = b.get(key).toString();
					Log.d(TAG, "onMessage ::: key:" + key + ", value:" + value);
				}

				if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
					try {
						int badgeCount = 0;

						String code = intent.getStringExtra("data.code");
						Log.e("", "before msg Tag : " + msgTag);
						message = intent.getStringExtra("data.message");
						sound = intent.getStringExtra("data.sound");
						badge = intent.getStringExtra("data.badge");
						img = intent.getStringExtra("data.img");
						imageURL = intent.getStringExtra("data.imgUrl");
						webLink = intent.getStringExtra("data.weblink");
						msgTag = intent.getStringExtra("data.msgTag");
						Log.e("", "after msg Tag : " + msgTag);
						custom1 = intent.getStringExtra("data.custom1");
						custom2 = intent.getStringExtra("data.custom2");
						custom3 = intent.getStringExtra("data.custom3");

						// 문자열 처리가 필요한 경우가 있으므로, GCMFingerPushManager class에 있는 getText
						// 메소드를 통해서 메세지 제목과 메세지 내용을 가져온다.
						manager = GCMFingerPushManager.getInstance(GCMIntentService.this);
						if (message != null && !message.trim().equals("")) {
							message = manager.getText(URLDecoder.decode(message, "UTF-8"));
						}

						mode = manager.getReceiveCode(intent.getStringExtra("data.code")).optString("PT");
						if (badge != null && !badge.trim().equals("")) {
							try {
								badgeCount = Integer.parseInt(badge);
							} catch (Exception e) {
								badgeCount = 0;
							}
						}

						// 뱃지 처리
						if (badgeCount >= 0) {
							Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
							badgeIntent.putExtra("badge_count", badgeCount);
							// 메인 메뉴에 나타나는 어플의 패키지 명
							badgeIntent.putExtra("badge_count_package_name", context.getPackageName());
							// 메인메뉴에 나타나는 어플의 클래스 명
							badgeIntent.putExtra("badge_count_class_name", "kr.co.kissoft.fingerpush.android.IntroActivity");
							context.sendBroadcast(badgeIntent);
						}

						if (manager.existImageURL(img)) {

							manager.getAttatchedImageURL(imageURL, new NetworkBitmapListener() {

								@Override
								public void onError(String code, String errorMessage) {
									// TODO Auto-generated method stub
									setNotification(context, message, null, msgTag);
								}

								@Override
								public void onComplete(String code, String resultMessage, Bitmap bitmap) {
									// TODO Auto-generated method stub
									setNotification(context, GCMIntentService.this.message, bitmap, msgTag);
								}

								@Override
								public void onCancel() {
								}
							});
							
						} else {
							// 노티
							Log.e("", "else");
							setNotification(context, message, null, msgTag);
						}

						// 얼럿창 생성
						showAlertService(context, message);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				
			}
		});
	}

	@Override
	protected void onRegistered(Context context, String regid) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onRegistered ::: regid:" + regid);
		GCMConstants.setProjectToken(getApplicationContext(), regid);
	}

	@Override
	protected void onUnregistered(Context context, String regid) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onUnregistered ::: regid:" + regid);
	}


	
	private void setNotification(Context context, String message, Bitmap bitmap, String message_id) {
		RecentTaskInfo recentTaskInfo = getRecentTaskInfo(getPackageName());
		
		Intent intent = new Intent(context, MainActivity.class);
		if(recentTaskInfo != null) {
			if(!recentTaskInfo.baseIntent.toString().contains("PushAlertActivity")) {
				intent = recentTaskInfo.baseIntent;
			}
		} else {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}
		
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.mipmap.ic_launcher);
		mBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
		mBuilder.setContentText(message);		
		//mBuilder.setContentText("손으로 당겨주세요.");
		
		if (bitmap != null) {
			BigPictureStyle notification = new BigPictureStyle();
			notification.setBigContentTitle(context.getResources().getString(R.string.app_name));
			notification.bigPicture(bitmap);
			notification.setSummaryText(message);			
	    	mBuilder.setStyle(notification);
	    	
		} else {
			BigTextStyle notification = new BigTextStyle();
			notification.setBigContentTitle(context.getResources().getString(R.string.app_name));
			notification.bigText(message);
			mBuilder.setContentText(message);
			mBuilder.setStyle(notification);
		}
	    
		mBuilder.setContentIntent(pi);
		mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
	
		Notification noti = mBuilder.build();
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// Send the notification.
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, noti); 
	}
	
	private void showAlertService(Context context, String message){

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		Bundle b = new Bundle();
		b.putString("msg", message);
		
		Intent i = new Intent(context, GCMAlertService.class);
		i.putExtras(b);
		
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		try {
			pi.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private RecentTaskInfo getRecentTaskInfo(final String packageName) {
	    final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    try {
	        // 10 : # of tasks you want to take a look at
	        final List<RecentTaskInfo> infoList = manager.getRecentTasks(10, 0); 
	        for (RecentTaskInfo info : infoList) {
	            if (info.baseIntent.getComponent().getPackageName().equals(packageName)) {
	                return info;
	            }
	        }
	    } catch (NullPointerException e) {}
	    return null;
	 }
}
