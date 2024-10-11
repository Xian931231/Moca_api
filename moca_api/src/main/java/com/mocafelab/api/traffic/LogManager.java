package com.mocafelab.api.traffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * 광고 이벤트에 대한 트래픽 관리 클래스
 * @author pps8853
 */
@Getter
public class LogManager {
	private String lastIndex;

	private int totalRecord; // 총 배치가 돌아간 레코드 수
	
	private boolean result; // 시작 - 종료 pair가 맞는지 확인하는 값 (맞을때 true)
	
	private String modifyId; // 시작 - 종료 pair가 맞는 디바이스 리스트를 찾을 키
	
	private Map<String, List<DeviceLog>> device; // 디바이스 ID에 해당하는 데이터를 담는 맵
	
	public LogManager() {}
	
	// 디바이스 정보
	public Map<String, List<DeviceLog>> getDevice() {
		if (device == null) {
			device = new HashMap<>();
		}
		
		return device;
	}
	
	// 디바이스 아이디 별 디바이스 데이터 목록
	public List<DeviceLog> getDeviceLogList(Map<String, List<DeviceLog>> device, String deviceId) {
		List<DeviceLog> deviceLogList = device.get(deviceId);
		
		if (deviceLogList == null) {
			List<DeviceLog> newList = new ArrayList<>();
			device.put(deviceId, newList);
			return newList;
		}
		
		return deviceLogList;
	}
	
	// 디바이스 로그 꺼내기
	public DeviceLog getDeviceLog(List<DeviceLog> deviceLogList, String sgKind, long sgId, long slotId) {
		String confirmKey = getConfirmKey(sgKind, sgId, slotId);
		
		for (DeviceLog deviceLog : deviceLogList) {
			// 이전에 생성된 슬롯id + 광고id 에 해당하는 데이터가 있으면 그값을 리턴
			if (confirmKey.equals(deviceLog.getConfirmKey())) {
				return deviceLog;
			}
		}
		
		// 처음 들어오는 슬롯 + 광고 데이터라면
		DeviceLog newDeviceLog = new DeviceLog(sgKind, sgId, slotId);
		deviceLogList.add(newDeviceLog);
		return newDeviceLog;
	}
	
	// 광고종류 + 광고id + 슬롯id 
	private String getConfirmKey(String sgKind, long sgId, long slotId) {
		return sgKind + "_" + sgId + "_" + slotId;
	}

	/**
	 * 처리해야 할 광고 키 정보 set
	 * @param result
	 * @param modifyId
	 */
	public void setSuccessResult(boolean result, String modifyId) {
		this.result = result;
		this.modifyId = modifyId;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
	
	public void addTotalRecord(int totalRecord) {
		this.totalRecord += totalRecord;
	}
	
	public void setLastIndex(String lastIndex) {
		this.lastIndex = lastIndex;
	}
}