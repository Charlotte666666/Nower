package com.project.enjoyit.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.project.enjoyit.global.MyApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class ImageUtil {
	private static final String TAG = "ImageUtil";
	public static final int CODE_UPLOADED_IMAGES = 0x55;
	public static final int CODE_UPLOAD_IMAGE_FAILED = 0x56;
	
    public static void uploadImg(Context context, final Bitmap bitmap, String url, final Handler handler){

        UploadApi.uploadImg(bitmap, url, new ResponseListener<String>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ͼƬ�ϴ�����"+error.toString());
                handler.obtainMessage(CODE_UPLOAD_IMAGE_FAILED, "ͼƬ�ϴ�ʧ��").sendToTarget();
            }
            @Override
            public void onResponse(String response) {
                try {
//                	if(bitmap.isRecycled()==false) //���û�л���         
//                        bitmap.recycle();
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					Log.e(TAG, jsonObject.getString("filenames"));
					Message msg = new Message();
					msg.what = CODE_UPLOADED_IMAGES;
					msg.obj = jsonObject.getString("filenames");
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
					handler.obtainMessage(CODE_UPLOAD_IMAGE_FAILED, "ͼƬ�ϴ�ʧ��").sendToTarget();
				}
                
            }
        }) ;
    }
    public static void uploadImgs(Context context, ArrayList<Bitmap> bitmaps, String url, final Handler handler){
    	if (bitmaps.size() == 0){
    		Message msg = new Message();
			msg.what = CODE_UPLOADED_IMAGES;
			msg.obj = "";
			handler.sendMessage(msg);
			return;
    	}
        UploadApi.uploadImgs(bitmaps, url, new ResponseListener<String>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ͼƬ�ϴ�����"+error.toString());
            }
            @Override
            public void onResponse(String response) {
            	try {
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					Log.e(TAG, jsonObject.getString("filenames"));
					Message msg = new Message();
					msg.what = CODE_UPLOADED_IMAGES;
					msg.obj = jsonObject.getString("filenames");
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
        }) ;
    }
}
class UploadApi {

    /**
     * �ϴ�ͼƬ�ӿ�
     * @param bitmap ��Ҫ�ϴ���ͼƬ
     * @param listener ����ص�
     */
    public static void uploadImg(Bitmap bitmap, String url, ResponseListener listener){
        List<FormImage> imageList = new ArrayList<FormImage>() ;
        imageList.add(new FormImage(bitmap)) ;
        Request request = new PostUploadRequest(url,imageList,listener) ;
        MyApplication.getMyVolley().getRequestQueue().add(request) ;
    }
    public static void uploadImgs(ArrayList<Bitmap> bitmaps, String url, ResponseListener listener){
        List<FormImage> imageList = new ArrayList<FormImage>() ;
        for (Bitmap bitmap : bitmaps){
        	imageList.add(new FormImage(bitmap));
        }
        Request request = new PostUploadRequest(url,imageList,listener) ;
        MyApplication.getMyVolley().getRequestQueue().add(request) ;
    }
}
class FormImage {
    //����������
    private String mName ;
    //�ļ���
    private String mFileName ;
    //�ļ��� mime����Ҫ�����ĵ���ѯ
    private String mMime ;
    //��Ҫ�ϴ���ͼƬ��Դ����Ϊ�������Ϊ�˷��������ֱ�Ӱ� bigmap ����������������Ŀ��һ�㲻������������ǰ�ͼƬ��·�����������������ͼƬ���ж�����ת��
    private Bitmap mBitmap;

    public FormImage(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public String getName() {
//        return mName;
//���ԣ��Ѳ�������д��
        return "file[]" ;
    }

    public String getFileName() {
    //���ԣ�ֱ��д���ļ�������
        return "test.png";
    }
    //��ͼƬ���ж�����ת��
    public byte[] getValue() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        mBitmap.compress(Bitmap.CompressFormat.JPEG,80,bos) ;
        return bos.toByteArray();
    }
    //��Ϊ��֪���� png �ļ�������ֱ�Ӹ����ĵ����
    public String getMime() {
        return "image/*";
    }
}
interface ResponseListener<T> extends Response.ErrorListener,Response.Listener<T> {

}
class PostUploadRequest extends Request<String> {

    /**
     * ��ȷ���ݵ�ʱ��ص���
     */
    private ResponseListener mListener ;
    /*���� ����ͨ����������ʽ����*/
    private List<FormImage> mListItem ;

    private String BOUNDARY = "=----WebKitFormBoundaryBYN5DeOb2fxyG5wa"; //���ݷָ���
    private String MULTIPART_FORM_DATA = "multipart/form-data";

    public PostUploadRequest(String url, List<FormImage> listItem, ResponseListener listener) {
        super(Method.POST, url, listener);
        this.mListener = listener ;
        setShouldCache(false);
        mListItem = listItem ;
        //�����������Ӧ�¼�����Ϊ�ļ��ϴ���Ҫ�ϳ���ʱ�䣬����������Ӵ��ˣ���Ϊ5��
        setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /**
     * ���￪ʼ��������
     * @param response Response from the network
     * @return
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String mString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(mString,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * �ص���ȷ������
     * @param response The parsed response returned by
     */
    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mListItem == null||mListItem.size() == 0){
            return super.getBody() ;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        int N = mListItem.size() ;
        FormImage formImage ;
        for (int i = 0; i < N ;i++){
            formImage = mListItem.get(i) ;
            StringBuffer sb= new StringBuffer() ;
            /*��һ��*/
            //`"--" + BOUNDARY + "\r\n"`
            sb.append("--"+BOUNDARY);
            sb.append("\r\n") ;
            /*�ڶ���*/
            //Content-Disposition: form-data; name="����������"; filename="�ϴ����ļ���" + "\r\n"
            sb.append("Content-Disposition: form-data;");
            sb.append(" name=\"");
            sb.append(formImage.getName()) ;
            sb.append("\"") ;
            sb.append("; filename=\"") ;
            sb.append(formImage.getFileName()) ;
            sb.append("\"");
            sb.append("\r\n") ;
            /*������*/
            //Content-Type: �ļ��� mime ���� + "\r\n"
            sb.append("Content-Type: ");
            sb.append(formImage.getMime()) ;
            sb.append("\r\n") ;
            /*������*/
            //"\r\n"
            sb.append("\r\n") ;
            try {
                bos.write(sb.toString().getBytes("utf-8"));
                /*������*/
                //�ļ��Ķ��������� + "\r\n"
                bos.write(formImage.getValue());
                bos.write("\r\n".getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        /*��β��*/
        //`"--" + BOUNDARY + "--" + "\r\n"`
        String endLine = "--" + BOUNDARY + "--" + "\r\n" ;
        try {
            bos.write(endLine.toString().getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }
    //Content-Type: multipart/form-data; boundary=----------8888888888888
    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary="+BOUNDARY;
    }
}