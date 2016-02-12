package com.likelab.likepet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class PushAlertActivity extends Activity {
	private String msg = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LinearLayout layout = new LinearLayout(this);
		layout.setBackgroundColor(Color.TRANSPARENT);
		setContentView(layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		onNewIntent(getIntent());
	}
	
	private static CustomDialog custom;
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		if(intent.getExtras() != null){
			Bundle b = intent.getExtras();
			if(b != null) {
				msg = b.getString("msg");
			}
			
			if(msg != null) {
				Log.e("", "Screen off");
				CheckedAlert();
				
				custom = new CustomDialog(this);
				custom.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
				custom.setTitle(getString(R.string.app_name));
				custom.setCancelable(false);
				custom.setCanceledOnTouchOutside(false);
				
				custom.setMsg(msg);
				custom.show();
			}
		}
	}
	
	public boolean CheckedAlert() {
		if(custom == null) return false;		
		
		custom.dismiss();
		custom = null;
		
		return true;
	}
	
	
	
	// home btn check
	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		if(custom != null) {
			custom.dismiss();
			custom = null;
			finish();
		}
	}
}
