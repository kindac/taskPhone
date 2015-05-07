package com.huadiangou.pulltask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.huadiangou.pulltask.Task.TaskIsZeroException;
import com.huadiangou.pulltask.TaskParams.ParamsInvaliedException;

public class PullTask {
	private static PullTask pullTask = null;
	private UpdateUI updateUIInterface;
	private Task task;

	private PullTask() {
	}

	private PullTask(UpdateUI updateUIInterface) {
		this.updateUIInterface = updateUIInterface;
	}

	public static PullTask getInstance(UpdateUI updateUIInterface) {
		if (pullTask == null) {
			pullTask = new PullTask(updateUIInterface);
		}

		return pullTask;
	}

	public void pullTask() {
		//clearLastTask();

		TaskParams taskParams;
		try {
			taskParams = new TaskParams(Common.getInstance().getPullTaskAddr(), 800, 480, "070457c801902417668", 1, 0);
			new PullTaskJsonTask().execute(taskParams);

		} catch (ParamsInvaliedException e) {
			e.printStackTrace();
		}
	}

	public interface UpdateUI {
		public void updateUI(String[] packgeNames);

		public void sendMessage(Message msg);

		public Context getContext();
	}

	private class PullTaskJsonTask extends AsyncTask<TaskParams, Void, JSONObject> {
		private final String TAG = PullTaskJsonTask.class.getCanonicalName();

		@Override
		protected JSONObject doInBackground(TaskParams... params) {
			TaskParams taskParams;
			String encodeParams;
			if (params.length == 0) {
				return null;
			} else {
				taskParams = params[0];
				encodeParams = taskParams.getEncodeParams();
				if (encodeParams == null) {
					return null;
				}
			}
			String url = taskParams.getUrl() + "?" + taskParams.getEncodeParams();
			HttpURLConnection conn = null;
			BufferedReader reader = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.addRequestProperty("Connection", "Keep-Alive");

				int code = conn.getResponseCode();
				if (code != HttpURLConnection.HTTP_OK) {
					Log.e(TAG, "Http Connect Error, respondCode : " + code);
					return null;
				}

				reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				return new JSONObject(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject jb) {
			int size = extracTaskFromJsonObject(jb);
			if (size == 0) {
				Message msg = Message.obtain();
				msg.obj = "Task Size is " + size;
				msg.what = MsgWhat.RESET;
				updateUIInterface.sendMessage(msg);
				return;
			}

			final Set<Integer> fileSet = task.fileMap.keySet();
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (Integer i : fileSet) {
						List<String> f = task.fileMap.get(i);
						String[] fileList = new String[f.size()];
						f.toArray(fileList);
						downloadFile(fileList);
					}
					Message msg = Message.obtain();
					msg.what = MsgWhat.SET_PROP;
					updateUIInterface.sendMessage(msg);
				}
			}).start();

			for (Task.RealSingleTask rst : task.realTaskList) {
				new DownloadFileTask().execute(rst);
			}
		}
	}

	public int extracTaskFromJsonObject(JSONObject taskJSONObject) {
		try {
			if (taskJSONObject != null) {
				task = new Task(taskJSONObject);
				ListViewData.task = task;
				return task.getTaskSize();
			}
		} catch (TaskIsZeroException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private class DownloadFileTask extends AsyncTask<Task.RealSingleTask, Void, Boolean> {
		private Task.RealSingleTask realSingleTask;

		@Override
		protected Boolean doInBackground(Task.RealSingleTask... params) {
			if (params == null || params.length == 0) {
				return Boolean.FALSE;
			}
			Task.RealSingleTask rst = params[0];
			realSingleTask = rst;
			int tryTimes = 0;
			int i, n = 0;
			for (i = 0; i < rst.fileList.size(); i++) {
				List<String> list = rst.fileList.get(i);
				String[] fileList = new String[list.size()];
				list.toArray(fileList);
				boolean b = downloadFile(fileList);
				if (b) {
					n++;
				} else if (++tryTimes <= 3) {
					i--;
				} else {
					tryTimes = 0;
				}
			}
			rst.DOWNLOAD_FINISHED = (n == rst.fileList.size());
			return Boolean.valueOf(rst.DOWNLOAD_FINISHED);
		}

		@Override
		protected void onPostExecute(Boolean b) {
			Message msg = Message.obtain();
			if (b.booleanValue()) {
				msg.what = MsgWhat.INSTALL_APK;
				msg.obj = realSingleTask;
			} else {
				msg.what = MsgWhat.DOWNLOAD_TASK_FAILED;
				// TODO
				msg.obj = "[Failed] download " + realSingleTask.APK;
			}
			updateUIInterface.sendMessage(msg);
		}
	}

	private boolean downloadFile(String... params) {
		boolean download_success = false;

		if (params == null || params.length < 2) {
			return Boolean.FALSE;
		}
		String url = params[0];
		String saveName = params[1];
		String saveToDir = params[2];
		String md5sum = null;
		if (params.length > 3) {
			md5sum = params[3];
		}

		String sdCard = Common.getInstance().getSdCard();
		if (sdCard == null) {
			return Boolean.FALSE;
		}
		File downloadDir = new File(sdCard, saveToDir);
		if (downloadDir.isFile()) {
			downloadDir.delete();
		}
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
			if (!downloadDir.canWrite()) {
				Message msg = Message.obtain();
				msg.what = MsgWhat.SDCARD_ERR;
				msg.obj = "Sdcard is not writable";
				updateUIInterface.sendMessage(msg);
				return Boolean.FALSE;
			}
		}
		File saveFile = new File(downloadDir, saveName);
		if (md5sum != null && saveFile.exists()) {
			if (checkMd5sum(saveFile, md5sum))
				return true;
		}

		HttpURLConnection conn = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			in = conn.getInputStream();
			out = new FileOutputStream(saveFile);
			byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer, 0, buffer.length)) > 0) {
				out.write(buffer, 0, n);
			}
			out.flush();

			if (md5sum != null) {
				download_success = checkMd5sum(saveFile, md5sum);
			} else {
				download_success = true;
			}
			return download_success;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (conn != null)
					conn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean checkMd5sum(File saveFile, String md5sum) {

		return true;
	}
	
	private void clearLastTask(){
		ListViewData.colorMap.clear();
		ListViewData.iconMap.clear();
		ListViewData.installAPKCount = 0;
		ListViewData.list.clear();
		ListViewData.task = null;
		ListViewData.uploadCount = 0;
	}

}
