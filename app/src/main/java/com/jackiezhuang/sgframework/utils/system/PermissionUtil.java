package com.jackiezhuang.sgframework.utils.system;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by zsigui on 15-8-19.
 */
public final class PermissionUtil {

	private static final String PERM_PREFIX = "android.permission.";
	public static final String PERM_ACCESS_NETWORK_STATE = PERM_PREFIX + "ACCESS_NETWORK_STATE";
	public static final String PERM_INTERNET = PERM_PREFIX + "INTERNET";
	public static final String PERM_WRITE_EXTERNAL_STORAGE = PERM_PREFIX + "WRITE_EXTERNAL_STORAGE";
	public static final String PERM_READ_EXTERNAL_STORAGE = PERM_PREFIX + "READ_EXTERNAL_STORAGE";
	public static final String PERM_ACCESS_WIFI_STATE = PERM_PREFIX + "ACCESS_WIFI_STATE";
	public static final String PERM_CHANGE_WIFI_STATE = PERM_PREFIX + "CHANGE_WIFI_STATE";
	public static final String PERM_BIND_APPWIDGET = PERM_PREFIX + "BIND_APPWIDGET";

	public static boolean hasPerm(Context context, String permName) {
		if (context.checkCallingOrSelfPermission(permName) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		return false;
	}
}
