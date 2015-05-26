package com.huadiangou.systemdata;

/**
 * @desc
 * 短信
 * 
 * @create_time 2014年9月16日
 * @version 1.0.0
 */
public class SMS {
	
	private long id;
	/**
	 * 对话序号。100，与同一个手机互发的短信 
	 */
	private int thread_id; 
	
	/**
	 * 发件人手机号
	 */
	private String address;
	
	/**
	 * 发件人姓名
	 */
	private String person;
	
	/**
	 * 发件日期
	 */
	private long date;
	
	/**
	 * 短信协议，0. SMS_RPORTO 短信 2. MMS_RPORTO 彩信 
	 */
	private int protocol;
	
	/**
	 * 是否阅读 0 未读 1 已读
	 */
	private int read;
	
	/**
	 * status -1. 接收，0. complete, 64pending,128 filed 
	 */
	private int status;
	
	/**
	 * 短信类型，1 收件箱 2.发件箱
	 */
	private int type;
	
	/**
	 * 短信内容
	 */
	private String body;
	
	/**
	 * 短信中心号码
	 */
	private String service_center;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getThread_id() {
		return thread_id;
	}

	public void setThread_id(int thread_id) {
		this.thread_id = thread_id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getService_center() {
		return service_center;
	}

	public void setService_center(String service_center) {
		this.service_center = service_center;
	}
}
