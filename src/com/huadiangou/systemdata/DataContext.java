package com.huadiangou.systemdata;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.huadiangou.utils.Exec;

/**
 * @desc
 * 一些共有数据
 * 
 * @create_time 2014年9月15日
 * @version 1.0.0
 */
@SuppressLint("SdCardPath")
public final class DataContext {
	
	private static final String TAG = DataContext.class.getName();

	/**
	 * middle 服务器下载数据的存放路径
	 */
	public static final String DATASAVEPATH = "/mnt/sdcard/mData/"; 
	
	/**
	 * middle 服务器下载数据的文件以及路径名称
	 */
	public static final String DATAFILENAME = "/mnt/sdcard/mData/data.zip";
	
	/**
	 * 联系人
	 */
	public static final String CONTACT = "tmpdata/contact";
	
	/**
	 * 通话记录
	 */
	public static final String CALLLOG = "tmpdata/calllog";
	
	/**
	 * 短信
	 */
	public static final String SMS = "tmpdata/sms";
	
	/**
	 * 音乐
	 */
	public static final String MUSIC = "tmpdata/music";
	
	/**
	 * 图片
	 */
	public static final String PICTURE = "tmpdata/picture";
	
	/**
	 * 常用程序
	 */
	public static final String APK = "tmpdata/apk";
	
	
	/**
	 * 
	 */
	public static final int BUFFSIZE = 2048;// 
	
	
	/**
	 * 模板数据获取路径后缀
	 */
	public static final String DATAADDRESS = "/IF/Tools/tmpdata.aspx";

	/**
	 * middle 链接地址的存放路径
	 */
	public static final String MIDDLURLPATH = "/mnt/sdcard/main/middleurl.ini";
	public static final String MAINPATH = "/mnt/sdcard";
	/**
	 * CPUId 
	 */
	public static final String CPU_ID = getCpuId();
	
	/**
	 * 获取当前机器的CPUID
	 * @return
	 */
	private static String getCpuId() {
		String cpuId = null;
		cpuId = Exec.exec("/system/xbin/bash","-c","/system/xbin/busybox cat /proc/cpuinfo | busybox grep Serial| busybox sed 's/.*: //g'");
		return cpuId;
	}
	
	/**
	 * middle的连接
	 */
	public static String middlUrl = null;
	
	/**
	 * 
	 */
	public static final void readMiddleUrl(){
		try {
			BufferedReader  reader = new BufferedReader(new FileReader(new File(MIDDLURLPATH)));
			middlUrl = reader.readLine();
			reader.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 从对应的路径中读出相应的数据集合，由于Android 中没有内省所以就只有暂时这个样子了。
	 * @param clazz
	 * @param catPath
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static final <DATA> List<DATA> readData(Class<DATA> clazz,String catPath){
		List<DATA> data = new ArrayList<DATA>();
		String path = new StringBuilder(DATASAVEPATH).append(catPath).toString();
		Log.d(DataContext.class.getName(), new StringBuilder("数据装载，路径：").append(path).append(",类型：").append(clazz.getName()).toString());
		try {
			File folder = new File(path);
			if(folder.exists()&&folder.isDirectory()){
				File [] files =  folder.listFiles();
				for (File file : files) {
					Properties p = new Properties();
					BufferedReader reader = new BufferedReader(new FileReader(file));
					p.load(reader);
					reader.close();
					DATA obj = clazz.newInstance();
					Field [] fields = clazz.getDeclaredFields();
					for (Field field : fields) {
						String filedName = field.getName().trim();
						String value = p.getProperty(filedName);
						String methodName = new StringBuilder("set").append(filedName.substring(0, 1).toUpperCase()).append(filedName.substring(1)).toString();
						Method setter = clazz.getDeclaredMethod(methodName,field.getType());
						if(null!=setter){
							// 由于这里只有用到int 和  long 类型。所以暂时只是转换这两种类型。
							if(field.getType()==int.class){
								if(null!=value&&!"".equals(value)){
									setter.invoke(obj, Integer.valueOf(value));
								}
							}else if(field.getType()==long.class){
								if(null!=value&&!"".equals(value)){
									setter.invoke(obj, Long.valueOf(value));
								}
							}else{
								setter.invoke(obj, value);
							}
						}
					}
					
					data.add(obj);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * 向系统中写入通讯录数据
	 * @param contacts
	 */
	@SuppressWarnings("unused")
	public static void writeContact(List<Contact> contacts,Context ctx){
		Log.d(DataContext.class.getName(), "开始写入通讯录");
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int inserIndex = 0;
		for (Contact contact : contacts) {
			Log.d(DataContext.class.getName(), contact.getFirst_name()+contact.getLast_name()+","+contact.getMobile());
			inserIndex = ops.size();
			 Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
		        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
		        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
		        builder.withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
		                ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED);
			ops.add(builder.build());
			// name
	        contact.setFirst_name(null==contact.getFirst_name()?"":contact.getFirst_name());
	        contact.setLast_name(null==contact.getLast_name()?"":contact.getLast_name());
	        builder = ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
	        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, inserIndex);
	        builder.withValue(ContactsContract.Data.MIMETYPE,
	                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
	        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.getFirst_name());
	        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getLast_name());
	        ops.add(builder.build());
	     // phone
	        if (contact.getMobile() != null) {
	            builder = ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
	            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, inserIndex);
	            builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
	            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getMobile());
	            builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
	                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
	            ops.add(builder.build());
	        }
	        if (contact.getWork() != null) {
	            builder = ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
	            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, inserIndex);
	            builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
	            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getWork());
	            builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
	                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
	            ops.add(builder.build());
	        }
	     // company
	        if (contact.getCompany() != null) {
	            builder = ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
	            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, inserIndex);
	            builder.withValue(ContactsContract.Data.MIMETYPE,
	                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
	            builder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.getCompany());
	            builder.withValue(ContactsContract.CommonDataKinds.Organization.TITLE, "");
	            builder.withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
	                    ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
	            ops.add(builder.build());
	        }
		}
		if(null!=ops&&ops.size()>0){
			try {
				ContentProviderResult [] result = ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 向系统中写入通话记录数据
	 * @param callLogs
	 */
	@SuppressWarnings("unused")
	public static void writeCallLog(List<DataCallLog> callLogs,Context ctx){
		Log.d(DataContext.class.getName(), "开始写入通话记录");
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentValues values = new ContentValues();
		for (DataCallLog call : callLogs) {
			Log.d(DataContext.class.getName(), call.getCached_name()+","+call.getNumber());
			values.clear();
			values.put(CallLog.Calls.CACHED_NAME, call.getCached_name());
		        values.put(CallLog.Calls.NUMBER, call.getNumber());
		        values.put(CallLog.Calls.TYPE, call.getType());
		        values.put(CallLog.Calls.DATE, call.getDate());
		        values.put(CallLog.Calls.DURATION, call.getDuration());
		        values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
		        ops.add(ContentProviderOperation
		                .newInsert(CallLog.Calls.CONTENT_URI).withValues(values)
		                .withYieldAllowed(true).build());
		}
		if(null!=ops&&ops.size()>0){
			// TODO 此处由于不支持暂时屏蔽掉！
			boolean support = false;
			if(!support){
				Log.d(DataContext.class.getName(), "系统不支持通话记录写入!!!!");
				return;
			}
			try {
				ContentProviderResult [] result = ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 想系统中写入短信数据
	 * @param sms
	 */
	public static void writeSms(List<SMS> smses,Context ctx){
		Log.d(DataContext.class.getName(), "开始写入短信");
		ContentValues values = new ContentValues();
		ContentResolver resolver = ctx.getContentResolver();
		for (SMS sms : smses) {
			Log.d(DataContext.class.getName(), sms.getAddress()+","+sms.getBody());
			values.clear();
	        values.put("address", sms.getAddress());
	        values.put("body", sms.getBody());
	        values.put("date", sms.getDate());
	        values.put("type", sms.getType());
	        values.put("read", "1");// "1"means has read ,1表示已读
	        resolver.insert(Uri.parse("content://sms"), values);
		}
	}
	
	/**
	 * 媒体文件的拷贝，音乐，图片
	 * @param catPath
	 */
	private static void cpMedia(String catPath){
		String path = new StringBuilder(DATASAVEPATH).append(catPath).toString();
		String savePath = null;
		if(catPath==MUSIC){
			savePath = "/mnt/sdcard/Music/";
		}else if(catPath == PICTURE){
			savePath = "/mnt/sdcard/Pictures/";
		}
		File f = new File(path);
		try {
			if(f.exists()&&f.isDirectory()){
				File [] files = f.listFiles();
				for (File file : files) {
					Log.d(DataContext.class.getName(), file.getName());
					FileInputStream fin = new FileInputStream(file);
					FileChannel infc = fin.getChannel();
					FileOutputStream fout = new FileOutputStream(new File(new StringBuilder(savePath).append(file.getName()).toString()));
					FileChannel outfc = fout.getChannel();
					outfc.transferFrom(infc, 0, infc.size());
					infc.close();
					fin.close();
					outfc.close();
					fout.flush();
					fout.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝音乐
	 */
	public static void copyMusic() {
		Log.d(DataContext.class.getName(), "开始写入音乐");
		cpMedia(MUSIC);
	}

	/**
	 * 拷贝图片
	 */
	public static void copyPicture() {
		Log.d(DataContext.class.getName(), "开始写入图片");
		cpMedia(PICTURE);
	}

	/**
	 * 程序安装
	 * @param ctx
	 */
	public static void installApp(Context ctx) {
		Log.d(DataContext.class.getName(), "开始安装应用");
		String path = new StringBuilder(DATASAVEPATH).append(APK).toString();
		Log.d(DataContext.class.getName(), "应用路径:"+path);
		File f = new File(path);
		if(f.exists()&&f.isDirectory()){
			File [] files = f.listFiles();
			for (File file : files) {
				Log.d(DataContext.class.getName(), "文件:"+file.getName());
				if(!file.getName().endsWith(".apk")){
					continue;
				}
				String args [] = {"pm","install","-r",file.getPath()};
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				Process process = null;  
				InputStream errIs = null;  
				InputStream inIs = null;  
				try {  
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				    int read = -1;  
				    process = processBuilder.start();  
				    errIs = process.getErrorStream();  
				    while ((read = errIs.read()) != -1) {  
				        baos.write(read);  
				    }  
				    baos.write('\n');  
				    inIs = process.getInputStream();  
				    while ((read = inIs.read()) != -1) {  
				        baos.write(read);  
				    }  
				    byte[] data = baos.toByteArray();  
				    Log.d(DataContext.class.getName(), new String(data));
				} catch (IOException e) {  
				    e.printStackTrace();  
				} catch (Exception e) {  
				    e.printStackTrace();  
				} finally {  
				    try {  
				        if (errIs != null) {  
				            errIs.close();  
				        }  
				        if (inIs != null) {  
				            inIs.close();  
				        }  
				    } catch (IOException e) {  
				        e.printStackTrace();  
				    }  
				    if (process != null) {  
				        process.destroy();  
				    }  
				}  
				Log.d(DataContext.class.getName(), file.getName()+"安装结束！");
			}
		}
	}
	
	/**
	 * 卸载现有apk 程序
	 * @param ctx
	 */
	public static void extractApp(Context ctx){
		Log.d(DataContext.class.getName(), "开始卸载应用");
		String path = new StringBuilder(DATASAVEPATH).append(APK).toString();
		File f = new File(path);
		PackageManager manager = ctx.getPackageManager();
		if(f.exists()&&f.isDirectory()){
			File [] files = f.listFiles();
			for (File file : files) {
				if(!file.getName().endsWith(".apk")){
					continue;
				}
				PackageInfo info = manager.getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
				//String name = new StringBuilder("package:").append(info.packageName).toString();
				String args [] = {"pm","uninstall",info.packageName};
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				Process process = null;  
				InputStream errIs = null;  
				InputStream inIs = null;  
				try {  
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				    int read = -1;  
				    process = processBuilder.start();  
				    errIs = process.getErrorStream();  
				    while ((read = errIs.read()) != -1) {  
				        baos.write(read);  
				    }  
				    baos.write('\n');  
				    inIs = process.getInputStream();  
				    while ((read = inIs.read()) != -1) {  
				        baos.write(read);  
				    }  
				    byte[] data = baos.toByteArray();  
				    Log.d(DataContext.class.getName(), new String(data));
				} catch (IOException e) {  
				    e.printStackTrace();  
				} catch (Exception e) {  
				    e.printStackTrace();  
				} finally {  
				    try {  
				        if (errIs != null) {  
				            errIs.close();  
				        }  
				        if (inIs != null) {  
				            inIs.close();  
				        }  
				    } catch (IOException e) {  
				        e.printStackTrace();  
				    }  
				    if (process != null) {  
				        process.destroy();  
				    }  
				}
				
				file.delete();
				Log.d(DataContext.class.getName(), info.packageName+"["+file.getPath()+"]卸载完成！！");
			}
		}
	}
	
	/**
	 * 删除媒体文件
	 */
	public static void extarctMedia(){
		Log.d(DataContext.class.getName(), "开始删除音乐和图片");
		String [] paths = {MUSIC,PICTURE};
		for (String catPath : paths) {
			String path = new StringBuilder(DATASAVEPATH).append(catPath).toString();
			String savePath = null;
			if(catPath==MUSIC){
				savePath = "/mnt/sdcard/Music/";
			}else if(catPath == PICTURE){
				savePath = "/mnt/sdcard/Pictures/";
			}
			File f = new File(path);
			Log.d(DataContext.class.getName(), "清理目录："+f.getPath());
			if(f.exists()&&f.isDirectory()){
				File [] files = f.listFiles();
				for (File file : files) {
					String mediaPath = new StringBuilder(savePath).append(file.getName()).toString();
					new File(mediaPath).delete();
					Log.d(DataContext.class.getName(), "删除文件:"+mediaPath);
					file.delete();
					Log.d(DataContext.class.getName(), "删除文件:"+file.getPath());
				}
			}
		}
	}
	
	/**
	 * 单纯的删除文件
	 * @param catPath
	 */
	private static void extractData(String catPath){
		String path = new StringBuilder(DATASAVEPATH).append(catPath).toString();
		File f = new File(path);
		Log.d(DataContext.class.getName(), "清理目录："+f.getPath());
		if(f.exists()&&f.isDirectory()){
			File [] files = f.listFiles();
			for (File file : files) {
				file.delete();
				Log.d(DataContext.class.getName(), "删除文件:"+file.getPath());
			}
		}
	}
	
	/**
	 * 清空联系人数据
	 */
	public static void extractConcat(Context cxt){
		Log.d(DataContext.class.getName(), "清空联系人！");
		extractData(CONTACT);
		Cursor cur = cxt.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);
		try {
			if(cur.getCount()>0){
				cur.moveToFirst();
				while(cur.moveToNext()){
					long rawID = cur.getLong(cur.getColumnIndex(ContactsContract.Data._ID));
					cxt.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
			                ContactsContract.Data._ID + "=?", new String[]{String.valueOf(rawID)});
					Log.d(DataContext.class.getName(), "联系人ID["+rawID+"]！");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cur.close();
		}
	}
	/**
	 * 清空通话记录
	 */
	public static void extractCallLog(Context cxt){
		Log.d(DataContext.class.getName(), "清空通话记录！");
		extractData(CALLLOG);
		Cursor cur = cxt.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,null);
		try {
			if(cur.getCount()>0){
				cur.moveToFirst();
				while(cur.moveToNext()){
					long rawID = cur.getLong(cur.getColumnIndex(CallLog.Calls._ID));
					cxt.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
			                CallLog.Calls._ID + "=?" ,new String[]{String.valueOf(rawID)});
					Log.d(DataContext.class.getName(), "通话记录ID["+rawID+"]！");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cur.close();
		}
	}
	/**
	 * 清空短信
	 */
	public static void extarctSMS(Context cxt){
		Log.d(DataContext.class.getName(), "清空短信！");
		extractData(SMS);
		Cursor cur = cxt.getContentResolver().query(Uri.parse("content://sms"), null, null, null,null);
		try {
			if(cur.getCount()>0){
				Log.d(DataContext.class.getName(), "短信条数是:"+cur.getCount());
				cur.moveToFirst();
				while(cur.moveToNext()){
					long rawID = cur.getLong(cur.getColumnIndex("_id"));
					cxt.getContentResolver().delete(Uri.parse("content://sms"),  
	                        "_id=?", new String[]{String.valueOf(rawID)}); 
					Log.d(DataContext.class.getName(), "短信ID["+rawID+"]！");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cur.close();
		}
	}
	
	/**
	 * 从连接中请求JSON对象
	 * @param connUrl
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public static JSONObject getJSONForUrl(CharSequence connUrl) throws IOException,
			MalformedURLException, JSONException {
		String str = getStringForUrl(connUrl);
		JSONObject middleips = new JSONObject(str);
		return middleips;
	}

	/**
	 * 从连接中请求字符串
	 * @param connUrl
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static String getStringForUrl(CharSequence connUrl)
			throws IOException, MalformedURLException {
		HttpURLConnection conn = null;
		URLConnection urlconn = new URL(connUrl.toString()).openConnection();
		if(urlconn instanceof HttpURLConnection){
			conn = (HttpURLConnection)urlconn;
		}
		conn.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line);
		}
		Log.d(TAG,sb.toString());
		reader.close();
		conn.disconnect();
		return sb.toString();
	}
	
	public static <T> T getHiddeMethodVal(Class<T> clazzOfT, Object obj,String methodName,Object... args) {
		T ival = null;
		try {
			
			Method method = obj.getClass().getDeclaredMethod(methodName);
			Object val = method.invoke(obj,args);
			
			if(clazzOfT==Integer.class){
				ival = clazzOfT.cast(Integer.valueOf(String.valueOf(val)));
			}else if(clazzOfT==String.class){
				ival = clazzOfT.cast(String.valueOf(val));
			}else if(clazzOfT==Boolean.class){
				ival = clazzOfT.cast(Boolean.valueOf(String.valueOf(val)));
			}else{
				ival = clazzOfT.cast(val);
			}
			
		} catch (Exception e) {e.printStackTrace();}
		return ival;
	}
}
