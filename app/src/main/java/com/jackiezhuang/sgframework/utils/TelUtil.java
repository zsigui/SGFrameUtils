package com.jackiezhuang.sgframework.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by JackieZhuang on 2015/8/18.
 */
public class TelUtil {
	/**
	 * 设备ID
	 */
	public static final String DEVICE_ID = "getDeviceId";
	/**
	 * 设备软件版本号
	 */
	public static final String DEVICE_SOFTWARE_VERSION = "getDeviceSoftwareVersion";
	/**
	 * 电话号码，现在SIM已经无法由此获取
	 */
	public static final String LINE1_NUMBER = "getLine1Number";
	/**
	 * 网络国家ISO码
	 */
	public static final String NETWORK_COUNTRY_ISO = "getNetworkCountryIso";
	/**
	 * 网络提供商数字名字（MCC+MNC）
	 */
	public static final String NETWORK_OPERATOR = "getNetworkOperator";
	/**
	 * 网络提供商名字（字母形式）
	 */
	public static final String NETWORK_OPERATOR_NAME = "getNetworkOperatorName";
	/**
	 * 网络类型
	 */
	public static final String NETWORK_TYPE = "getNetworkType";
	/**
	 * 语音通话无线类型
	 */
	public static final String PHONE_TYPE = "getPhoneType";
	/**
	 * SIM卡提供商国家代码
	 */
	public static final String SIM_COUNTRY_ISO = "getSimCountryIso";
	/**
	 * SIM卡提供商代码
	 */
	public static final String SIM_OPERATOR = "getSimOperator";
	/**
	 * SIM卡服务商名字
	 */
	public static final String SIM_OPERATOR_NAME = "getSimOperatorName";
	/**
	 * SIM卡编码
	 */
	public static final String SIM_SERIAL_NUMBER = "getSimSerialNumber";
	/**
	 * SIM卡状态
	 */
	public static final String SIM_STATE = "getSimState";
	/**
	 * 国际移动用户识别码
	 */
	public static final String SUBSCRIBER_ID = "getSubscriberId";
	/**
	 * 设备语音邮箱码
	 */
	public static final String VOICE_MAIL_NUMBER = "getVoiceMailNumber";

	/**
	 * 获取CPU类型和ABI规则
	 *
	 * @return
	 */
	public static String getCPU_API() {
		return Build.CPU_ABI;
	}

	/**
	 * 根据传入的变量名获取对应的设备信息
	 * 如 getDeviceId 获取 IMEI
	 *
	 * @param context 传入的Context
	 * @param name    获取对应信息的方法名
	 * @return 对应的查询信息
	 */
	public static String getInfoByName(Context context, String name) {
		String result = null;
		TelephonyManager iPhoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (iPhoneManager != null) {
			try {
				Method method = iPhoneManager.getClass().getDeclaredMethod(name);
				Object obj = method.invoke(iPhoneManager);
				if (obj instanceof String) {
					result = (String) obj;
				} else {
					result = String.valueOf(obj);
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
