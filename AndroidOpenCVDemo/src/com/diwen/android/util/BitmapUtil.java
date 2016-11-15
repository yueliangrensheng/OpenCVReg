package com.diwen.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

	public static Bitmap createBitmap(byte[] data, int sampleSize) {
		Bitmap bitmap = null;
		try {
			// 获得图片大小
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = sampleSize;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPurgeable = true;
			options.inInputShareable = true;

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
