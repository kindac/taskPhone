package com.huadiangou.pulltask;

import android.os.Environment;

public class Common {
	
	private static Common common = new Common();
	private String PULL_TASK_ADDR = "http://192.168.1.50:4501/IF/SIM/sim_task.aspx";
	private String UPLOAD_TASK_DATA_ADDR = "http://192.168.1.50:4501/IF/SIM/sim_main_result.aspx";
//http://192.168.1.50:4501/IF/SIM/sim_task.aspx?CELL_WIDTH=800&CELL_HEIGHT=480&BOX_SIGNATURE=070457c801902417668&number=2&task_type=0
	public static String USERDATA_ORIGINAL = "userdata_original";
	public static String USERDATA_active = "userdata_active";
	public static String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();

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

	public String getUploadTaskDataAddr() {
		return UPLOAD_TASK_DATA_ADDR;
	}
}
