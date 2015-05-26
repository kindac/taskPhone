package com.huadiangou.systemdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.huadiangou.pulltask.Common;

import android.content.Context;
import android.util.Log;

public class DataService {

	private static final String TAG = DataService.class.getName();

	private static DataService dataService;
	private Context context;

	public static DataService getInstance(Context context) {
		if (dataService == null) {
			dataService = new DataService();
			dataService.context = context;
		}
		return dataService;
	}

	public void update() {
		downLoadAndExtractData();
	}

	/**
	 * 清空现有系统数据
	 */
	private void clearSysData() {
		Log.d(getClass().getName(), "-------------------清空现有数据----------------");
		DataContext.extarctSMS(context);
		DataContext.extractCallLog(context);
		DataContext.extractConcat(context);
		DataContext.extarctMedia();
		DataContext.extractApp(context);
	}

	/**
	 * 下载并且解压数据,下载完成后，执行解压和写入操作
	 */
	private void downLoadAndExtractData() {
		final String link = Common.getInstance().getUpdateSystemdataAddr();
		new Thread() {
			@Override
			public void run() {
				Log.d(DataService.this.getClass().getName(), "进入下载线程，准备下载数据！");
				try {
					File folder = new File(DataContext.DATASAVEPATH);
					if (!folder.exists()) {
						boolean create = folder.mkdirs();
						String dir_log = create ? "文件夹创建成功,开始下载文件" : "文件家创建失败程序结束";
						Log.d(DataService.this.getClass().getName(), dir_log);
						if (!create) {
							return;
						}
					}
					HttpURLConnection conn = null;
					URLConnection urlconn = new URL(link).openConnection();
					if (urlconn instanceof HttpURLConnection) {
						conn = (HttpURLConnection) urlconn;
					}
					conn.connect();
					InputStream in = conn.getInputStream();
					FileOutputStream out = new FileOutputStream(new File(DataContext.DATAFILENAME));
					byte[] buff = new byte[DataContext.BUFFSIZE];
					int len = -1;
					while ((len = in.read(buff, 0, DataContext.BUFFSIZE)) > 0) {
						out.write(buff, 0, len);
					}
					in.close();
					out.flush();
					out.close();
					Log.d(getClass().getName(), "下载完成，开始清理系统原有数据！！！");
					clearSysData();
					extract();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				WriteData();
			}

		}.start();
	}

	/**
	 * 解压下载的文件
	 */
	private void extract() {
		ZipFile zip = null;
		try {
			byte[] buff = new byte[DataContext.BUFFSIZE];
			zip = new ZipFile(new File(DataContext.DATAFILENAME));
			for (Enumeration<?> entris = zip.entries(); entris.hasMoreElements();) {
				ZipEntry zipfile = (ZipEntry) entris.nextElement();
				String path = new StringBuilder(DataContext.DATASAVEPATH).append(zipfile.getName()).toString();
				File f = new File(path);
				if (zipfile.isDirectory()) {
					if (f.exists()) {
						f.delete();
						Log.d(getClass().getName() + "-创建文件夹", f.getPath());
					}
					f.mkdirs();
				} else {
					InputStream in = zip.getInputStream(zipfile);
					if (!f.getParentFile().exists()) {
						f.getParentFile().mkdirs();// 强制建立文件夹！
						Log.d(getClass().getName() + "-创建文件夹", f.getParentFile().getPath());
					}
					FileOutputStream out = new FileOutputStream(f);
					int len = -1;
					while ((len = in.read(buff, 0, DataContext.BUFFSIZE)) > 0) {
						out.write(buff, 0, len);
					}
					in.close();
					out.flush();
					out.close();
				}
				Log.d(getClass().getName() + "-解压:", path);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (zip != null) {
					zip.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 将下载的数据写入到系统中
	 */
	private void WriteData() {
		Log.d(getClass().getName(), "开始写入逻辑，将数据写入到系统中！");
		// 联系人写入
		List<Contact> contacts = DataContext.readData(Contact.class, DataContext.CONTACT);
		DataContext.writeContact(contacts, context);
		Log.d(getClass().getName(), "联系人写入完毕！！！");
		// 通话记录写入
		List<DataCallLog> calllogs = DataContext.readData(DataCallLog.class, DataContext.CALLLOG);
		DataContext.writeCallLog(calllogs, context);
		Log.d(getClass().getName(), "通话记录写入完毕！！！");
		// 短信写入
		List<SMS> smses = DataContext.readData(SMS.class, DataContext.SMS);
		DataContext.writeSms(smses, context);
		Log.d(getClass().getName(), "短信写入完毕！！！");
		// 音乐写入
		DataContext.copyMusic();
		// 图片写入
		DataContext.copyPicture();
		// 常用程序写入
		DataContext.installApp(context);
	}

}
