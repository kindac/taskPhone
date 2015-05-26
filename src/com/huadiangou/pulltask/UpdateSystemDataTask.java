package com.huadiangou.pulltask;

import com.huadiangou.systemdata.DataService;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class UpdateSystemDataTask {
	private static final String TAG = "com.huadiangou.pulltask.UpdateSystemDataTask";
	private Handler handler;
	private Context context;

	public UpdateSystemDataTask(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void update() {
		new InstallApkTask().execute();
	}

	public void realUpdate(Handler handler) {
		DataService.getInstance(context).update();
//		Message m = Message.obtain();
//		m.what = MsgWhat.FINISHED;
//		handler.sendMessage(m);
	}

	private class InstallApkTask extends AsyncTask<Void, Void, Boolean> {
		private final String TAG = InstallApkTask.class.getCanonicalName();

		@Override
		protected Boolean doInBackground(Void... params) {
			realUpdate(handler);
			return null;
		}

		@Override
		protected void onPostExecute(Boolean b) {
		}
	}

}
