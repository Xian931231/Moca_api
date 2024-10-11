package com.mocafelab.api.traffic;

import java.util.Map;
import java.util.TreeMap;

import com.mocafelab.api.enums.EventKind;

/**
 * 디바이스의 슬롯의 광고 id별 로그 
 * @author pps8853
 */
public class DeviceLog {

	private Integer exposureTime;
	
	private String sgKind;
	
	private long sgId;
	
	private long slotId;
	
	private TreeMap<Integer, LogData> logDataMap; // 같은 슬롯, 같은 광고에 대해서 순차적으로 시작 ~ 종료 데이터를 담는 맵

	public DeviceLog(String sgKind, long sgId, long slotId) {
		this.sgKind = sgKind;
		this.sgId = sgId;
		this.slotId = slotId;
	}

	/**
	 * 같은 슬롯의 같은 광고id를 갖는 데이터에 대해서 마지막 광고 데이터를 갖는 객체 리턴
	 * @return
	 */
	private LogData getLogData() {
		if (logDataMap == null) {
			logDataMap = new TreeMap<>();
		}
		
		for (int key : logDataMap.keySet()) {
			LogData logData = logDataMap.get(key);
			// 현재 맵의 데이터가 마지막 데이터라면
			if (logDataMap.get(++key) == null) {
				return logData;
			}
		}
		
		// 맵에 키에 해당하는 데이터가 하나도 없을 시 새로운 맵 생성
		LogData logData = new LogData();
		logDataMap.put(0, logData);
		return logData;
	}
	
	/**
	 * 새 logData를 마지막 키 + 1에 put
	 * @param newLogData
	 */
	public void logDataMapPutData(LogData newLogData) {
		for (int key : logDataMap.keySet()) {
			if (logDataMap.get(++key) == null) { // 마지막 키 + 1에 새로운 데이터 삽입
				logDataMap.put(key, newLogData);
			}
		}		
	}

	/**
	 * 로그 데이터 저장
	 * @param eventMap
	 * @param eventKind
	 * @return
	 */
	public boolean saveEventTraffic(Map<String, Object> eventMap, String eKind) {
		LogData logData = getLogData();
		EventKind eventKind = EventKind.getEventKind(eKind);
		
		return logData.saveLog(this, eventMap, eventKind);
	}

	/**
	 * key 생성
	 * @return
	 */
	public String getConfirmKey() {
		return sgKind + "_" + sgId + "_" + slotId;
	}
	
	public String getSgKind() {
		return sgKind;
	}

	public long getSgId() {
		return sgId;
	}

	public long getSlotId() {
		return slotId;
	}
	
	public TreeMap<Integer, LogData> getLogDataMap() {
		return logDataMap;
		
	}

	public Integer getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(Integer exposureTime) {
		this.exposureTime = exposureTime;
	}
}
