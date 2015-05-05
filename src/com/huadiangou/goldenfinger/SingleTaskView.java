package com.huadiangou.goldenfinger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class SingleTaskView extends LinearLayout {
	public SingleTaskView(Context context) {
		this(context, null);
	}

	public SingleTaskView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SingleTaskView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.singletaskview, this);
	}

}
