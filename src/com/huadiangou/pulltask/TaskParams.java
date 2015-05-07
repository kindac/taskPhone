package com.huadiangou.pulltask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TaskParams {

//http://192.168.1.50:4501/IF/SIM/sim_task.aspx?CELL_WIDTH=800&CELL_HEIGHT=480&BOX_SIGNATURE=070457c801902417668&number=2&task_type=0
	private static String CELL_WIDTH="CELL_WIDTH";
	private static String CELL_HEIGHT="CELL_HEIGHT";
	private static String BOX_SIGNATURE="BOX_SIGNATURE";
	private String url;
	private static String NUM = "number";
	private static String TYPE = "task_type";

	private Map<String, String> params;

	private TaskParams() {
	}

	public TaskParams(String url,int cell_width, int cell_heith, String box_signature, int num, int type) throws ParamsInvaliedException {
		params = new HashMap<String, String>();
		if (url == null) {
			throw new ParamsInvaliedException("task count is invalied");
		} else {
			this.url = url;
		}

		params.put(CELL_WIDTH, String.valueOf(cell_width));
		params.put(CELL_HEIGHT, String.valueOf(cell_heith));
		params.put(BOX_SIGNATURE, box_signature);

		params.put(NUM, String.valueOf(num));

		params.put(TYPE, String.valueOf(type));
	}
	
	public TaskParams(String url, String[]... para){
		params = new HashMap<String, String>();
		this.url = url;
		if(para != null){
			for(int i = 0; i < para.length; i++){
				String[] p = para[i];
				if(p.length != 2){
					continue;
				}
				this.params.put(p[0], p[1]);
			}
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEncodeParams() {
		try {
			StringBuilder encodedString = new StringBuilder();
			boolean first = true;
			for (String key : params.keySet()) {
				String value = params.get(key);
				if (value.equals(""))
					continue;
				if (first)
					first = false;
				else
					encodedString.append("&");
				encodedString.append(URLEncoder.encode(key, "UTF-8")).append("=")
						.append(URLEncoder.encode(value, "UTF-8"));
			}
			return encodedString.toString();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static class ParamsInvaliedException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParamsInvaliedException(String s) {
			super(s);
		}
	}
}
