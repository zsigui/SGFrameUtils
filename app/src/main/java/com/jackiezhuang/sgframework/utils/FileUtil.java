package com.jackiezhuang.sgframework.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zsigui on 15-8-11.
 */
public class FileUtil {

	// 定义分隔符
	public static final char SEP = File.separatorChar;
	public static final String DEFAULT_CHARSET = "UTF-8";


	/**
	 * 读取文件内容
	 *
	 * @param filePath 文件位置
	 * @param charset  返回字符串编码
	 * @return
	 */
	public static String readContent(String filePath, String charset) {
		byte[] bs = readContentBytesInStandard(filePath);
		if (bs != null)
			try {
				return new String(bs, charset);
			} catch (UnsupportedEncodingException e) {
				L.e("readContent", e.getMessage());
			}
		return null;
	}

	/**
	 * 读取文件内容
	 *
	 * @param filePath 文件位置
	 * @return 经默认编码的字符串
	 */
	public static String readContent(String filePath) {
		return readContent(filePath, DEFAULT_CHARSET);
	}

	/**
	 * 使用常用方式读取文件
	 *
	 * @param filePath
	 * @return
	 */
	public static byte[] readContentBytesInStandard(String filePath) {
		File file = new File(filePath);
		if (file.exists() && file.isFile() && file.canRead()) {
			ByteArrayOutputStream result = null;
			try {
				result = new ByteArrayOutputStream();
				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream(filePath));
				byte[] bs = new byte[1024];
				int len = -1;
				while ((len = bis.read(bs)) != -1) {
					result.write(bs, 0, len);
				}
				bis.close();
				bis = null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				L.e(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				L.e(e.getMessage());
			}
			return result.toByteArray();
		} else {
			L.e("readContentBytesInStandard", "文件\"" + filePath + "\"不存在或不可读");
		}
		return null;
	}

	/**
	 * 使用MappedByteBuffer读取文件，适于大文件
	 *
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("resource")
	public static byte[] readContentBytesInMap(String filePath) {
		File file = new File(filePath);
		if (file.exists() && file.isFile() && file.canRead()) {
			FileChannel fc = null;
			byte[] result = null;
			try {
				fc = new RandomAccessFile(file, "r").getChannel();
				MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
						fc.size());
				result = new byte[(int) fc.size()];
				if (byteBuffer.remaining() > 0) {
					byteBuffer.get(result, 0, byteBuffer.remaining());
				}
			} catch (FileNotFoundException e) {
				L.e("readContentBytesInMap",
						e.getMessage());
			} catch (IOException e) {
				L.e("readContentBytesInMap",
						e.getMessage());
			} finally {
				if (fc != null) {
					try {
						fc.close();
					} catch (IOException e) {
						L.e("readContentBytesInMap",
								e.getMessage());
					}
				}
			}
			return result;
		} else {
			L.e("readContentBytesInMap",
					"文件\"" + filePath + "\"不存在或不可读");
		}
		return null;
	}

	/**
	 * 使用NIO方式读取文件
	 *
	 * @param filePath
	 * @return
	 */
	public static byte[] readContentBytesInNIO(String filePath) {
		File file = new File(filePath);
		if (file.exists() && file.isFile() && file.canRead()) {
			FileChannel fc = null;
			ByteBuffer result = null;
			try {
				FileInputStream fis = new FileInputStream(file);
				fc = fis.getChannel();
				result = ByteBuffer.allocate((int) fc.size());
				while (fc.read(result) > 0)
					;
				fis.close();
				fis = null;
			} catch (FileNotFoundException e) {
				L.e("readContentBytesInNIO",
						e.getMessage());
			} catch (IOException e) {
				L.e("readContentBytesInNIO",
						e.getMessage());
			} finally {
				if (fc != null) {
					try {
						fc.close();
					} catch (IOException e) {
						L.e("readContentBytesInNIO",
								e.getMessage());
					}
				}
			}
			return result.array();
		} else {
			L.e("readContentBytesInNIO",
					"文件\"" + filePath + "\"不存在或不可读");
		}
		return null;
	}

	/**
	 * 创建路径上的文件夹
	 *
	 * @param dirPath
	 */
	public static void createPath(String dirPath) {
		File dir = new File(dirPath);
		if (dir.exists() && dir.isDirectory())
			return;
		dir.mkdirs();
	}

	/**
	 * 创建新文件
	 *
	 * @param dirPath
	 * @param fileName
	 */
	public static void createFile(String dirPath, String fileName) {
		try {
			File dir = new File(dirPath);
			File file = new File(dirPath + SEP + fileName);
			if (!dir.exists() || !dir.isDirectory())
				dir.mkdirs();
			if (!file.exists() || !file.isFile())
				file.createNewFile();
		} catch (IOException e) {
			L.e("createFile", e.getMessage());
		}
	}

	/**
	 * 将内容以指定编码写入文件
	 *
	 * @param msg
	 *            文本内容字符串
	 * @param filePath
	 *            文件位置
	 * @param charset
	 *            写入字符编码
	 * @param append
	 *            是否续写
	 */
	public static void write2File(String msg, String filePath, String charset,
	                              boolean append) {
		try {
			write2File(msg.getBytes(charset), filePath, append);
		} catch (UnsupportedEncodingException e) {
			L.e("write2File", e.getMessage());
		}
	}

	/**
	 * 将内容写入文件
	 *
	 * @param msg
	 *            文本内容自己数组
	 * @param filePath
	 *            文件位置
	 * @param append
	 *            是否续写
	 */
	public static void write2File(byte[] msg, String filePath, boolean append) {
		try {
			BufferedOutputStream oStream = new BufferedOutputStream(
					new FileOutputStream(filePath, append));
			oStream.write(msg);
			oStream.flush();
			oStream.close();
			oStream = null;
		} catch (FileNotFoundException e) {
			L.e("write2File", e.getMessage());
		} catch (IOException e) {
			L.e("write2File", e.getMessage());
		}
	}

}
