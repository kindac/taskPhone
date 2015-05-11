package com.huadiangou.goldenfinger.windowmanager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.huadiangou.goldenfinger.FloatWindowView;

public class FloatWindowManager {

	private static WindowManager mWindowManager;
	private static  FloatWindowView smallWindow;
	private static WindowManager.LayoutParams smallWindowParams;
	
	public static void createFloatWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (smallWindow == null) {
			smallWindow = new FloatWindowView(context);
			if (smallWindowParams == null) {
				smallWindowParams = new LayoutParams();
				smallWindowParams.type = LayoutParams.TYPE_PHONE;
				smallWindowParams.format = PixelFormat.RGBA_8888;
				smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
				smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				smallWindowParams.width = 100; //FloatWindowSmallView.viewWidth;
				smallWindowParams.height = 100;//FloatWindowSmallView.viewHeight;
				smallWindowParams.x = screenWidth;
				smallWindowParams.y = screenHeight / 2;
			}
			smallWindow.setParams(smallWindowParams);
			windowManager.addView(smallWindow, smallWindowParams);
		}
	}
	
	private static WindowManager getWindowManager(Context context) {  
        if (mWindowManager == null) {  
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);  
        }  
        return mWindowManager;  
    }  

}
