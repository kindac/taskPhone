package com.huadiangou.goldenfinger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class FloatWindowView extends LinearLayout {

	private WindowManager.LayoutParams mParams;

	public FloatWindowView(Context context) {
		this(context, null);
	}

	public FloatWindowView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.float_window, this);
	}
	

	public void setParams(WindowManager.LayoutParams params) {  
        mParams = params;  
    }  
	

}
