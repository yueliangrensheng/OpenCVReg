package com.diwen.android.util;

import java.io.File;
import java.io.FileOutputStream;

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

	public static boolean saveBitmap(Bitmap b, String absolutePath) {
		return saveBitmap(b, absolutePath, 100);
	}

	public static boolean saveBitmap(Bitmap b, String absolutePath, Bitmap.CompressFormat format) {
		return saveBitmap(b, absolutePath, 100, format);
	}

	public static boolean saveBitmap(Bitmap b, String absolutePath, int quality) {
		return saveBitmap(b, absolutePath, quality, Bitmap.CompressFormat.JPEG);
	}

	public static boolean saveBitmap(Bitmap b, String absolutePath, int quality, Bitmap.CompressFormat format) {
		String fileName = absolutePath;
		File f = new File(fileName);
		try {
			f.createNewFile();
			FileOutputStream fOut = new FileOutputStream(f);
			b.compress(format, quality, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (Exception e) {
		}
		return false;
	}
}
