package org.gditc.contacts.view;

import org.gditc.contacts.R;
import org.gditc.contacts.common.Contact;
import org.gditc.contacts.common.MyConstants;
import org.gditc.contacts.dao.ContactsDbHelper;
import org.gditc.contacts.utils.MyApplication;
import org.gditc.contacts.utils.ShareTool;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 联系人详细信息
 * @File ContactInfoActivity.java
 * @Package org.gditc.contacts.view
 * @Description TODO
 * @Copyright Copyright © 2014
 * @Site https://github.com/Cryhelyxx
 * @Blog http://blog.csdn.net/Cryhelyxx
 * @Email cryhelyxx@gmail.com
 * @Company 广东轻工职业技术学院计算机工程系
 * @Date 2014年7月2日 上午1:26:03
 * @author Cryhelyxx
 * @version 1.0
 */
public class ContactInfoActivity extends Activity {

	private ContactsDbHelper db = null;
	private Cursor cursor = null;
	
	private ImageView iv_contact_icon = null;
	private TextView tv_contact_name = null;
	private TextView tv_contact_telPhone = null;
	private Button btn_contact_telPhone_call = null;
	private Button btn_contact_telPhone_sms = null;
	private Button btn_contact_email = null;
	
	private TextView tv_contact_cornet = null;
	private Button btn_contact_cornet_call = null;
	private Button btn_contact_cornet_sms = null;
	private TextView tv_contact_group = null;
	private TextView tv_contact_email = null;
	private TextView tv_contact_address = null;
	private TextView tv_contact_birthday = null;
	private TextView tv_contact_description = null;

	private String contact_telPhoneNum = null;
	private String contact_cornetNum  = null;
	private String contact_email  = null;
	
	private Button btn_back = null;
	
	private Button btn_search_contact = null;
	private Button btn_add_contact = null;
	private Button btn_edit_contact = null;
	
	private Contact contact = null;
	
	private Button btnSystemContact = null;
	private Button btnEmail = null;
	private Button btnWeiXin = null;
	private Button btnShareMore = null;

	private static final int TBL_CONTACTS_NAME_INDEX = 1;
	private static final int TBL_CONTACTS_TELPHONE_INDEX = 2;
	private static final int TBL_CONTACTS_CORNET_INDEX = 3;
	private static final int TBL_CONTACTS_GROUP_INDEX = 4;
	private static final int TBL_CONTACTS_EMAIL_INDEX = 5;
	private static final int TBL_CONTACTS_ICON_INDEX = 6;
	private static final int TBL_CONTACTS_BITDHDAY_INDEX = 7;
	private static final int TBL_CONTACTS_ADDRESS_INDEX = 8;
	private static final int TBL_CONTACTS_DESCRIPTION_INDEX = 9;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);		// 自定义标题
		setContentView(R.layout.contact_info);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.info_custom_title_bar);
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
		iv_contact_icon = (ImageView) this.findViewById(R.id.info_contact_icon);
		tv_contact_name = (TextView) this.findViewById(R.id.info_contact_name);
		tv_contact_telPhone = (TextView) this.findViewById(R.id.info_contact_telPhone);
		btn_contact_telPhone_call = (Button) this.findViewById(R.id.btn_telPhone_call);
		btn_contact_telPhone_sms = (Button) this.findViewById(R.id.btn_telPhone_sms);
		btn_contact_email = (Button) this.findViewById(R.id.btn_contact_email);
		tv_contact_cornet = (TextView) this.findViewById(R.id.info_contact_cornet);
		btn_contact_cornet_call = (Button) this.findViewById(R.id.btn_cornet_call);
		btn_contact_cornet_sms = (Button) this.findViewById(R.id.btn_cornet_sms);
		tv_contact_group = (TextView) this.findViewById(R.id.info_contact_group);
		tv_contact_email = (TextView) this.findViewById(R.id.info_contact_email);
		tv_contact_address = (TextView) this.findViewById(R.id.info_contact_address);
		tv_contact_birthday = (TextView) this.findViewById(R.id.info_contact_birthday);
		tv_contact_description = (TextView) this.findViewById(R.id.info_contact_description);
		
		btn_back = (Button) this.findViewById(R.id.btn_back_info);
		btn_search_contact = (Button) this.findViewById(R.id.info_title_bar_btnSearch);
		btn_add_contact = (Button) this.findViewById(R.id.info_title_bar_btn_add_contact);
		btn_edit_contact = (Button) this.findViewById(R.id.info_title_bar_btn_edit_contact);

		btnSystemContact = (Button) this.findViewById(R.id.contact_info_btn_shareToSystemContacts);
		btnEmail = (Button) this.findViewById(R.id.contact_info_btn_shareToEmail);
		btnWeiXin = (Button) this.findViewById(R.id.contact_info_btn_shareToWeixinFrientCircle);
		btnShareMore = (Button) this.findViewById(R.id.contact_info_btn_shareToMore);
		
		setComponentsListener();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		contact = new Contact();
		
		Intent intent = getIntent();
		String contactName = intent.getStringExtra("name");
		cursor = db.getContactsByContactName(contactName);
		cursor.moveToNext();
		byte[] icon = cursor.getBlob(TBL_CONTACTS_ICON_INDEX);
		if (icon != null) {
			iv_contact_icon.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.length));
		} else {
			iv_contact_icon.setImageResource(R.drawable.defaulthead);
		}
		tv_contact_name.setText(cursor.getString(TBL_CONTACTS_NAME_INDEX));
		String telPhone = cursor.getString(TBL_CONTACTS_TELPHONE_INDEX);
		tv_contact_telPhone.setText(telPhone);
		String cornet = cursor.getString(TBL_CONTACTS_CORNET_INDEX);
		if (cornet != null && !"".equals(cornet)) {
			tv_contact_cornet.setText(cornet);
		} else {
			tv_contact_cornet.setText("暂无填写");
		}
		String groupName = cursor.getString(TBL_CONTACTS_GROUP_INDEX);
		tv_contact_group.setText(groupName);
		String email = cursor.getString(TBL_CONTACTS_EMAIL_INDEX);
		if (email != null && !"".equals(email)) {
			tv_contact_email.setText(email);
			contact_email = email;
		} else {
			tv_contact_email.setText("暂无填写");
		}
		String address = cursor.getString(TBL_CONTACTS_ADDRESS_INDEX);
		if (address != null && !"".equals(address)) {
			tv_contact_address.setText(address);
		} else {
			tv_contact_address.setText("暂无填写");
		}
		String birthday = cursor.getString(TBL_CONTACTS_BITDHDAY_INDEX);
		if (birthday != null && !"".equals(birthday)) {
			tv_contact_birthday.setText(birthday);
		} else {
			tv_contact_birthday.setText("暂无填写");
		}
		String description = cursor.getString(TBL_CONTACTS_DESCRIPTION_INDEX);
		if (description != null && !"".equals(description)) {
			tv_contact_description.setText(description);
		} else {
			tv_contact_description.setText("暂无填写");
		}

		contact_telPhoneNum = cursor.getString(TBL_CONTACTS_TELPHONE_INDEX).toString();
		contact_cornetNum = cursor.getString(TBL_CONTACTS_CORNET_INDEX).toString();
		
		contact.setName(contactName);
		contact.setTelPhone(telPhone);
		contact.setCornet(cornet);
		contact.setGroupName(groupName);
		contact.setEmail(email);
		contact.setAddress(address);
		contact.setBirthday(birthday);
		contact.setDescription(description);
	}

	/**
	 * 设置组件监听器
	 */
	private void setComponentsListener() {
		btn_contact_telPhone_call.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = null;
				uri = Uri.parse("tel:" + contact_telPhoneNum);
				Intent i = new Intent(Intent.ACTION_DIAL, uri);
				startActivity(i);
			}
		});
		btn_contact_telPhone_sms.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = null;
				uri = Uri.parse("smsto:" + contact_telPhoneNum);
				Intent i = new Intent(Intent.ACTION_SENDTO, uri);
				i.putExtra("sms_body", "Hi, Long time no see!");
				startActivity(i);
			}
		});
		btn_contact_cornet_call.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact_cornetNum != null && !"".equals(contact_cornetNum)) {
					if (contact_cornetNum != null && !"".equals(contact_cornetNum)) {
						Uri uri = null;
						uri = Uri.parse("tel:" + contact_cornetNum);
						Intent i = new Intent(Intent.ACTION_DIAL, uri);
						startActivity(i);
					} 
					
				} else {
					showToast("暂无短号");
				}

			}
		});
		btn_contact_cornet_sms.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact_cornetNum != null && !"".equals(contact_cornetNum)) {
					Uri uri = null;
					uri = Uri.parse("smsto:" + contact_cornetNum);
					Intent i = new Intent(Intent.ACTION_SENDTO, uri);
					i.putExtra("sms_body", "Hi, Long time no see!");
					startActivity(i);
				} else {
					showToast("暂无短号");
				}
				
			}
		});
		btn_contact_email.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (contact_email !=null && !"".equals(contact_email)) {
					Uri uri = Uri.parse("mailto:"+ contact_email);
					Intent it = new Intent(Intent.ACTION_SENDTO, uri);
					startActivity(it);
				} else {
					showToast("请先填写邮箱地址");
				}
			}
		});
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn_search_contact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ContactInfoActivity.this, SearchContactActivity.class);
				startActivity(intent);
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
		btn_edit_contact.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = tv_contact_name.getText().toString();
				Intent intent = new Intent();
				intent.putExtra("name", name);
				intent.setAction(Intent.ACTION_EDIT);
				intent.setDataAndType(Uri.parse(MyConstants.CONTENT_URI),
						MyConstants.CONTENT_TYPE_EDIT);
				startActivity(intent);
			}
		});
		
		btnSystemContact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ShareTool.getInstance(ContactInfoActivity.this).shareToLocalContacts(contact);
				showToast("成功添加联系人到本地通讯录");
			}
		});
		btnEmail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact.getEmail() != null && !"".equals(contact.getEmail())) {
					Uri uri = Uri.parse("mailto:"+ contact.getEmail());
					Intent it = new Intent(Intent.ACTION_SENDTO, uri);
					String text = ShareTool.getInstance(ContactInfoActivity.this).contactObjectToText(contact);
					it.putExtra(Intent.EXTRA_TEXT, text);
					//it.setType("text/plain");
					startActivity(it);
				} else {
					showToast("请先填写邮箱地址");
				}
			}
		});
		btnWeiXin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ShareTool.getInstance(ContactInfoActivity.this).ShareToWeixin(contact);
			}
		});
		btnShareMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent it = new Intent(Intent.ACTION_SEND);
				String text = ShareTool.getInstance(ContactInfoActivity.this).contactObjectToText(contact);
				it.putExtra(Intent.EXTRA_TEXT, text);
				it.setType("text/plain");
				startActivity(Intent.createChooser(it, "更多分享"));
			}
		});
	}
	
	public void refresh() {
		initData();
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	/**
	 *  弹出提示信息
	 * @param msg
	 */
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}


}
