package com.jackiezhuang.sgframework.utils;

import android.util.Log;

/**
 *
 * Log输出管理类
 *
 * Created by zsigui on 15-8-10.
 */
public final class L {
	private static boolean sDebuggable = true;
	// 2.verbose 3.debug 4.info 5.warn 6.error
	private static int sDebugLevel = L.DEBUG;
	private static final String DEBUG_TAG = L.class.toString();

	/**
	 * Priority constant for the println method; use L.v.
	 */
	public static final int VERBOSE = 2;

	/**
	 * Priority constant for the println method; use L.d.
	 */
	public static final int DEBUG = 3;

	/**
	 * Priority constant for the println method; use L.i.
	 */
	public static final int INFO = 4;

	/**
	 * Priority constant for the println method; use L.w.
	 */
	public static final int WARN = 5;

	/**
	 * Priority constant for the println method; use L.e.
	 */
	public static final int ERROR = 6;

	/**
	 *
	 * 设置测试输出log的全局变量
	 *
	 * @param isDebug
	 */
	public static void setDebug(boolean isDebug) {
		sDebuggable = isDebug;
	}

	/**
	 *
	 * set the debug level, e.g. verbose allowed all level to print log.
	 * <p>this will influence all<p/>
	 *
	 * @param debugLevel 调试等级：{@link #VERBOSE},{@link #DEBUG},{@link #INFO},{@link #WARN},{@link #ERROR}
	 */
	public static void setDebugLevel(int debugLevel) {
		sDebugLevel = debugLevel;
	}

	public static void v(String tag, String msg, int level) {
		if (sDebuggable) {
			if (level >= sDebugLevel) {
				Log.v(tag, msg);
			}
		}
	}

	public static void d(String tag, String msg, int level) {
		if (sDebuggable) {
			if (level >= sDebugLevel) {
				Log.d(tag, msg);
			}
		}
	}

	public static void i(String tag, String msg, int level) {
		if (sDebuggable) {
			if (level >= sDebugLevel) {
				Log.i(tag, msg);
			}
		}
	}

	public static void w(String tag, String msg, int level) {
		if (sDebuggable) {
			if (level >= sDebugLevel) {
				Log.w(tag, msg);
			}
		}
	}

	public static void e(String tag, String msg, int level) {
		if (sDebuggable) {
			if (level >= sDebugLevel) {
				Log.e(tag, msg);
			}
		}
	}

	public static void v(String tag, String msg) {
		v(tag, msg, VERBOSE);
	}

	public static void d(String tag, String msg) {
		d(tag, msg, DEBUG);
	}

	public static void i(String tag, String msg) {
		i(tag, msg, INFO);
	}

	public static void w(String tag, String msg) {
		w(tag, msg, WARN);
	}

	public static void e(String tag, String msg) {
		e(tag, msg, ERROR);
	}

	public static void v(String msg) {
		v(DEBUG_TAG, msg);
	}

	public static void d(String msg) {
		d(DEBUG_TAG, msg);
	}

	public static void i(String msg) {
		i(DEBUG_TAG, msg);
	}

	public static void w(String msg) {
		w(DEBUG_TAG, msg);
	}

	public static void e(String msg) {
		e(DEBUG_TAG, msg);
	}
}
