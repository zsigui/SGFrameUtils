package com.jackiezhuang.sgframework.utils;

import java.util.Locale;

/**
 * 通用操作工具
 * Created by zsigui on 15-8-17.
 */
public class CommonUtil {

	/**
	 * 判断给定对象是否为空
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if (obj instanceof CharSequence) {
			return obj == null || ((String) obj).isEmpty();
		} else {
			return obj == null;
		}
	}

	/**
	 * 将字节数组转换成十六进制字符串
	 *
	 * @param bs
	 * @return
	 */
	public static String bytes2Hex(byte[] bs) {
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
	 * 将字符转换为字节数
	 *
	 * @param c
	 * @return
	 */
	private static byte char2Byte(char c) {
		return (byte) "0123456789abcdefg".indexOf(c);
	}
}
