package com.jason.vo;

public class SocketResult {
	
	private String status;
	private String data;
	
	public SocketResult(String status, String data) {
		
		this.status = status;
		this.data = data;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getData() {
		return data;
	}



	public void setData(String data) {
		this.data = data;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
