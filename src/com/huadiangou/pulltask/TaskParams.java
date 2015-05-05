package com.huadiangou.pulltask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TaskParams {
	private String url;
	private static String NUM = "NUM";
	private static String TYPE = "TYPE";

	private Map<String, String> params;

	private TaskParams() {
	}

	public TaskParams(String url, int num, int type) throws ParamsInvaliedException {
		params = new HashMap<String, String>();
		if (url == null) {
			throw new ParamsInvaliedException("task count is invalied");
		} else {
			this.url = url;
		}

		if (num <= 0) {
			throw new ParamsInvaliedException("task count is invalied");
		} else {
			params.put(NUM, String.valueOf(num));
		}

		if (type < 0 || type > 2) {
			throw new ParamsInvaliedException("task type is invalied");
		} else {
			params.put(TYPE, String.valueOf(type));
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
