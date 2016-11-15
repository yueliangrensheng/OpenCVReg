package com.diwen.android.widget;

import com.diwen.android.CameraApplication;
import com.diwen.android.R;
import com.diwen.android.camera.SensorControler;
import com.diwen.android.camera.inter.IActivityLifiCycle;
import com.diwen.android.camera.inter.ICameraOperation;
import com.diwen.android.ui.activity.CameraActivity;
import com.diwen.android.util.SystemUtil;
import com.diwen.android.widget.CameraBridgeViewBase.CvCameraViewListener2;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class CameraContainer extends FrameLayout implements ICameraOperation, IActivityLifiCycle {
	public static final String TAG = "CameraContainer";

	private Context mContext;

	/**
	 * 相机绑定的SurfaceView
	 */
	private JavaCameraView mCameraView;

	/**
	 * 触摸屏幕时显示的聚焦图案
	 */
	//private FocusImageView mFocusImageView;
	/**
	 * 缩放控件
	 */
	private SeekBar mZoomSeekBar;

	private CameraActivity mActivity;

	private boolean mFocusSoundPrepared;

	private int mFocusSoundId;

	private String mImagePath;

	private SensorControler mSensorControler;

	int screenWidth = 0;

	public static final int RESETMASK_DELY = 1000; // 一段时间后遮罩层一定要隐藏

	public CameraContainer(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public CameraContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	void init() {
		inflate(mContext, R.layout.custom_camera_container, this);

		mCameraView = (JavaCameraView) findViewById(R.id.cameraView);
		//mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
		mZoomSeekBar = (SeekBar) findViewById(R.id.zoomSeekBar);

		mSensorControler = SensorControler.getInstance();
		screenWidth = (int) (CameraApplication.mScreenWidth - 250 * SystemUtil.getDisplayMetrics(mContext).density);
		mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
			@Override
			public void onFocus() {

				Point point = new Point(screenWidth / 2, screenWidth / 2);

				onCameraFocus(point);
			}
		});
		
		mZoomSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

		// //音效初始化
		// mSoundPool = getSoundPool();
	}

	public void setImagePath(String mImagePath) {
		this.mImagePath = mImagePath;
	}

	public void bindActivity(CameraActivity activity) {
		this.mActivity = activity;
		if (mCameraView != null) {
			mCameraView.bindActivity(activity);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		int len = screenWidth;
//
//		// 保证View是正方形
//		setMeasuredDimension(len, heightMeasureSpec);
	}

	/**
	 * 记录是拖拉照片模式还是放大缩小照片模式
	 */

	private static final int MODE_INIT = 0;
	/**
	 * 放大缩小照片模式
	 */
	private static final int MODE_ZOOM = 1;
	private int mode = MODE_INIT;// 初始状态

	private float startDis;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 手指压下屏幕
		case MotionEvent.ACTION_DOWN:
			mode = MODE_INIT;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			// 如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
			if (mZoomSeekBar == null)
				return true;
			// 移除token对象为mZoomSeekBar的延时任务
			mHandler.removeCallbacksAndMessages(mZoomSeekBar);
			// mZoomSeekBar.setVisibility(View.VISIBLE);
			mZoomSeekBar.setVisibility(View.GONE);

			mode = MODE_ZOOM;
			/** 计算两个手指间的距离 */
			startDis = spacing(event);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == MODE_ZOOM) {
				// 只有同时触屏两个点的时候才执行
				if (event.getPointerCount() < 2)
					return true;
				float endDis = spacing(event);// 结束距离
				// 每变化10f zoom变1
				int scale = (int) ((endDis - startDis) / 10f);
				if (scale >= 1 || scale <= -1) {
					int zoom = mCameraView.getZoom() + scale;
					// zoom不能超出范围
					if (zoom > mCameraView.getMaxZoom())
						zoom = mCameraView.getMaxZoom();
					if (zoom < 0)
						zoom = 0;
					mCameraView.setZoom(zoom);
					mZoomSeekBar.setProgress(zoom);
					// 将最后一次的距离设为当前距离
					startDis = endDis;
				}
			}
			break;
		// 手指离开屏幕
		case MotionEvent.ACTION_UP:
			if (mode != MODE_ZOOM) {
				// 设置聚焦
				Point point = new Point((int) event.getX(), (int) event.getY());
				onCameraFocus(point);
			} else {
				// ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
				mHandler.postAtTime(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mZoomSeekBar.setVisibility(View.GONE);
					}
				}, mZoomSeekBar, SystemClock.uptimeMillis() + 2000);
			}
			break;
		}
		return true;
	}
	
		
	
	/**
	 * 两点的距离
	 */
	private float spacing(MotionEvent event) {
		if (event == null) {
			return 0;
		}
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 相机对焦 默认不需要延时
	 *
	 * @param point
	 */
	private void onCameraFocus(final Point point) {
		onCameraFocus(point, false);
	}

	/**
	 * 相机对焦
	 *
	 * @param point
	 * @param needDelay
	 *            是否需要延时
	 */
	public void onCameraFocus(final Point point, boolean needDelay) {
		long delayDuration = needDelay ? 300 : 0;

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!mSensorControler.isFocusLocked()) {
					if (mCameraView.onFocus(point, autoFocusCallback)) {
						mSensorControler.lockFocus();
						//mFocusImageView.startFocus(point);

						// 播放对焦音效
						if (mFocusSoundPrepared) {
						}
					}
				}
			}
		}, delayDuration);
	}

	@Override
	public int getMaxZoom() {
		return mCameraView.getMaxZoom();
	}

	@Override
	public void setZoom(int zoom) {
		mCameraView.setZoom(zoom);
	}

	@Override
	public int getZoom() {
		return mCameraView.getZoom();
	}

	@Override
	public void releaseCamera() {
		if (mCameraView != null) {
			mCameraView.releaseCamera();
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

		}
	};

	private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			// 聚焦之后根据结果修改图片
			/*if (success) {
				mFocusImageView.onFocusSuccess();
			} else {
				// 聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
				mFocusImageView.onFocusFailed();
			}*/
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// 一秒之后才能再次对焦
					mSensorControler.unlockFocus();
				}
			}, 1000);
		}
	};

	private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(final byte[] data, Camera camera) {

			Log.i(TAG, "pictureCallback");

		}
	};

	private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			mCameraView.setZoom(progress);
			mHandler.removeCallbacksAndMessages(mZoomSeekBar);
			// ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
			mHandler.postAtTime(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mZoomSeekBar.setVisibility(View.GONE);
				}
			}, mZoomSeekBar, SystemClock.uptimeMillis() + 2000);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void onStart() {
		mSensorControler.onStart();
	}

	@Override
	public void onStop() {
		mSensorControler.onStop();
	}

	public void setMaskOn() {

	}

	public void setMaskOff() {

	}


	/**
	 * 获取以中心点为中心的正方形区域
	 *
	 * @param data
	 * @return
	 */
	private Rect getCropRect(byte[] data) {
		// 获得图片大小
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		int width = options.outWidth;
		int height = options.outHeight;
		int centerX = width / 2;
		int centerY = height / 2;

		int PHOTO_LEN = Math.min(width, height);
		return new Rect(centerX - PHOTO_LEN / 2, centerY - PHOTO_LEN / 2, centerX + PHOTO_LEN / 2,
				centerY + PHOTO_LEN / 2);
	}

	/**
	 * 给出合适的sampleSize的建议
	 *
	 * @param data
	 * @param target
	 * @return
	 */
	private int suggestSampleSize(byte[] data, int target) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPurgeable = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0)
			return 1;
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target)
				candidate -= 1;
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target)
				candidate -= 1;
		}
		// if (VERBOSE)
		Log.i(TAG,
				"for w/h " + w + "/" + h + " returning " + candidate + "(" + (w / candidate) + " / " + (h / candidate));
		return candidate;
	}

	public void fileScan(String filePath) {
		Uri data = Uri.parse("file://" + filePath);
		mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
	}

	long lastTime;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			boolean result = (Boolean) msg.obj;

			Log.i(TAG, "TASK onPostExecute:" + (System.currentTimeMillis() - lastTime));

			if (result) {
				fileScan(mImagePath);
				// releaseCamera(); //不要在这个地方释放相机资源 这里是浪费时间的最大元凶 约1500ms左右
				// mActivity.postTakePhoto();
				Log.i(TAG, "TASK:" + (System.currentTimeMillis() - lastTime));
			} else {
				Log.e(TAG, "photo save failed!");
			}
		}
	};

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (mCameraView != null) {
			mCameraView.setVisibility(visibility);
		}
	}

	public void setCvCameraViewListener(CvCameraViewListener2 listener) {
		if (listener != null && mCameraView != null) {
			mCameraView.setCvCameraViewListener(listener);
		}
	}

	public void disableView() {
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	public void enableView() {
		if (mCameraView != null) {
			mCameraView.enableView();
		}
	}
}
