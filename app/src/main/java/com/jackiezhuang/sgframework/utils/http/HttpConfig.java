package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.impl.HttpConnectionWorker;
import com.jackiezhuang.sgframework.utils.http.impl.ResultDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;
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

	/** 设定HttpManager网络请求线程的数量 */
	public static int sHttpThreadCount = 4;
	/** 设定CacheManager缓存操作线程的数量 */
	public static int sCacheThreadCount = 2;

	/** 执行网络请求结果处理的分发处理器 */
	public static IDelivery sDelivery = new ResultDelivery();

	/** 实际执行网络请求的工作器，可以自定义传入https执行协议 */
	public static IHttpWorker sWorker = new HttpConnectionWorker();
	/** 是否使用缓存控制的方法,用于决定HttpManager是否控制CacheManager的执行。当为false时,忽略HttpRequest的缓存控制策略 */
	public static boolean sNeedCacheControl = true;
	/** 是否使用缓存头信息，用于决定在执行HTTP请求时 */
	public static boolean sUseCacheHeader = true;
	/** 设置是否使用服务器的缓存控制设置。受限于{@link #sNeedCacheControl} */
	public static boolean sUseSeverControl = true;
	/** 自定义缓存控制的缓存有效时间，单位：分钟。受限于{@link #sUseSeverControl} */
	public static long sCacheTime = 10;

	private HttpConfig(){};

}
