package com.huadiangou.goldenfinger.service;

import java.lang.ref.WeakReference;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.huadiangou.goldenfinger.windowmanager.FloatWindowManager;
import com.huadiangou.pulltask.ListViewData;
import com.huadiangou.pulltask.Status;

public class GoldenFingerService extends Service {
	private Status STATUS = ListViewData.STATUS;
	public UpdateUI updateUI;

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
			if(gfs != null && gfs.updateUI != null){
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
		createFloatWindow();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	

	
	private void createFloatWindow(){
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

}
