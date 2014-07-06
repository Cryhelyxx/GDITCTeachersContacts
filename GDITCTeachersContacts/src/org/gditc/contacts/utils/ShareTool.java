package org.gditc.contacts.utils;

import org.gditc.contacts.common.Contact;
import org.gditc.contacts.common.MyConstants;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
/**
 * 分享联系人信息到各通讯平台工具类
 * @File ShareTool.java
 * @Package org.gditc.contacts.utils
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月4日 下午8:24:50
 * @author Cryhelyxx
 * @version 1.0
 */
public class ShareTool {

	private static IWXAPI api;
	private static ShareTool instance;
	private Context context;

	public ShareTool(Context context) {
		super();
		this.context = context;
	}
	
	/**
	 * 单例模式中获取唯一的ShareTools实例
	 * @return
	 */
	public static ShareTool getInstance(Context context) {
		if (null == instance) {
			instance = new ShareTool(context);
		}
		return instance;
	}

	/**
	 * 注册微信
	 */
	public void regToWx() {
		api = WXAPIFactory.createWXAPI(context, MyConstants.APP_ID, true);
		api.registerApp(MyConstants.APP_ID);
	}

	/**
	 * 分享到微信
	 * @param contact 
	 */
	public void ShareToWeixin(Contact contact) {
		String text = "";
		// 初始化一个WXTextObject对象
		WXTextObject textObj = new WXTextObject();
		text = contactObjectToText(contact);
		textObj.text =  text;
		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description = text;

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
		req.message = msg;
		// 加上下面这句就是分享到微信， 不添加下面这句就分享到微信好友
		req.scene = Req.WXSceneTimeline;

		// 调用api接口发送数据到微信
		api.sendReq(req);
	}

	public String contactObjectToText(Contact contact) {
		String text;
		text = "联系人：" + contact.getName()
				+ "\n手机：" + contact.getTelPhone();
		if (contact.getCornet() != null && !"".equals(contact.getCornet()))
			text += "\n短号：" + contact.getCornet();
		else 
			text += "\n短号：暂无填写";
		text += "\n所有分组：" + contact.getGroupName();
		if (contact.getEmail() != null && !"".equals(contact.getEmail()))
			text += "\nEmail：" + contact.getEmail();
		else
			text += "\nEmail：暂无填写";
		if (contact.getAddress() != null && !"".equals(contact.getAddress()))
			text += "\n地址：" + contact.getAddress();
		else
			text += "\n地址：暂无填写";
		if (contact.getBirthday() != null && !"".equals(contact.getBirthday()))
			text += "\n生日：" + contact.getBirthday();
		else
			text += "\n生日：暂无填写";
		if (contact.getDescription() != null && !"".equals(contact.getDescription()))
			text += "\n简介：" + contact.getDescription();
		else
			text += "\n简介：暂无填写";
		return text;
	}
	
	/**
	 * 分享到本地通讯录
	 * @param contact 
	 */
	public void shareToLocalContacts(Contact contact) {
		String name = contact.getName();
		String telPhone = contact.getTelPhone();
		String cornet = contact.getCornet();
		String email = contact.getEmail();
		/*String address = contact.getAddress();
		String birthday = contact.getBirthday();*/
		// 创建一个空的ContentValues
		ContentValues values = new ContentValues();
		/*
		 * 向RawContactsCONTENT_URI执行一个空值插入
		 * 目的是获取系统返回的rawContactId
		 */
		Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		//------------设置联系人姓名
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		// 设置内容类型
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		// 设置联系人姓名
		values.put(StructuredName.GIVEN_NAME, name);
		// 向联系人URI添加联系人名字
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
		//------------设置phone， 这里设置手机号码-------------
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		// 设置内容类型
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		// 设置联系人的电话号码
		values.put(Phone.NUMBER, telPhone);
		// 设置电话类型
		values.put(Phone.TYPE, Phone.TYPE_MOBILE);
		// 向联系人电话号码URI添加电话号码
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
		//------------设置phone其他， 这里设置短号----
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		// 设置内容类型
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		// 设置联系人的电话号码
		values.put(Phone.NUMBER, cornet);
		// 设置电话类型
		values.put(Phone.TYPE, Phone.TYPE_OTHER);
		// 向联系人其他电话号码URI添加短号
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
		//------------设置Email--------------
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		// 设置内容类型
		values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
		// 设置联系人的Email
		values.put(Email.DATA, email);
		// 设置Email
		values.put(Email.TYPE, Email.TYPE_WORK);
		// 向联系人EmailURI添加email数据
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

	}
}
