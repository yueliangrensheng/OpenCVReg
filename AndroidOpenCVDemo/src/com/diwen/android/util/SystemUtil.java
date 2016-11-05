package com.diwen.android.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by zhaishaoping on 2016/10/22.
 */

public class SystemUtil {

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        return mDisplayMetrics;
    }
}
