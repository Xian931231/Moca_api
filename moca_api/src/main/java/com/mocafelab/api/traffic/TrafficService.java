package com.mocafelab.api.traffic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.api.device.ApiDeviceMapper;
import com.mocafelab.api.enums.SgKind;
import com.mocafelab.api.log.LogMapper;

import net.newfrom.lib.util.CommonUtil;

/**
 *	ad_event_traffic 처리 쿨래스 
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TrafficService {
	
	@Autowired
	private ApiDeviceMapper apiDeviceMapper;
	
	@Autowired
	private LogMapper logMapper;

	/**
	 * 광고 로그 저장
	 * @param eventMap: ad_event_traffic row
	 * @return
	 */
	public void saveEventTraffic(LogManager logManager, Map<String, Object> eventMap) {
		String sgKind = String.valueOf(eventMap.get("sg_kind")); // 광고 종류
		long sgId = Long.valueOf(String.valueOf(eventMap.get("sg_id"))); // 광고 id
		long slotId = Long.valueOf(String.valueOf(eventMap.get("slot_id"))); // 슬롯 id
		String eventKind = String.valueOf(eventMap.get("event_kind")); // 이벤트 종류
		String deviceId = String.valueOf(eventMap.get("device")); // 디바이스 시리얼 번호

		Map<String, List<DeviceLog>> device = logManager.getDevice();
		
		List<DeviceLog> deviceLogList = logManager.getDeviceLogList(device, deviceId);
		
		// 광고 종류, 광고id, 슬롯id 를 갖는 클래스
		DeviceLog deviceLog = logManager.getDeviceLog(deviceLogList, sgKind, sgId, slotId);
		
		// 시작 ~ 종료 데이터가 들어있는 객체
		boolean logResult = deviceLog.saveEventTraffic(eventMap, eventKind);
		
		if (logResult) {
			logManager.setSuccessResult(true, deviceId);
		}
	}

	/**
	 * 시작 - 종료 pair 가 맞춰진 device의 광고 이벤트 처리 (logManager result => true일 때)
	 * @param logManager
	 */
	public void processingEventTraffic(LogManager logManager) {
		Map<String, List<DeviceLog>> device = logManager.getDevice();
		String modifyId = logManager.getModifyId(); // 변경할 디바이스 시리얼 번호
		
		List<DeviceLog> deviceDataList = logManager.getDeviceLogList(device, modifyId);
		
		for (DeviceLog deviceLog : deviceDataList) {
			TreeMap<Integer, LogData> logDataMap = deviceLog.getLogDataMap();
			// sgKind + slotId + sgId 별 logData(시작 ~ 종료) Loop
			for (Integer key : logDataMap.keySet()) {
				LogData logData = logDataMap.get(key);

				boolean isPairLog = logData.isPairLog();

				long sgId = deviceLog.getSgId();
				String sgKind = deviceLog.getSgKind();
				Map<String, Object> sgMap = new HashMap<>();
				// pair 가 맞는 로그면 광고 노출 시간을 구해준다.
				if (isPairLog) {
					if (deviceLog.getExposureTime() == null) {
						// 광고 종류로 광고 정보를 얻은 뒤 노출 시간 저장
						sgMap = getSgMap(sgId, sgKind);
						deviceLog.setExposureTime(Integer.valueOf(String.valueOf(sgMap.get("exposure_time"))));
					}
				}

				LogResponse logResponse = logData.processingLog(deviceLog);
				
				// ad_event_traffic status R -> C
				List<Map<String, Object>> processingList = logResponse.getProcessingList();
				logManager.addTotalRecord(processingList.size());
				
				processingList.forEach(log -> modifySgEventTrafficStatus(log));

				// 차량 정보를 paramMap에 set
				Map<String, Object> paramMap = logResponse.getParamMap();
				setMotorDataByParamMap(paramMap);

				// ad_event_log insert
				if (logMapper.addSgEventLog(paramMap) < 1) {
					throw new RuntimeException();
				}

				LogStatusCode logStatusCode = logResponse.getLogStatusCode();
				// 성공 로그이면 CPM광고 노출 수 차감
				if (logStatusCode.equals(LogStatusCode.SUCCESS)) {
					modifySgCount(paramMap, sgKind, sgMap);
				}
			}
		}
	
		// 리스트 모두 처리 후 클리어
		deviceDataList.clear();
		logManager.setResult(false);
	}
	
	/**
	 * ad_event_traffic status 변경
	 * @param eventMap
	 */
	private void modifySgEventTrafficStatus(Map<String, Object> eventMap) {
		Map<String, Object> param = new HashMap<>();
		param.put("traffic_id", eventMap.get("traffic_id"));
		param.put("traffic_status", "C");

		if (logMapper.modifySgEventTraffic(param) < 1) {
			throw new RuntimeException();
		}
	}

	/**
	 * 광고 종류별 광고 데이터(CPM, CPP, PUBLIC, DEFAULT)
	 * @param sgId
	 * @param sgKind
	 * @return
	 */
	private Map<String, Object> getSgMap(long sgId, String sgKind) {
		Map<String, Object> param = Map.of("sg_id", sgId);
		if (sgKind.equals(SgKind.AREA.getDesc()) || sgKind.equals(SgKind.TIME.getDesc()) || sgKind.equals(SgKind.CPM.getDesc()) || sgKind.equals(SgKind.CPP.getDesc())) { // CPM, CPP
			return apiDeviceMapper.getDspSgManager(param);
		} 
		// 공익, 디폴트 광고
		return apiDeviceMapper.getSgService(param);
	}

	/**
	 * 차량 정보 set
	 * @param paramMap
	 */
	private void setMotorDataByParamMap(Map<String, Object> paramMap) {
		// 디바이스 정보 확인
		Map<String,Object> sspDevice = apiDeviceMapper.getSspDevice(paramMap);
		if (CommonUtil.checkIsNull(sspDevice)) {
			throw new RuntimeException();
		}

		paramMap.put("check_product", true);
		paramMap.put("device_product_id", sspDevice.get("product_id"));
		Map<String,Object> productDetail = apiDeviceMapper.getProduct(paramMap);
		if (CommonUtil.checkIsNull(productDetail)) {
			throw new RuntimeException();
		}

		paramMap.put("product_id", productDetail.get("product_id"));
		Map<String, Object> motorDetail = apiDeviceMapper.getMotor(paramMap);
		if (CommonUtil.checkIsNull(motorDetail)) {
			throw new RuntimeException();
		}

		// 차량 정보 갱신
		paramMap.put("car_number", motorDetail.get("car_number"));
		paramMap.put("si_code", motorDetail.get("si_code"));
		paramMap.put("gu_code", motorDetail.get("gu_code"));
		paramMap.put("dong_code", motorDetail.get("dong_code"));
	}

	/**
	 * CPM 광고 카운트 차감 및 광고 시작, 종료 시간 설정
	 * @param paramMap
	 * @param sgKind
	 * @param sgMap
	 */
	private void modifySgCount(Map<String, Object> paramMap, String sgKind, Map<String, Object> sgMap) {
		if (sgKind.equals(SgKind.AREA.getDesc()) || sgKind.equals(SgKind.TIME.getDesc()) || sgKind.equals(SgKind.CPM.getDesc())) { // CPM일 시 광고 수 차감
			Date eventDateTemp = (Date) paramMap.get("event_date"); // 실제 광고 event time
			LocalDateTime ldt = LocalDateTime.ofInstant(eventDateTemp.toInstant(), ZoneId.systemDefault());
			paramMap.put("schedule_date", ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			
			Map<String,Object> scheduleTableSg = apiDeviceMapper.getScheduleTableSg(paramMap);

			// display_start_date 설정
			setDisplayStartDate(paramMap, sgMap);

			if (!CommonUtil.checkIsNull(scheduleTableSg)) {
				int remainCount = Integer.valueOf(String.valueOf(scheduleTableSg.get("remain_count")));

				// 잔여 노출량이 0보다 많이 남아있으면
				if (remainCount > 0) {
					if (logMapper.minusScheduleTableSg(paramMap) < 1) {
						throw new RuntimeException();
					}

					// dsp_sg_manager의 remain_exposure_count를 차감하고 남은 수 리턴
					Integer currRemainExposureCount = logMapper.minusRemainExposureCount(paramMap);
					// sg_manager의 남은 노출 수가 없으면 cpm 광고 종료일 등록
					if (CommonUtil.checkIsNull(sgMap, "display_end_date") && currRemainExposureCount != null && currRemainExposureCount == 0) {
						paramMap.put("sg_status", 8);
						if (logMapper.modifyDspSgManagerEndDate(paramMap) < 1) {
							throw new RuntimeException();
						}
					}
				} else { // 잔여 노출량이 0 이면 초과 노출량 + 1
					if (logMapper.plusExcessCount(paramMap) < 1) {
						throw new RuntimeException();
					}
				}
			}
		} else if (sgKind.equals(SgKind.CPP.getDesc())) { // CPP 광고의 첫 광고이면 시작 시간 설정
			// display_start_date 설정
			setDisplayStartDate(paramMap, sgMap);
		}
	}

	/**
	 * 실제 광고 시작 시간 설정
	 * @param paramMap
	 * @param sgMap
	 */
	public void setDisplayStartDate(Map<String, Object> paramMap, Map<String, Object> sgMap) {
		if (!CommonUtil.checkIsNull(sgMap, "display_start_date")) {
			return;
		}
		
		// 광고 시작 시간이 없으면 첫 광고
		// paramMap.put("display_start_date", endMap.get("event_date"));
		if (logMapper.modifyDspSgManagerStartDate(paramMap) < 1) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * 라스트 인덱스 구하기
	 * @param logManager
	 * @param currTime
	 * @param dtf
	 */
	public void setLastIndex(LogManager logManager, LocalDateTime currTime, DateTimeFormatter dtf) {
		Map<String, List<DeviceLog>> device = logManager.getDevice();

		for (String key : device.keySet()) {
			List<DeviceLog> deviceLogList = device.get(key);

			for (DeviceLog deviceLog : deviceLogList) {
				TreeMap<Integer, LogData> logDataMap = deviceLog.getLogDataMap();

				LogData logData = logDataMap.get(0);

				List<Map<String, Object>> logDataList = logData.createListByLogData();

				for (Map<String, Object> log : logDataList) {
					LocalDateTime eventDateTime = logData.getEventDateTime(log);

					if (eventDateTime.isBefore(currTime)) {
						currTime = eventDateTime;
					}
				}
			}
		}

		logManager.setLastIndex(currTime.format(dtf));
	}

	/**
	 * 미처리 데이터들을 모두 처리
	 * @param logManager
	 */
	public void processingRemainLog(LogManager logManager) {
		Map<String, List<DeviceLog>> device = logManager.getDevice();
		
		for (String key : device.keySet()) {
			List<DeviceLog> deviceLogList = device.get(key);

			for (DeviceLog deviceLog : deviceLogList) {
				TreeMap<Integer, LogData> logDataMap = deviceLog.getLogDataMap();

				for (Integer logDataMapKey : logDataMap.keySet()) {
					LogData logData = logDataMap.get(logDataMapKey);
					
					LogResponse logResponse = logData.processingLog(deviceLog);
					
					// ad_event_traffic status R -> C
					List<Map<String, Object>> processingList = logResponse.getProcessingList();
					logManager.addTotalRecord(processingList.size());
					
					processingList.forEach(log -> modifySgEventTrafficStatus(log));

					// 차량 정보를 paramMap에 set
					Map<String, Object> paramMap = logResponse.getParamMap();
					setMotorDataByParamMap(paramMap);

					// ad_event_log insert
					if (logMapper.addSgEventLog(paramMap) < 1) {
						throw new RuntimeException();
					}
				}

			}
		}
	}
}