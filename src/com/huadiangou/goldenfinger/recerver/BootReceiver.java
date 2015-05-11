package com.huadiangou.goldenfinger.recerver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	private String GfSAction = "com.huadiangou.goldenfinger.service.GoldenFingerService";

	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(GfSAction);
			context.startService(i);
		}
	}
}