package com.diwen.android.widget;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.diwen.android.camera.SensorControler;
import com.diwen.android.camera.inter.ICameraOperation;
import com.diwen.android.ui.activity.CameraActivity;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;

public class JavaCameraView extends CameraBridgeViewBase
		implements PreviewCallback, SurfaceHolder.Callback, ICameraOperation{

	private static final int MAGIC_TEXTURE_ID = 10;
	private static final String TAG = "JavaCameraView";

	private byte mBuffer[];
	private Mat[] mFrameChain;
	private int mChainIdx = 0;
	private Thread mThread;
	private boolean mStopThread;

	protected Camera mCamera;
	protected JavaCameraFrame[] mCameraFrame;
	private SurfaceTexture mSurfaceTexture;

	// ================start==============
	private Context mContext;
	private SensorControler mSensorControler;
	private CameraActivity mActivity;
	 /**
	 * 当前缩放
	 */
	 private int mZoom;

	public static class JavaCameraSizeAccessor implements ListItemAccessor {

		@Override
		public int getWidth(Object obj) {
			Camera.Size size = (Camera.Size) obj;
			return size.width;
		}

		@Override
		public int getHeight(Object obj) {
			Camera.Size size = (Camera.Size) obj;
			return size.height;
		}
	}

	public JavaCameraView(Context context, int cameraId) {
		super(context, cameraId);
	}

	public JavaCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public JavaCameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		setFocusable(true);
		getHolder().addCallback(this);// 为SurfaceView的句柄添加一个回调函数
		mSensorControler = SensorControler.getInstance();
	}

	public void bindActivity(CameraActivity activity) {
		this.mActivity = activity;
	}
	
	/**
	 * 手动聚焦
	 *
	 * @param point
	 *            触屏坐标
	 */
	protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
		if (mCamera == null) {
			return false;
		}

		Camera.Parameters parameters = null;
		try {
			parameters = mCamera.getParameters();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		// 不支持设置自定义聚焦，则使用自动聚焦，返回

		if (Build.VERSION.SDK_INT >= 14) {

			if (parameters.getMaxNumFocusAreas() <= 0) {
				return focus(callback);
			}

			Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

			List<Camera.Area> areas = new ArrayList<Camera.Area>();
			int left = point.x - 300;
			int top = point.y - 300;
			int right = point.x + 300;
			int bottom = point.y + 300;
			left = left < -1000 ? -1000 : left;
			top = top < -1000 ? -1000 : top;
			right = right > 1000 ? 1000 : right;
			bottom = bottom > 1000 ? 1000 : bottom;
			areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
			parameters.setFocusAreas(areas);
			try {
				// 本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
				// 目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
				mCamera.setParameters(parameters);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}

		return focus(callback);
	}

	private boolean focus(Camera.AutoFocusCallback callback) {
		try {
			mCamera.autoFocus(callback);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// =================end=================

	protected boolean initializeCamera(int width, int height) {
		Log.d(TAG, "Initialize java camera");
		boolean result = true;
		synchronized (this) {
			mCamera = null;

			if (mCameraIndex == CAMERA_ID_ANY) {
				Log.d(TAG, "Trying to open camera with old open()");
				try {
					mCamera = Camera.open();
				} catch (Exception e) {
					Log.e(TAG, "Camera is not available (in use or does not exist): " + e.getLocalizedMessage());
				}

				if (mCamera == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					boolean connected = false;
					for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
						Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(camIdx) + ")");
						try {
							mCamera = Camera.open(camIdx);
							connected = true;
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera #" + camIdx + "failed to open: " + e.getLocalizedMessage());
						}
						if (connected)
							break;
					}
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					int localCameraIndex = mCameraIndex;
					if (mCameraIndex == CAMERA_ID_BACK) {
						Log.i(TAG, "Trying to open back camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo(camIdx, cameraInfo);
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
								localCameraIndex = camIdx;
								break;
							}
						}
					} else if (mCameraIndex == CAMERA_ID_FRONT) {
						Log.i(TAG, "Trying to open front camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo(camIdx, cameraInfo);
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								localCameraIndex = camIdx;
								break;
							}
						}
					}
					if (localCameraIndex == CAMERA_ID_BACK) {
						Log.e(TAG, "Back camera not found!");
					} else if (localCameraIndex == CAMERA_ID_FRONT) {
						Log.e(TAG, "Front camera not found!");
					} else {
						Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(localCameraIndex) + ")");
						try {
							mCamera = Camera.open(localCameraIndex);
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera #" + localCameraIndex + "failed to open: " + e.getLocalizedMessage());
						}
					}
				}
			}

			if (mCamera == null)
				return false;

			/* Now set camera parameters */
			try {
				Camera.Parameters params = mCamera.getParameters();
				Log.d(TAG, "getSupportedPreviewSizes()");
				List<android.hardware.Camera.Size> sizes = params.getSupportedPreviewSizes();

				if (sizes != null) {
					/*
					 * Select the size that fits surface considering maximum
					 * size allowed
					 */
					Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), width, height);

					params.setPreviewFormat(ImageFormat.NV21);
					Log.d(TAG, "Set preview size to " + Integer.valueOf((int) frameSize.width) + "x"
							+ Integer.valueOf((int) frameSize.height));
					params.setPreviewSize((int) frameSize.width, (int) frameSize.height);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
							&& !android.os.Build.MODEL.equals("GT-I9100"))
						params.setRecordingHint(true);

					List<String> FocusModes = params.getSupportedFocusModes();
					if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}

					mCamera.setParameters(params);
					params = mCamera.getParameters();

					mFrameWidth = params.getPreviewSize().width;
					mFrameHeight = params.getPreviewSize().height;

					if ((getLayoutParams().width == LayoutParams.MATCH_PARENT)
							&& (getLayoutParams().height == LayoutParams.MATCH_PARENT))
						mScale = Math.min(((float) height) / mFrameHeight, ((float) width) / mFrameWidth);
					else
						mScale = 0;

					if (mFpsMeter != null) {
						mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
					}

					int size = mFrameWidth * mFrameHeight;
					size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
					mBuffer = new byte[size];

					mCamera.addCallbackBuffer(mBuffer);
					mCamera.setPreviewCallbackWithBuffer(this);

					mFrameChain = new Mat[2];
					mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight / 2), mFrameWidth, CvType.CV_8UC1);
					mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight / 2), mFrameWidth, CvType.CV_8UC1);

					AllocateCache();

					mCameraFrame = new JavaCameraFrame[2];
					mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight);
					mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
						mCamera.setPreviewTexture(mSurfaceTexture);
					} else
						mCamera.setPreviewDisplay(null);

					/* Finally we are ready to start the preview */
					Log.d(TAG, "startPreview");
					mCamera.startPreview();
				} else
					result = false;
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * 释放相机
	 */
	public void releaseCamera() {
		synchronized (this) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);

				mCamera.release();
			}
			mCamera = null;
			if (mFrameChain != null) {
				mFrameChain[0].release();
				mFrameChain[1].release();
			}
			if (mCameraFrame != null) {
				mCameraFrame[0].release();
				mCameraFrame[1].release();
			}
		}
	}

	private boolean mCameraFrameReady = false;

	@Override
	protected boolean connectCamera(int width, int height) {

		/*
		 * 1. We need to instantiate camera 2. We need to start thread which
		 * will be getting frames
		 */
		/* First step - initialize camera connection */
		Log.d(TAG, "Connecting to camera");
		if (!initializeCamera(width, height))
			return false;

		mCameraFrameReady = false;

		/* now we can start update thread */
		Log.d(TAG, "Starting processing thread");
		mStopThread = false;
		mThread = new Thread(new CameraWorker());
		mThread.start();

		return true;
	}

	@Override
	protected void disconnectCamera() {
		/*
		 * 1. We need to stop thread which updating the frames 2. Stop camera
		 * and release it
		 */
		Log.d(TAG, "Disconnecting from camera");
		try {
			mStopThread = true;
			Log.d(TAG, "Notify thread");
			synchronized (this) {
				this.notify();
			}
			Log.d(TAG, "Wating for thread");
			if (mThread != null)
				mThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mThread = null;
		}

		/* Now release camera */
		releaseCamera();

		mCameraFrameReady = false;
	}

	@Override
	public void onPreviewFrame(byte[] frame, Camera arg1) {
		Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
		synchronized (this) {
			mFrameChain[mChainIdx].put(0, 0, frame);
			mCameraFrameReady = true;
			this.notify();
		}
		if (mCamera != null)
			mCamera.addCallbackBuffer(mBuffer);
	}

	private class JavaCameraFrame implements CvCameraViewFrame {
		@Override
		public Mat gray() {
			return mYuvFrameData.submat(0, mHeight, 0, mWidth);
		}

		@Override
		public Mat rgba() {
			Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
			return mRgba;
		}

		public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
			super();
			mWidth = width;
			mHeight = height;
			mYuvFrameData = Yuv420sp;
			mRgba = new Mat();
		}

		public void release() {
			mRgba.release();
		}

		private Mat mYuvFrameData;
		private Mat mRgba;
		private int mWidth;
		private int mHeight;
	};

	private class CameraWorker implements Runnable {

		@Override
		public void run() {
			do {
				boolean hasFrame = false;
				synchronized (JavaCameraView.this) {
					try {
						while (!mCameraFrameReady && !mStopThread) {
							JavaCameraView.this.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (mCameraFrameReady) {
						mChainIdx = 1 - mChainIdx;
						mCameraFrameReady = false;
						hasFrame = true;
					}
				}

				if (!mStopThread && hasFrame) {
					if (!mFrameChain[1 - mChainIdx].empty())
						deliverAndDrawFrame(mCameraFrame[1 - mChainIdx]);
				}
			} while (!mStopThread);
			Log.d(TAG, "Finish processing thread");
		}
	}


	@Override
	public int getMaxZoom() {
		if (mCamera == null)
			return -1;
		Camera.Parameters parameters = mCamera.getParameters();
		if (!parameters.isZoomSupported())
			return -1;
		return parameters.getMaxZoom() > 40 ? 40 : parameters.getMaxZoom();
	}

	@Override
	public void setZoom(int zoom) {
		if (mCamera == null)
			return;
		Camera.Parameters parameters;
		// 注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
		// stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
		parameters = mCamera.getParameters();

		if (!parameters.isZoomSupported())
			return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom = zoom;
	}

	@Override
	public int getZoom() {
		return mZoom;
	}
}
