package com.huadiangou.goldenfinger;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huadiangou.pulltask.ApkInfo;
import com.huadiangou.pulltask.ChangeSystemProperty;
import com.huadiangou.pulltask.InstallTask;
import com.huadiangou.pulltask.ListViewData;
import com.huadiangou.pulltask.MsgWhat;
import com.huadiangou.pulltask.PullTask;
import com.huadiangou.pulltask.Status;
import com.huadiangou.pulltask.Task;
import com.huadiangou.pulltask.UploadTask;
import com.huadiangou.utils.Utils;

public class MainActivity extends Activity implements PullTask.UpdateUI {
	private String TAG = MainActivity.this.getClass().getCanonicalName();
	private TextView osStatusTextView = null;
	private Button getNewTaskButton = null;
	private TextView headMessageTextView = null;
	private TaskListView taskListView = null;
	private View pauseView = null;
	private ProgressBar progressBar = null;
	private Status STATUS = ListViewData.STATUS;
	private ScrollView scrollView = null;

	public static int ERR = 1;

	private static class MainHander extends Handler {
		private WeakReference<MainActivity> mainAc;

		public MainHander(MainActivity mc) {
			this.mainAc = new WeakReference<MainActivity>(mc);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity mc = mainAc.get();
			if (msg == null) {
				return;
			}

			switch (msg.what) {
			case MsgWhat.OK: {
				break;
			}
			case MsgWhat.ERR: {
				mc.STATUS.status = Status.IDLE;
				mc.getNewTaskButton.setText(mc.getResources().getString(R.string.get_task));
				String s = (String) msg.obj;
				mc.toastShow(s, 0);
				break;
			}
			case MsgWhat.SDCARD_ERR: {
				mc.STATUS.status = Status.IDLE;
				mc.getNewTaskButton.setText(mc.getResources().getString(R.string.get_task));
				String s = (String) msg.obj;
				mc.showMessageOnHead(s);
				break;
			}
			case MsgWhat.PULL_TASK: {
				mc.STATUS.status = Status.RUNNING;
				mc.pause();
				mc.clearLastTask();
				PullTask.getInstance(mc).pullTask();
				break;
			}
			case MsgWhat.INSTALL_APK: {
				Task.RealSingleTask rst = (Task.RealSingleTask) msg.obj;
				new InstallTask(mc, rst, this).install();
				break;
			}
			case MsgWhat.DOWNLOAD_TASK_FAILED: {
				String s = (String) msg.obj;
				mc.showMessageOnHead(s);
				break;
			}
			case MsgWhat.INSTALL_FAILED: {
				String s = (String) msg.obj;
				mc.showMessageOnHead(s);
				break;
			}
			case MsgWhat.INSTALL_SUCCESS: {
				String packageName = (String) msg.obj;
				mc.updateListView(packageName);
				break;
			}
			case MsgWhat.UP_LOAD: {
				mc.pause();
				new UploadTask(mc, null, mc.handler).upload();
				break;
			}
			case MsgWhat.UPLOAD_SUCCESS: {
				String s = (String) msg.obj;
				mc.popAlterDialogWithMessage(s, msg.what);
				mc.getNewTaskButton.setText(mc.getResources().getString(R.string.get_task));
				break;
			}
			case MsgWhat.UPLOAD_FAILED: {
				String s = (String) msg.obj;
				mc.popAlterDialogWithMessage(s, msg.what);
				break;
			}
			case MsgWhat.FINISHED: {
				mc.disPause();
				break;
			}
			case MsgWhat.RESET: {
				mc.getNewTaskButton.setText(mc.getResources().getString(R.string.get_task));
				mc.clearLastTask();
				mc.STATUS.status = Status.IDLE;
				mc.disPause();
				break;
			}
			case MsgWhat.SET_PROP: {
				new ChangeSystemProperty(mc, mc.handler).run();
				break;
			}
			case MsgWhat.SET_PROP_RESULT: {
				String s = (String) msg.obj;
				if (s != null && s.equals("OK")) {
					mc.appendLog(mc.getSystemInfo());
				}
				break;
			}
			}
		}
	}

	private Handler handler = new MainHander(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ABCD", "Status " + ListViewData.STATUS.status);
		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("ABCD===", "Status " + ListViewData.STATUS.status);
	}

	private void initView() {
		pauseView = (View) findViewById(R.id.vw_pause);
		progressBar = (ProgressBar) findViewById(R.id.pb_progress);
		headMessageTextView = (TextView) findViewById(R.id.tv_message);
		scrollView = (ScrollView) findViewById(R.id.sv_status);
		osStatusTextView = (TextView) findViewById(R.id.tv_status);
		getNewTaskButton = (Button) findViewById(R.id.bt_task);
		taskListView = (TaskListView) findViewById(R.id.task_listview);
		taskListView.addItem(null, null, null);

		getOsStatus();
		setTaskButtonCallBack();
		setPauseView();

		appendLog(getSystemInfo());
	}

	private void getOsStatus() {
		if (osStatusTextView == null) {
			return;
		}
	}

	private void setTaskButtonCallBack() {
		if (getNewTaskButton == null) {
			return;
		}
		if (STATUS.status == Status.RUNNING) {
			getNewTaskButton.setText(getResources().getString(R.string.push_task_back));
		}
		getNewTaskButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (STATUS.status == Status.RUNNING) {
					if (checkWhetherCanUpload()) {
						Message msg = Message.obtain();
						msg.what = MsgWhat.UP_LOAD;
						msg.obj = "will upload data";
						handler.sendMessage(msg);
					}
					return;
				}
				getNewTaskButton.setText(getResources().getString(R.string.push_task_back));
				pullTask();
			}
		});
	}

	private void setPauseView() {
		if (pauseView == null) {
			return;
		}
		pauseView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.performClick();
				return true;
			}
		});
		pauseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}

	private void pause() {
		pauseView.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
	}

	private void disPause() {
		pauseView.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
	}

	private void popAlterDialogWithMessage(String msg, int what) {
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (what == MsgWhat.UPLOAD_FAILED) {
			builder.setPositiveButton("重新传送", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO
					dialog.dismiss();
					toastShow("Not have implements", 1);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		} else {
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

		}

		builder.setMessage(msg);

		dialog = builder.create();
		dialog.show();
	}

	protected boolean checkWhetherCanUpload() {
		return true;
	}

	private void pullTask() {
		Message msg = Message.obtain();
		msg.what = MsgWhat.PULL_TASK;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * @see
	 * com.huadiangou.pulltask.PullTask.UpdateUI#updateUI(java.lang.String[])
	 */
	@Override
	public void updateUI(String[] packgeNames) {

	}

	/*
	 * @see
	 * com.huadiangou.pulltask.PullTask.UpdateUI#sendMessage(android.os.Message)
	 */
	@Override
	public void sendMessage(Message msg) {
		handler.sendMessage(msg);
	}

	@Override
	public Context getContext() {
		return this;
	}

	void toastShow(String message, int i) {
		if (i == 0) {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}

	void showMessageOnHead(String s) {
		if (headMessageTextView == null || s == null) {
			return;
		}
		headMessageTextView.setText(s);
		headMessageTextView.setVisibility(View.VISIBLE);
		headMessageTextView.postDelayed(new Runnable() {
			@Override
			public void run() {
				headMessageTextView.setVisibility(View.GONE);
			}
		}, 3000);
	}

	private void updateListView(String packageName) {
		if (packageName == null) {
			return;
		}
		ApkInfo ai = Utils.getIcon(this, packageName);
		taskListView.addItem(packageName, ai.l, ai.d);
	}

	private void clearLastTask() {
		ListViewData.colorMap.clear();
		ListViewData.iconMap.clear();
		ListViewData.installAPKCount = 0;
		ListViewData.list.clear();
		ListViewData.task = null;
		ListViewData.uploadCount = 0;
		ListViewData.SET_PROPERTY = false;
		taskListView.update();
	}

	private void appendLog(String msg) {
		osStatusTextView.append(msg);
		osStatusTextView.append("\n");
		scrollView.fullScroll(View.FOCUS_DOWN);
	}

	private String getSystemInfo() {
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String sys_imei = manager.getDeviceId();
		String sys_imsi = manager.getSubscriberId();

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String wifyString = info.getMacAddress();

		TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		String mtyb = android.os.Build.BRAND;// 手机品牌
		String mtype = android.os.Build.MODEL; // 手机型号
		String imei = tm.getDeviceId();
		String imsi = tm.getSubscriberId();
		String numer = tm.getLine1Number(); // 手机号码
		String serviceName = tm.getSimOperatorName(); // 运营商
		String s = "this is upgrade \n" + "品牌:" + mtyb + "\n" + "型号:" + mtype + "\n" + "名称" + android.os.Build.PRODUCT
				+ "\n" + "版本:Android " + android.os.Build.VERSION.RELEASE + "\n" + "IMEI:" + imei + "\n" + "IMSI:"
				+ imsi + "\n" + "手机号码:" + numer + "\n" + "运营商:" + serviceName + "\n" + "系统imei:" + sys_imei + "\n"
				+ "配置imei：" + sys_imsi + "\n" + "wify为mac：" + wifyString;
		// + "\nCPUID:"
		// + BoxContext.CPU_ID;
		return s;
	}

	private void saveState() {

	}

	private void resetorState() {

	}

}
