package com.huadiangou.goldenfinger;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huadiangou.pulltask.MsgWhat;
import com.huadiangou.pulltask.PullTask;

public class MainActivity extends Activity implements PullTask.UpdateUI {
	private String TAG = MainActivity.this.getClass().getCanonicalName();
	private TextView osStatusTextView = null;
	private Button getNewTaskButton = null;
	private TextView headMessageTextView = null;
	private Status STATUS = Status.IDLE;

	public static int ERR = 1;

	private enum Status {
		RUNNING, IDLE,
	}

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
				mc.STATUS = Status.IDLE;
				String s = (String) msg.obj;
				mc.toastShow(s, 0);
				break;
			}
			case MsgWhat.SDCARD_ERR: {
				mc.STATUS = Status.IDLE;
				String s = (String) msg.obj;
				mc.showMessageOnHead(s);
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

		initView();
	}

	private void initView() {
		headMessageTextView = (TextView) findViewById(R.id.tv_message);
		osStatusTextView = (TextView) findViewById(R.id.tv_status);
		getNewTaskButton = (Button) findViewById(R.id.bt_task);

		getOsStatus();
		setTaskButtonCallBack();
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
		getNewTaskButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (STATUS == Status.RUNNING) {
					toastShow("Running", 1);
					return;
				}
				toastShow("Will Running", 1);
				pullTask();
				STATUS = Status.RUNNING;
			}
		});
	}

	private void pullTask() {
		PullTask.getInstance(this).pullTask();
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

}
