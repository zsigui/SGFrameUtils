package com.jackiezhuang.sgframework.utils.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 使用ByteArrayPool进行字节数组的缓存管理
 * <p>
 * Created by zsigui on 15-8-20.
 */
public class PoolByteArrayOutputStream extends ByteArrayOutputStream {

	private static final int DEFAULT_SIZE = 256;

	/**
	 * 获取默认ByteArrayPool单例,该缓存大小由实际实例化的对象决定
	 */
	private ByteArrayPool mPool = ByteArrayPool.init();

	public PoolByteArrayOutputStream() {
		this(DEFAULT_SIZE);
	}

	public PoolByteArrayOutputStream(int size) {
		buf = mPool.obtain(Math.max(size, DEFAULT_SIZE));
	}

	@Override
	public void close() throws IOException {
		mPool.add(buf);
		buf = null;
		super.close();
	}

	@Override
	protected void finalize() throws Throwable {
		mPool.add(buf);
		super.finalize();
	}

	/**
	 * 扩展字节数组以保证有足够的存储空间
	 */
	private void expand(int i) {
	    /* Can the buffer handle @i more bytes, if not expand it */
		if (count + i <= buf.length) {
			return;
		}
		byte[] tmp = mPool.obtain((count + i) * 2);
		System.arraycopy(buf, 0, tmp, 0, count);
		mPool.add(buf);
		buf = tmp;
	}

	@Override
	public synchronized void write(byte[] buffer, int offset, int len) {
		expand(len);
		super.write(buffer, offset, len);
	}

	@Override
	public synchronized void write(int oneByte) {
		expand(1);
		super.write(oneByte);
	}
}
