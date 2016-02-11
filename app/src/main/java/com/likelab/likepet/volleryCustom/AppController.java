package com.likelab.likepet.volleryCustom;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.likelab.likepet.R;
import com.likelab.likepet.global.GlobalSharedPreference;
import com.likelab.likepet.global.GlobalVariable;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class AppController extends Application {

	private Tracker mTracker;

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "2QM9iK7Q8azv9nDSbVHYsEJSi";
    private static final String TWITTER_SECRET = "mnornpMetyd2F8L3Uv3TDTq80VxeBtsrCOzgk6hryEHd2jgNyC";


	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private Resources res;

	private static AppController mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig), new Crashlytics());

		TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String networkOperator = tel.getNetworkOperator();
		String countryCode = getApplicationContext().getResources().getConfiguration().locale.getCountry();

		if (networkOperator != null) {
			if(networkOperator.length() != 0) {

				System.out.println("mcc "+ networkOperator);
				int mcc = Integer.parseInt(networkOperator.substring(0, 3));
				int mnc = Integer.parseInt(networkOperator.substring(3));

				GlobalSharedPreference.setAppPreferences(this, "mcc", networkOperator.substring(0, 3));
				GlobalSharedPreference.setAppPreferences(this, "mnc", networkOperator.substring(3));


				GlobalVariable.mcc = networkOperator.substring(0, 3);
				GlobalVariable.mnc = networkOperator.substring(3);

			} else {
				GlobalVariable.mcc = "null";
				GlobalVariable.mnc = "null";
				GlobalSharedPreference.setAppPreferences(this, "mcc", "null");
				GlobalSharedPreference.setAppPreferences(this, "mnc", "null");
			}
		}

		if(countryCode != null) {
			GlobalVariable.countryCode = countryCode;
		} else {
			GlobalVariable.countryCode = "null";
		}

		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		String version = pi.versionName;
		GlobalSharedPreference.setAppPreferences(this, "appVersion", version);
		GlobalSharedPreference.setAppPreferences(this, "deviceName", Build.DEVICE);

		mInstance = this;
		AssetManager asset = this.getAssets();

		GlobalVariable.appVersion = version;
		GlobalVariable.deviceName = Build.DEVICE;
		GlobalVariable.deviceOS = Build.VERSION.RELEASE;

	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}
	
	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue,
					new LruMemoryDiskBitmapCache(getApplicationContext()));
		}
		return this.mImageLoader;
	}

}