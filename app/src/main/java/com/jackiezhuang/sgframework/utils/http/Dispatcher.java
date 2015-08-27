package com.jackiezhuang.sgframework.utils.http;

/**
 * 调度器统一定义接口
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public abstract class Dispatcher extends Thread{

	protected boolean mQuit;

	/**
	 * 退出线程,相比 {@link #exit} 比较缓和
	 */
	public void quit() {
		mQuit = true;
		if (isAlive() && !isInterrupted()) {
			interrupt();
		}
	}

	/**
	 * 强制退出线程执行
	 */
	public void exit() {
		mQuit = true;
		interrupt();
	}

}
