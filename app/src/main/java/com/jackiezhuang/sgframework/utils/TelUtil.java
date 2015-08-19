package com.jackiezhuang.sgframework.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by JackieZhuang on 2015/8/18.
 */
public final class TelUtil {
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
	 * 设备语音邮箱码字符标识
	 */
	public static final String VOICE_MAIL_TAG = "getVoiceMailAlphaTag";

	/**
	 * 根据传入的变量名获取对应的设备信息
	 * 如 getDeviceId 获取 IMEI
	 *
	 * @param context 传入的Context
	 * @param name    获取对应信息的方法名
	 * @return 对应的查询信息
	 *
	 * @see #DEVICE_ID
	 * @see #DEVICE_SOFTWARE_VERSION
	 * @see #LINE1_NUMBER
	 * @see #NETWORK_COUNTRY_ISO
	 * @see #NETWORK_OPERATOR
	 * @see #NETWORK_OPERATOR_NAME
	 * @see #NETWORK_TYPE
	 * @see #PHONE_TYPE
	 * @see #SIM_COUNTRY_ISO
	 * @see #SIM_OPERATOR
	 * @see #SIM_OPERATOR_NAME
	 * @see #SIM_SERIAL_NUMBER
	 * @see #SIM_STATE
	 * @see #SUBSCRIBER_ID
	 * @see #VOICE_MAIL_NUMBER
	 * @see #VOICE_MAIL_TAG
	 */
	public static String getInfoByName(Context context, String name) {
		String result = null;
		TelephonyManager telephonyManager = getTelephonyManager(context);
		if (telephonyManager != null) {
			result = ReflectUtil.invokeMethod(name, null, telephonyManager, null);
		}
		return result;
	}

	/**
	 * 判断手机的移动网络是否为3G以上高速网络
	 *
	 * @return
	 */
	public static boolean isFastMobileNetwork(Context context) {
		boolean result = false;
		TelephonyManager telephonyManager = getTelephonyManager(context);
		if (telephonyManager != null) {
			switch (telephonyManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					result = false;
					break;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					result = false; // ~ 100 kbps, 2G(2.5G)
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					result = false; // ~ 50-100 kbps, 2G(2.75G)
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					result = true; // ~ 400-7000 kbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					result = false; // ~ 14-64 kbps, 2G
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					result = true; // ~ 400-1000 kbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					result = true; // ~ 600-1400 kbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					result = false; // ~ 50-100 kbps, 2G
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					result = true; // ~ 2-14 Mbps, 3G(3.5G)
					break;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					result = true; // ~ 1-23 Mbps, 3G(3.5G)
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					result = true; // ~ 700-1700 kbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_IDEN:
					result = false; // ~25 kbps, 2G
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
					result = true; // ~ 5 Mbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					result = true; // ~ 10+ Mbps, 4G
					break;
				case TelephonyManager.NETWORK_TYPE_EHRPD:
					result = true; // ~ 1-2 Mbps, 3G
					break;
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					result = true; // ~ 10-20 Mbps, 3G
					break;
			}
		}
		return result;
	}

	/** 不具备电话语音服务 */
	public static int PHONE_TYPE_NONE = TelephonyManager.PHONE_TYPE_NONE;
	/** CDMA提供 */
	public static int PHONE_TYPE_CDMA = TelephonyManager.PHONE_TYPE_CDMA;
	/** GSM提供 */
	public static int PHONE_TYPE_GSM = TelephonyManager.PHONE_TYPE_GSM;
	/** SIP提供 */
	public static int PHONE_TYPE_SIP = TelephonyManager.PHONE_TYPE_SIP;

	/**
	 * 获取电话无线电传输的服务商类型
	 *
	 * @see #PHONE_TYPE_NONE
	 * @see #PHONE_TYPE_GSM
	 * @see #PHONE_TYPE_CDMA
	 * @see #PHONE_TYPE_SIP
	 */
	public static int getPhoneType(Context context) {
		return  getTelephonyManager(context).getPhoneType();
	}

	/**
	 * 获取TelephonyManager服务
	 *
	 * @param context
	 * @return
	 */
	private static TelephonyManager getTelephonyManager(Context context) {
		return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}
}
