package org.gditc.contacts.common;

import java.io.Serializable;

/**
 * 联系人资料信息
 * @File Contact.java
 * @Package org.gditc.contacts.common
 * @Description 实体Bean
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月11日 下午3:37:38
 * @author Cryhelyxx
 * @version 1.0
 */
public class Contact implements Serializable{

	private static final long serialVersionUID = 8800035738558007397L;
	private String name;				// 姓名
	private byte[] contactIcon;			// 联系人头像
	private String telPhone;			// 手机号码
	private String cornet;				// 手机短号
	private String groupName;			// 所在组名称
	private String birthday;			// 生日
	private String address;				// 地址
	private String email;				// 邮箱地址
	private String description;			// 联系人描述
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the contactIcon
	 */
	public byte[] getContactIcon() {
		return contactIcon;
	}
	/**
	 * @param contactIcon the contactIcon to set
	 */
	public void setContactIcon(byte[] contactIcon) {
		this.contactIcon = contactIcon;
	}
	/**
	 * @return the telPhone
	 */
	public String getTelPhone() {
		return telPhone;
	}
	/**
	 * @param telPhone the telPhone to set
	 */
	public void setTelPhone(String telPhone) {
		this.telPhone = telPhone;
	}
	/**
	 * @return the cornet
	 */
	public String getCornet() {
		return cornet;
	}
	/**
	 * @param cornet the cornet to set
	 */
	public void setCornet(String cornet) {
		this.cornet = cornet;
	}
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * @return the birthday
	 */
	public String getBirthday() {
		return birthday;
	}
	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
