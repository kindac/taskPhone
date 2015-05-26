package com.huadiangou.pulltask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class StaticData {
	public static List<String> list = new ArrayList<String>();
	public static Map<String, Drawable> iconMap = new HashMap<String, Drawable>();
	public static Map<String, String> lableMap = new HashMap<String, String>();
	public static Task task;
	public static Map<String, Integer> colorMap = new HashMap<String, Integer>();
	
	public static int installAPKCount = 0;
	public static int uploadCount = 0;
	public static boolean SET_PROPERTY = false;
	public static Status STATUS = new Status(Status.IDLE);
	
	public static boolean HaveUpdateSystemData = false;
	
	public static final int NORMAL = 0;
	public static final int PAUSE = 1;
	public static Drawable[] mainTopViewColor = new Drawable[2];
	
	
}
