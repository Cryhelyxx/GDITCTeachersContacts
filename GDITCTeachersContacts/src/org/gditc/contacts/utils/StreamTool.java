package org.gditc.contacts.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * I/O流工具类
 * @File StreamTool.java
 * @Package org.gditc.contacts.utils
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月30日 上午9:37:15
 * @author Cryhelyxx
 * @version 1.0
 */
public class StreamTool {

	public static void copyStream(InputStream is, OutputStream os) {
		final int BUFFER_SIZE = 1024;
		try {
			// 文件写入
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = 0;
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
