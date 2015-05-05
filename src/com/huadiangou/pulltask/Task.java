package com.huadiangou.pulltask;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Task {

	private String RESULT = null;
	private String USER_PROP = null;
	private String USER_CONFIG = null;
	private List<RealSingleTask> realTaskList = null;

	public Task(JSONObject jb) throws JSONException, TaskIsZeroException {
		RESULT = jb.getString("RESULT");
		USER_PROP = jb.getString("USER_PROP");
		USER_CONFIG = jb.getString("USER_CONFIG");
		JSONArray taskJSONArray = jb.getJSONArray("TASK");
		if (taskJSONArray.length() == 0) {
			throw new TaskIsZeroException("Task List is 0");
		}
		for (int i = 0; i < taskJSONArray.length(); i++) {
			JSONObject j = taskJSONArray.getJSONObject(i);
			realTaskList.add(new RealSingleTask(j));
		}
	}

	public class RealSingleTask {
		public String ID;
		public String APK;
		public String APK_MD5;
		public String SHELL;
		public String SHELL_MD5;
		public String DATA;
		public String DATA_MD5;
		
		public RealSingleTask(JSONObject jb) throws JSONException {
			ID = jb.getString("ID");
			APK = jb.optString("APK");
			APK_MD5 = jb.optString("APK_MD5");
			SHELL = jb.optString("SHELL");
			SHELL_MD5 = jb.optString("SHELL_MD5");
			DATA = jb.optString("DATA");
			DATA_MD5 = jb.optString("DATA_MD5");
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
	
	public List<RealSingleTask> getRealSingleTasks(){
		return realTaskList;
	}

}
