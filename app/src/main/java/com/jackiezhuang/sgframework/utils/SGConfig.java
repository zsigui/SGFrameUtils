package com.jackiezhuang.sgframework.utils;

import java.nio.charset.Charset;

/**
 * 通用默认常量类
 *
 * Created by zsigui on 15-8-18.
 */
public final class SGConfig {

	/**
	 * 系统默认编码格式,从系统获取
	 */
	public static final String DEFAULT_SYS_CHARSET = Charset.defaultCharset().displayName();
	/**
	 * 通用编码格式UTF-8
	 */
	public static final String DEFAULT_UTF_CHARSET = "UTF-8";

}
