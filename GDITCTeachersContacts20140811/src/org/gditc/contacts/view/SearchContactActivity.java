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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
	 * 创建上下文菜单时触发该方法
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		SortModel model = (SortModel)adapter.getItem(info.position);
		// 获取联系人的名称以作为上下文菜单的标题
		String title = model.getName();
		Drawable icon = ((ImageView)info.targetView.findViewById(
				R.id.search_contact_headIcon))
				.getDrawable();
		menu.setHeaderTitle(title);		// 设置上下文标题名
		menu.setHeaderIcon(icon);		// 设置上下文标题图标
		MenuInflater inflator = new MenuInflater(this);
		//装填R.menu.context对应的菜单， 并添加到menu中
		inflator.inflate(R.menu.contextmenu03, menu);
	}

	/**
	 * 上下文菜单中菜单项被单击时触发该方法
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.item_deleteContact03:
			deleteContact(menuInfo.position);
			break;

		case R.id.item_editContact03:
			editContact(menuInfo.position);
			break;

		}
		//return super.onContextItemSelected(item);
		return true;
	}

	/**
	 * 删除联系人
	 * @param position
	 */
	private void deleteContact(int position) {
		SortModel model = (SortModel)adapter.getItem(position);
		final String contact_name = model.getName();

		AlertDialog.Builder builder = new AlertDialog.Builder(SearchContactActivity.this);
		builder.setTitle("删除联系人");
		builder.setMessage("确定要删除联系人吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int count = db.deleteContact(contact_name);
				if (count > 0) {
					refresh();
					showToast("删除成功");
				} else {
					showToast("删除失败");
				}

			}
		});
		builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 编辑联系人
	 * @param position
	 */
	private void editContact(int position) {
		SortModel model = (SortModel)adapter.getItem(position);
		String contact_name = model.getName();

		Intent intent = new Intent();
		intent.putExtra("name", contact_name);
		intent.setAction(Intent.ACTION_EDIT);
		intent.setDataAndType(Uri.parse(MyConstants.CONTENT_URI),
				MyConstants.CONTENT_TYPE_EDIT);
		startActivity(intent);
	}
	
	/**
	 *  弹出提示信息
	 * @param msg
	 */
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 刷新界面(手动刷新)
	 */
	public void refresh() {
		initData();
		keywords = et_searchContact.getText().toString().trim();
		if (keywords != null) {
			filterData(keywords);
		}
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
