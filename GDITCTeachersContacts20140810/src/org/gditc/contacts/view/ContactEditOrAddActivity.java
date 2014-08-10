package org.gditc.contacts.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gditc.contacts.R;
import org.gditc.contacts.common.Contact;
import org.gditc.contacts.common.ContactsDbInfo;
import org.gditc.contacts.dao.ContactsDbHelper;
import org.gditc.contacts.utils.MyApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 编辑或新建联系人
 * @File ContactActivity.java
 * @Package org.gditc.contacts.view
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年6月28日 下午5:18:20
 * @author Cryhelyxx
 * @version 1.0
 */
public class ContactEditOrAddActivity extends Activity {

	private static final String TAG = "EDIT_ADD_CONTACT";
	// 该Activity处于两情景状态， 插入状态或编辑状态
	private static final int STATE_INSERT = 0;			// 插入状态
	private static final int STATE_EDIT = 1;			// 编辑状态
	// 记录当前情景的状态(插入或编辑)
	private int state;

	// 用来标识请求照相功能的activity
	private static final int PHOTO_REQUEST_CODE_TAKEPHOTO = 0x2014;

	// 用来标识请求gallery的activity
	private static final int PHOTO_REQUEST_CODE_GALLERT = 0x2012;
	
	// 用来标识裁剪后的Activity
	private static final int PHOTO_REQUEST_CODE_CUT = 0x2009;

	// 拍照的照片存储位置
	private static final String PHOTO_DIR = Environment
			.getExternalStorageDirectory()
			+ "/DCIM/Camera";

	//照相机拍照得到的图片
	private File mTempPhotoFile;

	// 生日(年-月-日)
	private int mYear;
	private int mMonth;
	private int mDay;

	private ContactsDbHelper db = null;

	// 表名
	private static String TableNames[] = ContactsDbInfo.getTableNames();
	// 字段名
	private static String FieldNames[][] = ContactsDbInfo.getFieldNames();

	private EditText name = null;
	private PhotoEditorView mPhotoEditor;//头像
	private EditText telPhone = null;
	private EditText cornet = null;
	private Spinner spinnerGroup = null;
	private EditText email = null;
	private EditText address = null;
	private Button btnBirthday = null;
	private EditText description = null;

	private Button btnOk = null;
	private Button btnCancel = null;
	// 缓存SpinnerGroup数据
	private ArrayAdapter<String> adapter = null;
	// 缓存联系人所有信息
	private Contact contactCache = null;
	// 在表tbl_groups中groupName的索引是1
	private static final int GROUPNAME_INDEX = 1;
	// 要编辑的联系人姓名
	private String editContactName = null;
	// 联系人的信息
	private String contact_name;
	private String contact_telPhone;
	private String contact_cornet;
	private String contact_group;
	private String contact_email;
	private byte[] contact_icon;
	private String contact_birthday;
	private String contact_address;
	private String contact_description;
	
	private Button btnBack = null;
	private TextView tv_title = null;
	private Button btnSearchContact = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
		setContentView(R.layout.edit_add_contact);
		MyApplication.getInstance().addActivity(this);

		loadingFormation();
		initData();

		Intent intent = getIntent();
		String action = intent.getAction();

		if (action.equals(Intent.ACTION_INSERT)) {
			state = STATE_INSERT;
			//this.setTitle("新建联系人");
			tv_title.setText("新建联系人");
			String birthday = mYear + "-" + mMonth + "-" + mDay;
			btnBirthday.setText(birthday);
		} else if (action.equals(Intent.ACTION_EDIT)) {
			state = STATE_EDIT;
			//this.setTitle("编辑联系人");
			tv_title.setText("编辑联系人");
			// 读取数据库里面的数据
			readDataFromDb(intent);
		} else {
			Log.e(TAG, "未知错误, 程序正在退出...");
			finish();
		}
	}

	/**
	 * 读取数据库里面的数据
	 * @param intent 
	 */
	private void readDataFromDb(Intent intent) {
		editContactName = intent.getStringExtra("name");
		String sql = "SELECT * FROM " + TableNames[0] 
				+ " WHERE " + FieldNames[0][1] + "=?";
		String selectionArgs[] = {editContactName};
		Cursor cursor = db.rawQuery(sql, selectionArgs);
		if (cursor != null) {
			// 联系人缓存为
			if (contactCache == null) {
				// 如果存在联系人同名， 将游标指针指向第一条记录(忽视同名联系人)
				cursor.moveToFirst();

				contact_name = cursor.getString(1);
				contact_telPhone = cursor.getString(2);
				contact_cornet = cursor.getString(3);
				contact_group = cursor.getString(4);
				contact_email = cursor.getString(5);
				contact_icon = cursor.getBlob(6);
				contact_birthday = cursor.getString(7);
				contact_address = cursor.getString(8);
				contact_description = cursor.getString(9);
				// 初始化生日
				initBirthday(contact_birthday);
			} else {
				contact_name = contactCache.getName();
				contact_telPhone = contactCache.getTelPhone();
				contact_cornet = contactCache.getCornet();
				contact_group = contactCache.getGroupName();
				contact_email = contactCache.getEmail();
				//contact_icon = contactCache.getContactIcon();
				contact_birthday = contactCache.getBirthday();
				contact_address = contactCache.getAddress();
				contact_description = contactCache.getDescription();
			}
			name.setText(contact_name);
			mPhotoEditor.setPhotoBitmap(getBitmapByte(contact_icon));
			telPhone.setText(contact_telPhone);
			cornet.setText(contact_cornet);
			int groupIndex = adapter.getPosition(contact_group);
			spinnerGroup.setSelection(groupIndex);
			email.setText(contact_email);
			address.setText(contact_address);
			btnBirthday.setText(contact_birthday);
			if (contact_description != null) {
				description.setTextKeepState(contact_description);	// 保持光标原先的位置
			} else {
				description.setText(contact_description);			// 光标跑到最后
			}
		}
	}
	
	/**
	 * 当应用遇到意外情况（如：内存不足、用户直接按Home键）由系统销毁一个Activity时，onSaveInstanceState() 会被调用.
	 * 通常onSaveInstanceState()只适合用于保存一些临时性的状态
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// 缓存联系人信息
		contactCache = new Contact();
		contactCache.setName(name.getText().toString().trim());
		BitmapDrawable bd = (BitmapDrawable) mPhotoEditor.getDrawable();
		Bitmap bitmap = bd.getBitmap();
		contactCache.setContactIcon(getBitmapByte(bitmap));
		contactCache.setTelPhone(telPhone.getText().toString().trim());
		contactCache.setCornet(cornet.getText().toString().trim());
		contactCache.setGroupName(spinnerGroup.getSelectedItem().toString());
		contactCache.setEmail(email.getText().toString().trim());
		contactCache.setAddress(address.getText().toString().trim());
		contactCache.setBirthday(btnBirthday.getText().toString());
		contactCache.setDescription(description.getText().toString().trim());
		
		outState.putSerializable("originalData", contactCache);
	}

	/**
	 * 初始化生日
	 * @param contact_birthday 
	 */
	private void initBirthday(String btn_birthday) {
		if (btn_birthday != null && !"".equals(btn_birthday)) {
			String args[] = btn_birthday.split("-");

			mYear = Integer.valueOf(args[0]);
			mMonth = Integer.valueOf(args[1]);
			mDay = Integer.valueOf(args[2]);
		}
	}

	/**
	 * 加载准备操作
	 */
	private void loadingFormation() {
		// 实例化数据库对象
		db = ContactsDbHelper.getInstance(this);
		// 打开数据库
		db.open();

		name = (EditText) this.findViewById(R.id.contact_name);
		mPhotoEditor = (PhotoEditorView) this.findViewById(R.id.contact_icon);
		telPhone = (EditText) this.findViewById(R.id.contact_telPhone);
		cornet = (EditText) this.findViewById(R.id.contact_cornet);
		spinnerGroup = (Spinner) this.findViewById(R.id.contact_group);
		email = (EditText) this.findViewById(R.id.contact_email);
		address = (EditText) this.findViewById(R.id.contact_address);
		btnBirthday = (Button) this.findViewById(R.id.contact_birthday_picker);
		description = (EditText) this.findViewById(R.id.contact_description);

		btnOk = (Button) this.findViewById(R.id.btn_ok);
		btnCancel = (Button) this.findViewById(R.id.btn_cancel);
		btnBack = (Button) this.findViewById(R.id.btn_back_add_contact);
		tv_title = (TextView) this.findViewById(R.id.title_add_contact);
		btnSearchContact = (Button) this.findViewById(R.id.add_contact_title_bar_btnSearch);
		
		// 给组件设置监听器
		setComponentsListener();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		getCurrentDate();
		initGroupData();
	}

	/**
	 * 初始化SpinnerGroup数据
	 */
	private void initGroupData() {
		adapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				getAllExistGroup());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerGroup.setAdapter(adapter);
		Intent intent = getIntent();
		String action = intent.getAction();

		if (action.equals(Intent.ACTION_INSERT)) {
			String group = intent.getStringExtra("groupName");
			if (group != null) {
				int position = adapter.getPosition(group);
				spinnerGroup.setSelection(position);
			}
		}
		

	}

	/**
	 * 获取所有分组
	 * @return
	 */
	private ArrayList<String> getAllExistGroup() {
		Cursor cursor = db.getAllGroups();
		ArrayList<String> groups = new ArrayList<String>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				groups.add(cursor.getString(GROUPNAME_INDEX));
			}
		}
		cursor.close();
		return groups;
	}

	/**
	 * 给组件设置监听器
	 */
	@SuppressLint("SimpleDateFormat")
	private void setComponentsListener() {
		mPhotoEditor.setEditorListener(new PhotoListener());
		
		btnBirthday.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String birthday = btnBirthday.getText().toString();
				createDatePickerDialog(birthday).show();
			}
		});
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				verifyData();
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btnSearchContact.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ContactEditOrAddActivity.this, SearchContactActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 验证数据， 并插入到数据库
	 */
	private void verifyData() {
		// 获取联系人信息
		contact_name = name.getText().toString().trim();
		BitmapDrawable bd = (BitmapDrawable) mPhotoEditor.getDrawable();
		Bitmap bitmap = null;
		boolean flag = PhotoEditorView.mHasSetPhoto;
		if (flag) {
			bitmap = bd.getBitmap();
		} else {
			bitmap = getDefaultIcon();
		}
		contact_icon = getBitmapByte(bitmap);
		contact_telPhone = telPhone.getText().toString().trim();
		contact_cornet = cornet.getText().toString().trim();
		contact_group = spinnerGroup.getSelectedItem().toString();
		contact_email = email.getText().toString().trim();
		contact_address = address.getText().toString().trim();
		contact_birthday = btnBirthday.getText().toString();
		contact_description = description.getText().toString().trim();
		// 检验数据的合法性
		if ("".equals(contact_name) || null == contact_name) {
			showToast("姓名不能为空");
			return;
		}
		if ("".equals(contact_telPhone) || null == contact_telPhone) {
			showToast("手机号码不能为空");
			return;
		}
		if (!"".equals(contact_email) && !isEmail(contact_email)) {
			showToast("邮箱地址格式错误");
			return;
		}

		contactCache = new Contact();
		contactCache.setName(contact_name);
		contactCache.setContactIcon(contact_icon);
		contactCache.setTelPhone(contact_telPhone);
		contactCache.setCornet(contact_cornet);
		contactCache.setGroupName(contact_group);
		contactCache.setEmail(contact_email);
		contactCache.setAddress(contact_address);
		contactCache.setBirthday(contact_birthday);
		contactCache.setDescription(contact_description);

		if (state == STATE_INSERT) {
			long count = db.addContact(contactCache);
			if (count > 0) {
				showToast("新建联系人成功");
				finish();
				jumpToContactInfoActivity();
			} else {
				showToast("新建联系人失败");
				finish();
			}
		} else {
			int count = db.updateContact(contactCache, editContactName);
			if (count > 0) {
				showToast("联系人更新成功");
				finish();
				jumpToContactInfoActivity();
			} else {
				showToast("联系人更新失败");
				finish();
			}
		}
	}

	/**
	 * 跳转到联系人详细信息页面
	 */
	private void jumpToContactInfoActivity() {
		Intent intent = new Intent();
		intent.setClass(ContactEditOrAddActivity.this, ContactInfoActivity.class);
		intent.putExtra("name", contact_name);
		startActivity(intent);
	}

	/**
	 * 检验是否是合法的邮箱地址
	 * @param str
	 * @return
	 */
	private boolean isEmail(String str) {
		String regex = "[a-zA-Z_0-9]{1,}@(([a-zA-Z0-9]-*){1,}\\.){1,3}[a-zA-Z\\-]{1,}";
		return match(regex, str);
	}

	/**
	 * 正则表达式匹配
	 * @param regex
	 * @param str
	 * @return
	 */
	private boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	// 弹出提示信息
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public DatePickerDialog createDatePickerDialog(String birthday) {
		initBirthday(birthday);
		DatePickerDialog datePickerDialog =
				new DatePickerDialog(ContactEditOrAddActivity.this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
						btnBirthday.setText(date);
					}
				}, mYear, mMonth - 1, mDay);
		return datePickerDialog;
	}

	/**
	 * 获取当前年月日
	 */
	private void getCurrentDate() {
		Calendar time = Calendar.getInstance();
		mYear = time.get(Calendar.YEAR);
		mMonth = time.get(Calendar.MONTH) + 1;
		mDay = time.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 将头像转换成byte[]以便能将图片存到数据库里
	 * @param bitmap
	 * @return
	 */
	private byte[] getBitmapByte(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "转换字节异常");
		}
		return out.toByteArray();
	}

	/**
	 * 获取存储在数据库中的头像
	 * @param temp
	 * @return
	 */
	private Bitmap getBitmapByte(byte[] temp) {
		if (temp != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		} else {
			return null;
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

	public class PhotoListener implements PhotoEditorView.EditorListener, DialogInterface.OnClickListener {

		@Override
		public void onRequest(int request) {
			if (request == PhotoEditorView.REQUEST_PICK_PHOTO) {
				if (mPhotoEditor.hasSetPhoto()) {
					// 当前已经有了照片
					createPhotoDialog().show();
				} else {
					doPickPhotoAction();
				}
			} 
		}

		/**
		 * 更换头像对话框
		 * @return
		 */
		private Dialog createPhotoDialog() {
			Context dialogContext = new ContextThemeWrapper(
					ContactEditOrAddActivity.this, android.R.style.Theme_Light);
			String[] items = new String[3];
			items[0] = getString(R.string.use_picture_as_primary);
			items[1] = getString(R.string.removePicture);
			items[2] = getString(R.string.changePicture);
			ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, items);
			AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
			builder.setTitle(R.string.contactIcon);
			builder.setSingleChoiceItems(adapter, -1, this);
			builder.setNegativeButton("返 回", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss(); 		// 关闭对话框
				}
			});
			return builder.create();
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			switch (which) {
			case 0:
				break;		// 什么也不做
			case 1:
				// 删除头像
				mPhotoEditor.setPhotoBitmap(null);
				break;
			case 2:
				// 更换头像
				doPickPhotoAction();
				break;
			}
		}

		/**
		 * 初次设置头像
		 */
		private void doPickPhotoAction() {
			Context dialogContext = new ContextThemeWrapper(ContactEditOrAddActivity.this,
					android.R.style.Theme_Light);
			String[] items = new String[2];
			items[0] = getString(R.string.take_picture);
			items[1] = getString(R.string.pick_picture);
			ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, items);
			AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
			builder.setTitle(R.string.contactIcon);
			builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					switch (which) {
					case 0:
						String status = Environment.getExternalStorageState();
						// 判断是否有SD卡
						if (status.equals(Environment.MEDIA_MOUNTED)) {
							doTakePhoto();				// 拍照获取图片
						} else {
							showToast("没有SD卡");
						}
						break;
					case 1:
						doPickPhotoFromGallery();		// 从图库中去选取图片
						break;

					default:
						break;
					}
				}
			});
			builder.setNegativeButton("返 回", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}

		
	}
	
	/**
	 * 拍照获取图片作为头像
	 */
	private void doTakePhoto() {
		File file = new File(PHOTO_DIR);
		if (!file.exists()) {
			file.mkdirs();		// 创建图片的存储目录
		}
		// 为新拍摄的图片的文件命名
		mTempPhotoFile = new File(file, getPhotoFileName());
		Intent intent = getTakePhotoIntent(mTempPhotoFile);
		startActivityForResult(intent, PHOTO_REQUEST_CODE_TAKEPHOTO);
		
	}

	/**
	 * 封装请求相机的intent
	 * @param file
	 * @return
	 */
	private Intent getTakePhotoIntent(File file) {
		// 调用系统的拍照功能
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 指定调用相机拍照后照片的存储路径
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		return intent;
	}
	
	/**
	 * 为新拍摄的图片的文件命名
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private String getPhotoFileName() {
		Date time = new Date(System.currentTimeMillis());
		DateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(time) + ".jpg";
	}

	/**
	 * 从图库中选取一张图片作为头像
	 */
	protected void doPickPhotoFromGallery() {
		Intent intent = getPickPhotoIntentFromGallery();
		startActivityForResult(intent, PHOTO_REQUEST_CODE_GALLERT);
	}

	/**
	 * 封装请求Gallery的intent
	 * @return
	 */
	private Intent getPickPhotoIntentFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		// crop为true是设置在开启的intent中设置显示的view裁剪
		intent.putExtra("crop", "true");
		// aspectX, aspectY是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX, outputY是裁剪图片的宽高
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		
		intent.putExtra("return-data", true);
		
		return intent;
	}

	/**
	 * 调用了Camera和Gallery所以要判断他们各自的返回情况, 他们启动时用的是startActivityForResult
	 * @param requestCode 确认返回的数据是从哪个Activity返回的
	 * @param resultCode 由子Activity通过其setResult()方法返回
	 * @param data 一个Intent对象，带有返回的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		// 调用图库(Gallery)程序返回的
		case PHOTO_REQUEST_CODE_GALLERT:
			if (data != null) {
				setPhotoToView(data);
			}
			break;
		// 调用照相机程序返回的, 再次调用图片剪辑程序去修剪图片
		case PHOTO_REQUEST_CODE_TAKEPHOTO:
			if (data != null) {
				// 获取拍摄后的照片的存储路径
				Uri uri = Uri.fromFile(mTempPhotoFile);
				// 对拍摄后的照片进行裁剪
				doCropPhoto(uri);
			}
			break;
		// 照相机拍摄了图片并进行裁剪后返回的
		case PHOTO_REQUEST_CODE_CUT:
			if (data != null) {
				setPhotoToView(data);
			}
			break;
		}
	}

	/**
	 * 对拍摄后的照片进行裁剪
	 * @param uri
	 */
	private void doCropPhoto(Uri uri) {
		Intent intent = getCropImageIntent(uri);
		startActivityForResult(intent, PHOTO_REQUEST_CODE_CUT);
	}

	/**
	 * 调用Androi系统内部自带了图片的剪裁功能
	 * @param photoUri
	 * @return
	 */
	private Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view裁剪
		intent.putExtra("crop", "true");
		// aspectX, aspectY是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX, outputY是裁剪图片的宽高
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		
		intent.putExtra("return-data", true);
		
		return intent;
	}

	/**
	 * 设置图片为联系人头像
	 * @param data
	 */
	private void setPhotoToView(Intent data) {
		Bitmap bitmap = data.getParcelableExtra("data");
		contact_icon = getBitmapByte(bitmap);
		mPhotoEditor.setPhotoBitmap(bitmap);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	
}
