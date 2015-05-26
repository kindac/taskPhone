package com.huadiangou.pulltask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.huadiangou.utils.Utils;

public abstract class DownloadTask extends AbstracAsyncTask {
	private Handler handler;
	private DownloadFileTask downloadFileTask;
	private String[] params;

	public DownloadTask(Handler handler, String... params) {
		this.handler = handler;
		this.params = params;
	}

	@Override
	public void execute() {
		downloadFileTask = new DownloadFileTask();
		downloadFileTask.execute(params);
	}

	@Override
	public void cancel() {

	}

	@Override
	public void add() {

	}

	@Override
	public void remove() {

	}

	@Override
	public void handler(Message msg) {
		handler.sendMessage(msg);
	}

	public abstract void onPostExecute(boolean b);

	private class DownloadFileTask extends AsyncTask<String, Void, Boolean> {
		// private Task.RealSingleTask realSingleTask;

		@Override
		protected Boolean doInBackground(String... params) {
			if (params == null || params.length == 0) {
				return Boolean.FALSE;
			}
			int tryTimes = 0;
			boolean b = false;
			while (tryTimes++ < 2) {
				b = downloadFile(params);
				if (b) {
					break;
				}
			}
			return b;
		}

		@Override
		protected void onPostExecute(Boolean b) {
			DownloadTask.this.onPostExecute(b);
		}
	}

	private boolean downloadFile(String... params) {
		boolean download_success = false;

		if (params == null || params.length != 4) {
			return Boolean.FALSE;
		}
		String url = params[0];
		String saveName = params[1];
		String saveToDir = params[2];
		String md5sum = null;
		if (params.length > 3) {
			md5sum = params[3];
			if(md5sum.equals("")){
				md5sum = null;
			}
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
				handler.sendMessage(msg);
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
		String savedFileMd5sum = Utils.getMD5(saveFile);
		return savedFileMd5sum.equals(md5sum);
	}

}
