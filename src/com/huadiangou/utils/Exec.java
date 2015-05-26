package com.huadiangou.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.util.Log;

public class Exec {
	
	public static boolean run(boolean isRoot, String cmd) {
		Process process = null;
		DataOutputStream os = null;
		BufferedReader stdoutBuffer = null;
		try {
			if (isRoot) {
				process = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(process.getOutputStream());
				os.writeBytes(cmd + "\n");
				os.flush();
				os.writeBytes("exit \n");
				os.flush();
			} else {
				process = Runtime.getRuntime().exec(cmd);
			}
			InputStream is = process.getInputStream();
			stdoutBuffer = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = stdoutBuffer.readLine()) != null) {
				Log.d("CMD", line);
			}
			int result = process.waitFor();

			if (result == 0) {
				return true;
			} else {
				String errStr = getString(process.getErrorStream());
				System.out.println(errStr);
				return false;
			}
		} catch (Exception e) {
			System.out.println("报错了！");
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) os.close();
				if (stdoutBuffer != null) stdoutBuffer.close();
				process.destroy();
			} catch (Exception e) {}
		}
	}

	private static String getString(InputStream is) {
		String result = "";
		try {
			byte[] buff = new byte[1024];
			int count = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((count = is.read(buff)) != -1) {
				baos.write(buff, 0, count);
			}
			result = baos.toString();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public synchronized static void killProcess(String name) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ps");
			BufferedReader br = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			ArrayList<String> proList = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.contains(name)) {
					proList.add(line);
				}
			}
			br.close();
			for(String proLine : proList) {
				String processId = "";
				StringTokenizer tokenizer = new StringTokenizer(proLine);
				int count = 0;
				while (tokenizer.hasMoreTokens()) {
					processId = tokenizer.nextToken();
					if (++count == 2) {
						break;
					}
				}
				if (!run(true, "kill -9 " + processId)) {
				} else {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static String exec(String... cmd){
		String str=null;
		Process process = null;
		DataOutputStream os = null;
		BufferedReader stdoutBuffer = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			InputStream is = process.getInputStream();
			stdoutBuffer = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = stdoutBuffer.readLine()) != null) {
				sb.append(line);
			}
			int result = process.waitFor();

			if (result == 0) {
				str = sb.toString();
			} else {
				String errStr = getString(process.getErrorStream());
				System.out.println("cmd failed, result: " + result);
				System.out.println("stderr: " + errStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) os.close();
				if (stdoutBuffer != null) stdoutBuffer.close();
				process.destroy();
			} catch (Exception e) {
				Log.d(Exec.class.getName(), e.getMessage(),e);
			}
		}
		return str;
	}
	
	public synchronized static boolean isProggressRunning(String name) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ps");
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.contains(name)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}

