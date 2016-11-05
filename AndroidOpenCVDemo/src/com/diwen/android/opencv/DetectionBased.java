package com.diwen.android.opencv;

import org.opencv.core.Mat;

public class DetectionBased {

	public String recognized(Mat frame) {
		return nativeRecognized(frame.getNativeObjAddr());
	}

	private static native String nativeRecognized(long frameAddr);
}
