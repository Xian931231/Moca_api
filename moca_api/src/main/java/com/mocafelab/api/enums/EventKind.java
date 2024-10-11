package com.mocafelab.api.enums;

/**
 * 광고 진행상황
 * @author pps8853
 */
public enum EventKind {
	C("클릭"),
	DS("이미지 노출 시작"),
	DE("이미지 노출 종료"),
	DP("이미지 노출 중지"),
	PS("재생 시작"),
	PE("재생 종료"),
	PP("재생 중지"),
	PR("중지 후 다시 시작"),
	PC("중단(기타 이유로)"),
	;
	
	private String desc;

	private EventKind(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
	
	public static void verifyEventKind(String eventKind) {
		for (EventKind kind : EventKind.values()) {
			if (kind.name().equals(eventKind)) {
				return;
			}
		}
		
		throw new RuntimeException();
	}
	
	public static EventKind getEventKind(String eventKind) {
		for (EventKind kind : EventKind.values()) {
			if (kind.name().equals(eventKind)) {
				return kind;
			}
		}
		
		throw new RuntimeException();
	}
}
	