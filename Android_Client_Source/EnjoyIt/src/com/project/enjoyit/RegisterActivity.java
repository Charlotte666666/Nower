package com.project.enjoyit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.project.enjoyit.global.MyAlgorithm;
import com.project.enjoyit.global.MyApplication;
import com.project.enjoyit.global.MyURL;
import com.project.enjoyit.utils.ImageUtil;
import com.project.enjoyit.utils.PictureUtil;
import com.project.enjoyit.view.SelectPicPopupWindow;
import com.rxy.edittextmodel_master.ClearableEditText;

import dmax.dialog.SpotsDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final String TAG = "RegisterActivity";

	private ClearableEditText etUsername;
	private ClearableEditText etPhone;
	private ClearableEditText etPassword;
	private ClearableEditText etRePassword;
	private Button btRegister;
	private Button btCancel;
	private ImageButton ibHead;

	private static AlertDialog uploading;
	// �Զ���ĵ�������
	SelectPicPopupWindow menuWindow;
	/* ͷ���ļ� */
	private static final String IMAGE_FILE_NAME = "temp_head_image.jpg";
	/* ����ʶ���� */
	private static final int CODE_GALLERY_REQUEST = 0xa0;
	private static final int CODE_CAMERA_REQUEST = 0xa1;
	private static final int CODE_RESULT_REQUEST = 0xa2;
	// �ü���ͼƬ�Ŀ�(X)�͸�(Y),480 X 480�������Ρ�
	private static int output_X = 160;
	private static int output_Y = 160;
	private static String head_pic_name = "";

	private static Context context;
	private final static int CODE_TOAST_MSG = 0;
	public static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CODE_TOAST_MSG:
				Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT)
						.show();
				break;
			case ImageUtil.CODE_UPLOADED_IMAGES:
				head_pic_name = msg.obj.toString();
				uploading.dismiss();
				Toast.makeText(context, "ͷ���ϴ��ɹ�", Toast.LENGTH_SHORT).show();
				break;
			case ImageUtil.CODE_UPLOAD_IMAGE_FAILED:
				Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				uploading.dismiss();
				break;
			}
		}
	};

	public void myToast(String msg) {
		handler.obtainMessage(CODE_TOAST_MSG, msg).sendToTarget();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		setContentView(R.layout.activity_register);
		context = RegisterActivity.this;
		initView();
		initListener();
		uploading = new SpotsDialog(RegisterActivity.this);
	}

	private void initView() {
		etUsername = (ClearableEditText) findViewById(R.id.et_username);
		etPhone = (ClearableEditText) findViewById(R.id.et_phone);
		etPassword = (ClearableEditText) findViewById(R.id.et_password);
		etRePassword = (ClearableEditText) findViewById(R.id.et_re_password);
		btRegister = (Button) findViewById(R.id.bt_register);
		btCancel = (Button) findViewById(R.id.bt_cancel);
		ibHead = (ImageButton) findViewById(R.id.ib_head_pic);

	}

	private void initListener() {
		
		//http://www.2cto.com/kf/201402/276814.html
		etRePassword.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				 if(keyCode == KeyEvent.KEYCODE_ENTER){
		                /*����������*/
		                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		                if(inputMethodManager.isActive()){
		                    inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		                }
		                 btRegister.performClick();
		                return true;
		            }
		            return false;
			}
		});
		
		btRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				register();
			}
		});
		btCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		ibHead.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.showSoftInput(etUsername, InputMethodManager.SHOW_FORCED);
				imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0); //ǿ�����ؼ��� 
				
				initMenuWindow();
			}
		});
	}

	protected void register() {

		final String username = etUsername.getText().toString().trim();
		final String phone = etPhone.getText().toString().trim();
		final String password = MyAlgorithm.hashMd5(etPassword.getText()
				.toString().trim());
		final String rePassword = MyAlgorithm.hashMd5(etRePassword.getText()
				.toString().trim());
		if (username.isEmpty() || phone.isEmpty() || password.isEmpty()
				|| rePassword.isEmpty()) {
			myToast("��û����Ŷ");
		} else if (!password.equals(rePassword)) {
			myToast("������������벻��ͬ");
			etPassword.setText("");
			etRePassword.setText("");
		} else {
			final AlertDialog loading = new SpotsDialog(RegisterActivity.this);
			loading.setTitle("ע���У����Ժ�...");
			loading.show();
			Listener<String> listener = new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					try {
						JSONObject res = new JSONObject(response);
						Log.e(TAG, res.getString("msg"));
						myToast(res.getString("msg"));
						if (res.getInt("code") == 1) {
							Intent intent = new Intent();
							intent.putExtra("username", username);
							setResult(RESULT_OK, intent);
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						loading.dismiss();
					}
				}
			};
			ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.printStackTrace();
					myToast("�ǲ���������������أ�");
					loading.dismiss();
				}

			};
			Map<String, String> map = new HashMap<String, String>();
			map.put("username", username);
			map.put("phone", phone);
			map.put("password", password);
			map.put("head_pic", head_pic_name);
			MyApplication.getMyVolley().addPostStringRequest(
					MyURL.REGISTER_URL, listener, errorListener, map, TAG);
		}

	}

	
	
	protected void initMenuWindow() {
		// ʵ����SelectPicPopupWindow
		menuWindow = new SelectPicPopupWindow(RegisterActivity.this,
				itemsOnClick);
		// ��ʾ����
		menuWindow.showAtLocation(
				RegisterActivity.this.findViewById(R.id.bt_register),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // ����layout��PopupWindow����ʾ��λ��
		// Ϊ��������ʵ�ּ�����
	}

	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			case R.id.btn_take_photo:
				choseHeadImageFromCameraCapture();
				break;
			case R.id.btn_pick_photo:
				choseHeadImageFromGallery();
				break;
			default:
				break;
			}

		}

	};

	// �ӱ������ѡȡͼƬ��Ϊͷ��
	private void choseHeadImageFromGallery() {
		Intent intentFromGallery = new Intent();
		// �����ļ�����
		intentFromGallery.setType("image/*");
		intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
	}

	// �����ֻ����������Ƭ��Ϊͷ��
	private void choseHeadImageFromCameraCapture() {
		Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// �жϴ洢���Ƿ���ã��洢��Ƭ�ļ�
		if (hasSdcard()) {
			intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri
					.fromFile(new File(Environment
							.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
		}

		startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		// �û�û�н�����Ч�����ò���������
		if (resultCode == RESULT_CANCELED) {
			// Toast.makeText(getApplication(), "ȡ��", Toast.LENGTH_LONG).show();
			return;
		}

		switch (requestCode) {
		case CODE_GALLERY_REQUEST:
			cropRawPhoto(intent.getData());
			break;

		case CODE_CAMERA_REQUEST:
			if (hasSdcard()) {
				File tempFile = new File(
						Environment.getExternalStorageDirectory(),
						IMAGE_FILE_NAME);
				Bitmap bitmap = PictureUtil.getSmallBitmap(tempFile
						.getAbsolutePath());

				Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(
						getContentResolver(), bitmap, null, null));
				cropRawPhoto(uri);
				// cropRawPhoto(Uri.fromFile(tempFile));
			} else {
				Toast.makeText(getApplication(), "û��SDCard!",
						Toast.LENGTH_SHORT).show();
			}

			break;

		case CODE_RESULT_REQUEST:
			if (intent != null) {
				setImageToHeadView(intent);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/**
	 * �ü�ԭʼ��ͼƬ
	 */
	public void cropRawPhoto(Uri uri) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");

		// ���òü�
		intent.putExtra("crop", "true");

		// aspectX , aspectY :���ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX , outputY : �ü�ͼƬ����
		intent.putExtra("outputX", output_X);
		intent.putExtra("outputY", output_Y);
		intent.putExtra("return-data", true);

		startActivityForResult(intent, CODE_RESULT_REQUEST);
	}

	/**
	 * ��ȡ����ü�֮���ͼƬ���ݣ�������ͷ�񲿷ֵ�View
	 */
	private void setImageToHeadView(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			ibHead.setImageBitmap(photo);
			uploading.show();
			upload_head_pic(photo);
		}
	}

	private void upload_head_pic(Bitmap bitmap) {
		ImageUtil.uploadImg(RegisterActivity.this, bitmap,
				MyURL.UPLOAD_IMAGE_URL, handler);
	}

	/**
	 * ����豸�Ƿ����SDCard�Ĺ��߷���
	 */
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			// �д洢��SDCard
			return true;
		} else {
			return false;
		}
	}

}