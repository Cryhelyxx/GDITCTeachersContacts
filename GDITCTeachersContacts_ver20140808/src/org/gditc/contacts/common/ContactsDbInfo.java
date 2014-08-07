package org.gditc.contacts.common;
/**
 * 通讯录数据库信息
 * @File ContactsDbInfo.java
 * @Package org.gditc.contacts.common
 * @Description 数据库中每个表的架构
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月11日 下午6:37:06
 * @author Cryhelyxx
 * @version 1.0
 */
public class ContactsDbInfo {
	
	// 表名， 使用一维数组， 使用全局变量(static)
	private static String TableNames[] = {
		"tbl_contacts", 			// 联系人表
		"tbl_groups"				// 联系人分组表
	};
	
	// 与表对应的字段， 使用二维数组， 使用全局变量(static)
	private static String FieldNames[][] = {
		{"_id",						// 行id
			"name",					// 姓名
			"telPhone",				// 电话/手机号码
			"cornet",				// 手机短号
			"groupName",			// 所在分组
			"email",				// 邮箱地址
			"icon",					// 联系人头像
			"birthday",				// 生日
			"address",				// 地址
			"description",			// 联系人简介
			"createTime",			// 创建时间
			"modifyTime"}, 			// 修改时间
		{"_id",						// 行id
			"groupName",			// 分组名称
			"createTime",			// 创建时间
			"modifyTime"}			// 修改时间
	};
	
	// 与表的字段对应的字段类型
	private static String FieldTypes[][] = {
		{"INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
			"TEXT NOT NULL",
			"TEXT NOT NULL",
			"TEXT",
			"TEXT CONSTRAINT FK1 " + "REFERENCES " + TableNames[1] + "(" + FieldNames[1][1] + ") ON DELETE CASCADE ON UPDATE CASCADE",
			"TEXT",
			"BLOB",
			"TEXT",
			"TEXT",
			"TEXT",
			"TEXT",
			"TEXT"},
		{"INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
			"TEXT UNIQUE NOT NULL",
			"TEXT",
			"TEXT"}
	};

	/**
	 * @return the tableNames
	 */
	public static String[] getTableNames() {
		return TableNames;
	}

	/**
	 * @return the fieldNames
	 */
	public static String[][] getFieldNames() {
		return FieldNames;
	}

	/**
	 * @return the fieldTypes
	 */
	public static String[][] getFieldTypes() {
		return FieldTypes;
	}
}
