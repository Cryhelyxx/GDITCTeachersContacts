package org.gditc.contacts.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gditc.contacts.common.Contact;
import org.gditc.contacts.common.ContactsDbInfo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * 通讯录操作数据库-DAO层
 * @File ContactsDbHelper.java
 * @Package org.gditc.contacts.dao
 * @Description sqlite数据库的创建/打开/关闭， 及对数据的增、 删、 改、 查
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月11日 下午5:03:11
 * @author Cryhelyxx
 * @version 1.0
 */
public class ContactsDbHelper {
	
	private static final String TAG = "ContactsDb";
	// SQLiteOpenHelper实例对象
	private DatabaseHelper mDbHelper;
	// 数据库实例对象
	private SQLiteDatabase mDb;
	// 数据库调用实例
	private static ContactsDbHelper openHelper = null;
	// 数据库名称
	private static final String DATABASE_NAME = "contacts.db";
	// 数据库版本
	private static int DATABASE_VERSION = 1;
	// 上下文对象
	private Context mCtx;

	//表名
	private static String TableNames[];
	//字段名
	private static String FieldNames[][];
	//字段类型
	private static String FieldTypes[][];

	// 构造方法
	public ContactsDbHelper(Context ctx) {
		super();
		this.mCtx = ctx;
	}

	/**
	 * 获取数据库调用实例
	 * @param context
	 * @return 数据库调用实例
	 */
	public static ContactsDbHelper getInstance(Context context) {
		if (openHelper == null) {
			openHelper = new ContactsDbHelper(context);
			TableNames = ContactsDbInfo.getTableNames();
			FieldNames = ContactsDbInfo.getFieldNames();
			FieldTypes = ContactsDbInfo.getFieldTypes();
		}
		return openHelper;
	}

	// 数据库辅助类(内部类)
	private static class DatabaseHelper extends SQLiteOpenHelper {

		// 构造方法
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * 创建数据库后，对数据库的操作
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			
			if (TableNames == null) {
				return;
			}
			for (int i = 0; i < TableNames.length; i++) {
				String sql = "CREATE TABLE " + TableNames[i] + " (";
				for(int j = 0; j < FieldNames[i].length; j++){
					sql += FieldNames[i][j] + " " + FieldTypes[i][j] + ",";
				}
				sql = sql.substring(0, sql.length() - 1);		//去掉最后的","
				sql += ")";
				db.execSQL(sql);
			}
			// 创建默认的分组, 命名为"未分组"
			String tempGroups[] = {"未分组"};
			for (int i = 0; i < tempGroups.length; i++) {
				String sql = "INSERT INTO " + TableNames[1] 
						+ " VALUES(?, ?, null, null)";
				Object[] bindArgs = {i + 1, tempGroups[i]};
				db.execSQL(sql, bindArgs);
			}
		}

		/**
		 * 更改数据库版本的操作
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			for (int i = 0; i < TableNames[i].length(); i++) {
				String sql = "DROP TABLE IF EXISTS " + TableNames[i];
				db.execSQL(sql);
			}
			onCreate(db);
		}

		/**
		 * 打开数据库后首先被执行
		 */
		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}
	}

	/**
	 * 打开数据库
	 * @return
	 * @throws SQLException
	 */
	public ContactsDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		mDbHelper.close();
	}
	
	/**
	 * sql查询语句
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public Cursor rawQuery(String sql, String[] selectionArgs) {
		Cursor cursor = mDb.rawQuery(sql, selectionArgs);
		return cursor;
	}
	
	/**
	 * 查询数据
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public Cursor select(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		Cursor cursor = mDb.query(
				table, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	
	/**
	 * 添加数据
	 * @param table
	 * @param fields
	 * @param values
	 * @return
	 */
	public long insert(String table, String fields[], String values[]) {
		ContentValues cv = new ContentValues();
		for (int i = 0; i < fields.length; i++) {
			cv.put(fields[i], values[i]);
		}
		return mDb.insert(table, null, cv);
	}
	
	/**
	 * 删除数据
	 * @param table
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public int delete(String table, String whereClause, String[] whereArgs) {
		return mDb.delete(table, whereClause, whereArgs);
	}
	
	/**
	 * 更新数据
	 * @param table
	 * @param updateFields
	 * @param updateValues
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public int update(String table, String updateFields[], String updateValues[],
			String whereClause, String[] whereArgs) {
		ContentValues cv = new ContentValues();
		for (int i = 0; i < updateFields.length; i++) {
			cv.put(updateFields[i], updateValues[i]);
		}
		return mDb.update(table, cv, whereClause, whereArgs);
	}
	
	/**
	 * 将头像转换成byte[]以便能将图片存到数据库
	 * @param bitmap
	 * @return
	 */
	public byte[] getBitmapByte(Bitmap bitmap){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Transform byte exception");
		}
		return out.toByteArray();
	}
	
	/**
	 * 获取所有分组
	 * @return 为组提供数据的游标对象
	 */
	public Cursor getAllGroups() {
		/*return mDb.query(
				TableNames[1],
				FieldNames[1],
				null, null, null, null, null);*/
		String sql = "SELECT * FROM " + TableNames[1];
		return mDb.rawQuery(sql, null);
	}
	
	/**
	 * 统计指定组的人数
	 * @param groupName
	 * @return 统计人数
	 */
	public int getContactCountByGroupName(String groupName) {
		int count = 0;
		String sql = "SELECT COUNT(*) FROM tbl_contacts WHERE groupName='" + groupName + "'";
		Cursor cursor = mDb.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}
	/**
	 * 获取给定组的所有成员
	 * @param groupName
	 * @return
	 */
	public Cursor getContactsByGroupName(String groupName) {
		// 方案一
		/*return mDb.query(
				TableNames[0],
				FieldNames[0],
				"groupName='" + groupName + "'",
				null, null, null, null);*/
		// 方案二
		String sql = "SELECT * FROM " + TableNames[0] + " WHERE " + FieldNames[0][4] + "=?";
		String[] selectionArgs = {groupName};
		return mDb.rawQuery(sql, selectionArgs);
	}
	
	/**
	 * 创建一个分组
	 * @param groupName
	 * @return
	 */
	public long addGroup(String groupName) {
		String time = getCurrentSysTime();
		
		ContentValues params = new ContentValues();
		params.put(FieldNames[1][1], groupName);
		params.put(FieldNames[1][2], time);
		params.put(FieldNames[1][3], time);
		return mDb.insert(TableNames[1], null, params);
	}
	
	/**
	 * 删除一个分组
	 * @param groupName
	 * @return
	 */
	public int deleteGroup(String groupName) {
		return mDb.delete(TableNames[1], "groupName=?", new String[]{groupName});
		//return mDb.delete(TableNames[1], "groupName='" + groupName + "'", null);
	}

	/**
	 * 获取系统当前时间
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String getCurrentSysTime() {
		Date time = new Date();
		//return time.toLocaleString();
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
		String formatTime = format.format(time);
		
		return formatTime;
	}

	/**
	 * 删除分组后， 同步更新表tbl_contacts中的groupName字段数据
	 * @param sql
	 * @param args
	 */
	public void updateSyncData(String sql, Object[] args) {
		mDb.execSQL(sql, args);
	}

	/**
	 * 重命名分组
	 * @param newGroupName
	 * @param oldGroupName
	 * @return
	 */
	public int renameGroupName(String newGroupName, String oldGroupName) {
		String time = getCurrentSysTime();
		ContentValues params = new ContentValues();
		params.put(FieldNames[1][1], newGroupName);
		params.put(FieldNames[1][3], time);
		return mDb.update(TableNames[1], params, FieldNames[1][1] + "=?", new String[]{oldGroupName});
	}

	/**
	 * 新建联系人
	 * @param contact 
	 * @return
	 */
	public long addContact(Contact contact) {
		String formatTime = getCurrentSysTime();
		ContentValues params = new ContentValues();
		params.put(FieldNames[0][1], contact.getName());
		params.put(FieldNames[0][2], contact.getTelPhone());
		params.put(FieldNames[0][3], contact.getCornet());
		params.put(FieldNames[0][4], contact.getGroupName());
		params.put(FieldNames[0][5], contact.getEmail());
		params.put(FieldNames[0][6], contact.getContactIcon());
		params.put(FieldNames[0][7], contact.getBirthday());
		params.put(FieldNames[0][8], contact.getAddress());
		params.put(FieldNames[0][9], contact.getDescription());
		params.put(FieldNames[0][10], formatTime);
		params.put(FieldNames[0][11], formatTime);
		
		return mDb.insert(TableNames[0], null, params);
	}

	/**
	 * 更新联系人
	 * @param contactCache
	 * @param editContactName
	 * @return
	 */
	public int updateContact(Contact contact, String name) {
		String formatTime = getCurrentSysTime();
		ContentValues params = new ContentValues();
		params.put(FieldNames[0][1], contact.getName());
		params.put(FieldNames[0][2], contact.getTelPhone());
		params.put(FieldNames[0][3], contact.getCornet());
		params.put(FieldNames[0][4], contact.getGroupName());
		params.put(FieldNames[0][5], contact.getEmail());
		params.put(FieldNames[0][6], contact.getContactIcon());
		params.put(FieldNames[0][7], contact.getBirthday());
		params.put(FieldNames[0][8], contact.getAddress());
		params.put(FieldNames[0][9], contact.getDescription());
		params.put(FieldNames[0][11], formatTime);
		
		return mDb.update(TableNames[0], params, FieldNames[0][1] + "=?", new String[]{name});
	}

	/**
	 * 删除联系人
	 * @param name
	 * @return
	 */
	public int deleteContact(String name) {
		return mDb.delete(TableNames[0], FieldNames[0][1] + "=?", new String[]{name});
	}

	public String findContactGroup(String sql, String[] selectionArgs) {
		String groupName = "";
		Cursor cursor = mDb.rawQuery(sql, selectionArgs);
		if (cursor.moveToFirst()) {
			groupName = cursor.getString(0);
		}
		cursor.close();
		return groupName;
	}

	/**
	 * 获取所有联系人
	 * @return
	 */
	public Cursor getAllContacts() {
		String sql = "SELECT * FROM " + TableNames[0];
		return mDb.rawQuery(sql, null);
	}

	/**
	 * 根据搜索关键词查询联系人
	 * @param keywords
	 * @return
	 */
	public Cursor searchContactByKeyWords(String keywords) {
		String sql = "SELECT * FROM " + TableNames[0] + " WHERE " + FieldNames[0][1] + " LIKE ?";
		String[] selectionArgs = {keywords};
		return mDb.rawQuery(sql, selectionArgs);
	}

	/**
	 * 根据联系人的姓名查找该联系人所有信息
	 * @param contactName
	 * @return
	 */
	public Cursor getContactsByContactName(String contactName) {
		String sql = "SELECT * FROM " + TableNames[0] + " WHERE " + FieldNames[0][1] + "=?";
		String[] selectionArgs = {contactName};
		return mDb.rawQuery(sql, selectionArgs);
	}



}
