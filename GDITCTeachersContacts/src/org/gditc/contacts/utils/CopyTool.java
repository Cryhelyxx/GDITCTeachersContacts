package org.gditc.contacts.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
/**
 * 拷贝数据库文件工具类
 * @File CopyTool.java
 * @Package org.gditc.contacts.utils
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月4日 下午8:24:30
 * @author Cryhelyxx
 * @version 1.0
 */
public class CopyTool {
	
	private SharedPreferences mPerPreferences = null;
	private static CopyTool instance;
	private Context context;

	public CopyTool(Context context) {
		super();
		this.context = context;
	}
	
	/**
	 * 单例模式中获取唯一的CopyTool实例
	 * @return
	 */
	public static CopyTool getInstance(Context context) {
		if (null == instance) {
			instance = new CopyTool(context);
		}
		return instance;
	}

	
	
	
	/**
	 * 准备数据库文件
	 */
	public void prepareDatabaseFile() {
		boolean flag = dbFileHasCopied();
		// 如果还没拷贝准备好的数据库文件， 则执行拷贝操作 
		if (!flag) {
			copyOfDatabaseFile();
			// 用dbfile_isExist.xml文件作为日志标记文件已拷贝
			mPerPreferences = context.getSharedPreferences("dbfile_isExist", Context.MODE_PRIVATE);
			SharedPreferences.Editor mEditor = mPerPreferences.edit();
			mEditor.putString("isExist", "数据库文件已拷贝");
			mEditor.commit();
		}
	}

	public boolean dbFileHasCopied() {
		// 如果不存在则创建dbfile_isExist.xml文件
		mPerPreferences = context.getSharedPreferences("dbfile_isExist", Context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = mPerPreferences.edit();
		mEditor.commit();

		boolean isExist = mPerPreferences.contains("isExist");
		return isExist;
	}

	/**
	 * 准备数据库数据
	 */
	public void copyOfDatabaseFile() {
		String db_name = "contacts.db";
		File db_file = context.getDatabasePath(db_name);
		File db_path = new File(db_file.getParent());
		if (!db_path.exists()) {
			db_path.mkdir();	// 如果数据库目录不存在， 则创建
		}
		if (!db_file.exists()) {
			try {
				db_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// 得到  assets 目录下我们实现准备好的 SQLite 数据库作为输入流
			InputStream is = context.getAssets().open(db_name);
			// 输出流
			OutputStream os = new FileOutputStream(db_file);
			// 将assets文件夹下的数据库文件拷贝到应用的数据库目录下
			StreamTool.copyStream(is, os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
