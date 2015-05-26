package com.huadiangou.pulltask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CheckGoldenfingerUpdateTask extends AbstracAsyncTask {

	private Handler handler;
	private Context context;
	private int runningInstance = 0;

	private String caller = null;

	private static CheckGoldenfingerUpdateTask checkGoldenfingerUpdateTask;
	private PullTaskJsonTask pullTaskJsonTask;

	private CheckGoldenfingerUpdateTask(Context context, Handler handler, String caller) {
		this.context = context;
		this.handler = handler;
		this.caller = caller;
	}

	public static CheckGoldenfingerUpdateTask getInstance(Context context, Handler handler, String caller) {
		if (checkGoldenfingerUpdateTask == null) {
			checkGoldenfingerUpdateTask = new CheckGoldenfingerUpdateTask(context, handler, caller);
		}
		return checkGoldenfingerUpdateTask;
	}

	@Override
	public void execute() {
		synchronized (CheckGoldenfingerUpdateTask.class) {
			if (runningInstance < 1) {
				new PullTaskJsonTask().execute(Common.CHECKUPDATE_URL);
				runningInstance = 1;
			} else {
				Message msg = Message.obtain();
				msg.obj = "已经在更新，请稍后";
				msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
				handler(msg);
			}
		}
	}

	@Override
	public void cancel() {
		synchronized (CheckGoldenfingerUpdateTask.class) {
			//TODO
			checkGoldenfingerUpdateTask = null;
			runningInstance = 0;
		}
	}

	@Override
	public void remove() {

	}

	@Override
	public void add() {

	}

	@Override
	public void handler(Message msg) {
		handler.sendMessage(msg);

		Message fragmentDismiss = Message.obtain();
		if (caller != null) {
			fragmentDismiss.what = MsgWhat.FRAGMENT_DISMISS;
			fragmentDismiss.obj = caller;
			handler.sendMessage(fragmentDismiss);
		}
	}

	private class PullTaskJsonTask extends AsyncTask<String, Void, JSONObject> {
		private final String TAG = PullTaskJsonTask.class.getCanonicalName();

		@Override
		protected JSONObject doInBackground(String... params) {
			String url;
			if (params == null || params.length == 0) {
				return null;
			} else {
				url = params[0];
			}
			HttpURLConnection conn = null;
			BufferedReader reader = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.addRequestProperty("Connection", "Keep-Alive");
				conn.setConnectTimeout(10 * 1000);

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

			if (jb == null) {
				Message msg = Message.obtain();
				msg.obj = "升级请求错误，有可能是服务器错误，或网络未连接";
				msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
				handler(msg);
				return;
			}
			PackageManager pm = context.getPackageManager();
			int nowVersionCode = -1;
			try {
				PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
				nowVersionCode = pi.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				Message msg = Message.obtain();
				msg.obj = "肯定不会看见此消息";
				msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
				handler(msg);
				return;
			}
			int newVersionCode = -1;
			newVersionCode = jb.optInt("version", -1);
			if (newVersionCode <= nowVersionCode) {
				Message msg = Message.obtain();
				msg.obj = "已是最新版本";
				msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
				handler(msg);
				return;
			}
			String[] params = new String[4];
			try {
				params[0] = jb.getString("apk_url");
				params[1] = "Goldenfinger.apk";
				params[2] = "apps";
				params[3] = jb.optString("apk_md5", "");
				new DownloadTask(handler, params) {
					@Override
					public void onPostExecute(boolean b) {
						Message msg = Message.obtain();
						if (b) {
							msg.what = MsgWhat.INSTALL_GOLDENFINGER;
							String path = Common.getInstance().getSdCard() + "/apps/" + "Goldenfinger.apk";
							msg.obj = path;
						} else {
							msg.what = MsgWhat.DOWNLOAD_TASK_FAILED;
							msg.obj = "[Failed] 下载Goldenfinger失败";
						}
						handler(msg);
						// CheckUpdateFragment.this.dismiss();
					}
				}.execute();

			} catch (JSONException e) {
				e.printStackTrace();
				Message msg = Message.obtain();
				msg.obj = "服务器返回的 json 有错误：\n" + jb.toString();
				msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
				handler(msg);
				// CheckUpdateFragment.this.dismiss();
			}
			/*
			Message msg = Message.obtain();
			msg.obj = "服务器返回的 json 有错误：\n" + jb.toString();
			msg.what = MsgWhat.CHECK_UPDATE_MESSAGE;
			handler(msg);
			*/
		}
	}
}
