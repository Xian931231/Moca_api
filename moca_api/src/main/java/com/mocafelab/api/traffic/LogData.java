package com.mocafelab.api.traffic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mocafelab.api.enums.EventKind;

import lombok.Getter;

@Getter
public class LogData {
	
	private Map<String, Object> startLog;
	
	private List<Map<String, Object>> pauseLogList = new ArrayList<>();
	
	private List<Map<String, Object>> resumeLogList = new ArrayList<>();
	
	private Map<String, Object> endLog;
	
	private Map<String, Object> cancelLog;
	
	/**
	 * DeviveLog.logDataMap에 로그 저장
	 * @param deviceLog
	 * @param eventMap
	 * @param eventKind
	 * @return
	 */
	public boolean saveLog(DeviceLog deviceLog, Map<String, Object> eventMap, EventKind eventKind) {
		// 광고 이벤트가 C: CPC일 때
		if (eventKind.equals(EventKind.C)) {
			// TODO 추후 CPC 추가 시 처리
		}
		// 광고 이벤트가 이미지, 영상 노출 시작
		else if (eventKind.equals(EventKind.DS) || eventKind.equals(EventKind.PS)) {
			setStartLogData(deviceLog, eventMap, eventKind);
		}
		// 중지, 재시작, 중단
		else { 
			setOtherLogData(deviceLog, eventMap, eventKind);
			
			// 광고 이벤트가 PE: 이미지 노출 종료, PE: 영상 재생 종료
			if (eventKind.equals(EventKind.DE) || eventKind.equals(EventKind.PE)) {
				// 종료 데이터가 들어왔을 때 시작 데이터가 존재하면 광고 처리
				if (isExistStart()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 광고 시작 이벤트 로그 데이터 set
	 * @param deviceLog
	 * @param eventMap
	 * @param eventKind
	 */
	private void setStartLogData(DeviceLog deviceLog, Map<String, Object> eventMap, EventKind eventKind) {
		// 시작 이전의 데이터가 존재하면 에러이므로 새 데이터 생성 후 로그 저장
		if (isExistStart() || isExistEnd() || isExistCancel() || isExistResume() || isExistPause()) {
			LogData newLogData = new LogData();
			setLogDataByEventKind(newLogData, eventMap, eventKind);
			deviceLog.logDataMapPutData(newLogData);
			return;
		}
		
		// 이전 데이터가 없으면 객체에 시작 데이터를 넣어줌(첫 데이터 일 때)
		setLogDataByEventKind(this, eventMap, eventKind);
	}
	/**
	 * 광고 시작 이벤트 외의 로그 데이터 set
	 * @param deviceLog
	 * @param eventMap
	 * @param eventKind
	 */
	private void setOtherLogData(DeviceLog deviceLog, Map<String, Object> eventMap, EventKind eventKind) {
		// 중단 데이터나 종료데이터가 존재하면 이전 데이터와 다른 사이클의 데이터임
		if (isExistCancel() || isExistEnd()) {
			LogData newLogData = new LogData();
			setLogDataByEventKind(newLogData, eventMap, eventKind); // log
			deviceLog.logDataMapPutData(newLogData); // 맵에 put
			return;
		}
		
		// 종료 데이터가 존재하지 않으면 기존 데이터에 더함
		setLogDataByEventKind(this, eventMap, eventKind);
	}

	/**
	 * 광고 이벤트별 광고 데이터 add
	 * @param logData
	 * @param eventMap
	 * @param eventKind
	 */
	private void setLogDataByEventKind(LogData logData, Map<String, Object> eventMap, EventKind eventKind) {
		switch (eventKind) {
			case DS: case PS:
				logData.addStartLog(eventMap);
				break;
			case DP: case PP:
				logData.addPauseLog(eventMap);
				break;
			case PR:
				logData.addResumeLog(eventMap);
				break;
			case PC:
				logData.addCancelLog(eventMap);
				break;
			case DE: case PE:
				logData.addEndLog(eventMap);
				break;
			default:
				break;
		}
	}
	
	/**
	 * 로그의 시작 ~ 종료 pair가 맞아 노출시간이 필요한지 체크
	 */
	public boolean isPairLog() {
		// 중단 데이터가 존재하지 않으면서 시작 - 종료 pair 데이터가 존재할 때
		if (!isExistCancel() && isSamePauseAndResumeSize()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 처리할 시작 ~ 종료 데이터를 list에 add
	 * @return
	 */
	public List<Map<String, Object>> createListByLogData() {
		List<Map<String, Object>> list = new ArrayList<>();
		if (isExistStart()) {
			list.add(startLog);
		}

		if (isExistEnd()) {
			list.add(endLog);
		}

		if (isExistCancel()) {
			list.add(cancelLog);
		}

		pauseLogList.forEach(p -> list.add(p));
		resumeLogList.forEach(r -> list.add(r));

		return list;
	}

	
	/**
	 * 로그 처리
	 * @param deviceLog
	 * @return
	 */
	public LogResponse processingLog(DeviceLog deviceLog) {
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> processingLogDataList = createListByLogData();
		LogStatusCode logStatusCode = LogStatusCode.ABNORMAL;
		paramMap.put("display_time", 0);

		// 시작 - 종료 pair가 맞을 때 (정상 or 오류 에러)
		if (isExistStartToEnd()) {
			// 중지 - 재시작 데이터가 같으면 
			if (isSamePauseAndResumeSize()) {
				Integer actualitySgTime = calculateActualitySgTime(deviceLog.getExposureTime()); // 실제 노출 시간

				// 이상 로그 (재시작 시간이 중지 시간보다 빠름)
				if (actualitySgTime ==	 null) {
					setParamMap(paramMap, startLog, startLog, logStatusCode);
					return LogResponse.of(processingLogDataList, paramMap, logStatusCode);
				}

				return LogResponse.of(processingLogDataList, paramMap, setParamByExposureTime(deviceLog, actualitySgTime, paramMap));
			}

			// 중지 - 재시작 데이터 사이즈가 다를 때 (이상 에러)
			setParamMap(paramMap, startLog, endLog, logStatusCode);

			if (isPauseListBiggerThanResumeList()) {
				paramMap.put("display_start_date", startLog.get("event_date"));
			} else {
				paramMap.put("display_start_date", endLog.get("event_date"));
			}

			return LogResponse.of(processingLogDataList, paramMap, logStatusCode);
		}

		// 시작 데이터만 존재할 때 (종료 x)
		if (isExistStart()) {
			// 중지 데이터나 중단 데이터가 존재하면 display_start_date에 startMap의 event_date를 설정
			if (isExistPause()) {
				paramMap.put("display_start_date", getEventDateTime(getPauseLogList().get(0)));
				setParamMap(paramMap, startLog, startLog, logStatusCode);
				return LogResponse.of(processingLogDataList, paramMap, logStatusCode);
			}
		}

		// 종료 데이터만 존재할 때 (시작 x)
		if (isExistEnd()) {
			// 재시작 데이터가 있으면 display_end_date에 endMap의 event_date를 설정
			if (isExistResume()) {
				paramMap.put("display_end_date", getEventDateTime(endLog));
				setParamMap(paramMap, getResumeLogList().get(0), endLog, logStatusCode);
				return LogResponse.of(processingLogDataList, paramMap, logStatusCode);
			}
		}

		// 그 외 모두 이상 에러 (시작 - 종료 데이터가 모두 없거나 등)
		LocalDateTime prevTime = LocalDateTime.now();
		Map<String, Object> processingMap = new HashMap<>();

		// 가장 이전의 날짜의 데이터를 구함
		for (Map<String, Object> eventMap : processingLogDataList) {
			// 가장 이전의 날짜의 데이터를 구함
			if (getEventDateTime(eventMap).isBefore(prevTime)) {
				processingMap = eventMap;
			}
		}

		setParamMap(paramMap, processingMap, processingMap, logStatusCode);
		return LogResponse.of(processingLogDataList, paramMap, logStatusCode);
	}
	
	/**
	 * 노출 시간 오차범위에 따른 파라미터 set
	 * @param logData
	 * @param actualitySgTime
	 * @param paramMap
	 */
	private LogStatusCode setParamByExposureTime(DeviceLog deviceLog, int actualitySgTime, Map<String, Object> paramMap) {
		LogStatusCode lotStatusCode = LogStatusCode.SUCCESS;
		int exposureTime = deviceLog.getExposureTime(); // 광고 노출 시간
		paramMap.put("display_time", actualitySgTime);

		// 노출시간이 오차범위 안이면
		if (isRangeOfExposureTime(actualitySgTime, exposureTime)) {
			setParamMap(paramMap, startLog, endLog, lotStatusCode);
			return lotStatusCode;
		}
		// 오차범위 밖이면
		lotStatusCode = LogStatusCode.ERROR;
		setParamMap(paramMap, startLog, endLog, lotStatusCode);
		return lotStatusCode;
	}

	/**
	 * paramMap 파라미터 set
	 * @param paramMap
	 * @param startMap
	 * @param endMap
	 * @param logStatusCode
	 */
	private void setParamMap(Map<String, Object> paramMap, Map<String, Object> startMap, Map<String, Object> endMap, LogStatusCode logStatusCode) {
		paramMap.putAll(endMap);
		paramMap.put("event_date", startMap.get("event_date"));
		paramMap.put("event_log_status", "R");
		// ad_event_traffic row의 event_kind 값이 있으므로 반드시 덮어써야함
		paramMap.put("event_kind", "D");

		switch (logStatusCode) {
			case SUCCESS: // 성공
				paramMap.put("log_status", "S");
				paramMap.put("display_start_date", startMap.get("event_date"));
				paramMap.put("display_end_date", endMap.get("event_date"));
				break;
			case ERROR: // 오류
				paramMap.put("error_kind", "E");
				paramMap.put("log_status", "E");
				paramMap.put("display_start_date", startMap.get("event_date"));
				paramMap.put("display_end_date", endMap.get("event_date"));
				break;
			case ABNORMAL: // 이상
				paramMap.put("error_kind", "A");
				paramMap.put("log_status", "E");
				break;
			default:
				break;
		}
	}
	
	/**
	 * 두 시간의 차이 계산
	 * @return
	 */
	private int calculateTimeDiff() {
		LocalDateTime startDate = getEventDateTime(startLog);
		LocalDateTime endDate = getEventDateTime(endLog);

		Duration startToEnd = Duration.between(startDate, endDate);
		long startToEndSec = startToEnd.getSeconds();

		return (int) startToEndSec;
	}

	/**
	 * 두 시간의 차이 계산
	 * @param startLog
	 * @param endLog
	 * @return
	 */
	private double calculateTimeDiff(Map<String, Object> startLog, Map<String, Object> endLog) {
		LocalDateTime startDate = getEventDateTime(startLog);
		LocalDateTime endDate = getEventDateTime(endLog);

		long startToEndSecLong = ChronoUnit.MILLIS.between(startDate, endDate);
		
		return (double) startToEndSecLong / 1000;
	}

	/**
	 * 실제 광고 시간이 설정된 노출 시간 오차 범위 안에 있는지 체크
	 * @param actualitySgTime
	 * @param exposureTime
	 * @return
	 */
	private boolean isRangeOfExposureTime(long actualitySgTime, int exposureTime) {
		return (exposureTime >= actualitySgTime && actualitySgTime >= (exposureTime - 3)) || (exposureTime <= actualitySgTime && actualitySgTime <= (exposureTime + 3));
	}
	
	private Integer calculateActualitySgTime(int exposureTime) {
		if (isPauseAndResumeSizeEqualZero()) {
			return calculateTimeDiff();
		}
		
		double actualitySgTime = 0;
		int pauseLogListSize = pauseLogList.size();
		// 실제 광고 시간 계산
		for (int i = 0; i < pauseLogListSize; i++) {
			LocalDateTime pauseDateTime = getEventDateTime(pauseLogList.get(i));
			LocalDateTime resumeDateTime = getEventDateTime(resumeLogList.get(i));
			// 중지 시간이 재시작 시간보다 이전이 아니면
			if (!pauseDateTime.isBefore(resumeDateTime)) {
				return null;
			}
			
			if (pauseLogListSize == 1) { // 중지 - 재시작 pair 사이즈가 1
				double startToPauseSec = calculateTimeDiff(startLog,  pauseLogList.get(i));
				double resumeToEndSec = calculateTimeDiff(resumeLogList.get(i), endLog);
				actualitySgTime = (int) (startToPauseSec + resumeToEndSec);
			} else if (pauseLogListSize == 2) { // 중지 - 재시작 pair 사이즈가 2
				if (i == 0) {
					double startToPauseSec = calculateTimeDiff(startLog, pauseLogList.get(i));
					double resumeToPauseSec = calculateTimeDiff(resumeLogList.get(i), pauseLogList.get(i + 1));
					actualitySgTime = (int) (startToPauseSec + resumeToPauseSec);
				} else if (i == (pauseLogListSize - 1)) {
					double resumeToEndSec = calculateTimeDiff(resumeLogList.get(i), endLog);
					actualitySgTime += resumeToEndSec;
				}
			} else { // 중지 - 재시작 pair 사이즈가 3 이상 
				if (i == 0) {
					double startToPauseSec = calculateTimeDiff(startLog, pauseLogList.get(i));
					actualitySgTime += startToPauseSec;
				} else if (i < (pauseLogListSize - 1)) {
					double resumeToPauseSec = calculateTimeDiff(resumeLogList.get(i - 1), pauseLogList.get(i));
					actualitySgTime += resumeToPauseSec;
				} else { 
					double resumeToPauseSec = calculateTimeDiff(resumeLogList.get(i - 1), pauseLogList.get(i));
					double resumeToEndSec = calculateTimeDiff(resumeLogList.get(i), endLog);
					actualitySgTime = actualitySgTime + resumeToPauseSec + resumeToEndSec;
				}
			}
		}
		
		// 노출시간보다 작으면 내림, 노출 시간보다 크면 올림
		if ((int) actualitySgTime <= exposureTime) {
			return (int) Math.floor(actualitySgTime);
		} else {
			return (int) Math.ceil(actualitySgTime);
		}
	}

	/**
	 * row의 시간컬럼을 LocalDateTime으로 변환
	 * @param eventMap
	 * @return
	 */
	public LocalDateTime getEventDateTime(Map<String, Object> eventMap) {
		Date eventDateTemp = (Date) eventMap.get("event_date"); // 실제 광고 event time
		return LocalDateTime.ofInstant(eventDateTemp.toInstant(), ZoneId.systemDefault());
	}
	
	private void addStartLog(Map<String, Object> startLog) {
		this.startLog = startLog;
	}
	
	private void addPauseLog(Map<String, Object> pauseMap) {
		this.pauseLogList.add(pauseMap);
	}
	
	private void addResumeLog(Map<String, Object> resumeMap) {
		this.resumeLogList.add(resumeMap);
	}
	
	private void addEndLog(Map<String, Object> endLog) {
		this.endLog = endLog;
	}
	
	private void addCancelLog(Map<String, Object> cancelLog) {
		this.cancelLog = cancelLog;
	}

	// 시작 데이터가 존재하는지 확인
	private boolean isExistStart() {
		return startLog != null;
	}

	// 종료 데이터가 존재하는지 확인
	private boolean isExistEnd() {
		return endLog != null;
	}

	// 중단 데이터가 존재하는지 확인
	private boolean isExistCancel() {
		return cancelLog != null;
	}

	// 중지 데이터가 존재하는지 확인
	private boolean isExistPause() {
		return pauseLogList.size() != 0;
	}

	// 중지 데이터가 존재하는지 확인
	private boolean isExistResume() {
		return resumeLogList.size() != 0;
	}

	// 시작 - 종료 로그의 pair가 맞을 때
	private boolean isExistStartToEnd() {
		return startLog != null && endLog != null;
	}

	// 중지, 재시작 데이터가 없는지 확인
	private boolean isPauseAndResumeSizeEqualZero() {
		return pauseLogList.size() == 0 && resumeLogList.size() == 0;
	}

	// 중지, 재시작 데이터가 pair 사이즈가 같은 지 확인
	private boolean isSamePauseAndResumeSize() {
		return (pauseLogList.size() == resumeLogList.size());
	}

	// 중지 리스트 사이즈가 재시작 리스트 사이즈보다 더 클 때
	private boolean isPauseListBiggerThanResumeList() {
		return pauseLogList.size() > resumeLogList.size();
	}
}
