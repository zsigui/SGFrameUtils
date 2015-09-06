package com.jackiezhuang.sgframework.utils.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JackieZhuang on 2015/9/4.
 */
public class PackageUtil {

	/**
	 * 判断某Apk是否安装
	 */
	public boolean isAppInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 判断应用是否运行在前台
	 * <p>注意：需要android.permission.GET_TASKS权限<p/>
	 */
	public static boolean isRunInForeground(Context context, String packageName) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断App是否为系统预装应用，否则为手动安装
	 */
	public static boolean isSystemApp(Context context, String packageName) throws PackageManager
			.NameNotFoundException {
		ApplicationInfo info = getAppInfo(context, packageName);
		if (info == null) {
			throw new PackageManager.NameNotFoundException(packageName + " is not found");
		}
		return (info.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
	}

	/**
	 * 获取指定包名的应用信息，无则返回null
	 */
	public static ApplicationInfo getAppInfo(Context context, String packageName) {
		List<ApplicationInfo> appInfoList = getAllInstalledAppInfo(context);
		for (ApplicationInfo info : appInfoList) {
			if (info.packageName.equals(packageName)) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 获取指定位置Apk文件的信息
	 */
	public static PackageInfo getApkInfo(Context context, String apkPath) {
		return context.getPackageManager().getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
	}

	/**
	 * 安装APP包
	 *
	 * @param context          执行上下文
	 * @param absoluteFilePath 安装包文件的绝对路径
	 */
	public static void installAppInAbsolutePath(Context context, String absoluteFilePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + absoluteFilePath), "application/vnd.android.package-archive");
		context.startActivity(i);
	}

	/**
	 * 安装APP包
	 *
	 * @param context          执行上下文
	 * @param relativeFilePath 安装包文件在外部SD卡的相对路径
	 */
	public static void installApp(Context context, String relativeFilePath) {
		installAppInAbsolutePath(context, FileUtil.getExternalFilePath(relativeFilePath));
	}

	/**
	 * 获取所有已安装App的包名
	 */
	public static List<String> getAllInstalledAppName(Context context) {
		List<String> result = new ArrayList<>();
		List<ApplicationInfo> packages = getAllInstalledAppInfo(context);
		for (ApplicationInfo applicationInfo : packages) {
			result.add(applicationInfo.packageName);
		}
		return result;
	}

	/**
	 * 获取所有已安装App信息
	 */
	public static List<ApplicationInfo> getAllInstalledAppInfo(Context context) {
		return context.getPackageManager().getInstalledApplications(0);
	}
}
