package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.io.FileUtil;

/**
 * 用于定义Http模块的通用配置
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public final class HttpConfig {

	/** Cache存放位置 */
	public static String sCacheDirPath = FileUtil.getExternalFilePath("SGFramework/cache");
	/** 最大Cache大小 */
	public static long sDiskCacheSize = 5 * 1024 * 1024;
	/** Cache限制清除警戒点 */
	public static float sCacheClearFactor = 0.9f;

	/** 为了模拟更真实的网络请求,用于决定是否读取完缓存后立即返回,false为指定延迟时间后返回。此参数为全局参数 */
	public static boolean sDelayCache = false;
	/** 延迟返回的时间, {@link #sDelayTime} 启用时才有效。单位:ms */
	public static long sDelayTime = 200;

	/** 进行网络请求连接超时时间。此参数为全局参数 */
	public static int sConnTimeout = 5 * 1000;
	/** 网络连接建立后拉取数据等待超时时间。此参数为全局参数 */
	public static int sReadTimeout = 30 * 1000;
	/** 是否执行自动跳转 */
	public static boolean sFollowRedirect = true;

}
