package com.jackiezhuang.sgframework.utils;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.jackiezhuang.sgframework.utils.chiper.MD5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * 文件操作工具类
 * <p/>
 * Created by zsigui on 15-8-17.
 */
public class FileUtil {

	private static final String PRE_TAG = FileUtil.class.toString();
	// 定义3MB为大小文件分隔
	private static final int BIG_FILE_COUNT = 3 * 1024 * 1024;
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().displayName();


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

	/**
	 * 从指定输入流中读取字节数组
	 *
	 * @param in 输入流实例
	 * @return
	 */
	public static byte[] readBytes(InputStream in) {

		if (in == null) {
			throw new IllegalArgumentException(PRE_TAG + ".readBytes : params in(InputStream) is null");
		}

		byte[] result = null;
		if (in instanceof FileInputStream) {

			// 使用NIO方式读取
			FileChannel fc = ((FileInputStream) in).getChannel();
			try {
				ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
				while (fc.read(buffer) != -1) {
					// do nothing here
				}

				result = buffer.array();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(fc);
			}
		} else {

			// 使用普通IO流读取
			BufferedInputStream bis = (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new
					BufferedInputStream(in);
			try {
				byte[] tempBuf = new byte[1024];
				int length;
				ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(bis.available());
				while ((length = bis.read(tempBuf)) != -1) {
					byteOutStream.write(tempBuf, 0, length);
				}
				byteOutStream.flush();
				result = byteOutStream.toByteArray();
				byteOutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(bis);
			}
		}

		return result;
	}

	/**
	 * 从大文件中读取数据存放到字节数组中
	 *
	 * @param file
	 * @return
	 */
	private static byte[] readBytesFromBigFile(File file) {
		byte[] result = null;
		FileChannel inChannel = null;
		try {
			inChannel = new RandomAccessFile(file, "r").getChannel();
			MappedByteBuffer mapBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
			result = mapBuffer.array();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(inChannel);
		}
		return result;
	}

	/**
	 * 从大文件中读取数据存放到字节数组中
	 *
	 * @param filePath
	 * @return
	 */
	private static byte[] readBytesFromBigFile(String filePath) {
		return readBytesFromBigFile(new File(filePath));
	}

	/**
	 * 从文件中读取数据存放到字节数组中
	 *
	 * @param filePath
	 * @return
	 */
	public static byte[] readBytes(String filePath) {
		if (filePath == null) {
			throw new IllegalArgumentException(PRE_TAG + ".readBytes : params filePath(String) is null");
		}
		return readBytes(new File(filePath));
	}

	/**
	 * 从文件中读取数据存放到字节数组中
	 *
	 * @param file
	 * @return
	 */
	public static byte[] readBytes(File file) {
		byte[] result = null;
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			int size = fin.available();
			if (size > BIG_FILE_COUNT && size < Integer.MAX_VALUE) {
				// 执行大文件读写
				result = readBytesFromBigFile(file);
			} else {
				result = readBytes(fin);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(fin);
		}
		return result;
	}

	/**
	 * 从文件中读取指定编码格式的文本字符串
	 *
	 * @param filePath
	 * @param charset
	 * @return
	 */
	public static String readString(String filePath, String charset) {
		return readString(new File(filePath), charset);
	}

	/**
	 * 从文件中读取指定编码格式的文本字符串
	 *
	 * @param file
	 * @param charset
	 * @return
	 */
	public static String readString(File file, String charset) {
		String result = null;
		try {
			result = new String(readBytes(file), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 从文件中读取系统默认编码格式的文本字符串
	 *
	 * @param filePath
	 * @return
	 */
	public static String readString(String filePath) {
		return readString(filePath, DEFAULT_CHARSET);
	}

	/**
	 * 从文件中读取指定编码格式的文本字符串
	 *
	 * @param file
	 * @return
	 */
	public static String readString(File file) {
		return readString(file, DEFAULT_CHARSET);
	}

	/**
	 * 写入字节数组数据到指定输出流中(该方法会创建新文件)
	 *
	 * @param out
	 * @param data
	 */
	public static void writeBytes(OutputStream out, byte[] data) {
		if (out == null) {
			throw new IllegalArgumentException(PRE_TAG + ".writeBytes : params out(OutputStream) is null");
		}
		if (data == null) {
			throw new IllegalArgumentException(PRE_TAG + ".writeBytes : params data(byte[]) is null");
		}

		if (out instanceof FileOutputStream) {
			// 通道方式写入
			FileChannel outChannel = null;
			try {
				outChannel = ((FileOutputStream) out).getChannel();
				outChannel.write(ByteBuffer.wrap(data));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(outChannel);
			}
		} else {
			// 正常缓存IO写入
			BufferedOutputStream bout = (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new
					BufferedOutputStream(out);
			try {
				bout.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(bout);
			}
		}
	}

	/**
	 * 写入字节数组数据到指定文件中(该方法会创建新文件)
	 *
	 * @param file
	 * @param data
	 */
	public static void writeBytes(File file, byte[] data) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			int size = data.length;
			if (size > BIG_FILE_COUNT && size < Integer.MAX_VALUE) {
				// 使用Mapped
				writeBytesBigToFile(file, data, false);
			} else {
				writeBytes(fout, data);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			closeIO(fout);
		}
	}

	/**
	 * 写入和追加字节数组类型的大文本数据到文件中
	 *
	 * @param file
	 * @param data
	 * @param append
	 */
	public static void writeBytesBigToFile(File file, byte[] data, boolean append) {
		FileChannel outChannel = null;
		try {
			outChannel = new RandomAccessFile(file, "rw").getChannel();
			long size = data.length + (append ? outChannel.size() : 0);
			MappedByteBuffer mapBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
			int position = append ? 0 : (int) outChannel.size();
			mapBuffer.position(position);
			mapBuffer.put(data);
			mapBuffer.force();
			mapBuffer.flip();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(outChannel);
		}
	}

	/**
	 * 写入和追加字节数组类型的大文本数据到文件中
	 *
	 * @param filePath
	 * @param data
	 * @param append
	 */
	public static void writeBytesBigToFile(String filePath, byte[] data, boolean append) {
		writeBytesBigToFile(new File(filePath), data, append);
	}

	/**
	 * 写入指定编码的文本字符串到文件中(该方法会创建新文件)
	 *
	 * @param file
	 * @param data
	 * @param charset
	 */
	public static void writeString(File file, String data, String charset) {
		try {
			writeBytes(file, data.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 写入指定编码的文本字符串到文件中(该方法会创建新文件)
	 *
	 * @param filePath
	 * @param data
	 * @param charset
	 */
	public static void writeString(String filePath, String data, String charset) {
		writeString(new File(filePath), data, charset);
	}

	/**
	 * 写入系统默认编码的文本字符串到文件中(该方法会创建新文件)
	 *
	 * @param file
	 * @param data
	 */
	public static void writeString(File file, String data) {
		writeString(file, data, DEFAULT_CHARSET);
	}

	/**
	 * 写入系统默认编码的文本字符串到文件中(该方法会创建新文件)
	 *
	 * @param filePath
	 * @param data
	 */
	public static void writeString(String filePath, String data) {
		writeString(filePath, data, DEFAULT_CHARSET);
	}

	public static void appendBytes(File file, byte[] data) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file, true);
			FileChannel outChannel = fout.getChannel();
			int size = (int) (outChannel.size() + data.length);
			closeIO(outChannel);
			if (size > BIG_FILE_COUNT && size < Integer.MAX_VALUE) {
				writeBytesBigToFile(file, data, true);
			} else {
				writeBytes(fout, data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(fout);
		}
	}

	/**
	 * 追加字节数组类型的数据到指定文件中
	 *
	 * @param filePath
	 * @param data
	 */
	public static void appendBytes(String filePath, byte[] data) {
		if (filePath == null) {
			throw new IllegalArgumentException(PRE_TAG + ".appendBytes : params filePath(String) is null");
		}
		appendBytes(new File(filePath), data);
	}

	/**
	 * 追加指定编码格式的文本字符串到指定文件中
	 *
	 * @param file
	 * @param data
	 * @param charset
	 */
	public static void appendString(File file, String data, String charset) {
		try {
			appendBytes(file, data.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 追加指定编码格式的文本字符串到指定文件中
	 *
	 * @param filePath
	 * @param data
	 * @param charset
	 */
	public static void appendString(String filePath, String data, String charset) {
		appendString(new File(filePath), data, charset);
	}

	/**
	 * 将指定输入流数据写入到指定输出流中
	 *
	 * @param in
	 * @param out
	 */
	public static void copy(InputStream in, OutputStream out) {
		if (in == null) {
			throw new IllegalArgumentException(PRE_TAG + ".copy : params in(OutputStream) is null");
		}
		if (out == null) {
			throw new IllegalArgumentException(PRE_TAG + ".copy : params out(OutputStream) is null");
		}

		if (in instanceof FileInputStream && out instanceof FileOutputStream) {
			// 使用通道方式快速复制
			FileChannel inChannel = null;
			FileChannel outChannel = null;
			try {
				inChannel = ((FileInputStream) in).getChannel();
				outChannel = ((FileOutputStream) out).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(inChannel, outChannel);
			}
		} else {
			// 使用普通IO方式复制
			BufferedInputStream bin = null;
			BufferedOutputStream bout = null;
			try {
				bin = (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
				bout = (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream
						(out);
				byte[] bs = new byte[1024];
				int length;
				while ((length = bin.read(bs, 0, bs.length)) != -1) {
					bout.write(bs, 0, length);
				}
				bout.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				closeIO(bin, bout);
			}
		}
	}

	/**
	 * 将指定输入文件信息写入到指定输出文件中
	 *
	 * @param inFile
	 * @param outFile
	 */
	public static void copy(File inFile, File outFile) {
		if (inFile == null) {
			throw new IllegalArgumentException(PRE_TAG + ".copy : params inFile(File) is null");
		}
		if (outFile == null) {
			throw new IllegalArgumentException(PRE_TAG + ".copy : params outFile(File) is null");
		}

		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			fin = new FileInputStream(inFile);
			if (fin.available() > BIG_FILE_COUNT) {
				writeBytes(outFile, readBytes(inFile));
			} else {
				fout = new FileOutputStream(outFile);
				copy(fin, fout);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeIO(fin, fout);
		}
	}

	/**
	 * 将指定输入文件信息写入到指定输出文件中
	 *
	 * @param inFilePath
	 * @param outFilePath
	 */
	public static void copy(String inFilePath, String outFilePath) {
		copy(new File(inFilePath), new File(outFilePath));
	}

	/**
	 * 创建指定路径文件夹
	 *
	 * @param dirPath
	 */
	public static void mkdirs(String dirPath) {
		if (CommonUtil.isEmpty(dirPath)) {
			return;
		}
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) {
			mkdirs(dirPath);
		}
	}

	/**
	 * 创建指定路径的父路径
	 *
	 * @param childPath
	 */
	public static void mkParentDirs(String childPath) {
		if (CommonUtil.isEmpty(childPath)) {
			return;
		}
		File file = new File(childPath);
		if (file.getParentFile() != null) {
			mkdirs(file.getParent());
		}
	}

	/**
	 * 递归删除指定文件夹
	 *
	 * @param dirPath
	 */
	public static void deleteDir(String dirPath) {
		if (CommonUtil.isEmpty(dirPath)) {
			return;
		}
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			for (String filePath : dir.list()) {
				deleteDir(dirPath);
			}
		} else {
			dir.delete();
		}
	}


	/**
	 * 检验输入内容与所给MD5摘要是否想等
	 *
	 * @param data
	 * @param md5Val MD5摘要字符串
	 * @param isHex  所提供的md5串是十六进制还是Base64编码
	 * @return
	 */
	public static boolean checkValid(byte[] data, String md5Val, boolean isHex) {
		return md5Val.toLowerCase().equals(isHex ? MD5.digestInHex(data) : MD5.digestInBase64(data));
	}


	/**
	 * 检验输入内容与所给MD5摘要是否想等
	 *
	 * @param data
	 * @param md5Val Base64编码的MD5摘要字符串
	 * @return
	 */
	public static boolean checkValidBase64(String data, String md5Val) {
		boolean result = false;
		try {
			result = checkValid(data.getBytes(DEFAULT_CHARSET), md5Val, false);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 检验输入内容与所给MD5摘要是否想等
	 *
	 * @param data
	 * @param md5Val 十六进制格式的MD5摘要字符串
	 * @return
	 */
	public static boolean checkValidHex(String data, String md5Val) {
		boolean result = false;
		try {
			result = checkValid(data.getBytes(DEFAULT_CHARSET), md5Val, true);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 获取指定文件MD5值字节数组
	 *
	 * @param filePath
	 * @return
	 */
	public static byte[] getMD5Bytes(String filePath) {
		if (CommonUtil.isEmpty(filePath)){
			throw new IllegalArgumentException(PRE_TAG + ".getMD5Val : params filePath(String) is null");
		}
		return MD5.genDigest(readBytes(filePath));
	}

	/**
	 * 获取指定文件的十六进制MD5值字符串
	 *
	 * @param filePath
	 * @return
	 */
	public static String getMD5String(String filePath) {
		return CommonUtil.bytes2Hex(getMD5Bytes(filePath));
	}

	/**
	 * 判断两个文件是否一样，即MD5值是否相同
	 *
	 * @param fileAPath
	 * @param fileBPath
	 * @return
	 */
	public static boolean isSame(String fileAPath, String fileBPath) {
		byte[] aBytes = getMD5Bytes(fileAPath);
		byte[] bBytes = getMD5Bytes(fileBPath);
		if (CommonUtil.isEmpty(aBytes) || CommonUtil.isEmpty(bBytes) || aBytes.length != bBytes.length) {
			return false;
		}
		boolean result = true;
		for (int i = 0; i<aBytes.length; i++) {
			if (aBytes[i] != bBytes[i]) {
				result = false;
				break;
			}
		}
		return result;
	}

	/* --------------------- 以下部分仅适用于Android下开发 --------------------- */

	/**
	 * 检查SD卡是否存在
	 *
	 * @return
	 */
	public static boolean checkSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * 返回指定SD下文件的绝对路径,并判断创建其父路径
	 *
	 * @param relativeFilePath 基于SD卡路径的相对路径
	 * @return
	 */
	public static String getExternalFilePath(String relativeFilePath) {
		if (!relativeFilePath.startsWith(File.separator)) {
			relativeFilePath = File.separatorChar + relativeFilePath;
		}
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + relativeFilePath;
		mkParentDirs(path);
		return path;
	}

	/**
	 * 将uri转换为文件路径字符串
	 *
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static String uriToString(Activity activity, Uri uri) {
		if (SystemTool.getSDKVersion() < 11) {
			// 在API11以下可以使用：managedQuery
			String[] proj = {MediaStore.Images.Media.DATA};
			@SuppressWarnings("deprecation")
			Cursor actualimagecursor = activity.managedQuery(uri, proj, null, null,
					null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			return img_path;
		} else {
			// 在API11以上：要转为使用CursorLoader,并使用loadInBackground来返回
			String[] projection = {MediaStore.Images.Media.DATA};
			CursorLoader loader = new CursorLoader(activity, uri, projection, null,
					null, null);
			Cursor cursor = loader.loadInBackground();
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
	}

	/**
	 * 将uri转换为File对象
	 *
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static File uriToFile(Activity activity, Uri uri) {
		return new File(uriToString(activity, uri));
	}

}