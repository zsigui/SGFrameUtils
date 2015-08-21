package com.jackiezhuang.sgframework.utils.common;

import java.util.LinkedList;

/**
 * 字节数组缓冲池
 * <p>
 * Created by zsigui on 15-8-20.
 */
public class ByteArrayPool {

	/** 默认字节缓存大小 */
	public static final int DEFAULT_LIMIT_SIZE = 4096;
	private volatile static ByteArrayPool mInstance = new ByteArrayPool(DEFAULT_LIMIT_SIZE);

	private int mLimitSize;
	private int mCurrentSize;
	/** 用于执行LRU存储策略的缓存字节数组列表 */
	private final LinkedList<byte[]> mLRUQueue = new LinkedList<>();

	private ByteArrayPool(int limitSize) {
		mLimitSize = limitSize;
	}

	/**
	 * 使用指定大小初始化ByteArrayPool实例,需要确保超过默认缓存大小{@link #DEFAULT_LIMIT_SIZE},
	 * 否则直接返回已有实例
	 */
	public static final ByteArrayPool init(int limitSize) {
		if (limitSize > DEFAULT_LIMIT_SIZE) {
			mInstance = new ByteArrayPool(limitSize);
		}
		return mInstance;
	}

	/**
	 * 获取ByteArrayPool实例
	 * <p>
	 * 如果还未初始化,则会首先以默认大小进行初始化操作
	 */
	public static final ByteArrayPool init() {
		return init(0);
	}

	/**
	 * 从缓存池中取得不小于指定长度的字节数组
	 *
	 * @param len 需要取得字节数组最小长度
	 */
	public synchronized byte[] obtain(int len) {
		if (len > mCurrentSize)
			return new byte[len];
		for (int i = 0; i < mLRUQueue.size(); i++) {
			byte[] tmp = mLRUQueue.get(i);
			mLRUQueue.remove(i);
			mCurrentSize -= tmp.length;
			if (tmp.length >= len)
				return tmp;
		}
		return new byte[len];
	}

	/**
	 * 添加字节数组到缓冲池中
	 *
	 * @param bs 待添加字节数组
	 */
	public synchronized void add(byte[] bs) {
		if (bs == null || bs.length > mLimitSize)
			return;

		mLRUQueue.add(bs);
		mCurrentSize += bs.length;
		trim();
	}

	/**
	 * 调整当前的缓冲池以保证大小处于限定范围内
	 */
	private synchronized void trim() {
		while (mCurrentSize > mLimitSize) {
			mCurrentSize -= mLRUQueue.removeFirst().length;
		}
	}


}
