package com.mocafelab.api.enums;

import java.util.Arrays;

public enum SgStatus {
	APPROVE_REQUEST(0, "승인요청"),
	APPROVE_COMPLETE(1, "승인완료(진행중)"),
	PAUSE(2, "설정중(일시중지)"),
	EXHAUST_LIMIT(3, "소진제한"),
	PAY_INSUFFICIENT(4, "잔액부족"),
	EMERGENCY_TERMINATION(7, "긴급종료"),
	TERMINATION(8, "광고종료"),
	APPROVE_REJECT(9, "승인거절")
	;
	
	private int value;
	private String description;

	private SgStatus(int value, String description) {
		this.value = value;
		this.description = description;
	}

	public int getValue() {
		return value;
	}
	
	public String getDescription() {
		return description;
	}

	public static SgStatus getSgStatus(int value) {
		return Arrays.stream(SgStatus.values())
			.filter(s -> s.getValue() == value)
			.findFirst()
			.orElse(null);
	}
}
