package com.jackiezhuang.sgframework.utils.http;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zsigui on 15-9-10.
 */
public class DownloadDBUtil {

	private static final String TAG = DownloadDBUtil.class.getName();

	private DownloadInfoDB mHelper;

	public DownloadDBUtil(Context context) {
		mHelper = new DownloadInfoDB(context);
	}

	public void insert(DownloadInfo info) {
		String sql = "INSERT INTO " + DownloadInfo.Param.TABLE_NAME + "("
				+ DownloadInfo.Param._KEY + ", "
				+ DownloadInfo.Param._URL + ", "
				+ DownloadInfo.Param._START_POS + ", "
				+ DownloadInfo.Param._STOP_POS + ", "
				+ DownloadInfo.Param._STORE_PATH + ", "
				+ DownloadInfo.Param._CURRENT_SIZE + ", "
				+ DownloadInfo.Param._STATE + ") values (?, ?, ?, ?, ?, ?, ?);";
		Object[] args = new Object[]{info.getKey(), info.getUrl(), info.getStartPos(), info.getStopPos(),
				info.getStorePath(), info.getCurSize(), info.getStatus().ordinal()};
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.execSQL(sql, args);
		database.close();
	}

	public void update(DownloadInfo info) {
		String sql = "UPDATE " + DownloadInfo.Param.TABLE_NAME + " SET "
				+ DownloadInfo.Param._START_POS + "=?, "
				+ DownloadInfo.Param._STOP_POS + "=?, "
				+ DownloadInfo.Param._STATE + "=?, "
				+ DownloadInfo.Param._CURRENT_SIZE + "=? WHERE "
				+ DownloadInfo.Param._KEY + "=?;";
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.execSQL(sql, new Object[]{info.getStartPos(), info.getStopPos(), info.getStatus(), info.getCurSize(),
				info.getKey()});
	}

	/**
	 * 更新下载状态为完成或者是废弃
	 */
	public void updateToFinishOrDicard(String key, int curSize, int status) {
		String sql = "UPDATE " + DownloadInfo.Param.TABLE_NAME + " SET "
				+ DownloadInfo.Param._START_POS + "=0, "
				+ DownloadInfo.Param._STOP_POS + "=?, "
				+ DownloadInfo.Param._CURRENT_SIZE + "=?, "
				+ DownloadInfo.Param._STATE + "=? WHERE "
				+ DownloadInfo.Param._KEY + "=?;";
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.execSQL(sql, new Object[]{curSize, curSize, status, key});
	}

	public DownloadInfo select(String key) {
		String sql = "SELECT " + DownloadInfo.Param._KEY + ", "
				+ DownloadInfo.Param._URL + ", "
				+ DownloadInfo.Param._START_POS + ", "
				+ DownloadInfo.Param._STOP_POS + ", "
				+ DownloadInfo.Param._STORE_PATH + ", "
				+ DownloadInfo.Param._CURRENT_SIZE + ", "
				+ DownloadInfo.Param._STATE + " FROM "
				+ DownloadInfo.Param.TABLE_NAME + " WHERE "
				+ DownloadInfo.Param._KEY + "=?;";
		SQLiteDatabase database = mHelper.getReadableDatabase();
		Cursor c = database.rawQuery(sql, new String[]{key});
		DownloadInfo info = new DownloadInfo();
		if (c.isBeforeFirst()) {
			c.moveToNext();
			info.setKey(c.getString(0));
			info.setUrl(c.getString(1));
			info.setStartPos(c.getInt(2));
			info.setStopPos(c.getInt(3));
			info.setStorePath(c.getString(4));
			info.setCurSize(c.getInt(5));
			info.setStatus(c.getInt(6));
		}
		c.close();
		database.close();
		return info;
	}

	public Map<String, DownloadInfo> selectAll() {
		String sql = "SELECT " + DownloadInfo.Param._KEY + ", "
				+ DownloadInfo.Param._URL + ", "
				+ DownloadInfo.Param._START_POS + ", "
				+ DownloadInfo.Param._STOP_POS + ", "
				+ DownloadInfo.Param._STORE_PATH + ", "
				+ DownloadInfo.Param._CURRENT_SIZE + ", "
				+ DownloadInfo.Param._STATE + " FROM "
				+ DownloadInfo.Param.TABLE_NAME;
		SQLiteDatabase database = mHelper.getReadableDatabase();
		Cursor c = database.rawQuery(sql, null);
		Map<String, DownloadInfo> result = new HashMap<>();
		if (c.isBeforeFirst()) {
			while (c.moveToNext()) {
				DownloadInfo info = new DownloadInfo();
				info.setKey(c.getString(0));
				info.setUrl(c.getString(1));
				info.setStartPos(c.getInt(2));
				info.setStopPos(c.getInt(3));
				info.setStorePath(c.getString(4));
				info.setCurSize(c.getInt(5));
				info.setStatus(c.getInt(6));
				result.put(info.getKey(), info);
			}
		}
		c.close();
		database.close();
		return result;
	}

	public void delete(String key) {
		String sql = "DELETE FROM " + DownloadInfo.Param.TABLE_NAME + " WHERE " + DownloadInfo.Param._KEY + "=?;";
		SQLiteDatabase database = mHelper.getWritableDatabase();
		database.execSQL(sql, new Object[]{key});
		database.close();
	}

	public void closeDB() {
		mHelper.close();
	}


	private final class DownloadInfoDB extends SQLiteOpenHelper {

		private static final String DB_NAME = "sg_download.db";
		private static final int DB_VERSION = 1;

		public DownloadInfoDB(Context context) {
			this(context, null);
		}

		public DownloadInfoDB(Context context, SQLiteDatabase.CursorFactory factory) {
			this(context, DB_NAME, factory, DB_VERSION);
		}

		public DownloadInfoDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(getCreateSql());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 简单粗暴，删除后重建
			db.execSQL("DROP TABLE IF EXISTS " + DownloadInfo.Param.TABLE_NAME);
			onCreate(db);
		}

		/**
		 * 获取建库语句
		 */
		private String getCreateSql() {
			return "CREATE TABLE IF NOT EXISTS " + DownloadInfo.Param.TABLE_NAME + " ("
					+ DownloadInfo.Param._ID + "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
					+ DownloadInfo.Param._KEY + " VARCHAR(32) NOT NULL, "
					+ DownloadInfo.Param._URL + " TEXT NOT NULL, "
					+ DownloadInfo.Param._START_POS + "INTEGER NOT NULL, "
					+ DownloadInfo.Param._STOP_POS + " INTEGER NOT NULL, "
					+ DownloadInfo.Param._CURRENT_SIZE + " INTEGER NOT NULL, "
					+ DownloadInfo.Param._STORE_PATH + " TEXT NOT NULL, "
					+ DownloadInfo.Param._STATE + " INTEGER NOT NULL,"
					+ "UNIQUE (" + DownloadInfo.Param._KEY + ") ON CONFLICT REPLACE" + ");";
		}
	}
}
