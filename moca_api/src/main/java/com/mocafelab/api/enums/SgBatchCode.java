package com.mocafelab.api.enums;

public enum SgBatchCode {
	MODIFY_CPP_END("BC0030"),
	MODIFY_NOT_PAID("BC0031"),
	MODIFY_NOT_APPROVAL("BC0032");
	
	private String code;
	
	private SgBatchCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
