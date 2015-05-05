package com.huadiangou.goldenfinger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class TaskListView extends ListView {

	private TaskAdapter adapter;
	
	public TaskListView(Context context) {
		this(context, null);
	}

	public TaskListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TaskListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		adapter = new TaskAdapter();
		setAdapter(adapter);
	}

	private class TaskAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SingleTaskView singleTaskView = null;
			if(convertView != null){
				singleTaskView = (SingleTaskView)convertView;
			}else{
				singleTaskView = new SingleTaskView(parent.getContext());
			}
			return singleTaskView;
		}

	}
}
