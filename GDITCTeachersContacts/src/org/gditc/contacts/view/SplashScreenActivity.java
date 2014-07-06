package org.gditc.contacts.view;

import org.gditc.contacts.R;
import org.gditc.contacts.utils.CopyTool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
/**
 * 进入程序时的欢迎界面
 * @File SplashScreenActivity.java
 * @Package org.gditc.contacts.view
 * @Description 闪屏界面
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月4日 下午12:02:15
 * @author Cryhelyxx
 * @version 1.0
 */
public class SplashScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
		
		this.setContentView(R.layout.activity_splash_screen);
		// 将assets文件夹下的数据库文件拷贝到应用的数据库目录下以实现准备数据库文件操作
		CopyTool.getInstance(this).prepareDatabaseFile();
		// 闪屏的核心代码
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);  //从启动动画ui跳转到主ui
				startActivity(intent);
				SplashScreenActivity.this.finish();    // 结束启动动画界面
			}
		}, 1500);    //启动动画持续1.5秒钟
	}
}
