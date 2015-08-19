package com.jackiezhuang.sgframework.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Toastc操作工具
 * <p/>
 * Created by zsigui on 15-8-11.
 */
public final class T {
	private Toast mToast;
	private static T mInstance;
	private static Context mContext;
	private int mDuration = Toast.LENGTH_SHORT;

	private T(Context context) {
		mToast = new Toast(context);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(mDuration);
		mToast.setMargin(0, 0);
	}

	public static T newInstance(Context context) {
		if (context == null && mInstance == null) {
			throw new IllegalArgumentException("please set a not null context to get the instance");
		}
		if (context != mContext || mInstance == null) {
			mContext = context;
			mInstance = new T(context);
		}
		return mInstance;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public void show(CharSequence msg) {
		mToast.cancel();
		mToast.setText(msg);
		mToast.show();
	}

	public void show(int resId) {
		show(mContext.getString(resId));
	}

	public static void show(Context context, CharSequence msg, int duration) {
		Toast.makeText(context, msg, duration).show();
	}

	public static void showL(Context context, CharSequence msg) {
		T.show(context, msg, Toast.LENGTH_LONG);
	}

	public static void showS(Context context, CharSequence msg) {
		T.show(context, msg, Toast.LENGTH_SHORT);
	}

	public static void showInCenter(Context context, CharSequence msg, int duration) {
		Toast t = Toast.makeText(context, msg, duration);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	public static void showLInCenter(Context context, CharSequence msg) {
		showInCenter(context, msg, Toast.LENGTH_LONG);
	}

	public static void showSInCenter(Context context, CharSequence msg) {
		showInCenter(context, msg, Toast.LENGTH_SHORT);
	}
}
