package com.huadiangou.goldenfinger.service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;

import com.huadiangou.goldenfinger.windowmanager.FloatWindowManager;
import com.huadiangou.pulltask.StaticData;
import com.huadiangou.pulltask.Status;
import com.huadiangou.pulltask.Task;
import com.ktereyp.GrabPoint;

public class GoldenFingerService extends Service {
	private Status STATUS = StaticData.STATUS;
	public UpdateUI updateUI;
	private GrabPoint grabPoint;
	private Map<Long, Integer> map = new HashMap<Long, Integer>();
	private List<Long> timeList;
	private List<Integer> actionList;

	public static class MainHander extends Handler {
		private WeakReference<GoldenFingerService> wrGfs;

		public MainHander(GoldenFingerService gfs) {
			this.wrGfs = new WeakReference<GoldenFingerService>(gfs);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			GoldenFingerService gfs = wrGfs.get();
			if (gfs != null && gfs.updateUI != null) {
				gfs.updateUI.updateUI(msg);
			}
		}
	}

	private MainHander handler = new MainHander(this);
	private GfsIBinder binder = new GfsIBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startActivityScannerIfNotRunning();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void createFloatWindow() {
		FloatWindowManager.createFloatWindow(this);
	}

	public class GfsIBinder extends Binder {

		public GoldenFingerService getService() {
			return GoldenFingerService.this;
		}

		public MainHander getMainHandler() {
			return GoldenFingerService.this.handler;
		}

	}

	public int debug() {
		return new Random().nextInt();
	}

	public void setUpdateUI(UpdateUI updateUI) {
		this.updateUI = updateUI;
	}

	public interface UpdateUI {
		void updateUI(Message msg);
	}

	private String getCurrentTopActivityName() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		if (taskInfo != null && taskInfo.size() > 0) {
			ComponentName componentInfo = taskInfo.get(0).topActivity;
			return componentInfo.getPackageName();
		} else {
			return null;
		}
	}

	private void touchPoint() {
	}

	Thread activityScannerThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				String appName = getCurrentTopActivityName();
				if (appName != null && StaticData.task != null && StaticData.task.realTaskList != null ) {
					for(Task.RealSingleTask rst : StaticData.task.realTaskList){
						if(rst.packageName != null && rst.packageName.equals(appName)){
							rst.totalRunTime += 5000;
						}
					}
					System.out.println("KKK " + appName);
				}
				
				if(timeList != null){
					for(Long l : timeList){
						System.out.println("KKK TTT" + l);
					}
				}else{
						System.out.println("KKK timeList is null");
				}
				if(actionList != null){
					for(Integer i : actionList){
						System.out.println("KKK AAA" + i);
					}
				}else{
						System.out.println("KKK actionList is null");
				}
				
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
	

	private void startActivityScannerIfNotRunning() {
		if (!activityScannerThread.isAlive()) {
			activityScannerThread.start();
			System.out.println("KKK start Thread");
		} else {
			System.out.println("KKK Thread is running");
		}
		if (grabPoint == null) {
			grabPoint = GrabPoint.getInstance();
			timeList = grabPoint.getTimeList();
			actionList = grabPoint.getActionList();
		}

		Intent i = new Intent(GoldenFingerService.class.getCanonicalName());
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(0, 60 * 1000 + System.currentTimeMillis(), pi);
	}

}
