package com.diwen.android.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.diwen.android.R;
import com.diwen.android.bean.LineData;
import com.diwen.android.opencv.DetectionBased;
import com.diwen.android.opencv.DetectionBasedTracker;
import com.diwen.android.widget.CameraBridgeViewBase;
import com.diwen.android.widget.CameraBridgeViewBase.CvCameraViewFrame;
import com.diwen.android.widget.CameraBridgeViewBase.CvCameraViewListener2;

import com.diwen.android.widget.CameraContainer;
import com.diwen.android.widget.CustomLineView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	private DetectionBased mDetectionBased;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;
	private CustomLineView mLineView;
	private LinearLayout vllFouce,vllFCanvas;
	private ImageView vImageTest;
	int bigScale = 2;
	int smallScale = -2;
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

//				try {
//					// load cascade file from application resources
//					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
//					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
//					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
//					FileOutputStream os = new FileOutputStream(mCascadeFile);
//
//					byte[] buffer = new byte[4096];
//					int bytesRead;
//					while ((bytesRead = is.read(buffer)) != -1) {
//						os.write(buffer, 0, bytesRead);
//					}
//					is.close();
//					os.close();
//
//					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//					if (mJavaDetector.empty()) {
//						Log.e(TAG, "Failed to load cascade classifier");
//						mJavaDetector = null;
//					} else
//						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
//
//					mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
					mDetectionBased = new DetectionBased();
//
//					cascadeDir.delete();
//
//				} catch (IOException e) {
//					e.printStackTrace();
//					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
//				}

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
		vImageTest =  (ImageView) findViewById(R.id.iv_test);
		mLineView.finishLine();
		vllFouce = (LinearLayout)findViewById(R.id.ll_fouce);
		vllFCanvas = (LinearLayout)findViewById(R.id.ll_canvas);
	}

	private long exitTime = 0;
	CvCameraViewListener2 mCvCameraViewListener = new CvCameraViewListener2() {

		@Override
		public void onCameraViewStarted(int width, int height) {
			mRgba = new Mat();
//			mGray = new Mat();
			Log.i(TAG, "CvCameraViewListener2: width= " + width + " ,height= " + height);
		}

		@Override
		public void onCameraViewStopped() {
			Log.i(TAG, "CvCameraViewListener2: onCameraViewStopped");
			if (mRgba != null) {
				mRgba.release();
			}
//			if (mGray != null) {
//				mGray.release();
//			}
		}

		/**
		 * This method is invoked when delivery of the frame needs to be done.
		 * The returned values - is a modified frame which needs to be displayed
		 * on the screen. TODO: pass the parameters specifying the format of the
		 * frame (BPP, YUV or RGB and etc)
		 */
		@Override
		public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			// 这里是屏幕上的每一帧的数据，请在这里处理图像

			mRgba = inputFrame.rgba();
//			mGray = inputFrame.gray();
			
			/*String str = new String();
			for(int i =0;i<location.length;i++){
				str += location[i]+",";
			}
			Log.i(TAG, "location:"+str);*/ 
			
			if ((System.currentTimeMillis() - exitTime) > 400) {
				long startTime = System.currentTimeMillis();
				String[] location = mDetectionBased.recognized(mRgba);
				long endTime = System.currentTimeMillis();
				long time = endTime - startTime;
				//time = time /1000;
				Log.i(TAG, "间隔时间:"+time );	
				final StringBuffer str = new StringBuffer();
				for(int i =0;i<location.length;i++){
					str.append(location[i]+",") ;
				}
				
				Log.i(TAG, "返回的位置:"+str);
				exitTime = System.currentTimeMillis();
					
				 /*final Bitmap bmp =Bitmap.createBitmap( mRgba.width(),  mRgba.height(),  Bitmap.Config.RGB_565);
				 //saveBitmap(bmp);
			       covMat2bm(mRgba,bmp);  */
			       runOnUiThread(new Runnable() {
			    	   public void run() {
			    		  // vImageTest.setImageBitmap(bmp);
			    		   
			    		   String[] dd = str.toString().split(",");
							if(dd.length >= 2 && Integer.parseInt(dd[0])  > 0){
							  float x= Float.parseFloat(dd[1]);
							  float y= Float.parseFloat(dd[2]);
							  int r= Integer.parseInt(dd[3]);
							  //x = x-150;
							  y += 50;
							  startWork(r,x,y);
							}
			    	   }
					}
				);
			         
			} else {	
					
			}	
			
			
			
			
			// 个人理解：下面代码是 加载人脸识别特征
			// if (mAbsoluteFaceSize == 0) {
			// int height = mGray.rows();
			// if (Math.round(height * mRelativeFaceSize) > 0) {
			// mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			// }
			// mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
			// }
			//
			// MatOfRect faces = new MatOfRect();
			// mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
			// // //objdetect.CV_HAAR_SCALE_IMAGE
			// new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
			//
			// Rect[] facesArray = faces.toArray();
			// for (int i = 0; i < facesArray.length; i++)
			// Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
			// FACE_RECT_COLOR, 3);

			return mRgba;
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		if (mCameraContainer != null)
			mCameraContainer.disableView();
	}
	private int covMat2bm(Mat mat,Bitmap bm)  
	{  
	   Utils.matToBitmap(mat, bm);  
	   return 1;  
	}  
	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}
	/** 保存方法 */
	 public void saveBitmap(Bitmap bm) {
	  Log.e(TAG, "保存图片");
	  File f = new File(Environment.getExternalStorageDirectory().getPath(), "test.jpg");
	  if (f.exists()) {
	   f.delete();
	  }else{
		 try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  }
	  try {
	   FileOutputStream out = new FileOutputStream(f);
	   bm.compress(Bitmap.CompressFormat.PNG, 90, out);
	   out.flush();
	   out.close();
	   Log.i(TAG, "已经保存");
	  } catch (FileNotFoundException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }

	 }

	public void onDestroy() {
		super.onDestroy();
		mCameraContainer.disableView();
	}

	public void LigaOnclick(View v) {
		mLineView.finishLine();
	}
	public void BackOnclick(View v){
		vllFouce.setVisibility(View.VISIBLE);
		vllFCanvas.setVisibility(View.GONE);
		mLineView.clean();
		mLineView.finishLine();
	}
	public void WorkOnclick(View v){
		 
	}
	public void CleanOnclick(View v){
		mLineView.clean();
	}
	
	public void BigOnclick(View v){
		setZoom(bigScale);
	}
	public void SmallOnclick(View v){
		setZoom(smallScale);
	}
	public void FouceOnclick(View v){
		mLineView.clean();
		vllFouce.setVisibility(View.GONE);
		vllFCanvas.setVisibility(View.VISIBLE);
	}
	/**
	 * 设置放大缩小
	 * @param vZoom
	 */
	private void setZoom(int vZoom){
		if (vZoom >= 1 || vZoom <= -1) {
			int zoom = mCameraContainer.getZoom()+vZoom;
			
			// zoom不能超出范围
			if (zoom > mCameraContainer.getMaxZoom())
				zoom = mCameraContainer.getMaxZoom();
			if (zoom < 0)
				zoom = 0;
			mCameraContainer.setZoom(zoom);
		
			// 将最后一次的距离设为当前距离
		}
	}
	public void startWork(int r,float x,float y){
		LineData lineData = new LineData();
		lineData.startX = x;
		lineData.startY = y ;
    	if(mLineView != null ){
    		if(mLineView.isFinish()){
    			mLineView.startWork(r,lineData);
    		}
    		
    	}	
	}
}
