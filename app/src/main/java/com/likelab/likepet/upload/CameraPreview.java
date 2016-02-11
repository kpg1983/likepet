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
	private List<Camera.Size> cameraPictureSize;
	private List<Camera.Size> cameraPreviewSize;
	private Camera.Size size;

	int previewWidth;
	int previewHeight;

	private final static double epsilon = 0.17;


	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
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

			final Camera.Parameters parameters = mCamera.getParameters();
			cameraPictureSize = getSupportedPictureSizes();
			cameraPreviewSize = parameters.getSupportedPreviewSizes();


			boolean cameraPreviewSize_1920 = false;


			for(int i=0; i<cameraPreviewSize.size(); i++) {

				if(cameraPreviewSize.get(i).width == 1920 && cameraPreviewSize.get(i).height == 1080) {
					cameraPreviewSize_1920 = true;
					break;
				}
			}

			//카메라 해상도 설정
			 if(cameraPreviewSize_1920) {
				parameters.setPictureSize(1920, 1080);
				parameters.setPreviewSize(1920, 1080);
			} else {
				parameters.setPictureSize(1280, 720);
				parameters.setPreviewSize(1280, 720);
			}

			parameters.set("jpeg-quality", 100);
			parameters.setPictureFormat(PixelFormat.JPEG);

			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);//only 2.2>
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

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

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio=(double)h / w;

		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}


		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		Log.d("optiWidth", Integer.toString(optimalSize.width));
		Log.d("optiHeight", Integer.toString(optimalSize.height));
		previewWidth = optimalSize.width;
		previewHeight = optimalSize.height;
		return optimalSize;
	}

	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
		Camera.Size bestSize = null;
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

		bestSize = sizeList.get(0);

		for(int i = 1; i < sizeList.size(); i++){
			if((sizeList.get(i).width * sizeList.get(i).height) >
					(bestSize.width * bestSize.height)){
				bestSize = sizeList.get(i);
			}
		}

		return bestSize;
	}

	private Camera.Parameters setSize(Camera.Parameters parameters) {
		// TODO Auto-generated method stub

		Log.d("<<picture>>", "W:"+parameters.getPictureSize().width+"H:"+parameters.getPictureSize().height);
		Log.d("<<preview>>", "W:"+parameters.getPreviewSize().width+"H:"+parameters.getPreviewSize().height);

		int tempWidth = parameters.getPictureSize().width;
		int tempHeight = parameters.getPictureSize().height;
		int Result = 0;
		int Result2 = 0;
		int picSum = 0;
		int picSum2 = 0;
		int soin = 2;

		while(tempWidth >= soin && tempHeight >= soin){
			Result = tempWidth%soin;
			Result2 = tempHeight%soin;
			if(Result == 0 && Result2 == 0){
				picSum = tempWidth/soin;
				picSum2 = tempHeight/soin;
				System.out.println("PictureWidth :"+tempWidth+"/"+soin+"결과:"+picSum+"나머지:"+Result);
				System.out.println("PictureHeight :"+tempHeight+"/"+soin+"결과:"+picSum2+"나머지:"+Result2);
				tempWidth = picSum;
				tempHeight = picSum2;
			}else {
				soin++;
			}

		}
		System.out.println("최종결과 "+picSum+":"+picSum2);

		List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
		for (Camera.Size size : previewSizeList){
			tempWidth = size.width;
			tempHeight = size.height;
			Result = 0;
			Result2 = 0;
			int preSum = 0;
			int preSum2 = 0;
			soin = 2;

			while(tempWidth >= soin && tempHeight >= soin){
				Result = tempWidth%soin;
				Result2 = tempHeight%soin;
				if(Result == 0 && Result2 == 0){
					preSum = tempWidth/soin;
					preSum2 = tempHeight/soin;
					System.out.println("PreviewWidth :"+tempWidth+"/"+soin+"결과:"+preSum+"나머지:"+Result);
					System.out.println("PreviewHeight :"+tempHeight+"/"+soin+"결과:"+preSum2+"나머지:"+Result2);
					tempWidth = preSum;
					tempHeight = preSum2;
				}else {
					soin++;
				}

			}
			System.out.println("최종결과 "+preSum+":"+preSum2);
			if(picSum == preSum && picSum2 == preSum2){
				parameters.setPreviewSize(size.width, size.height);
				previewWidth = size.width;
				previewHeight = size.height;
				break;
			}
		}
		return parameters;
	}



	private static Camera.Size getCameraPreviewSize(List<Camera.Size> previewList, int targetPreviewWidth, int targetPreviewHeight, double targetRatio, int targetHeight) {
		final double aspectTolerance = 0.05;
		double      minDiff = Double.MAX_VALUE;
		Camera.Size optimalSize = null;

		for (Camera.Size size : previewList) {
			double ratio = (double) size.width / size.height;

			if (Math.abs(ratio - targetRatio) > aspectTolerance) {
				continue;
			}

			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		return optimalSize;
	}






	public List<Camera.Size> getSupportedPictureSizes() {
		if (mCamera == null) {
			return null;
		}

		List<Camera.Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();

		checkSupportedPictureSizeAtPreviewSize(pictureSizes);

		return pictureSizes;
	}

	private void checkSupportedPictureSizeAtPreviewSize(List<Camera.Size> pictureSizes) {
		List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
		Camera.Size pictureSize;
		Camera.Size previewSize;
		double  pictureRatio = 0;
		double  previewRatio = 0;
		final double aspectTolerance = 0.05;
		boolean isUsablePicture = false;

		for (int indexOfPicture = pictureSizes.size() - 1; indexOfPicture >= 0; --indexOfPicture) {
			pictureSize = pictureSizes.get(indexOfPicture);
			pictureRatio = (double) pictureSize.width / (double) pictureSize.height;
			isUsablePicture = false;

			for (int indexOfPreview = previewSizes.size() - 1; indexOfPreview >= 0; --indexOfPreview) {
				previewSize = previewSizes.get(indexOfPreview);

				previewRatio = (double) previewSize.width / (double) previewSize.height;

				if (Math.abs(pictureRatio - previewRatio) < aspectTolerance) {
					isUsablePicture = true;
					break;
				}
			}

			if (isUsablePicture == false) {
				pictureSizes.remove(indexOfPicture);
			}
		}
	}



	public int getPreviewWidth() {
		return previewWidth;
	}

	public int getPreviewHeight() {
		return previewHeight;
	}

}