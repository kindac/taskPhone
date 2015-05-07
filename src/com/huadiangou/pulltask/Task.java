package com.huadiangou.pulltask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Task {

	private String RESULT = null;
	private String USER_PROP = null;
	private String USER_CONFIG = null;
	public static String BOX_SIGNATURE = "070457c801902417668";
	public List<RealSingleTask> realTaskList = new ArrayList<RealSingleTask>();
	public Map<Integer, List<String>> fileMap = new HashMap<Integer, List<String>>();

	public Task(JSONObject jb) throws JSONException, TaskIsZeroException {
		setRESULT(jb.getString("RESULT"));
		setUSER_PROP(jb.getString("USER_PROP"));
		setUSER_CONFIG(jb.getString("USER_CONFIG"));
		JSONArray taskJSONArray = jb.getJSONArray("TASK");
		if (taskJSONArray.length() == 0) {
			throw new TaskIsZeroException("Task List is 0");
		}
		for (int i = 0; i < taskJSONArray.length(); i++) {
			JSONObject j = taskJSONArray.getJSONObject(i);
			realTaskList.add(new RealSingleTask(j));
		}

		String[] urls = USER_PROP.split("/");
		fileMap.put(0, Arrays.asList(USER_PROP,  urls[urls.length - 1], "main"));

		urls = USER_CONFIG.split("/");
		fileMap.put(1, Arrays.asList(USER_CONFIG,  urls[urls.length - 1], "scripts"));
	}

	public class RealSingleTask {
		public String ID;
		public String APK;
		public String APK_MD5;
		public String SHELL;
		public String SHELL_MD5;
		public String DATA;
		public String DATA_MD5;
		
		public String packageName;
		public int doneTimes = 0;

		public boolean DOWNLOAD_FINISHED = false;
		public List<List<String>> fileList = new ArrayList<List<String>>();

		public RealSingleTask(JSONObject jb) throws JSONException {
			ID = jb.getString("ID");
			APK = jb.optString("APK");
			APK_MD5 = jb.optString("APK_MD5");
			SHELL = jb.optString("SHELL");
			SHELL_MD5 = jb.optString("SHELL_MD5");
			DATA = jb.optString("DATA");
			DATA_MD5 = jb.optString("DATA_MD5");
			if (!APK.equals("")) {
				String[] urls = APK.split("/");
				fileList.add(Arrays.asList(APK, urls[urls.length - 1], "apps", APK_MD5));
			}
			if (!DATA.equals("")) {
				String[] urls = APK.split("/");
				fileList.add(Arrays.asList(DATA, urls[urls.length - 1], "Data/" + ID, APK_MD5));
			}

		}
	}

	public class TaskIsZeroException extends Exception {
		private static final long serialVersionUID = 4116275668074872605L;

		public TaskIsZeroException(String s) {
			super(s);
		}
	}

	public int getTaskSize() {
		if (realTaskList != null) {
			return realTaskList.size();
		}
		return 0;
	}

	public List<RealSingleTask> getRealSingleTasks() {
		return realTaskList;
	}

	public String getUSER_PROP() {
		return USER_PROP;
	}

	public void setUSER_PROP(String uSER_PROP) {
		USER_PROP = uSER_PROP;
	}

	public String getUSER_CONFIG() {
		return USER_CONFIG;
	}

	public void setUSER_CONFIG(String uSER_CONFIG) {
		USER_CONFIG = uSER_CONFIG;
	}

	public String getRESULT() {
		return RESULT;
	}

	public void setRESULT(String rESULT) {
		RESULT = rESULT;
	}

}
