package com.diwen.android.ui.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import com.diwen.android.R;
import com.diwen.android.opencv.DetectionBased;
import com.diwen.android.opencv.DetectionBasedTracker;
import com.diwen.android.util.FileUtil;
import com.diwen.android.widget.CameraBridgeViewBase;
import com.diwen.android.widget.CameraBridgeViewBase.CvCameraViewFrame;
import com.diwen.android.widget.CameraBridgeViewBase.CvCameraViewListener2;
import com.diwen.android.widget.CameraContainer;
import com.diwen.android.widget.CustomLineView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";

	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	private DetectionBased mDetectionBased;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;
	private CustomLineView mLineView;

	static {
		System.loadLibrary("OpenCV");
	}

	private CameraContainer mCameraContainer;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("opencv_java3");
				// System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
					mDetectionBased = new DetectionBased();

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mCameraContainer.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.face_detect_surface_view);

		mCameraContainer = (CameraContainer) findViewById(R.id.cameraContainer);
		mCameraContainer.setVisibility(CameraBridgeViewBase.VISIBLE);
		mCameraContainer.setCvCameraViewListener(mCvCameraViewListener);

		mLineView = (CustomLineView) findViewById(R.id.lineView);
	}

	private long exitTime = 0;
	CvCameraViewListener2 mCvCameraViewListener = new CvCameraViewListener2() {

		@Override
		public void onCameraViewStarted(int width, int height) {
			mRgba = new Mat();
			mGray = new Mat();
			Log.i(TAG, "CvCameraViewListener2: width= " + width + " ,height= " + height);
		}

		@Override
		public void onCameraViewStopped() {
			Log.i(TAG, "CvCameraViewListener2: onCameraViewStopped");
			if (mRgba != null) {
				mRgba.release();
			}
			if (mGray != null) {
				mGray.release();
			}
		}

		/**
		 * This method is invoked when delivery of the frame needs to be done.
		 * The returned values - is a modified frame which needs to be displayed
		 * on the screen. TODO: pass the parameters specifying the format of the
		 * frame (BPP, YUV or RGB and etc)
		 */
		@Override
		public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
			// 这里是屏幕上的每一帧的数据，请在这里处理图像
			
//			System.out.println("temp ===  "+(System.currentTimeMillis()-exitTime));
			
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			
			// 拿到每帧后保存到本地
//			boolean issuccess = Imgcodecs.imwrite(FileUtil.getAppFoler()+File.separator + "OpenCvimwrite.jpg", mRgba);
//			System.out.println("图片是否保存： "+ mRgba.isContinuous());
			
//			
//			Timer timer = new Timer();
//			timer.scheduleAtFixedRate(new TimerTask() {
//
//				@Override
//				public void run() {
////					String location = mDetectionBased.recognized(mRgba);
//				}
//			}, 10000, 40);

			return mRgba;
		}
	};
	
	

	@Override
	public void onPause() {
		super.onPause();
		if (mCameraContainer != null)
			mCameraContainer.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		mCameraContainer.disableView();
	}

	public void LigaOnclick(View v) {
		mLineView.finishLine();
	}
}
