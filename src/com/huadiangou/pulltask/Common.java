package com.huadiangou.pulltask;

import android.os.Environment;

public class Common {
	
	private static Common common = new Common();
	private String PULL_TASK_ADDR = "";
	private String sdCard = null;
	
	private Common(){
		sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public static Common getInstance(){
		return common;
	}
	
	public String getSdCard(){
		return sdCard;
	}
	
	public String getPullTaskAddr(){
		return PULL_TASK_ADDR;
	}
	
}
