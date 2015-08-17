package com.jackiezhuang.sgframework.utils.chiper;

import android.util.Base64;

import com.jackiezhuang.sgframework.utils.CommonUtil;
import com.jackiezhuang.sgframework.utils.FileUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * MD5摘要算法类
 *
 * Created by zsigui on 15-8-17.
 */
public class MD5 {
	/**
	 * 生成摘要字节数组
	 *
	 * @param content
	 * @return
	 */
	public static byte[] genDigest(String content) {
		return genDigest(content, FileUtil.DEFAULT_CHARSET);
	}

	/**
	 * 生成摘要字节数组
	 *
	 * @param content 原文本内容字符串
	 * @param charset 获取字节数组编码
	 * @return
	 */
	public static byte[] genDigest(String content, String charset) {
		try {
			return genDigest(content.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 生成摘要字节数组
	 *
	 * @param content 原文本内容字节数组
	 * @return
	 */
	public static byte[] genDigest(byte[] content) {
		if (content != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(content);
				return md.digest();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 使用默认编码UTF-8获取输入内容字节数组，并对其进行MD5摘要加密
	 *
	 * @param content
	 * @return 十六进制格式的字符串
	 */
	public static String digestInHex(byte[] content) {
		return CommonUtil.bytes2Hex(genDigest(content));
	}

	/**
	 * 对输入字节数组内容进行MD5摘要加密
	 *
	 * @param content
	 * @return 经Base64编码后的字符串
	 */
	public static String digestInBase64(byte[] content) {
		return Base64.encodeToString(content, Base64.DEFAULT);
	}

	/**
	 * 使用默认编码UTF-8获取输入内容字节数组，并对其进行MD5摘要加密
	 *
	 * @param content
	 * @param charset
	 * @return 十六进制格式的字符串
	 */
	public static String digestInHex(String content, String charset) {
		return CommonUtil.bytes2Hex(genDigest(content, charset));
	}

	/**
	 * 对输入字节数组内容进行MD5摘要加密
	 *
	 * @param content
	 * @param charset
	 * @return 经Base64编码后的字符串
	 */
	public static String digestInBase64(String content, String charset) {
		try {
			return Base64.encodeToString(content.getBytes(charset), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}