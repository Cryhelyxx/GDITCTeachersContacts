package org.gditc.contacts.common;

import android.graphics.Bitmap;

/**
 * 按字母排序
 * @File SortByLetter.java
 * @Package org.gditc.contacts.common
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月5日 下午2:33:31
 * @author Cryhelyxx
 * @version 1.0
 */
public class SortModel {

	private Bitmap img; 			//显示头像
	private String name;   			//显示的数据
	private String sortLetters;  	//显示数据拼音的首字母
	public Bitmap getImg() {
		return img;
	}
	public void setImg(Bitmap img) {
		this.img = img;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
