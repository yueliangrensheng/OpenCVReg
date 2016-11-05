package com.diwen.android.camera.inter;

import android.hardware.Camera;

/**
 * CameraHelper的统一接口
 */
public interface ICameraHelper {
    /**
     *
     * @return 获取设备上摄像头的个数
     */
    int getNumberOfCameras();

    Camera openCameraFacing(int facing) throws Exception;

    boolean hasCamera(int facing);

    void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo);
}
