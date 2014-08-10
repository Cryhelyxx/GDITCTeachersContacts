package org.gditc.contacts.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gditc.contacts.R;
import org.gditc.contacts.adapter.SortAdapter;
import org.gditc.contacts.common.MyConstants;
import org.gditc.contacts.common.SortModel;
import org.gditc.contacts.dao.ContactsDbHelper;
import org.gditc.contacts.handle.CharacterParser;
import org.gditc.contacts.handle.PinyinComparator;
import org.gditc.contacts.handle.SideBar;
import org.gditc.contacts.handle.SideBar.OnTouchingLetterChangedListener;
import org.gditc.contacts.utils.MyApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 查找联系人
 * @File SearchContact.java
 * @Package org.gditc.contacts.view
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月1日 上午9:46:27
 * @author Cryhelyxx
 * @version 1.0
 */
public class SearchContactActivity extends Activity {

	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	private SearchEditText et_searchContact = null;
	private ListView lv_contactList = null;
	private Button btnBack = null;

	private List<SortModel> contactList = null;
	private SortModel sortModel = null;

	private ContactsDbHelper db = null;
	private Cursor cursor = null;
	private String keywords = null;

	private Button btn_add_contact = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);		// 自定义标题
		setContentView(R.layout.search_contact);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_custom_title_bar);
		MyApplication.getInstance().addActivity(this);
		loadingFormation();

		db = ContactsDbHelper.getInstance(this);
		db.open();

		initData();
	}

	/**
	 * 加载组件
	 */
	private void loadingFormation() {
		et_searchContact = (SearchEditText) this.findViewById(R.id.search_contact);
		lv_contactList = (ListView) this.findViewById(R.id.contact_list);
		btnBack = (Button) this.findViewById(R.id.btn_back_search);
		btn_add_contact = (Button) this.findViewById(R.id.search_title_bar_btn_add_contact);

		//实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar = (SideBar) findViewById(R.id.sidebar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		setComponentsListener();
	}
	/**
	 * 为组件设置监听器
	 */
	private void setComponentsListener() {
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn_add_contact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_INSERT);
				Uri uri = null;
				uri = Uri.parse(MyConstants.CONTENT_URI);
				intent.setDataAndType(uri, MyConstants.CONTENT_TYPE_INSERT);
				startActivity(intent);
			}
		});
		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					lv_contactList.setSelection(position);
				}

			}
		});
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// 注册上下文菜单
		registerForContextMenu(lv_contactList);

		cursor = db.getAllContacts();
		SourceDateList = filledData();
		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		loadDataToAdapter();

		//设置拖动之后是否再次显示背景，也就是说设为true后，拖动listview，就不会显示背景图片了
		lv_contactList.setAlwaysDrawnWithCacheEnabled(false);
		lv_contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				sortModel = (SortModel)adapter.getItem(position);
				String name = sortModel.getName();
				Intent intent = new Intent();
				intent.setClass(SearchContactActivity.this, ContactInfoActivity.class);
				intent.putExtra("name", name);
				startActivity(intent);
			}
		});
		// 快速搜索
		et_searchContact.addTextChangedListener(new TextWatcher() {

			/**
			 * 文件变化时
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				keywords = et_searchContact.getText().toString().trim();
				/*if (keywords != null) {
					String[] temp = keywords.split("");
					keywords = "";
					for (int i = 0; i < temp.length; i++) {
						keywords += temp[i] + "%";
					}
					cursor = db.searchContactByKeyWords(keywords);
					loadDataToAdapter();
				
				}
				loadDataToAdapter();*/
				filterData(keywords);
			}

			/**
			 * 文本变化前
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			/**
			 * 文本变化后
			 */
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}
	
	@SuppressLint("DefaultLocale")
	private List<SortModel> filledData() {
		contactList = new ArrayList<SortModel>();
		while (cursor.moveToNext()) {
			SortModel sortModel = new SortModel();
			byte[] imgByte= cursor.getBlob(6);
			if (imgByte != null) {
				Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
				sortModel.setImg(photo);
			} else {
				sortModel.setImg(getDefaultIcon());
			}
			sortModel.setName(cursor.getString(1).toString());

			//汉字转换成拼音
			String pinyin = characterParser.getSelling(cursor.getString(1));
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")){
				sortModel.setSortLetters(sortString.toUpperCase());
			}else{
				sortModel.setSortLetters("#");
			}
			contactList.add(sortModel);
		}
		return contactList;
	}
	
	private void loadDataToAdapter() {
		adapter = new SortAdapter(getApplicationContext(), SourceDateList);
		lv_contactList.setAdapter(adapter);
	}
	
	/**
	 * 获取默认图标
	 * @return
	 */
	private Bitmap getDefaultIcon() {
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.defaulthead);
		return bmp;
	}
	
	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		List<SortModel> filterDateList = new ArrayList<SortModel>();
		
		if(TextUtils.isEmpty(filterStr)){
			filterDateList = SourceDateList;
		}else{
			filterDateList.clear();
			for(SortModel sortModel : SourceDateList){
				String name = sortModel.getName();
				if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
					filterDateList.add(sortModel);
				}
			}
		}
		
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}


	/*@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭游标对象
		if (cursor != null) {
			cursor.close();
		}
		// 关闭数据库对象
		if(db != null){
			db.close();
		}
	}*/




}
