package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;

import java.util.Collection;

/**
 * 定义管理器的几个统一操作接口
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public interface IManager {

	/**
	 * 开启队列调度线程
	 */
	void start();

	/**
	 * 停止队列调度线程
	 */
	void stop();

	/**
	 * 添加指定请求
	 */
	void add(HttpRequest request);

	/**
	 * 添加多个请求
	 */
	void addAll(Collection<HttpRequest> requests);

	/**
	 * 取消指定请求
	 */
	void cancel(HttpRequest request);

	/**
	 * 取消当前所有请求
	 */
	void cancelAll();

	/**
	 * 销毁
	 */
	void destroy();

}
