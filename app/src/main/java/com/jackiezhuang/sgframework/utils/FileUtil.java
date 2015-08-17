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
 * �ļ�����������
 * <p/>
 * Created by zsigui on 15-8-17.
 */
public class FileUtil {

	private static final String PRE_TAG = FileUtil.class.toString();
	// ����3MBΪ��С�ļ��ָ�
	private static final int BIG_FILE_COUNT = 3 * 1024 * 1024;
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().displayName();


	/**
	 * �ر�IO��
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
	 * ��ָ���������ж�ȡ�ֽ�����
	 *
	 * @param in ������ʵ��
	 * @return
	 */
	public static byte[] readBytes(InputStream in) {

		if (in == null) {
			throw new IllegalArgumentException(PRE_TAG + ".readBytes : params in(InputStream) is null");
		}

		byte[] result = null;
		if (in instanceof FileInputStream) {

			// ʹ��NIO��ʽ��ȡ
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

			// ʹ����ͨIO����ȡ
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
	 * �Ӵ��ļ��ж�ȡ���ݴ�ŵ��ֽ�������
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
	 * �Ӵ��ļ��ж�ȡ���ݴ�ŵ��ֽ�������
	 *
	 * @param filePath
	 * @return
	 */
	private static byte[] readBytesFromBigFile(String filePath) {
		return readBytesFromBigFile(new File(filePath));
	}

	/**
	 * ���ļ��ж�ȡ���ݴ�ŵ��ֽ�������
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
	 * ���ļ��ж�ȡ���ݴ�ŵ��ֽ�������
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
				// ִ�д��ļ���д
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
	 * ���ļ��ж�ȡָ�������ʽ���ı��ַ���
	 *
	 * @param filePath
	 * @param charset
	 * @return
	 */
	public static String readString(String filePath, String charset) {
		return readString(new File(filePath), charset);
	}

	/**
	 * ���ļ��ж�ȡָ�������ʽ���ı��ַ���
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
	 * ���ļ��ж�ȡϵͳĬ�ϱ����ʽ���ı��ַ���
	 *
	 * @param filePath
	 * @return
	 */
	public static String readString(String filePath) {
		return readString(filePath, DEFAULT_CHARSET);
	}

	/**
	 * ���ļ��ж�ȡָ�������ʽ���ı��ַ���
	 *
	 * @param file
	 * @return
	 */
	public static String readString(File file) {
		return readString(file, DEFAULT_CHARSET);
	}

	/**
	 * д���ֽ��������ݵ�ָ���������(�÷����ᴴ�����ļ�)
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
			// ͨ����ʽд��
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
			// ��������IOд��
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
	 * д���ֽ��������ݵ�ָ���ļ���(�÷����ᴴ�����ļ�)
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
				// ʹ��Mapped
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
	 * д���׷���ֽ��������͵Ĵ��ı����ݵ��ļ���
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
	 * д���׷���ֽ��������͵Ĵ��ı����ݵ��ļ���
	 *
	 * @param filePath
	 * @param data
	 * @param append
	 */
	public static void writeBytesBigToFile(String filePath, byte[] data, boolean append) {
		writeBytesBigToFile(new File(filePath), data, append);
	}

	/**
	 * д��ָ��������ı��ַ������ļ���(�÷����ᴴ�����ļ�)
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
	 * д��ָ��������ı��ַ������ļ���(�÷����ᴴ�����ļ�)
	 *
	 * @param filePath
	 * @param data
	 * @param charset
	 */
	public static void writeString(String filePath, String data, String charset) {
		writeString(new File(filePath), data, charset);
	}

	/**
	 * д��ϵͳĬ�ϱ�����ı��ַ������ļ���(�÷����ᴴ�����ļ�)
	 *
	 * @param file
	 * @param data
	 */
	public static void writeString(File file, String data) {
		writeString(file, data, DEFAULT_CHARSET);
	}

	/**
	 * д��ϵͳĬ�ϱ�����ı��ַ������ļ���(�÷����ᴴ�����ļ�)
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
	 * ׷���ֽ��������͵����ݵ�ָ���ļ���
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
	 * ׷��ָ�������ʽ���ı��ַ�����ָ���ļ���
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
	 * ׷��ָ�������ʽ���ı��ַ�����ָ���ļ���
	 *
	 * @param filePath
	 * @param data
	 * @param charset
	 */
	public static void appendString(String filePath, String data, String charset) {
		appendString(new File(filePath), data, charset);
	}

	/**
	 * ��ָ������������д�뵽ָ���������
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
			// ʹ��ͨ����ʽ���ٸ���
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
			// ʹ����ͨIO��ʽ����
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
	 * ��ָ�������ļ���Ϣд�뵽ָ������ļ���
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
	 * ��ָ�������ļ���Ϣд�뵽ָ������ļ���
	 *
	 * @param inFilePath
	 * @param outFilePath
	 */
	public static void copy(String inFilePath, String outFilePath) {
		copy(new File(inFilePath), new File(outFilePath));
	}

	/**
	 * ����ָ��·���ļ���
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
	 * ����ָ��·���ĸ�·��
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
	 * �ݹ�ɾ��ָ���ļ���
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
	 * ������������������MD5ժҪ�Ƿ����
	 *
	 * @param data
	 * @param md5Val MD5ժҪ�ַ���
	 * @param isHex  ���ṩ��md5����ʮ�����ƻ���Base64����
	 * @return
	 */
	public static boolean checkValid(byte[] data, String md5Val, boolean isHex) {
		return md5Val.toLowerCase().equals(isHex ? MD5.digestInHex(data) : MD5.digestInBase64(data));
	}


	/**
	 * ������������������MD5ժҪ�Ƿ����
	 *
	 * @param data
	 * @param md5Val Base64�����MD5ժҪ�ַ���
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
	 * ������������������MD5ժҪ�Ƿ����
	 *
	 * @param data
	 * @param md5Val ʮ�����Ƹ�ʽ��MD5ժҪ�ַ���
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
	 * ��ȡָ���ļ�MD5ֵ�ֽ�����
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
	 * ��ȡָ���ļ���ʮ������MD5ֵ�ַ���
	 *
	 * @param filePath
	 * @return
	 */
	public static String getMD5String(String filePath) {
		return CommonUtil.bytes2Hex(getMD5Bytes(filePath));
	}

	/**
	 * �ж������ļ��Ƿ�һ������MD5ֵ�Ƿ���ͬ
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

	/* --------------------- ���²��ֽ�������Android�¿��� --------------------- */

	/**
	 * ���SD���Ƿ����
	 *
	 * @return
	 */
	public static boolean checkSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * ����ָ��SD���ļ��ľ���·��,���жϴ����丸·��
	 *
	 * @param relativeFilePath ����SD��·�������·��
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
	 * ��uriת��Ϊ�ļ�·���ַ���
	 *
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static String uriToString(Activity activity, Uri uri) {
		if (SystemTool.getSDKVersion() < 11) {
			// ��API11���¿���ʹ�ã�managedQuery
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
			// ��API11���ϣ�ҪתΪʹ��CursorLoader,��ʹ��loadInBackground������
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
	 * ��uriת��ΪFile����
	 *
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static File uriToFile(Activity activity, Uri uri) {
		return new File(uriToString(activity, uri));
	}

}