package com.huadiangou.pulltask;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huadiangou.utils.Exec;
import com.huadiangou.utils.Utils;

public class InstallTask {
	private static final String TAG = "com.huadiangou.pulltask.InstallTask";
	private Handler handler;
	private Task.RealSingleTask rst;
	private Context context;
	private String packageName;

	public InstallTask(Context context, Task.RealSingleTask rst, Handler handler) {
		this.context = context;
		this.rst = rst;
		this.handler = handler;
	}

	public void install() {
		new InstallApkTask().execute(rst);
	}

	public void realInstall(Task.RealSingleTask rst, Handler handler) {
		if (!rst.DOWNLOAD_FINISHED) {
			return;
		}

		String[] urls = rst.APK.split("/");
		String path = Common.getInstance().getSdCard() + "/apps/" + urls[urls.length - 1];
		if (!(new File(path).exists())) {
			return;
		}

		String packageName = Utils.getPackage(context, path);

		boolean b = true;
		Message msg = Message.obtain();
		if (!Utils.checkWhetherInstlled(context, packageName)) {
			String cmd = "pm install -r " + path;
			b = Exec.run(true, cmd);
		}

		if (b) {
			rst.packageName = packageName;
			Utils.createUserdataTarGz(context, packageName, Common.USERDATA_ORIGINAL);

			msg.what = MsgWhat.INSTALL_SUCCESS;
			msg.obj = Utils.getPackage(context, path);
		} else {
			msg.what = MsgWhat.INSTALL_FAILED;
			msg.obj = "[Failed] install " + path;
		}
		handler.sendMessage(msg);
		ListViewData.installAPKCount += 1;
		if (ListViewData.installAPKCount == ListViewData.task.realTaskList.size() && ListViewData.SET_PROPERTY) {
			Message m = Message.obtain();
			m.what = MsgWhat.FINISHED;
			handler.sendMessage(m);
		}
	}

	private class InstallApkTask extends AsyncTask<Task.RealSingleTask, Void, Boolean> {
		private final String TAG = InstallApkTask.class.getCanonicalName();

		@Override
		protected Boolean doInBackground(Task.RealSingleTask... params) {
			if (params == null || params.length == 0) {
				return Boolean.FALSE;
			}
			Task.RealSingleTask rst = params[0];
			realInstall(rst, handler);
			return null;
		}

		@Override
		protected void onPostExecute(Boolean b) {
		}
	}

}
