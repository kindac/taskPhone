package com.huadiangou.pulltask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huadiangou.utils.Exec;
import com.huadiangou.utils.Utils;

public class UploadTask {
	private static final String TAG = "com.huadiangou.pulltas.UploadTask";
	private Handler handler;
	private Task.RealSingleTask rst;
	private Context context;
	private Task task = ListViewData.task;

	public UploadTask(Context context, Task.RealSingleTask rst, Handler handler) {
		this.context = context;
		this.rst = rst;
		this.handler = handler;
	}

	public void upload() {
		// for(Task.RealSingleTask rst : task.realTaskList){
		new UploadUserDataTask().execute(task);
		// }
	}

	private boolean createUserdataTarGz(String packageName) {
		return Utils.createUserdataTarGz(context, packageName, Common.USERDATA_active);
	}

	public String realUpload(Task.RealSingleTask rst, Handler handler) {
		if (!createUserdataTarGz(rst.packageName)) {
			return "create " + rst.packageName + ".tar.gz failed";
		}

		File cacheDir = context.getApplicationContext().getCacheDir();
		File savePath = new File(cacheDir, Common.USERDATA_active);
		File file = new File(savePath, rst.packageName + ".tar.gz");
		if (!file.exists() || !file.isFile()) {
			return rst.packageName + ".tar.gz is not exist" ;
		}

		String CHARSET = "UTF-8";
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型

		long fileSize = file.length();
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date d1 = new Date(time);
		String t1 = format.format(d1);
		Date d2 = new Date(time + 1000);
		String t2 = format.format(d2);
		TaskParams tp = new TaskParams(Common.getInstance().getUploadTaskDataAddr(), new String[] { "BEGIN_TIME", t1 },
				new String[] { "END_TIME", t2 }, new String[] { "ID", rst.ID }, new String[] { "BOX_SIGNATURE",
						Task.BOX_SIGNATURE }, new String[] { "FILE_SIZE", String.valueOf(fileSize) });
		String url = Common.getInstance().getUploadTaskDataAddr() + "?" + tp.getEncodeParams();
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		OutputStream out = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET);
			// 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			if (file != null) {
				/** * 当文件不为空，把文件包装并且上传 */
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
				 */
				sb.append("Content-Disposition: form-data; name=\"files\"; filename=\"" + file.getName() + "\""
						+ LINE_END);
				sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();

				dos.close();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();

				Log.e(TAG, "response code:" + res);
				if (res == 200) {

					Log.e(TAG, "upload success~!!!!!!!!!!!:" + res);
				}

				int code = conn.getResponseCode();
				if (code != HttpURLConnection.HTTP_OK) {
					return "Http Connect Error, respondCode : " + code;
				}

				reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder ssb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					ssb.append(line);
				}
				JSONObject jb = new JSONObject(ssb.toString());
				return jb.optString("RESULT", "FALSE");
			}
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
		return "OTher Error";

	}

	private class UploadUserDataTask extends AsyncTask<Task, Void, Boolean> {
		private final String TAG = UploadUserDataTask.class.getCanonicalName();

		@Override
		protected Boolean doInBackground(Task... params) {
			if (params == null || params.length == 0) {
				return Boolean.FALSE;
			}
			Task task = params[0];
			for (Task.RealSingleTask rst : task.realTaskList) {
				ListViewData.uploadCount++;
				String back = realUpload(rst, handler);
				boolean b = back.equals("OK");
				Message msg = Message.obtain();
				if (b) {
					msg.what = MsgWhat.UPLOAD_SUCCESS;
					msg.obj = "[Success] upload " + rst.packageName + "userdata success";
				} else {
					msg.what = MsgWhat.UPLOAD_FAILED;
					msg.obj = "[Failed] upload " + rst.packageName + "userdata failed\n"
							  + "[Reason]"  +  back;
				}
				handler.sendMessage(msg);
			}
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean b) {
			Message msg = Message.obtain();
			msg.what = MsgWhat.RESET;
			handler.sendMessage(msg);
		}
	}

}
