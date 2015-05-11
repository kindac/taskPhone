package com.huadiangou.pulltask;

import java.io.File;

import org.w3c.dom.UserDataHandler;

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
		String msgs = "";
		Message msg = Message.obtain();
		if (!Utils.checkWhetherInstlled(context, packageName)) {
			String userDataPath = "/data/data/" + packageName;
			String cmd0 = "rm -rf " + userDataPath;
			String cmd1 = "pm install -r " + path;
			Exec.run(true, cmd0);
			b = Exec.run(true, cmd1);
			msgs = b ? "[OK]success install " + packageName : "[Failed]failed install " + packageName;
		} else if (rst.isNewUser()) {
			String cmd = "pm clear " + packageName;
			b = Exec.run(true, cmd);
			msgs = b ? "[OK]success install " + packageName : "[Failed]failed to clear " + packageName;
		} else {
			// TODO
			if (rst.userDataSaveName != null) {
				String userDataPath = (Common.sdCard == null ? "" : Common.sdCard) + "/Data" + rst.userDataSaveName;
				String cmd0 = "rm -rf /data/data/" + packageName;
				Exec.run(true, cmd0);
				File userDataFile = new File(userDataPath);
				if (userDataFile.exists()) {
					String cmd1 = "busybox tar xvf " + userDataPath + " -C " + "/";
					b = Exec.run(true, cmd1);
					msgs = b ? "[OK]success to copy " + packageName : "[Failed]failed to copy " + packageName;
				} else {
					b = false;
					msgs = "[Failed]active user data package is not found";
				}
			}
		}

		if (b) {
			rst.packageName = packageName;
			Utils.createUserdataTarGz(context, packageName, Common.USERDATA_ORIGINAL);
			msg.what = MsgWhat.INSTALL_SUCCESS;
			msg.obj = Utils.getPackage(context, path);
		} else {
			msg.what = MsgWhat.INSTALL_FAILED;
			msg.obj = msgs;
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
