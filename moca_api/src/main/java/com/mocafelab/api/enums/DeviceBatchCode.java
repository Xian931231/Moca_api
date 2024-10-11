package com.mocafelab.api.enums;

public enum DeviceBatchCode {
	REMOVE_MOTOR_LOCATION("BC0040");
	
	private String code;
	
	private DeviceBatchCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
