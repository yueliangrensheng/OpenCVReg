package com.diwen.android.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.diwen.android.R;
import com.diwen.android.bean.LineData;
import com.diwen.android.opencv.DetectionBased;
import com.diwen.android.ui.fragment.PatientDataFragment;
import com.diwen.android.ui.fragment.PreTreatFragment;
import com.diwen.android.ui.fragment.TreatmentFragment;
import com.diwen.android.widget.CameraBridgeViewBase;
import com.diwen.android.widget.CameraContainer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by zhaishaoping on 20/11/2016.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private FragmentManager mFragmentManager;
    private TextView tvPatientData, tvPreTreatment, tvTreatment;


    //投影区
    private CameraContainer mCameraContainer;
    private DetectionBased mDetectionBased;
    private Mat mRgba;

    static {
        System.loadLibrary("OpenCV");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.layout_main);

        tvPatientData = (TextView) findViewById(R.id.main_tv_patient_data);
        tvPreTreatment = (TextView) findViewById(R.id.main_tv_pre_treatment);
        tvTreatment = (TextView) findViewById(R.id.main_tv_treatment);

        tvPatientData.setOnClickListener(this);
        tvPreTreatment.setOnClickListener(this);
        tvTreatment.setOnClickListener(this);


        mCameraContainer = (CameraContainer) findViewById(R.id.cameraContainer);
        mCameraContainer.setVisibility(CameraBridgeViewBase.VISIBLE);
        mCameraContainer.setCvCameraViewListener(mCvCameraViewListener);


        mFragmentManager = getSupportFragmentManager();

        //首页面
        exchangeFragment(new PatientDataFragment());


    }

    private void exchangeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.main_tv_patient_data:
                fragment = new PatientDataFragment();
                exchangeFragment(fragment);
                break;
            case R.id.main_tv_pre_treatment:
                fragment = new PreTreatFragment();
                exchangeFragment(fragment);
                break;
            case R.id.main_tv_treatment:
                fragment = new TreatmentFragment();
                exchangeFragment(fragment);

                break;

            default:

                break;
        }

    }

    private long exitTime = 0;
    CameraBridgeViewBase.CvCameraViewListener2 mCvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mRgba = new Mat();
        }

        @Override
        public void onCameraViewStopped() {
            if (mRgba != null) {
                mRgba.release();
            }
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            // 这里是屏幕上的每一帧的数据，请在这里处理图像

            mRgba = inputFrame.rgba();

            if ((System.currentTimeMillis() - exitTime) > 400) {
                long startTime = System.currentTimeMillis();
                String[] location = mDetectionBased.recognized(mRgba);
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;
                //time = time /1000;
                final StringBuffer str = new StringBuffer();
                for (int i = 0; i < location.length; i++) {
                    str.append(location[i] + ",");
                }

                exitTime = System.currentTimeMillis();

				 /*final Bitmap bmp =Bitmap.createBitmap( mRgba.width(),  mRgba.height(),  Bitmap.Config.RGB_565);
                 //saveBitmap(bmp);
			       covMat2bm(mRgba,bmp);  */
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      // vImageTest.setImageBitmap(bmp);

                                      String[] dd = str.toString().split(",");
                                      if (dd.length >= 2 && Integer.parseInt(dd[0]) > 0) {
                                          float x = Float.parseFloat(dd[1]);
                                          float y = Float.parseFloat(dd[2]);
                                          int r = Integer.parseInt(dd[3]);
                                          //x = x-150;
                                          y += 50;
                                          startWork(r, x, y);
                                      }
                                  }
                              }
                );

            } else {

            }
            return mRgba;
        }
    };

    public void startWork(int r, float x, float y) {
        LineData lineData = new LineData();
        lineData.startX = x;
        lineData.startY = y;
//        if(mLineView != null ){
//            if(mLineView.isFinish()){
//                mLineView.startWork(r,lineData);
//            }
//
//        }
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("opencv_java3");
                    mDetectionBased = new DetectionBased();
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

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraContainer != null) {
            mCameraContainer.disableView();
        }
    }


}
