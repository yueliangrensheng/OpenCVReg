package com.diwen.android.camera.inter;


/**
 * 相机操作的接口
 */
public interface ICameraOperation {
    /**
     *  相机最大缩放级别
     *  @return
     */
    public int getMaxZoom();
    /**
     *  设置当前缩放级别
     *  @param zoom
     */
    public void setZoom(int zoom);
    /**
     *  获取当前缩放级别
     *  @return
     */
    public int getZoom();
    /**
     * 释放相机
     *
     */
    public void releaseCamera();
}
