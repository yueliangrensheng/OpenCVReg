package com.diwen.android;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.diwen.android.util.FileUtil;
import com.diwen.android.util.SystemUtil;

/**
 * Created by zhaishaoping on 2016/10/20.
 */

public class CameraApplication extends Application {
    public static CameraApplication CONTEXT;
    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;
    private Bitmap mCameraBitmap;

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = this;

        DisplayMetrics mDisplayMetrics = SystemUtil.getDisplayMetrics(this);
        CameraApplication.mScreenWidth = mDisplayMetrics.widthPixels;
        CameraApplication.mScreenHeight = mDisplayMetrics.heightPixels;
        FileUtil.initFolder();
    }

    public Bitmap getCameraBitmap() {
        return mCameraBitmap;
    }

    public void setCameraBitmap(Bitmap mCameraBitmap) {
        if (mCameraBitmap != null) {
            recycleCameraBitmap();
        }
        this.mCameraBitmap = mCameraBitmap;
    }

    public void recycleCameraBitmap() {
        if (mCameraBitmap != null) {
            if (!mCameraBitmap.isRecycled()) {
                mCameraBitmap.recycle();
            }
            mCameraBitmap = null;
        }
    }

}
