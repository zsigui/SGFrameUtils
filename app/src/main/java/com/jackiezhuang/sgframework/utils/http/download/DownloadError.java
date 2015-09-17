package com.jackiezhuang.sgframework.utils.http.download;

/**
 * 下载失败状态码及相应错误信息
 * <p></p>
 * Created by zsigui on 15-9-17.
 */
public class DownloadError {

	/**
	 * 成功
	 */
	public static final int SUCCESS = 0;
	/**
	 * 建立连接失败
	 */
	public static final int ERR_CONNECT = 101;
	/**
	 * 获取下载文件大小失败
	 */
	public static final int ERR_GET_SIZE = 102;
	/**
	 * 执行任务状态异常
	 */
	public static final int ERR_STATE = 103;
	/**
	 * 暂停下载
	 */
	public static final int FAIL_PAUSE = 201;
	/**
	 * 取消下载
	 */
	public static final int FAIL_DISCARD = 202;
	/**
	 * 移除下载信息
	 */
	public static final int FAIL_REMOVED = 203;

}
