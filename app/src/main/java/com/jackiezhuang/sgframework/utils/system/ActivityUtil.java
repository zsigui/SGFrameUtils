package com.jackiezhuang.sgframework.utils.system;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.Surface;

import com.jackiezhuang.sgframework.utils.L;

/**
 * Created by zsigui on 15-9-2.
 */
public class ActivityUtil {

	private static final String TAG = ActivityUtil.class.getName();

	/**
	 * 获取当前屏幕方向
	 *
	 * @param activity
	 * @return
	 */
	public static int detachScreenOrientation(Activity activity) {
		try {
			int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			int width = dm.widthPixels;
			int height = dm.heightPixels;
			int orientation;
			// if the device's natural orientation is portrait:
			if ((rotation == Surface.ROTATION_0
					|| rotation == Surface.ROTATION_180) && height > width || (rotation == Surface.ROTATION_90
					|| rotation == Surface.ROTATION_270) && width > height) {
				switch (rotation) {
					case Surface.ROTATION_0:
						orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						break;
					case Surface.ROTATION_90:
						orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						break;
					case Surface.ROTATION_180:
						orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
						break;
					case Surface.ROTATION_270:
						orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
						break;
					default:
						orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						break;
				}
			}
			// if the device's natural orientation is landscape or if the device
			// is square:
			else {
				switch (rotation) {
					case Surface.ROTATION_0:
						orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						break;
					case Surface.ROTATION_90:
						orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						break;
					case Surface.ROTATION_180:
						orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
						break;
					case Surface.ROTATION_270:
						orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
						break;
					default:
						orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						break;
				}
			}
			return orientation;
		} catch (Throwable e) {
			e.printStackTrace();
			L.e(TAG, "detachScreenOrientation(Activity) : meet with a exception");
		}
		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		try {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (dpValue * scale + 0.5f);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		try {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (pxValue / scale + 0.5f);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return 0;
	}
}
