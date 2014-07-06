package org.gditc.contacts.view;

import org.gditc.contacts.R;
import org.gditc.contacts.common.Contact;
import org.gditc.contacts.common.ContactsDbInfo;
import org.gditc.contacts.common.MyConstants;
import org.gditc.contacts.dao.ContactsDbHelper;
import org.gditc.contacts.utils.MyApplication;
import org.gditc.contacts.utils.ShareTool;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 显示联系人分组列表
 * @File MainActivity.java
 * @Package org.gditc.contacts.view
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月11日 下午3:27:29
 * @author Cryhelyxx
 * @version 1.0
 */
public class MainActivity extends ExpandableListActivity {

	private static final String TAG = "TeachersContacts";
	private long firstTime = 0;

	// 数据库操作对象
	private ContactsDbHelper db = null;
	// 游标对象
	private Cursor groupCursor = null;
	// 自定义适配器
	private MyCursorTreeAdapter mCursorTreeAdapter = null;

	// 自定义标题栏的搜索按钮
	private Button btnSearch = null;
	// 自定义标题栏的添加联系人按钮
	private Button btnAddContact = null;
	// 自定义标题栏的选项菜单按钮
	private Button btnOptionMenu = null;

	private Button btnSystemContacts = null;
	private Button btnEmail = null;
	private Button btnWeiXin = null;
	private Button btnShareMore = null;

	// 表名
	private static String TableNames[] = ContactsDbInfo.getTableNames();
	// 字段名
	private static String FieldNames[][] = ContactsDbInfo.getFieldNames();

	// 表contacts中和字段索引
	private static final int tbl_contacts_name_index = 1;
	private static final int tbl_contacts_telPhone_index = 2;
	private static final int tbl_contacts_cornet_index = 3;
	private static final int tbl_contacts_groupName_index = 4;
	private static final int tbl_contacts_email_index = 5;
	private static final int tbl_contacts_icon_index = 6;
	private static final int tbl_contacts_birthday_index = 7;
	private static final int tbl_contacts_address_index = 8;
	private static final int tbl_contacts_description_index = 9;

	// 表tbl_groups中字段groupName的索引
	private static final int tbl_groups_groupName_index = 1;
	// 弹出窗口的视图
	private View view;
	// 弹出的窗口
	private PopupWindow pop;
	// 缓存联系人所在的组， 用在移动联系人上
	private String myGroupName;
	// 缓存除了所选联系人所在组的所有组，用在移动联系人上
	private String groups[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);		// 自定义标题
		setContentView(R.layout.activity_main);
		//this.setTheme(R.style.custom_title_bar_theme);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_custom_title_bar);
		// 自定义ActionBar
		//setOverflowShowingAlways();
		MyApplication.getInstance().addActivity(this);
		loadingFormation();
		initData();					// 初始化数据
		initPopupWindow();			// 初始化弹出窗口

		// 为可展开的列表组件注册上下文菜单
		registerForContextMenu(getExpandableListView());

		getExpandableListView().setBackgroundResource(R.drawable.default_bg);
		getExpandableListView().setCacheColorHint(0);		//拖动时避免出现黑色
		getExpandableListView().setDivider(null);			//去掉每项下面的黑线(分割线)
		// 自定义每个Group之前的那个箭头图标
		getExpandableListView().setGroupIndicator(getResources().getDrawable(R.drawable.expander_ic_folder));
		getExpandableListView().setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Cursor cursor = mCursorTreeAdapter.getChild(groupPosition, childPosition);
				String name = cursor.getString(tbl_contacts_name_index);
				Intent intent = new Intent();
				intent.putExtra("name", name);
				intent.setClass(MainActivity.this, ContactInfoActivity.class);
				startActivity(intent);
				cursor.close();
				return false;
			}
		});
	}

	/**
	 * 加载准备操作
	 */
	private void loadingFormation() {
		// 实例化数据库对象
		db = ContactsDbHelper.getInstance(this);
		// 打开数据库
		db.open();

		btnSearch = (Button) this.findViewById(R.id.title_bar_btnSearch);
		btnAddContact = (Button) this.findViewById(R.id.title_bar_btn_add_contact);
		btnOptionMenu = (Button) this.findViewById(R.id.title_bar_btn_option_menu);

		btnSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchContact();
			}
		});
		btnAddContact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				addContact(null);
			}
		});
		btnOptionMenu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 创建PopupMenu对象
				PopupMenu popup = new PopupMenu(MainActivity.this, v);
				// 将R.menu.popup_menu菜单资源加载到popup菜单中
				getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
				// 为popup菜单的菜单项单击事件绑定事件监听器
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){

					@Override
					public boolean onMenuItemClick(MenuItem item){
						switch (item.getItemId()) {
						case R.id.popup_menu_add_group:
							addGroup().show();
							break;
						case R.id.popup_menu_about_us:
							createAboutUs().show();
							break;
						case R.id.popup_menu_exit:
							createExit().show();
							break;
						}
						return true;
					}
				});
				popup.show();
			}
		});
	}


	private Dialog addGroup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		final EditText content = new EditText(this);
		content.setSingleLine(true);
		builder.setTitle("创建分组");
		builder.setView(content);
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 添加新的分组到数据库
				String groupName = content.getText().toString();
				Cursor cursor = db.getAllGroups();
				if (!groupName.equals("")) {
					while (cursor.moveToNext()) {
						// 分组名称在数据库表中tbl_groups的索引是1
						if (cursor.getString(1).equals(groupName)) {
							showToast(groupName + "已存在");
							return;
						}
					}
					// 往数据库插入数据， 实现创建分组
					db.addGroup(groupName);
					showToast("创建分组成功");
					// 刷新界面
					refresh();		
				} else {
					showToast("分组名称不能为空");
				}
			}
		});
		builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();  // 关闭对话框
			}
		});
		return builder.create();
	}

	private void addContact(String groupName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_INSERT);
		Uri uri = null;
		uri = Uri.parse(MyConstants.CONTENT_URI);
		intent.setDataAndType(uri, MyConstants.CONTENT_TYPE_INSERT);
		if (groupName != null) {
			intent.putExtra("groupName", groupName);
		}
		startActivity(intent);
	}

	private void searchContact() {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchContactActivity.class);
		startActivity(intent);
	}

	/**
	 * 初始化弹出框
	 */
	@SuppressWarnings("deprecation")
	private void initPopupWindow() {
		view = this.getLayoutInflater().inflate(R.layout.popup_window, null);
		// 实例化弹出窗口， 并设置窗口的内容视图， 宽及高
		pop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setBackgroundDrawable(new BitmapDrawable());//点击窗口外消失
		// 设置显示PopuWindow之后在外面点击是否有效, 这里设置为无效
		pop.setOutsideTouchable(true);
		pop.setFocusable(false);

		btnSystemContacts = (Button) view.findViewById(R.id.btnSystemContacts);
		btnEmail = (Button) view.findViewById(R.id.btnEmail);
		btnWeiXin = (Button) view.findViewById(R.id.btnWeixin);
		btnShareMore = (Button) view.findViewById(R.id.btnShareMore);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		ShareTool.getInstance(MainActivity.this).regToWx();;
		// 获取所有分组
		groupCursor = db.getAllGroups();
		// 自定义适配器
		mCursorTreeAdapter = new MyCursorTreeAdapter(groupCursor, this, true);
		setListAdapter(mCursorTreeAdapter);
	}

	/*
	 * CursorTreeAdapter是BaseExpandableListAdapter的一个子类
	 * 通过该适配类可以用一连续的游标(Coursor)对象访问数据库，并将查询出来的数据展示到
	 * 可伸缩的列表视图(ExpandableListView)部件上。
	 * 顶层游标(Cursor)对象(在构造器中指定)显示全部组，
	 * 后面的游标(Cursor)对象从getChildrenCursor(Cursor)获取并展示子元素组。
	 * 其中游标携带的结果集中必须有个名为"_id"的列，否则这个类不起任何作用。
	 */
	public class MyCursorTreeAdapter extends CursorTreeAdapter {

		/**
		 * 构造方法
		 * @param cursor 为组(groups)提供数据的游标(Coursor)
		 * @param context 应用程序上下文
		 * @param autoRequery 设置为true时，每当数据库的数据发生改变时，适配器将调用requery()重新查询以显示最新的数据
		 */
		public MyCursorTreeAdapter(Cursor cursor, Context context,
				boolean autoRequery) {
			super(cursor, context, autoRequery);
		}

		/**
		 * 
		 */
		@Override
		protected Cursor getChildrenCursor(Cursor cursor) {
			// 得到当前的组名
			String groupName = cursor.getString(tbl_groups_groupName_index);
			Cursor childCursor = db.getContactsByGroupName(groupName);
			return childCursor;
		}

		/**
		 * 
		 */
		@Override
		protected View newGroupView(Context context, Cursor cursor,
				boolean isExpanded, ViewGroup parent) {
			Log.i(TAG, "newGroupView");
			LayoutInflater inflate = LayoutInflater.from(MainActivity.this);
			View view = inflate.inflate(R.layout.grouplayout, null);
			bindGroupView(view, context, cursor, isExpanded);
			return view;
		}

		/**
		 * 
		 */
		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			// 获取分组名称
			TextView tv_groupName = (TextView) view.findViewById(R.id.groupName);
			String groupName = cursor.getString(tbl_groups_groupName_index);
			tv_groupName.setText(groupName);
			// 获取分组子元素的个数
			TextView groupCount = (TextView) view.findViewById(R.id.groupCount);
			int count = db.getContactCountByGroupName(groupName);
			groupCount.setText("[" + count + "]");
		}

		/**
		 * 
		 */
		@Override
		protected View newChildView(Context context, Cursor cursor,
				boolean isLastChild, ViewGroup parent) {
			LayoutInflater inflate = LayoutInflater.from(MainActivity.this);
			View view = inflate.inflate(R.layout.childlayout, null);

			bindChildView(view, context, cursor, isLastChild);
			return view;
		}

		/**
		 * 
		 */
		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {

			final Contact contact = new Contact();

			// 获取联系人头像
			ImageView contactIcon = (ImageView) view.findViewById(R.id.contactIcon);
			contactIcon.setImageBitmap(getBitmapFromByte(cursor.getBlob(tbl_contacts_icon_index)));
			// 获取联系人姓名
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(cursor.getString(tbl_contacts_name_index));
			// 获取联系人简介
			TextView description = (TextView) view.findViewById(R.id.description);
			String contact_desc = cursor.getString(tbl_contacts_description_index);
			if (contact_desc != null && !"".equals(contact_desc)) {
				description.setTextKeepState(contact_desc);
				contact.setDescription(contact_desc);
			} else {
				description.setTextKeepState("这个人很懒， 什么都没留下！！！");
			}

			// 分组下子元素操作
			ImageView options = (ImageView) view.findViewById(R.id.childoptions);

			contact.setName(cursor.getString(tbl_contacts_name_index));
			contact.setTelPhone(cursor.getString(tbl_contacts_telPhone_index));
			contact.setGroupName(cursor.getString(tbl_contacts_groupName_index));
			String cornet = cursor.getString(tbl_contacts_cornet_index);
			if (cornet != null && !"".equals(cornet))
				contact.setCornet(cornet);
			final String email = cursor.getString(tbl_contacts_email_index);
			if (email != null && !"".equals(email))
				contact.setEmail(email);
			String address = cursor.getString(tbl_contacts_address_index);
			if (address != null && !"".equals(address))
				contact.setAddress(address);
			String birthday = cursor.getString(tbl_contacts_birthday_index);
			if (birthday != null && !"".equals(birthday))
				contact.setBirthday(birthday);
			if (contact_desc != null && !"".equals(contact_desc))
				contact.setDescription(contact_desc);
			options.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 如果弹出的窗口还显示， 则将其关闭
					if (pop.isShowing()) {
						pop.dismiss();
					} else {
						pop.showAsDropDown(v);		// 下拉显示方式
						//pop.showAtLocation(parent, gravity, x, y);
						//pop.showAsDropDown(v, 0, 10);
						btnSystemContacts.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								createSaveToSystemContactsDialog(contact).show();
							}
						});
						btnEmail.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								if (email != null && !"".equals(email)) {
									createShareToEmailDialog(contact, email).show();
								} else {
									showToast("请先填写邮箱地址");
								}
							}
						});
						btnWeiXin.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								pop.dismiss();
								ShareTool.getInstance(MainActivity.this).ShareToWeixin(contact);
							}
						});
						btnShareMore.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								pop.dismiss();
								Intent it = new Intent(Intent.ACTION_SEND);
								String text = ShareTool.getInstance(MainActivity.this).contactObjectToText(contact);
								it.putExtra(Intent.EXTRA_TEXT, text);
								it.setType("text/plain");
								startActivity(Intent.createChooser(it, "更多分享"));
							}
						});
					}
				}
			});
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	/**
	 * 创建将联系人信息保存到系统通讯录对话框
	 * @param contact 
	 * @return
	 */
	private Dialog createSaveToSystemContactsDialog(final Contact contact) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("提 示");
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage("确定要将该联系人信息保存到系统通讯录吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				pop.dismiss();
				ShareTool.getInstance(MainActivity.this).shareToLocalContacts(contact);
				showToast("成功添加联系人到本地通讯录");
			}
		});
		builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * 创建将联系人信息分享到Email对话框
	 * @param contact
	 * @param email 
	 * @return
	 */
	private Dialog createShareToEmailDialog(
			final Contact contact, final String email) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("提 示");
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage("确定要将该联系人信息分享到好友Email吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				pop.dismiss();
				Uri uri = Uri.parse("mailto:"+ email);
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				String text = ShareTool.getInstance(MainActivity.this).contactObjectToText(contact);
				it.putExtra(Intent.EXTRA_TEXT, text);
				//it.setType("text/plain");
				startActivity(it);
			}
		});
		builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * 获取存储在数据库中的头像
	 * @param temp
	 * @return
	 */
	public Bitmap getBitmapFromByte(byte[] temp) {
		if (temp != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		} else {
			return getDefaultIcon();
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
	 * 创建上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		// 在组上长按 
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			// 获取组上的标题以作为上下文菜单的标题
			String title = ((TextView) info.targetView.findViewById(R.id.groupName))
					.getText().toString();
			menu.setHeaderTitle(title);
			MenuInflater inflator = new MenuInflater(this);
			inflator.inflate(R.menu.contextmenu01, menu);
			// 在联系人(子元素)上长按
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			// 获取联系人的名称以作为上下文菜单的标题
			String title = ((TextView) info.targetView.findViewById(R.id.name))
					.getText().toString();
			Drawable icon = ((ImageView) info.targetView.findViewById(R.id.contactIcon))
					.getDrawable();
			menu.setHeaderTitle(title);		// 设置上下文标题名
			menu.setHeaderIcon(icon);		// 设置上下文标题图标
			MenuInflater inflator = new MenuInflater(this);
			inflator.inflate(R.menu.contextmenu02, menu);
		}
	}

	/**
	 * 上下文菜单被选中事件监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView.getPackedPositionType(menuInfo.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			// 获取组名
			String groupName = ((TextView) menuInfo.targetView.findViewById(R.id.groupName))
					.getText().toString();
			switch (item.getItemId()) {
			case R.id.item_addGroup01:			// 创建分组
				createDialog("addGroup", groupName).show();
				break;
			case R.id.item_deleteGroup01:			// 删除分组
				createDialog("deleteGroup", groupName).show();
				break;
			case R.id.item_renameGroup01:			// 重命名分组
				createDialog("renameGroup", groupName).show();
				break;
			case R.id.item_addContact01:			// 添加联系人
				addContact(groupName);
				break;
			}
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			// 获取联系人姓名
			final String name = ((TextView) menuInfo.targetView.findViewById(R.id.name))
					.getText().toString();
			switch (item.getItemId()) {
			case R.id.item_deleteContact02:			// 删除联系人
				deleteContact(name);
				break;
			case R.id.item_editContact02:			// 编辑联系人
				editContact(name);
				break;
			case R.id.item_moveContactTo02:			// 移动联系人至
				createMoveContactDialog(name).show();
				break;
			}
		}

		return true;
	}

	private void deleteContact(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("删除联系人");
		builder.setMessage("确定要删除联系人吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int count = db.deleteContact(name);
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

	private void editContact(final String name) {
		Intent intent = new Intent();
		intent.putExtra("name", name);
		intent.setAction(Intent.ACTION_EDIT);
		intent.setDataAndType(Uri.parse(MyConstants.CONTENT_URI),
				MyConstants.CONTENT_TYPE_EDIT);
		startActivity(intent);
	}

	/**
	 * "移动联系人至"对话框
	 * @param name
	 * @return
	 */
	private Dialog createMoveContactDialog(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("移动联系人至");
		builder.setSingleChoiceItems(getOtherAllGroup(name), -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newGroupName = groups[which];		// 获取要移动至的分组
				String sql = "UPDATE " + TableNames[0] + " SET " + FieldNames[0][4] + "=?"
						+ " WHERE " + FieldNames[0][4] + "=?" + " AND "
						+ FieldNames[0][1] + "=?";
				Object[] args = {newGroupName, myGroupName, name};
				db.updateSyncData(sql, args);
				refresh();
				showToast("成功移动联系人至" + newGroupName);
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	private String[] getOtherAllGroup(String name) {
		String sql = "SELECT " + FieldNames[0][4] + " FROM " + TableNames[0]
				+ " WHERE " + FieldNames[0][1] + "=?";
		String selectionArgs[] = {name};
		myGroupName = db.findContactGroup(sql, selectionArgs);
		Cursor cursor = db.getAllGroups();
		int count = cursor.getCount() - 1;
		groups = new String[count];
		int i = 0;
		while (cursor.moveToNext()) {
			String newGroupName = cursor.getString(tbl_groups_groupName_index);
			if (!newGroupName.equals(myGroupName)) {
				groups[i++] = newGroupName;
			}
		}
		cursor.close();
		return groups;
	}

	/**
	 * 创建对话框
	 * @param msg
	 * @param groupName
	 * @return
	 */
	private Dialog createDialog(String msg, final String groupName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// 创建分组
		if (msg.equals("addGroup")) {
			final EditText content = new EditText(this);
			content.setSingleLine(true);
			builder.setTitle("创建分组");
			builder.setView(content);
			builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 添加新的分组到数据库
					String groupName = content.getText().toString();
					Cursor cursor = db.getAllGroups();
					if (!groupName.equals("")) {
						while (cursor.moveToNext()) {
							// 分组名称在数据库表中tbl_groups的索引是1
							if (cursor.getString(1).equals(groupName)) {
								showToast(groupName + "已存在");
								return;
							}
						}
						// 往数据库插入数据， 实现创建分组
						db.addGroup(groupName);
						showToast("创建分组成功");
						// 刷新界面
						refresh();		
					} else {
						showToast("分组名称不能为空");
					}
				}
			});
			builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();  // 关闭对话框
				}
			});
			return builder.create();
		}
		// 删除分组
		if (msg.equals("deleteGroup")) {
			builder.setTitle("删除分组");
			builder.setMessage("确定要删除该组和该组内的所有联系人吗?");
			builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (groupName.equals("未分组")) {
						showToast("该分组不允许被删除");
						return;
					}
					// 删除该分组及其联系人（子元素）
					db.deleteGroup(groupName);
					// 删除分组后， 更新表tbl_contacts， 同步数据
					String sql = "DELETE FROM " + TableNames[0]
							+ " WHERE " + FieldNames[0][4] + "=?";
					Object args[] = {groupName};
					db.updateSyncData(sql, args);
					refresh();
					showToast("删除成功");
				}
			});
			builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();  // 关闭对话框
				}
			});
			return builder.create();
		}
		// 重命名分组
		if (msg.equals("renameGroup")) {
			final EditText content = new EditText(this);
			content.setSingleLine(true);
			content.setText(groupName);
			builder.setTitle("重命名分组");
			builder.setView(content);
			builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (groupName.equals("未分组")) {
						showToast("该分组不允许被重命名");
						return;
					}
					String newGroupName = content.getText().toString().trim();
					Cursor cursor = db.getAllGroups();
					if (!newGroupName.equals("")) {
						while (cursor.moveToNext()) {
							if (cursor.getString(1).equals(newGroupName)) {
								if (!newGroupName.equals(groupName)) {	// 如果新组名不同旧组名
									showToast(newGroupName + "已存在");
									return;
								} else {			// 如果新组名等同于旧组名， 不执行任何操作
									return;
								}
							}
						}
						// 重命名分组
						db.renameGroupName(newGroupName, groupName);
						String sql = "UPDATE " + TableNames[0] + " SET "
								+ FieldNames[0][4] + "=?" + " WHERE "
								+ FieldNames[0][4] + "=?";
						Object args[] = {newGroupName, groupName};
						db.updateSyncData(sql, args);
						refresh();
						showToast("重命名分组成功");
					}
				}
			});
			builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		return null;
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
	}




	/**
	 * 任何的按键都是 onKeyDown() 先接收的
	 * 如果按的是 menu 键，应该返回 false ，表示让后面需要接收 menu 键的事件继续处理。
	 * 返回 true 就表示这个事件到我这里就完结了，返回false表示继续传递给后面想要接收这个事件的地方
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {  
		case KeyEvent.KEYCODE_MENU: 
			return false;
		case KeyEvent.KEYCODE_BACK:  
			long secondTime = System.currentTimeMillis(); 
			// 如果弹出的窗口还显示， 则将其关闭
			if (pop.isShowing()) {
				pop.dismiss();
				return true;
			} else if (secondTime - firstTime > 2000) {   //如果两次按键时间间隔大于2秒，则不退出  
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();   
				firstTime = secondTime;//更新firstTime  
				return true;   
			} else {
				//完全退出程序
				MyApplication.getInstance().exit();
			}   
			break;  
		}
		//return false;
		return super.onKeyDown(keyCode, event); 
	}

	/**
	 * 自动刷新
	 */
	@Override
	protected void onResume() {
		super.onResume();
		initData();
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	 *//**
	 * 选项菜单中的项被选中事件
	 *//*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search_contact :
			searchContact();
			return true;
		case R.id.action_add_contact :
			addContact(null);
			return true;
		case R.id.action_add_group :
			addGroup().show();
			return true;
		case R.id.action_about_us :
			createAboutUs().show();
			return true;
		case R.id.action_exit :
			createExit().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * 工具栏菜单
	 * 当用户单击Menu键时触发该方法
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 选项菜单中的项被选中事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_menu_search_contact :
			searchContact();
			return true;
		case R.id.option_menu_add_contact :
			addContact(null);
			return true;
		case R.id.option_menu_add_group :
			addGroup().show();
			return true;
		case R.id.option_menu_about_us :
			createAboutUs().show();
			return true;
		case R.id.option_menu_exit :
			createExit().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 创建"关于我们"对话框
	 * @return
	 */
	private Dialog createAboutUs() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("关于我们");
		builder.setMessage("Copyright © 2014 GDITC 软件121\n"
				+ "Designed by 朱雄现 & 吴操 & 郑福健");
		builder.setNegativeButton("关 闭", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * 创建"退出程序"对话框
	 * @return
	 */
	private Dialog createExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("提 示");
		builder.setMessage("确定要退出程序吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//完全退出程序
				MyApplication.getInstance().exit();
			}
		});
		builder.setNegativeButton("关 闭", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭游标对象
		if (groupCursor != null) {
			groupCursor.close();
		}
		// 关闭适配器中的游标对象
		if (mCursorTreeAdapter != null && mCursorTreeAdapter.getCursor() != null) {
			mCursorTreeAdapter.getCursor().close();
		}
		// 关闭数据库对象
		if(db != null){
			db.close();
		}
	}
}
