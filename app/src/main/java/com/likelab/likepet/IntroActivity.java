package com.likelab.likepet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.likelab.likepet.Main.MainActivity;

public class IntroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ImageView image = new ImageView(this);
		image.setImageResource(R.drawable.intro);
		setContentView(image);
		
		Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				startActivity(new Intent(IntroActivity.this, MainActivity.class));
				finish();
			}
		};
		handler.sendEmptyMessageDelayed(0, 3000);
	}
}
