package com.huadiangou.pulltask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.huadiangou.utils.Exec;
import com.huadiangou.utils.Utils;

public class ChangeSystemProperty {
	private static final String TAG = "com.huadiangou.pulltas.UploadTask";
	private Handler handler;
	private Context context;
	private Task task = ListViewData.task;

	public ChangeSystemProperty(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void run() {
		new ChanageSystemPropertyTask().execute();
	}

	private String realAction(Handler handler) {
		String filePath = Common.sdCard + "/" + "main/prop.txt";
		File propFile = new File(filePath);
		if (!propFile.exists() || !propFile.isFile()) {
			return "[Error]/sdcard/main/prop.txt is not exist";
		}
		String md5sum = Utils.getMD5(propFile);
		if (!checkWhetherPropertyChange(md5sum)) {
			String back = flashProper(propFile);
			if (back.equals("OK")) {
				save(md5sum);
			}
			return back;
		}
		return null;
	}

	private void save(String md5sum) {
		SharedPreferences sp = context.getSharedPreferences("GoldenFinger", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("prop.txt_md5sum", md5sum);
		editor.apply();
	}

	private boolean checkWhetherPropertyChange(String md5sum) {
		SharedPreferences sp = context.getSharedPreferences("GoldenFinger", Context.MODE_PRIVATE);
		String last = sp.getString("prop.txt_md5sum", "");
		return last.equals(md5sum);
	}

	private String flashProper(File propFile) {
		String set_prop = context.getFilesDir().getAbsolutePath() + "/" + "set_prop.sh";
		if (!(new File(set_prop).exists())) {
			String back = copySetPropSh();
			if (!back.equals(set_prop)) {
				return back;
			}
		}
		String cmd = "bash " + set_prop + " " + propFile.getAbsolutePath();
		boolean b = Exec.run(true, cmd);
		if (b) {
			return "OK";
		} else {
			return "Failed";
		}
	}

	private String copySetPropSh() {
		AssetManager am = context.getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			String file = context.getFilesDir().getAbsolutePath() + "/" + "set_prop.sh";
			out = new FileOutputStream(new File(file));
			in = am.open("set_prop.sh");

			byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer, 0, 4096)) > 0) {
				out.write(buffer, 0, n);
			}
			out.flush();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "[Error] copy set_prop.sh failed";
	}

	private class ChanageSystemPropertyTask extends AsyncTask<Void, Void, String> {
		private final String TAG = ChanageSystemPropertyTask.class.getCanonicalName();

		@Override
		protected String doInBackground(Void... params) {
			return realAction(handler);
		}

		@Override
		protected void onPostExecute(String s) {
			Message msg = Message.obtain();
			msg.what = MsgWhat.SET_PROP_RESULT;
			msg.obj = s;
			handler.sendMessage(msg);
			ListViewData.SET_PROPERTY = true;
			if (ListViewData.installAPKCount == ListViewData.task.realTaskList.size()) {
				Message m = Message.obtain();
				m.what = MsgWhat.FINISHED;
				handler.sendMessage(m);
			}
		}
	}

}
