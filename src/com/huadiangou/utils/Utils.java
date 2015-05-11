package com.huadiangou.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huadiangou.pulltask.ApkInfo;

public class Utils {
	private static final String TAG = "com.huadiangou.utils.Utils";

	public static String getPackage(Context cxt, String filePath) {

		String packageName = null;
		PackageManager pm = cxt.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			packageName = appInfo.packageName; // 得到安装包名称
			// String appName = pm.getApplicationLabel(appInfo).toString();
			// String version=info.versionName; //得到版本信息
		}
		return packageName;
	}

	public static boolean tarUserDate(Context cxt, String packName) {

		File cacheDir = cxt.getCacheDir();
		String filebase = cacheDir.getParentFile().getParent() + File.separator + packName;
		String outPath = cacheDir.getAbsolutePath() + File.separator + packName + ".tar.gz ";
		String authority = "busybox tar zcvf " + outPath + filebase;
		return Exec.run(true, authority);
	}

	public static ApkInfo getIcon(Context cxt, String packageName) {
		PackageManager pm = cxt.getPackageManager();
		List<PackageInfo> apkInfos = pm.getInstalledPackages(0);
		String name = "";
		String lable = "";
		Drawable icon;
		PackageInfo apk;
		for (int i = 0; i < apkInfos.size(); i++) {
			apk = apkInfos.get(i);
			name = apk.packageName;
			if (name.contains(packageName)) {
				icon = pm.getApplicationIcon(apk.applicationInfo);
				lable = (String) pm.getApplicationLabel(apk.applicationInfo);
				return new ApkInfo(lable, icon);
			}
		}
		return null;
	}

	public static boolean checkWhetherInstlled(Context cxt, String packageName) {
		PackageManager pm = cxt.getPackageManager();
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_GIDS);
			return true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		/*
		List<PackageInfo> apkInfos = pm.getInstalledPackages(0);
		String name = "";
		PackageInfo apk;
		for (int i = 0; i < apkInfos.size(); i++) {
			apk = apkInfos.get(i);
			name = apk.packageName;
			if (name.contains(packageName)) {
				return true;
			}
		}
		*/
		return false;
	}

	public static boolean createUserdataTarGz(Context context, String packageName, String savePath) {
		if(context == null || packageName == null || savePath == null){
			return false;
		}
		File cacheDir = context.getApplicationContext().getCacheDir();
		String path = cacheDir.getAbsolutePath() + "/" + savePath;
		new File(path).mkdirs();
		String cmd = "busybox tar zcvf " + path + "/" + packageName + ".tar.gz" + " /data/data/" + packageName;
		boolean b = Exec.run(true, cmd);
		if (b) {
			Log.d(TAG, "package " + packageName + " success");
		} else {
			Log.d(TAG, "package " + packageName + " falied");
		}
		return b;
	}
	
	public static String getMD5(File file) {
		FileInputStream fis = null;
		MessageDigest digest = null;
		try {
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				digest.update(buffer, 0, numRead);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			int zeros = 32 - output.length();
			for (; zeros > 0; zeros--) {
				output = "0" + output;
			}
			Log.d("MD5", "File MD5:" + output);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}


}
