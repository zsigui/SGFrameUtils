package com.jackiezhuang.sgframework.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

/**
 *
 * SharedPreferences操作工具类
 *
 * Created by zsigui on 15-8-10.
 */
public final class SPUtil {
	/**
	 * 默认字符,空
	 */
	public final String DEFAULT_STRING = "";
	/**
	 * 默认长整形值,-1L
	 */
	public final long DEFAULT_LONG = -1L;
	/**
	 * 默认整形值,-1
	 */
	public final int DEFAULT_INT = -1;
	/**
	 * 默认浮点值,-1.0F
	 */
	public final float DEFAULT_FLOAT = -1.0F;
	/**
	 * 默认bool值,false
	 */
	public final boolean DEFAULT_BOOLEAN = false;
	/**
	 * 默认字符串集合,null
	 */
	public final Set<String> DEFAULT_STRING_SET = null;

	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private static boolean isNeedRebuild = true;
	private static SPUtil mInstance;

	/**
	 * 同步方法，用于初始化对象，必须首先调用
	 *
	 * @param context
	 * @param prefs_file
	 * @param mode
	 */
	public static synchronized void init(Context context, String prefs_file, int mode) {
		if (isNeedRebuild && mInstance != null) {
			if (context == null || TextUtils.isEmpty(prefs_file)) {
				throw new IllegalArgumentException("context和prefs_file参数不能为空");
			}
			if (mode != Context.MODE_PRIVATE && mode != Context.MODE_WORLD_READABLE && mode != Context
					.MODE_WORLD_WRITEABLE) {
				mode = Context.MODE_PRIVATE;
			}
			mInstance = new SPUtil(context.getApplicationContext(), prefs_file, mode);
			isNeedRebuild = false;
		}
	}

	/**
	 * 同步方法，用于初始化对象，必须首先调用，采用默认私有模式Context.MODE_PRIVATE
	 *
	 * @param context
	 * @param prefs_file
	 */
	public static synchronized void init(Context context, String prefs_file) {
		init(context, prefs_file);
	}

	/**
	 * 私有构造函数
	 */
	private SPUtil(Context context, String prefs_file, int mode) {
		sp = context.getSharedPreferences(prefs_file, mode);
		editor = sp.edit();
	}

	/**
	 * 获取SP实例，需要先调用init()方法初始化
	 *
	 * @return
	 * @throws java.lang.RuntimeException 如果还没有初始化，抛出异常
	 */
	public static SPUtil getInstance() {
		if (mInstance == null) {
			throw new RuntimeException("must call init() before");
		}
		return mInstance;
	}


	public String getString(String key) {
		return getString(key, DEFAULT_STRING);
	}

	public long getLong(String key) {
		return getLong(key, DEFAULT_LONG);
	}

	public int getInt(String key) {
		return getInt(key, DEFAULT_INT);
	}

	public Float getFloat(String key) {
		return getFloat(key, DEFAULT_FLOAT);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, DEFAULT_BOOLEAN);
	}

	public Set<String> getStringSet(String key) {
		return getStringSet(key, DEFAULT_STRING_SET);
	}

	public String getString(String key, String defVal) {
		return sp.getString(key, defVal);
	}

	public long getLong(String key, long defVal) {
		return sp.getLong(key, defVal);
	}

	public int getInt(String key, int defVal) {
		return sp.getInt(key, defVal);
	}

	public Float getFloat(String key, float defVal) {
		return sp.getFloat(key, defVal);
	}

	public boolean getBoolean(String key, boolean defVal) {
		return sp.getBoolean(key, defVal);
	}

	public Set<String> getStringSet(String key, Set<String> defVal) {
		return sp.getStringSet(key, defVal);
	}

	public void putBoolean(String key, boolean val) {
		editor.putBoolean(key, val);
	}

	public void putLong(String key, long val) {
		editor.putLong(key, val);
	}

	public void putInt(String key, int val) {
		editor.putInt(key, val);
	}

	public void putFloat(String key, float val) {
		editor.putFloat(key, val);
	}

	public void putString(String key, String val) {
		editor.putString(key, val);
	}

	public void putStringSet(String key, Set<String> val) {
		editor.putStringSet(key, val);
	}

	public void writeBNow(String key, boolean val) {
		putBoolean(key, val);
		commitOrApplyWithType(true);
	}

	public void writeLAsync(String key, long val) {
		putLong(key, val);
		commitOrApplyWithType(false);
	}

	public void writeIAsync(String key, int val) {
		putInt(key, val);
		commitOrApplyWithType(false);
	}

	public void writeFAsync(String key, float val) {
		putFloat(key, val);
		commitOrApplyWithType(false);
	}

	public void writeSAsync(String key, String val) {
		putString(key, val);
		commitOrApplyWithType(false);
	}

	public void writeSSetAsync(String key, Set<String> val) {
		putStringSet(key, val);
		commitOrApplyWithType(false);
	}

	public void writeBSync(String key, boolean val) {
		putBoolean(key, val);
		commitOrApplyWithType(true);
	}

	public void writeLSync(String key, long val) {
		putLong(key, val);
		commitOrApplyWithType(true);
	}

	public void writeISync(String key, int val) {
		putInt(key, val);
		commitOrApplyWithType(true);
	}

	public void writeFSync(String key, float val) {
		putFloat(key, val);
		commitOrApplyWithType(true);
	}

	public void writeSSync(String key, String val) {
		putString(key, val);
		commitOrApplyWithType(true);
	}

	public void writeSSetSync(String key, Set<String> val) {
		putStringSet(key, val);
		commitOrApplyWithType(true);
	}

	public void remove(String key) {
		editor.remove(key);
	}

	public void commitRemove(String key) {
		editor.remove(key);
		editor.commit();
	}

	public void clear() {
		editor.clear();
		editor.commit();
	}

	public void destory() {
		clear();
		isNeedRebuild = true;
		editor = null;
		mInstance = null;
	}

	public void commitOrApplyWithType(boolean isCommit) {
		if (isCommit) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
}
