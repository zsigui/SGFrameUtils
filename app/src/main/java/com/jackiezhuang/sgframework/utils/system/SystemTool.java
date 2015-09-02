package com.jackiezhuang.sgframework.utils.system;

import android.os.Build;

/**
 * 获取系统信息的工具类
 *
 * <p></p>
 * Created by zsigui on 15-8-17.
 */
public final class SystemTool {

	/**
	 * 返回系统的SDK版本号
	 *
	 * @return
	 */
	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取设备型号
	 *
	 * @return
	 */
	public static String getDeviceType() {
		return Build.MODEL;
	}

	/**
	 * 获取设备系统版本号
	 *
	 * @return
	 */
	public static String getDeviceOsVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取硬件设备名
	 *
	 * @return
	 */
	public static String getHardware() {
		return Build.HARDWARE;
	}

	/**
	 * 获取CPU类型和ABI规则
	 *
	 * @return
	 */
	public static String getCPU_API() {
		return Build.CPU_ABI;
	}


}
