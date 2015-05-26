package com.huadiangou.systemdata;

/**
 * @desc
 * 通话记录
 * 
 * @create_time 2014年9月16日
 * @version 1.0.0
 */
public class DataCallLog {
	
	private long id;
	
	/**
	 * 电话号码
	 */
	private String number; //number
	
	/**
	 * 缓存联系人名称
	 */
	private String cached_name;
	
	/**
	 * 类型 ，1. INCOMING_TYPE 呼入 ，2. OUTGOING_TYPE 呼出，3. MISSED_TYPE 未接
	 */
	private int type;
	/**
	 * 通话日期
	 */
	private long date;
	
	/**
	 * 通话时长
	 */
	private long duration;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCached_name() {
		return cached_name;
	}

	public void setCached_name(String cached_name) {
		this.cached_name = cached_name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
}
