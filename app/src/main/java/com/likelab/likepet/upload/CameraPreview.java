package com.likelab.likepet.upload;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private List<Camera.Size> cameraSize;


	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// create the surface and start camera preview
			if (mCamera == null) {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			}
		} catch (IOException e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void refreshCamera(Camera camera) {
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings
		setCamera(camera);
		try {

			mCamera.setDisplayOrientation(90);//only 2.2>
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		mCamera.setDisplayOrientation(90);//only 2.2>

		Camera.Parameters p = mCamera.getParameters();

		cameraSize = p.getSupportedPreviewSizes();
		Camera.Size opti = null;

		int diff = 10000;
		for (Camera.Size s : cameraSize) {

			if (Math.abs(s.height - h) < diff) {
				diff = Math.abs(s.height - h);
				opti = s;
			}
		}
		//Camera.Size tmpSize = cameraSize.get(0);

		p.set("jpeg-quality", 100);
		p.setRotation(90);
		p.setPictureFormat(PixelFormat.JPEG);
		p.setPreviewSize(1920, 1080);
		p.setFocusMode(p.FOCUS_MODE_CONTINUOUS_PICTURE);
		mCamera.setParameters(p);

//		Camera.Parameters parameters = mCamera.getParameters();
//		  parameters.set("orientation", "portrait");
//		  parameters.setPreviewSize(w, h);
//		  mCamera.setParameters(parameters);

		refreshCamera(mCamera);

	}

	public void setCamera(Camera camera) {
		//method to set a camera instance
		mCamera = camera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		//mCamera.release();
	//	mCamera = null;

	}

	private Camera.Size getBestPreviewSize(int width, int height) {
		Camera.Size result = null;
		Camera.Parameters p = mCamera.getParameters();
		for (Camera.Size size : p.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}
		return result;

	}

}