package com.huadiangou.goldenfinger;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huadiangou.pulltask.StaticData;
import com.huadiangou.pulltask.Task;

public class TaskListView extends ListView {

	private TaskAdapter adapter;
	private List<String> list = StaticData.list;
	private Map<String, Drawable> iconMap = StaticData.iconMap;
	private Map<String, String> lableMap = StaticData.lableMap;
	private Map<String, Integer> colorMap = StaticData.colorMap;
	private int clickedcolor;

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
		clickedcolor = getResources().getColor(android.R.color.holo_orange_dark);

		adapter = new TaskAdapter();
		setAdapter(adapter);

		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String packageName = list.get(position);
				PackageManager pm = getContext().getPackageManager();
				Intent i = pm.getLaunchIntentForPackage(packageName);
				if (i != null) {
					getContext().startActivity(i);
					getRealSingleTask(packageName).doneTimes += 1;
					colorMap.put(packageName, clickedcolor);
				} else {
					Toast.makeText(getContext(), packageName + " is not installed", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private class TaskAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
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
			if (convertView != null) {
				singleTaskView = (SingleTaskView) convertView;
			} else {
				singleTaskView = new SingleTaskView(parent.getContext());
			}
			ImageView iv = (ImageView) singleTaskView.findViewById(R.id.iv_taskpic);
			TextView tv = (TextView) singleTaskView.findViewById(R.id.tv_task_status);
			Drawable icon = iconMap.get(list.get(position));
			if (icon != null) {
				iv.setBackground(icon);
			}

			String lable = lableMap.get(list.get(position));
			if (lable != null) {
				tv.setText(lable);
			} else {
				tv.setText("check Code");
			}
			String packageName = list.get(position);
			int color = colorMap.get(packageName);
			singleTaskView.setBackgroundColor(color);
			return singleTaskView;
		}

	}

	public synchronized void addItem(String packageName, String lable, Drawable icon) {
		if (packageName != null) {
			list.add(packageName);
		}
		if (icon != null) {
			iconMap.put(packageName, icon);
		}
		if (lable != null) {
			lableMap.put(packageName, lable);
		}
		colorMap.put(packageName, getResources().getColor(android.R.color.white));
		adapter.notifyDataSetChanged();
	}

	private Task.RealSingleTask getRealSingleTask(String packageName) {
		for (Task.RealSingleTask rst : StaticData.task.realTaskList) {
			if (rst.packageName.equals(packageName))
				return rst;
		}
		return null;
	}

	public void update() {
		adapter.notifyDataSetChanged();
	}
}
