package com.huadiangou.goldenfinger;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class PauseFragment extends DialogFragment {
	private StopPause stopPause;

	static PauseFragment newInstance(StopPause stopPause) {
		PauseFragment pf = new PauseFragment();
		pf.stopPause = stopPause;
		pf.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		return pf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pause_view, container, false);
		v.findViewById(R.id.force_stop).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(stopPause != null){
					stopPause.stopPause();
				}
			}
		});
		return v;
	}
	
	public interface StopPause{
		public void stopPause();
	}

}
