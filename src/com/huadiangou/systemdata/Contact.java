package com.huadiangou.systemdata;

/**
 * @desc
 * 联系人
 * 
 * @create_time 2014年9月16日
 * @version 1.0.0
 */
public class Contact {

	private long id;
	/**
	 * 第一名称
	 */
	private String first_name;
	
	/**
	 * 第二名称
	 */
	private String last_name;
	
	/**
	 * 公司
	 */
	private String company;
	
	/**
	 * 手机
	 */
	private String mobile;
	
	/**
	 * 公司电话
	 */
	private String work;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}
	
}
