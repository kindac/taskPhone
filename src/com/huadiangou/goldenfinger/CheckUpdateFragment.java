package com.huadiangou.goldenfinger;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huadiangou.pulltask.CheckGoldenfingerUpdateTask;
import com.huadiangou.pulltask.DownloadTask;

public class CheckUpdateFragment extends DialogFragment {
	private ProgressBar progressBar;
	private TextView textView;

	private DownloadTask downloadTask;
	private Handler handler;
	private CheckGoldenfingerUpdateTask checkGoldenfingerUpdateTask;

	static CheckUpdateFragment newInstance(Handler handler) {
		CheckUpdateFragment cf = new CheckUpdateFragment();
		cf.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		cf.handler = handler;
		return cf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.check_update_view, container, false);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		checkGoldenfingerUpdateTask = CheckGoldenfingerUpdateTask.getInstance(getActivity(), handler,
				CheckUpdateFragment.class.getName());
		checkGoldenfingerUpdateTask.execute();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		checkGoldenfingerUpdateTask.cancel();
	}

}
