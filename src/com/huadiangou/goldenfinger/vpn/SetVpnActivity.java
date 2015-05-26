package com.huadiangou.goldenfinger.vpn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huadiangou.goldenfinger.R;

public class SetVpnActivity extends Activity {
	private static final String TAG = "SetUsbTether";
	private View pauseView;
	private ProgressBar progressBar;
	private ScrollView scrollView;
	private TextView statusTextView;
	private TextView hintTextView;
	private ImageButton connectButton;
	private ImageButton vpnEnableButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_vpn);

		init();
	}

	private void init() {
		connectButton = (ImageButton) findViewById(R.id.ib_connect);
		vpnEnableButton = (ImageButton) findViewById(R.id.ib_vpn_enable);

		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setUsbTetheringEnabled2(true);
			}
		});
		
		vpnEnableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
	}

	private boolean setUsbTetheringEnabled2(boolean enable) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Method[] methods = connMgr.getClass().getDeclaredMethods();
		Method setUsbInternet = null;
		for (Method method : methods) {
			if (method.getName().equals("setUsbInternet")) {
				setUsbInternet = method;
			}
		}
		if (setUsbInternet != null) {
			try {
				boolean reCode = (boolean) setUsbInternet.invoke(connMgr, enable, 0);
				if (reCode) {
					if (enable) {
						Toast.makeText(this, "enable usb internet success", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(this, "enable usb internet failed", Toast.LENGTH_LONG).show();
					}
				} else {
					if (enable) {
						Toast.makeText(this, "disable usb internet success", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(this, "disable usb internet failed", Toast.LENGTH_LONG).show();
					}
				}
				return reCode;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	private boolean setWifiEnable(boolean enable) {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		return wifiManager.setWifiEnabled(enable);
	}
	
	private boolean isVpnConnect(){
		return false;
	}
	
	private void setVpnEnable(boolean b){
		
	}
}