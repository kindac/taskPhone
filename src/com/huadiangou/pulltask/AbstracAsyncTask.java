package com.huadiangou.pulltask;

import android.os.Message;

public abstract class AbstracAsyncTask {
	public abstract void execute();

	public abstract void cancel();

	public abstract void remove();

	public abstract void add();
	
	public abstract void handler(Message msg);
}
