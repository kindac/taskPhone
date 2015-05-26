package com.huadiangou.goldenfinger.vpn;

import java.io.File;

import org.w3c.dom.UserDataHandler;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huadiangou.utils.Exec;
import com.huadiangou.utils.Utils;

public class VpnTask {
	private static final String TAG = "com.huadiangou.pulltask.InstallTask";
	private Handler handler;
	private Context context;

	private static VpnTask vpnTask;

	private VpnTask(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public static synchronized VpnTask getInstance(Context context, Handler handler) {
		if (vpnTask == null) {
			vpnTask = new VpnTask(context, handler);
		}
		return vpnTask;
	}

	public synchronized boolean setUpVpn(String server, String userName, String passwd) {

		return false;
	}

	public synchronized boolean disableVpn() {

		return false;
	}

	private class EnableVpnTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			if (params.length != 3) {
				return null;
			}

			String server = params[0];
			String userName = params[1];
			String passwd = params[2];
			/*
			 * mtpd wlan0 pptp 192.168.1.15 1723 name vpn1 password 123456
			 * linkname vpn refuse-eap nodefaultroute usepeerdns idle 1800 mtu
			 * 1400 mru 1400 +mppe
			 */
			StringBuilder vpnSb = new StringBuilder();
			vpnSb.append("mtpd wlan0 ").append("pptp ").append(server).append(" ").append("name ").append(userName)
					.append(" ").append("password ").append(passwd).append(" ")
					.append("linkname vpn refuse-eap nodefaultroute usepeerdns idle 1800 mtu 1400 mru 1400 +mppe");

			Exec.run(true, vpnSb.toString());

			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
		}
	}

	private class DisableVpnTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			int i = 0;
			String progress = "mtpd";
			while (i++ > 2) {
				Exec.killProcess(progress);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				boolean b = Exec.isProggressRunning(progress);
				if (b) {
					return true;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean b) {
		}
	}
}
