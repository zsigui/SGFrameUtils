package com.jackiezhuang.sgframework.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络检查工具类
 * <p>用于获取网络状态等相关信息<p/>
 * <p/>
 * Created by JackieZhuang on 2015/8/18.
 */
public class NetUtil {

	/**
	 * 连接正常，成功标志
	 */
	public static final int CONN_SUCCESS = 0;

	/**
	 * 连接失败，获取服务出错
	 */
	public static final int CONN_ERR_SERVICE = -1;
	/**
	 * 连接失败，无可用连接或获取失败
	 */
	public static final int CONN_ERR_UNAVALIABLE = -2;

	/**
	 * 网络类型，3G及3G+
	 */
	public static final int TYPE_NET_3G = 101;
	/**
	 * 网络类型，WIFI
	 */
	public static final int TYPE_NET_WIFI = 102;
	/**
	 * 网络类型，2G
	 */
	public static final int TYPE_NET_2G = 103;
	/**
	 * 网络类型，wap
	 */
	public static final int TYPE_NET_WAP = 104;
	/**
	 * 网络类型，未知or获取失败
	 */
	public static final int TYPE_UNKNOWN = 106;

	/**
	 * 内网IP地址
	 */
	public static final String IP_ADDRESS = "ipAddress";
	/**
	 * 网络掩码
	 */
	public static final String NET_MASK = "netmask";
	/**
	 * 网关地址
	 */
	public static final String GATE_WAY = "gateway";
	/**
	 * 服务器地址
	 */
	public static final String SERVER_ADDRESS = "serverAddress";
	/**
	 * 首选DNS域名地址
	 */
	public static final String DNS1 = "dns1";
	/**
	 * 备用DNS域名地址
	 */
	public static final String DNS2 = "dns2";


	/**
	 * 获取当前网络状态
	 *
	 * @param context 传入的Context
	 * @see #CONN_SUCCESS
	 * @see #CONN_ERR_SERVICE
	 * @see #CONN_ERR_UNAVALIABLE
	 */
	public static int getNetState(Context context) {
		int type = NetUtil.CONN_SUCCESS;
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conManager != null) {
			NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
			if (networkInfo != null) {
				if (networkInfo.isAvailable() && networkInfo.isConnected()) {
				} else {
					type = NetUtil.CONN_ERR_UNAVALIABLE;
				}
			} else {
				type = NetUtil.CONN_ERR_UNAVALIABLE;
			}
		} else {
			type = NetUtil.CONN_ERR_SERVICE;
		}
		return type;
	}

	/**
	 * 获取当前网络类型
	 * 2G, 3G, WIFI, WAP
	 *
	 * @see #TYPE_NET_2G
	 * @see #TYPE_NET_3G
	 * @see #TYPE_NET_WIFI
	 * @see #TYPE_NET_WAP
	 */
	public static int getNetType(Context context) {
		int type = NetUtil.TYPE_UNKNOWN;

		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context
				.CONNECTIVITY_SERVICE);
		NetworkInfo actvNetInfo = connManager.getActiveNetworkInfo();
		if (actvNetInfo != null) {
			if (actvNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// WIFI
				type = NetUtil.TYPE_NET_WIFI;
			} else if (actvNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// 如果为WAP模式，需要添加WAP网关 10.0.0.172
				@SuppressWarnings("deprecation")
				String proxyHost = android.net.Proxy.getDefaultHost();
				if (TextUtils.isEmpty(proxyHost)) {
					if (TelUtil.isFastMobileNetwork(context)) {
						// 3G以上为高速网络
						type = NetUtil.TYPE_NET_3G;
					} else {
						type = NetUtil.TYPE_NET_2G;
					}
				} else {
					// WAP不需要设置代理
					type = NetUtil.TYPE_NET_WAP;
				}
			}
		}
		return type;
	}

	/**
	 * 获取本机IP地址
	 */
	public static String getLocalHostIp() {
		String result = null;
		try {
			for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();
					) {
				NetworkInterface netInf = nis.nextElement();
				for (Enumeration<InetAddress> ipAddr = netInf.getInetAddresses(); ipAddr.hasMoreElements(); ) {
					InetAddress inetAddress = ipAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress
							())) {
						result = inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取本机MAC地址
	 */
	public static String getLocalHostMac() {
		String result = null;
		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalHostIp()));
			result = CommonUtil.byte2MacStr(ni.getHardwareAddress());
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取WIFI连接下的Mac地址
	 *
	 * @param context
	 * @return
	 */
	public static String getMacInWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			return wifiInfo.getMacAddress();
		}
		return null;
	}


	/**
	 * 获取WIFI连接下的IP地址
	 */
	public static String getIpInWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			return CommonUtil.int2IpStr(wifiInfo.getIpAddress());
		}
		return null;
	}

	/**
	 * 通过属性名获取WIFI连接下自动分配的DHCP信息属性
	 *
	 * @param context 上下文
	 * @param name    DhcpInfo属性名
	 * @see #DNS1
	 * @see #DNS2
	 * @see #IP_ADDRESS
	 * @see #GATE_WAY
	 * @see #NET_MASK
	 * @see #SERVER_ADDRESS
	 */
	public static String getDhcpInfoByName(Context context, String name) {
		String result = null;
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			result = ReflectUtil.getField(name, wifiManager.getDhcpInfo());
		}
		return result;
	}

	public static boolean tryConnectUrl() {
		boolean result = false;
		return result;
	}

}
