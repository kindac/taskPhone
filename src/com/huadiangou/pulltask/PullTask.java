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

import org.json.JSONException;
import org.json.JSONObject;

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
		TaskParams taskParams;
		try {
			taskParams = new TaskParams(Common.getInstance().getPullTaskAddr(), 8, 0);
			new PullTaskJsonTask().execute(taskParams);

		} catch (ParamsInvaliedException e) {
			e.printStackTrace();
		}
	}

	public interface UpdateUI {
		public void updateUI(String[] packgeNames);

		public void sendMessage(Message msg);
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
			HttpURLConnection conn = null;
			BufferedReader reader = null;
			OutputStream out = null;
			try {
				conn = (HttpURLConnection) new URL(taskParams.getUrl()).openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				out = conn.getOutputStream();
				out.write(encodeParams.getBytes());
				out.flush();
				out.close();

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
				msg.what = 1;
				updateUIInterface.sendMessage(msg);
			}
			for(int i = 0; i < size; i++){
			}

		}
	}

	public int extracTaskFromJsonObject(JSONObject taskJSONObject) {
		try {
			if (taskJSONObject != null) {
				task = new Task(taskJSONObject);
				return task.getTaskSize();
			}
		} catch (TaskIsZeroException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private class DownloadFileTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (params == null || params.length < 2) {
				return null;
			}
			String url = params[0];
			String saveName = params[1];
			String saveToDir = params[2];

			String sdCard = Common.getInstance().getSdCard();
			if (sdCard == null) {
				return null;
			}
			File downloadDir = new File(sdCard, saveToDir);
			if (downloadDir.isFile()) {
				downloadDir.delete();
			}
			if (!downloadDir.exists()) {
				downloadDir.mkdir();
				if (!downloadDir.canWrite()) {
					Message msg = Message.obtain();
					msg.what = MsgWhat.SDCARD_ERR;
					msg.obj = "Sdcard is not writable";
					updateUIInterface.sendMessage(msg);
					return null;
				}
			}
			File saveFile = new File(downloadDir, saveName);

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
			return null;
		}

	}

}
