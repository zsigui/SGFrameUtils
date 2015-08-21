package com.jackiezhuang.sgframework.utils.io;

import com.jackiezhuang.sgframework.utils.common.ByteArrayPool;
import com.jackiezhuang.sgframework.utils.common.PoolByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 普通IO读取写入操作类
 *
 * Created by zsigui on 15-8-18.
 */
public class IOUtil {

	/**
	 * 从输入流中读取字节数组,该操作完成后会关闭流
	 */
	public static byte[] readBytes(InputStream in) {
		byte[] result = null;
		BufferedInputStream bin = (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new
				BufferedInputStream(in);
		byte[] tempBuf = null;
		try {
			tempBuf = ByteArrayPool.init().obtain(1024);
			int length;
			PoolByteArrayOutputStream byteOutStream = new PoolByteArrayOutputStream(bin.available());
			while ((length = bin.read(tempBuf)) != -1) {
				byteOutStream.write(tempBuf, 0, length);
			}
			byteOutStream.flush();
			result = byteOutStream.toByteArray();
			byteOutStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ByteArrayPool.init().add(tempBuf);
			closeIO(bin, in);
		}
		return result;
	}

	/**
	 * 从输入流中读取数据并返回指定编码的字符串,该操作完成后会关闭流
	 */
	public static String readBytes(InputStream in, String charset) {
		String result = null;
		try {
			result = new String(readBytes(in), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 写入字节数组数据到输出流中,该操作完成后会关闭流
	 */
	public static void writeBytes(OutputStream out, byte[] data) {
		BufferedOutputStream bout = (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new
				BufferedOutputStream(out);
		try {
			bout.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(bout, out);
		}
	}

	/**
	 * 写入指定编码格式,该操作完成后会关闭流
	 */
	public static void writeBytes(OutputStream out, String data, String charset) {
		try {
			writeBytes(out, data.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从输入流读取数据到输出流中,该操作完成后会关闭流
	 *
	 * @param in
	 * @param out
	 */
	public static void copy(InputStream in, OutputStream out) {
		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;
		byte[] bs = null;
		try {
			bin = (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
			bout = (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream
					(out);
			bs = ByteArrayPool.init().obtain(1024);
			int length;
			while ((length = bin.read(bs, 0, bs.length)) != -1) {
				bout.write(bs, 0, length);
			}
			bout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ByteArrayPool.init().add(bs);
			closeIO(bin, bout, in, out);
		}
	}

	/**
	 * 关闭IO流
	 *
	 * @param closeables
	 */
	public static void closeIO(Closeable... closeables) {
		if (closeables != null || closeables.length <= 0) {
			return;
		}
		for (Closeable c : closeables) {
			if (c == null) {
				continue;
			}
			try {
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
