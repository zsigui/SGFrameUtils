package com.jackiezhuang.sgframework.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 通用操作工具
 * Created by zsigui on 15-8-17.
 */
public class CommonUtil {

	private static final String PRE_TAG = CommonUtil.class.getName();

	/**
	 * 判断给定对象是否为空
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		boolean result = obj == null;
		if (!result) {
			if (obj instanceof CharSequence) {
				result = ((String) obj).isEmpty();
			} else if (obj instanceof Map) {
				result = ((Map) obj).isEmpty();
			} else if (obj instanceof List) {
				result = ((List) obj).isEmpty();
			} else if (obj instanceof Set) {
				result = ((Set) obj).isEmpty();
			}
		}
		return result;
	}

	/**
	 * 将字节数组转换成十六进制字符串
	 *
	 * @param bs
	 * @return
	 */
	public static String bytes2Hex(byte... bs) {
		if (isEmpty(bs)) {
			throw new IllegalArgumentException(PRE_TAG + ".bytes2Hex : param bs(byte...) is null");
		}
		StringBuilder builder = new StringBuilder();
		for (byte b : bs) {
			int bt = b & 0xff;
			if (bt < 16) {
				builder.append(0);
			}
			builder.append(Integer.toHexString(bt));
		}
		return builder.toString();
	}

	/**
	 * 将十六进制字符串转换成字节数组
	 *
	 * @param hex
	 * @return
	 */
	public static byte[] hex2Bytes(String hex) {
		if (isEmpty(hex)) {
			throw new IllegalArgumentException(PRE_TAG + ".hex2Bytes : param hex(String...) is null");
		}
		if (hex.length() % 2 == 1)
			hex += '0';
		byte[] result = new byte[hex.length() / 2];
		char[] cs = hex.toLowerCase(Locale.CHINA).toCharArray();
		for (int i = 0; i < result.length; i++) {
			int pos = i * 2;
			result[i] = (byte) (char2Byte(cs[pos]) << 4 | char2Byte(cs[pos + 1]));
		}
		return result;
	}

	/**
	 * 将字节数组转为十六进制表示的MAC地址
	 *
	 * @param bs
	 * @return
	 */
	public static String byte2MacStr(byte[] bs) {
		if (isEmpty(bs)) {
			throw new IllegalArgumentException(PRE_TAG + ".byte2MacStr : param bs(byte[]) is null");
		}
		StringBuilder sb = new StringBuilder(bs.length);
		for (byte b : bs) {
			sb.append(":");
			sb.append(bytes2Hex(b));
		}
		sb.deleteCharAt(0);
		return sb.toString();
	}

	/**
	 * 将整形转换为点十六进制表示法的IP地址
	 *
	 * @param info
	 * @return
	 */
	public static String int2IpStr(int info) {
		return (info & 0xFF) + "." + (info >> 8 & 0xFF) + "." + (info >> 16 & 0xFF) + "." + (info >> 24 & 0xFF);
	}

	/**
	 * 将字符转换为字节数
	 *
	 * @param c
	 * @return
	 */
	public static byte char2Byte(char c) {
		return (byte) "0123456789abcdefg".indexOf(c);
	}

	/**
	 * 将字节数组转换为指定格式编码的字符串
	 */
	public static String toString(byte[] data, String charset) {
		String result = null;
		try {
			result = new String(data, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 将字节数组转换为系统默认格式编码的字符串
	 */
	public static String toString(byte[] data) {
		return toString(data, SGConfig.DEFAULT_SYS_CHARSET);
	}
}
