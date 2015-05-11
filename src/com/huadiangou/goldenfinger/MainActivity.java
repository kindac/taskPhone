package com.huadiangou.goldenfinger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
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

import com.huadiangou.goldenfinger.service.GoldenFingerService;
import com.huadiangou.goldenfinger.service.GoldenFingerService.UpdateUI;
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

public class MainActivity extends Activity implements PullTask.UpdateUI, UpdateUI {
	private String TAG = MainActivity.this.getClass().getCanonicalName();
	private TextView osStatusTextView = null;
	private Button getNewTaskButton = null;
	private TextView headMessageTextView = null;
	private TaskListView taskListView = null;
	private View pauseView = null;
	private ProgressBar progressBar = null;
	private Status STATUS = ListViewData.STATUS;
	private ScrollView scrollView = null;

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			handler = ((GoldenFingerService.GfsIBinder) service).getMainHandler();
			gfsService = ((GoldenFingerService.GfsIBinder) service).getService();
		}
	};

	private GoldenFingerService.MainHander handler;
	private GoldenFingerService gfsService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent i = new Intent(GoldenFingerService.class.getCanonicalName());
		startService(i);

	}

	@Override
	protected void onResume() {
		super.onResume();
		bindGoldenFinferService();
		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindGoldenFinferService();
	}

	@Override
    public void onBackPressed() {
		return;
	}
	

	private void bindGoldenFinferService() {
		Intent i = new Intent(this, GoldenFingerService.class);
		bindService(i, mServiceConnection, BIND_ABOVE_CLIENT);
	}

	private void unbindGoldenFinferService() {
		unbindService(mServiceConnection);
	}
	
	private void createFloatWindow() {

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
				if(gfsService != null && gfsService.updateUI == null){
					gfsService.setUpdateUI(MainActivity.this);
				}

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
		if(ai != null)
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

	@Override
	public void updateUI(Message msg) {
		switch (msg.what) {
		case MsgWhat.ERR: {
			STATUS.status = Status.IDLE;
			getNewTaskButton.setText(getResources().getString(R.string.get_task));
			String s = (String) msg.obj;
			toastShow(s, 0);
			break;
		}
		case MsgWhat.SDCARD_ERR: {
			STATUS.status = Status.IDLE;
			getNewTaskButton.setText(getResources().getString(R.string.get_task));
			String s = (String) msg.obj;
			showMessageOnHead(s);
			break;
		}
		case MsgWhat.PULL_TASK: {
			STATUS.status = Status.RUNNING;
			pause();
			clearLastTask();
			PullTask.getInstance(this).pullTask();
			break;
		}
		case MsgWhat.INSTALL_APK: {
			Task.RealSingleTask rst = (Task.RealSingleTask) msg.obj;
			new InstallTask(this, rst, handler).install();
			break;
		}
		case MsgWhat.DOWNLOAD_TASK_FAILED: {
			String s = (String) msg.obj;
			showMessageOnHead(s);
			break;
		}
		case MsgWhat.INSTALL_FAILED: {
			String s = (String) msg.obj;
			showMessageOnHead(s);
			break;
		}
		case MsgWhat.INSTALL_SUCCESS: {
			String packageName = (String) msg.obj;
			updateListView(packageName);
			break;
		}
		case MsgWhat.UP_LOAD: {
			pause();
			new UploadTask(this, null, handler).upload();
			break;
		}
		case MsgWhat.UPLOAD_SUCCESS: {
			String s = (String) msg.obj;
			popAlterDialogWithMessage(s, msg.what);
			getNewTaskButton.setText(getResources().getString(R.string.get_task));
			break;
		}
		case MsgWhat.UPLOAD_FAILED: {
			String s = (String) msg.obj;
			popAlterDialogWithMessage(s, msg.what);
			break;
		}
		case MsgWhat.FINISHED: {
			disPause();
			break;
		}
		case MsgWhat.RESET: {
			String s = (String)msg.obj;
			if(s != null){
				toastShow(s, 1);
			}
			getNewTaskButton.setText(getResources().getString(R.string.get_task));
			clearLastTask();
			STATUS.status = Status.IDLE;
			disPause();
			break;
		}
		case MsgWhat.SET_PROP: {
			new ChangeSystemProperty(this, handler).run();
			break;
		}
		case MsgWhat.SET_PROP_RESULT: {
			String s = (String) msg.obj;
			if (s != null && s.equals("OK")) {
				appendLog(getSystemInfo());
			}
			break;
		}
		}

	}

}
