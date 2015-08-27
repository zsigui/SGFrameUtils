package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.chiper.MD5;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheEntry;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache缓存的文件操作控制器,执行读取/写入/刷新缓存等操作
 * <p/>
 * <p></p>
 * Created by zsigui on 15-8-24.
 */
public class CacheDiskController {

	public static final String TAG = CacheDiskController.class.getName();
	private static final int MAGIC = 0x57df724;
	private static final String FILE_HEADER = "_header";
	private static final String FILE_DATA = "_data";


	/**
	 * 存放缓存数据
	 */
	private Map<String, CacheHeader> mCacheMap = new HashMap<>(16, 0.75F);
	/**
	 * 指定磁盘缓存大小
	 */
	private long mTotalCacheSize;
	/**
	 * 清理缓存限制水平因子
	 */
	private float mClearFactor;
	private long mCurrentCacheSize;
	private boolean mIsInit = false;
	private String mCacheDirPath;

	public CacheDiskController() {
		this(HttpConfig.sCacheDirPath);
	}

	public CacheDiskController(String cacheDirPath) {
		this(cacheDirPath, HttpConfig.sDiskCacheSize, HttpConfig.sCacheClearFactor);
	}

	public CacheDiskController(String cacheDirPath, long totalCacheSize, float clearFactor) {
		mCacheDirPath = cacheDirPath;
		mTotalCacheSize = totalCacheSize;
		mClearFactor = clearFactor;
		mCurrentCacheSize = 0;
	}

	/**
	 * 设置缓存文件存放地址,该方法调用只有在未初始化成功前有效
	 */
	public void setCacheDirPath(String cacheDirPath) {
		if (mIsInit) {
			return;
		}
		mCacheDirPath = cacheDirPath;
	}

	/**
	 * 初始化缓存存储的外部文件夹,除非执行失败,否则无须重复调用
	 * <p></p>
	 * 该方法会清除同名文件以及创建不存在的缓存文件夹
	 */
	public synchronized void init() {

		if (mIsInit) {
			L.i(TAG, "init : not need to call init() again");
			return;
		}
		File file = new File(mCacheDirPath);
		if (!FileUtil.canReadAndWrite(file, true)) {
			FileUtil.delete(file);
			FileUtil.mkdirs(file);
			if (!FileUtil.canReadAndWrite(mCacheDirPath, true)) {
				L.e(TAG, String.format("init : you have no permission to manager the path : %s", mCacheDirPath));
				throw new IllegalArgumentException(String.format("init failed : you have no permission to manager" +
						" the path : %s", mCacheDirPath));
			}
		}

		mCacheMap.clear();
		mCurrentCacheSize = 0;
		List<String> waitToDelete = new ArrayList<>();
		CacheHeader tmpHeader;
		// 读取所有缓存文件
		for (File cacheFile : file.listFiles()) {
			if (FileUtil.canReadAndWrite(cacheFile, true) &&
					(file.getName().endsWith(FILE_HEADER) && file.getName().length() > 32)) {
				if (CommonUtil.isEmpty((tmpHeader = readHeader(file)))) {
					mCacheMap.put(tmpHeader.getKey(), tmpHeader);
					mCurrentCacheSize += tmpHeader.getDataSize();
				} else {
					// 文件出错，移除
					waitToDelete.add(file.getAbsolutePath());
				}
			}
		}
		// 移除存在错误的缓存文件
		for (String path : waitToDelete) {
			FileUtil.delete(path);
			FileUtil.delete(path.substring(0, path.length() - 7) + FILE_DATA);
		}
		mIsInit = true;

	}

	/**
	 * 获取指定键值的Cache头信息
	 */
	public CacheHeader getHeader(String key) {
		if (!mIsInit) {
			L.e(TAG, "getHeader : need to call init(String) first");
			return null;
		}
		return mCacheMap.get(key);
	}

	/**
	 * 获取指定键值的Cache内容对象
	 */
	public CacheEntry getEntry(String key) {
		if (!mIsInit) {
			L.e(TAG, "getEntry : need to call init(String) first");
			return null;
		}
		if (!mCacheMap.containsKey(key)) {
			return null;
		}
		return new CacheEntry(mCacheMap.get(key), FileUtil.readBytes(getCacheFilePath(key) + FILE_HEADER));
	}

	/**
	 * 获取指定键值Cache数据
	 */
	public byte[] getData(String key) {
		if (!mIsInit) {
			L.e(TAG, "getData : need to call init(String) first");
			return null;
		}
		if (!mCacheMap.containsKey(key)) {
			return null;
		}
		return readData(key);
	}

	/**
	 * 添加或重新设置指定键值的缓存信息
	 */
	public synchronized void put(String key, CacheEntry entry) {
		if (!mIsInit) {
			L.e(TAG, "put : need to call init(String) first");
			return;
		}
		if (CommonUtil.isEmpty(entry) || CommonUtil.isEmpty(key)) {
			L.e(TAG, "put : key and entry is not allowed to be null");
			return;
		}
		if (mCacheMap.containsKey(key)) {
			mCurrentCacheSize += (entry.getData().length - mCacheMap.get(key).getDataSize());
		} else {
			mCurrentCacheSize += entry.getData().length;
		}
		prunedIfNeed();
		mCacheMap.put(key, new CacheHeader(key, entry));
		writeHeader(key, mCacheMap.get(key));
		writeData(key, entry.getData());
	}

	/**
	 * 重新设置指定键值的Cache头信息
	 */
	public synchronized void put(String key, CacheHeader entry) {
		if (!mIsInit) {
			L.e(TAG, "put : need to call init(String) first");
			return;
		}
		if (CommonUtil.isEmpty(entry) || CommonUtil.isEmpty(key)) {
			L.e(TAG, "put : key and entry is not allowed to be null");
			return;
		}
		if (!mCacheMap.containsKey(key) || mCacheMap.get(key).getDataSize() != entry.getDataSize()) {
			L.e(TAG, "put : the method only used to correct some change of cache header (except for sth. about data)");
			return;
		}
		mCacheMap.put(key, entry);
		writeHeader(key, mCacheMap.get(key));
	}

	/**
	 * 当当前缓存大小超过最大缓存的警戒值时,执行剪枝操作(控制为最大缓存的一半)
	 */
	private void prunedIfNeed() {
		if (mCurrentCacheSize <= mTotalCacheSize * mClearFactor) {
			return;
		}
		List<CacheHeader> headers = new ArrayList<>(mCacheMap.size());
		for (CacheHeader entry : mCacheMap.values()) {
			headers.add(entry);
		}
		// 进行排序，然后删除最旧的数据直到满足限制
		Collections.sort(headers, new Comparator<CacheHeader>() {
			@Override
			public int compare(CacheHeader lhs, CacheHeader rhs) {
				if (lhs.isExpired() && rhs.isExpired()) {
					return 0;
				} else if (lhs.isExpired()) {
					return -1;
				} else if (rhs.isExpired()) {
					return 1;
				} else if (lhs.getServerTime() > 0 && rhs.getServerTime() > 0) {
					return (int) (lhs.getServerTime() - rhs.getServerTime());
				} else if (lhs.getLastModifiedTime() > 0 && rhs.getLastModifiedTime() > 0) {
					return (int) (lhs.getLastModifiedTime() - rhs.getLastModifiedTime());
				}
				return 0;
			}
		});
		for (CacheHeader entry : headers) {
			remove(entry.getKey());
			if (mCurrentCacheSize <= mTotalCacheSize * 0.5) {
				break;
			}
		}
	}

	/**
	 * 清除所有缓存
	 */
	public synchronized void clear() {
		if (!mIsInit) {
			L.e(TAG, "clear : need to call init(String) first");
			return;
		}
		FileUtil.delete(mCacheDirPath);
		mCacheMap.clear();
		mCurrentCacheSize = 0;
	}

	/**
	 * 移除指定缓存
	 *
	 * @param key 查找键值
	 */
	public synchronized void remove(String key) {
		if (!mIsInit) {
			L.e(TAG, "remove : need to call init(String) first");
			return;
		}
		if (CommonUtil.isEmpty(key)) {
			L.e(TAG, "remove : key is not allowed to be null");
			return;
		}
		if (!mCacheMap.containsKey(key)) {
			L.i(TAG, "remove : key is not found");
			return;
		}
		// 移除指定key的缓存header和data文件
		FileUtil.delete(getCacheFilePath(key) + FILE_DATA);
		FileUtil.delete(getCacheFilePath(key) + FILE_HEADER);
		mCurrentCacheSize -= mCacheMap.remove(key).getDataSize();
	}

	/**
	 * 根据键值取得缓存文件的路径
	 */
	private String getCacheFilePath(String key) {
		return FileUtil.getAbsolutePath(String.format("%s/%s", mCacheDirPath, getFilenameByKey(key)));
	}

	/**
	 * 根据键值计算取得缓存文件名前缀
	 */
	private String getFilenameByKey(String key) {
		return MD5.digestInBase64(key, SGConfig.DEFAULT_UTF_CHARSET);
	}


	/**
	 * 写入Cache数据到指定键值对应的文件中
	 */
	private void writeData(String key, byte[] data) {
		FileUtil.writeBytes(new File(getCacheFilePath(key) + FILE_DATA), data);
	}


	/**
	 * 写入Cache头对象信息到指定键值对应的文件中
	 */
	private void writeHeader(String key, CacheHeader entry) {
		StringBuilder builder = new StringBuilder();
		builder.append(MAGIC).append("\n");
		builder.append(key).append("\n");
		builder.append(entry.getEtag()).append("\n");
		builder.append(entry.getServerTime()).append("\n");
		builder.append(entry.getLastModifiedTime()).append("\n");
		builder.append(entry.getExpireTime()).append("\n");
		builder.append(entry.getDataSize()).append("\n");
		builder.append(MAGIC);
		for (Map.Entry<String, String> item : entry.getResponseheaders().entrySet()) {
			builder.append(item.getKey()).append("=").append(item.getValue()).append("\n");
		}
		FileUtil.writeString(getCacheFilePath(key) + FILE_HEADER, builder.toString(), SGConfig.DEFAULT_UTF_CHARSET);
	}


	/**
	 * 读取并返回指定键值对应文件的Cache数据
	 */
	private byte[] readData(String key) {
		return FileUtil.readBytes(getCacheFilePath(key) + FILE_DATA);
	}

	/**
	 * 读取并返回指定键值对应文件的Cache头信息对象
	 */
	private CacheHeader readHeader(String key) {
		CacheHeader result = null;
		String[] headers = FileUtil.readString(getCacheFilePath(key) + FILE_HEADER,
				SGConfig.DEFAULT_UTF_CHARSET).split("\n");
		if (MAGIC == Integer.parseInt(headers[0]) && (MAGIC == Integer.parseInt(headers[7]))) {
			result = new CacheHeader();
			setHeaderInfo(result, headers);
		}
		return result;
	}

	/**
	 * 读取并返回指定文件的Cache头信息对象
	 */
	private CacheHeader readHeader(File file) {
		CacheHeader result = null;
		String[] headers = FileUtil.readString(file, SGConfig.DEFAULT_UTF_CHARSET).split("\n");
		if (MAGIC == Integer.parseInt(headers[0]) && (MAGIC == Integer.parseInt(headers[7]))) {
			result = new CacheHeader();
			setHeaderInfo(result, headers);
		}
		return result;
	}

	/**
	 * 设置Cache头对象信息
	 */
	private void setHeaderInfo(CacheHeader result, String[] headers) {
		result.setKey(headers[1]);
		result.setEtag(headers[2]);
		result.setServerTime(Long.parseLong(headers[3]));
		result.setLastModifiedTime(Long.parseLong(headers[4]));
		result.setExpireTime(Long.parseLong(headers[5]));
		result.setDataSize(Long.parseLong(headers[6]));
		Map<String, String> respHeaders = new HashMap<>(headers.length - 8);
		for (int i = 8; i < headers.length; i++) {
			String[] keyVal = headers[i].split("=");
			respHeaders.put(keyVal[0], keyVal[1]);
		}
	}
}
