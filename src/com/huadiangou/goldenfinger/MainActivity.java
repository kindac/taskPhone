package com.huadiangou.goldenfinger;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huadiangou.goldenfinger.PauseFragment.StopPause;
import com.huadiangou.goldenfinger.service.GoldenFingerService;
import com.huadiangou.goldenfinger.service.GoldenFingerService.UpdateUI;
import com.huadiangou.goldenfinger.vpn.SetVpnActivity;
import com.huadiangou.pulltask.ApkInfo;
import com.huadiangou.pulltask.ChangeSystemProperty;
import com.huadiangou.pulltask.Common;
import com.huadiangou.pulltask.InstallTask;
import com.huadiangou.pulltask.MsgWhat;
import com.huadiangou.pulltask.PullTask;
import com.huadiangou.pulltask.StaticData;
import com.huadiangou.pulltask.Status;
import com.huadiangou.pulltask.Task;
import com.huadiangou.pulltask.Task.RealSingleTask;
import com.huadiangou.pulltask.UpdateSystemDataTask;
import com.huadiangou.pulltask.UploadTask;
import com.huadiangou.utils.Utils;

public class MainActivity extends Activity implements UpdateUI, StopPause {
	private String TAG = MainActivity.this.getClass().getCanonicalName();
	private TextView osStatusTextView = null;
	private Button getNewTaskButton = null;
	private TextView headMessageTextView = null;
	private TaskListView taskListView = null;
	// private View pauseView = null;
	private Status STATUS = StaticData.STATUS;
	private ScrollView scrollView = null;
	private RadioGroup taskTypeRadioGroup = null;
	private RelativeLayout changenumberRelativeLayout;
	private TextView nownumbeerTxtView = null;
	private RelativeLayout topView = null;
	private int task_type = 0;
	private int number = 1;
	private AlertDialog pauseDialog = null;
	private PauseFragment pauseFragment = null;

	private CheckUpdateFragment checkUpdateFragment = null;

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			handler = ((GoldenFingerService.GfsIBinder) service).getMainHandler();
			gfsService = ((GoldenFingerService.GfsIBinder) service).getService();
			if (gfsService != null && gfsService.updateUI == null) {
				gfsService.setUpdateUI(MainActivity.this);
			}
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

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onPostResume() {
		super.onPostResume();
		
		System.out.println("FFF->" + (gfsService == null));
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
		// pauseView = (View) findViewById(R.id.vw_pause);
		headMessageTextView = (TextView) findViewById(R.id.tv_message);
		scrollView = (ScrollView) findViewById(R.id.sv_status);
		osStatusTextView = (TextView) findViewById(R.id.tv_status);
		getNewTaskButton = (Button) findViewById(R.id.bt_task);
		taskListView = (TaskListView) findViewById(R.id.task_listview);
		taskListView.addItem(null, null, null);
		taskTypeRadioGroup = (RadioGroup) findViewById(R.id.rg_task_type);
		changenumberRelativeLayout = (RelativeLayout) findViewById(R.id.rl_changenumber);
		nownumbeerTxtView = (TextView) findViewById(R.id.tv_nownumber);
		topView = (RelativeLayout) findViewById(R.id.top_view);

		pauseDialog = createDialog();
		pauseFragment = PauseFragment.newInstance(this);

		StaticData.mainTopViewColor[StaticData.NORMAL] = topView.getBackground();
		StaticData.mainTopViewColor[StaticData.PAUSE] = new ColorDrawable(getResources().getColor(R.color.grey));

		getOsStatus();
		setTaskButtonCallBack();
		// setPauseView();
		setMainTopView();
		setTaskTypeRadioGroupCallBack();
		setChangeNumberRelativeLayoutClick();

		appendLog(getSystemInfo());
	}

	private void setMainTopView() {

		if (topView == null) {
			return;
		}
		topView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int i = 0;
				System.out.println(i);
				return true;
			}
		});
	}

	private void setChangeNumberRelativeLayoutClick() {
		if (changenumberRelativeLayout == null) {
			return;
		}
		changenumberRelativeLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP: {
					float x = event.getX();
					float w = v.getWidth();
					String s = nownumbeerTxtView.getText().toString();
					try {
						number = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						number = 1;
					}
					if (x < w / 2) {
						if (--number < 1) {
							number = 10;
						}
					} else {
						if (++number > 10) {
							number = 1;
						}
					}
					nownumbeerTxtView.setText(String.valueOf(number));
					break;
				}
				}
				return v.performClick();
			}
		});
		changenumberRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	private void setTaskTypeRadioGroupCallBack() {
		if (taskTypeRadioGroup == null) {
			return;
		}
		taskTypeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_active: {
					task_type = 1;
					break;
				}
				case R.id.rb_new: {
					task_type = 0;
					break;
				}
				case R.id.rb_alte: {
					break;
				}
				}
			}
		});
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
					Map<String, Long> map = getTaskCannotUpload();
					if (map.size() == 0) {
						Message msg = Message.obtain();
						msg.what = MsgWhat.UP_LOAD;
						msg.obj = "will upload data";
						handler.sendMessage(msg);
					} else {
						StringBuilder sb = new StringBuilder();
						sb.append("以下包的使用时长未达到**" + (Common.TASK_LEAST_RUN_TIME / 1000 / 60) + "**分钟\n");
						for (String p : map.keySet()) {
							sb.append(p);
							sb.append(" **");
							sb.append(map.get(p) / 1000 / 60 + "**分钟\n");
						}
						toastShow(sb.toString(), 1);
					}
					return;
				}
				getNewTaskButton.setText(getResources().getString(R.string.push_task_back));
				pullTask();
			}
		});
	}

	/*
	 * private void setPauseView() { if (pauseView == null) { return; }
	 * pauseView.setOnTouchListener(new OnTouchListener() {
	 * 
	 * @Override public boolean onTouch(View v, MotionEvent event) { return
	 * v.performClick(); } }); pauseView.setOnClickListener(new
	 * OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * 
	 * } }); }
	 */

	private void pause() {
		// pauseView.setVisibility(View.VISIBLE);
		// topView.setClickable(false);
		// topView.setBackground(StaticData.mainTopViewColor[StaticData.PAUSE]);
		// progressBar.setVisibility(View.VISIBLE);
		// getNewTaskButton.setClickable(false);
		// taskListView.setClickable(false);

		// pauseDialog.show();
		pauseFragment.show(getFragmentManager(), "pauseDialog");

	}

	private void disPause() {
		// pauseView.setVisibility(View.GONE);
		// topView.setClickable(true);
		// topView.setBackground(StaticData.mainTopViewColor[StaticData.NORMAL]);
		// progressBar.setVisibility(View.GONE);
		// getNewTaskButton.setClickable(true);
		// taskListView.setClickable(true);
		// pauseDialog.dismiss();
		pauseFragment.dismiss();
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

	private AlertDialog createDialog() {
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.pause_view, null));
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	private void popPauseView(boolean pop) {
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.pause_view, null));
		dialog = builder.create();

		dialog.show();
	}

	private Map<String, Long> getTaskCannotUpload() {
		Map<String, Long> map = new HashMap<String, Long>();
		if (StaticData.task == null || StaticData.task.realTaskList == null)
			return map;
		for (RealSingleTask rst : StaticData.task.realTaskList) {
			if (rst.totalRunTime > Common.TASK_LEAST_RUN_TIME) {
				continue;
			} else {
				if (rst.packageName != null) {
					map.put(rst.packageName, rst.totalRunTime);
				}
			}
		}
		return map;
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
		switch (id) {
		case R.id.action_settings: {
			Intent i = new Intent(this, SetVpnActivity.class);
			startActivity(i);
			break;
		}
		case R.id.action_check_update: {
			//TODO to rewrite the method of setting this interface;

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag(CheckUpdateFragment.class.getName());
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			checkUpdateFragment = CheckUpdateFragment.newInstance(handler);
			checkUpdateFragment.show(ft, CheckUpdateFragment.class.getName());
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * @see
	 * com.huadiangou.pulltask.PullTask.UpdateUI#updateUI(java.lang.String[])
	 */
	/*
	 * @Override public void updateUI(String[] packgeNames) {
	 * 
	 * }
	 * 
	 * /*
	 * 
	 * @see
	 * com.huadiangou.pulltask.PullTask.UpdateUI#sendMessage(android.os.Message)
	 */
	/*
	 * @Override public void sendMessage(Message msg) {
	 * handler.sendMessage(msg); }
	 * 
	 * @Override public Context getContext() { return this; }
	 */

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
		if (ai != null)
			taskListView.addItem(packageName, ai.l, ai.d);
	}

	private void clearLastTask() {
		StaticData.colorMap.clear();
		StaticData.iconMap.clear();
		StaticData.installAPKCount = 0;
		StaticData.list.clear();
		StaticData.task = null;
		StaticData.uploadCount = 0;
		StaticData.SET_PROPERTY = false;
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
			ifNotUpateSystemdataThenUpdate();
			PullTask.getInstance(handler).pullTask(getNumber(), getTaskType());
			break;
		}
		case MsgWhat.INSTALL_APK: {
			Task.RealSingleTask rst = (Task.RealSingleTask) msg.obj;
			new InstallTask(this, rst, handler).execute();
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
			String s = (String) msg.obj;
			if (s != null) {
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
		case MsgWhat.UPDATE_SYSTEMDATA: {
			String s = (String) msg.obj;
			if (s != null && s.equals("OK")) {
				toastShow(s, 1);
			}
			new UpdateSystemDataTask(this, handler).update();
			break;
		}
		case MsgWhat.CHECK_UPDATE_MESSAGE: {
			String s = (String) msg.obj;
			if (s != null) {
				toastShow(s, 1);
			}
			break;
		}
		case MsgWhat.INSTALL_GOLDENFINGER: {
			String path = (String) msg.obj;
			if (path == null) {
				toastShow("Check Code in MainActivity.java", 1);
			}
			installGoldenfinger(path);
			break;
		}
		case MsgWhat.FRAGMENT_DISMISS: {
			String caller = (String) msg.obj;
			if (caller == null)
				return;
			if (caller.equals(CheckUpdateFragment.class.getName())) {
				if (checkUpdateFragment != null) {
					checkUpdateFragment.dismiss();
				}
			}
			break;
		}
		}

	}

	private void ifNotUpateSystemdataThenUpdate() {
		if (!StaticData.HaveUpdateSystemData) {
			StaticData.HaveUpdateSystemData = true;
			Message msg = Message.obtain();
			msg.what = MsgWhat.UPDATE_SYSTEMDATA;
			msg.obj = "下载短信图片...";
			handler.sendMessage(msg);
		}
	}

	private int getNumber() {
		String s = nownumbeerTxtView.getText().toString();
		try {
			number = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			number = 1;
		}
		return number;
	}

	private int getTaskType() {

		return task_type;
	}

	/*
	 * @see com.huadiangou.goldenfinger.PauseFragment.StopPause#stopPause()
	 */
	@Override
	public void stopPause() {

		if (handler != null) {

		} else {
			toastShow("Check Code right now!!!!!!!", 1);
		}
	}

	private void installGoldenfinger(String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
		this.startActivity(intent);

	}

}
