package com.mocafelab.api.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.mocafelab.api.device.ApiDeviceMapper;
import com.mocafelab.api.enums.SgKind;
import com.mocafelab.api.traffic.LogManager;
import com.mocafelab.api.traffic.TrafficService;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.DateUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class LogService {

	@Value("${log.message.success}")
	private String LOG_MESSAGE_SUCCESS;
	
	@Value("${batch.path.default}")
	private String DEFAULT_FILE_PATH;
	
	@Value("${file.path.ad.event.log}")
	private String AD_EVENT_LOG_FILE_PATH;
	
	@Value("${log.message.success}")
	private String LOG_SUCCESS_MESSAGE;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private LogMapper logMapper;
	
	@Autowired
	private ApiDeviceMapper apiDeviceMapper;
	
	@Autowired
	private LogClassificationService logClassificationService;

	@Autowired
	private TrafficService trafficService;
	
	public Map<String, Object> addTestData(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> getDeviceApiKey = logMapper.getDeviceApiKey(param);
		Map<String, Object> getAreaCode = logMapper.getAreaCode(param);
		if(CommonUtil.checkIsNull(getDeviceApiKey) || CommonUtil.checkIsNull(getAreaCode)) {
			throw new RuntimeException();
		}
		param.putAll(getDeviceApiKey);
		param.putAll(getAreaCode);
		
		int count = Integer.parseInt(String.valueOf(param.get("count")));
		
		for(int i=0; i<count; i++) {
			logMapper.addTestTrafficData(param);
			logMapper.addTestEventData(param);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그 생성 ( 단일 ) 
	 * @param param
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	public Map<String, Object> addSingleLog(Map<String, Object> param, HttpServletRequest request) throws IOException {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> logData = new LinkedHashMap<String, Object>();
		
		String event_date = (String) param.get("event_date");
		String ip = CommonUtil.getRemoteIP(request);
		String sg_id = (String) param.get("sg_id");
		String slot_id = (String) param.get("slot_id");
		
		String sg_kind = "";
		
		String api_key = request.getHeader("app-key");
		String refer_url = request.getHeader("referer");
		String device = (String) param.get("device_id"); // 시리얼 넘버?
		String event_kind = (String) param.get("event_kind");
		
		
		logData.put("event_date", event_date);
		logData.put("cip", ip);
		logData.put("sg_id", sg_id);
		logData.put("slot_id", slot_id);
		logData.put("sg_kind", sg_kind);
		logData.put("api_key", api_key);
		logData.put("refer_url", refer_url);
		logData.put("device", device);
		logData.put("event_kind", event_kind);
		
		writeAdEventLog(logData);
		if(logMapper.addSingleLog(logData) < 0) {
			return respMap.getResponse(Code.ERROR);
        }
		
		return respMap.getResponse();
	}
	
	/**
	 * adEventLog 파일 기록하기
	 * @param param
	 * @throws IOException
	 */
	private void writeAdEventLog(Map<String, Object> param) throws IOException {
		File dir = new File(DEFAULT_FILE_PATH + AD_EVENT_LOG_FILE_PATH);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		String currDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
		param.put("insert_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		
		File file = new File(dir + "/" + currDate + ".log");
		
		Gson gson = new Gson();
		String json = gson.toJson(param);
		
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
	
		bw.write(json);
		bw.newLine();
		bw.close();
	}
	
	/**
	 * 로그 생성 ( 더미 ) / 1030 수정
	 * @param param
	 * @param request
	 * @return
	 * @throws ParseException 
	 */
	public Map<String, Object> addDumyLog(Map<String, Object> param, HttpServletRequest request) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int count = Integer.parseInt((String) param.get("count"));
		int errCount = Integer.parseInt((String) param.get("err_count"));
		if(count < errCount) {
			return respMap.getErrResponse(Code.ERROR);
		}
		
		List<Map<String, Object>> sgList = logMapper.getSgInfo(param);
		
		String imgEventKind[] = {"DS", "DE"};
		String vidEventKind[] = {"PS", "PE"};
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		param.put("refer_url", request.getHeader("referer"));
		
		Random rand = new Random();
		// 1. DS -> DE 정상 로그
		// 2. DS / DE 에러 로그
		// 3. 3초 이상 이상 로그
		int j = 0;
		while(j<errCount) { // 이상 / 에러 데이터
			Map<String, Object> sgInfo = sgList.get(rand.nextInt(sgList.size()));
			if(sgInfo.get("sg_kind").equals("A")) {
				Map<String, Object> cpoint = logMapper.getRandomCpoint(sgInfo);
				sgInfo.putAll(cpoint);
			}
			sgInfo.putAll(param);
			
			Map<String, Object> startEvent = new HashMap<>();
			Map<String, Object> endEvent = new HashMap<>();
			startEvent.putAll(sgInfo);
			endEvent.putAll(sgInfo);
			
			int randNum = (int) (Math.random() * 2);
			String startEventKind = "";

			Date errDate = randomDate(param);
			startEvent.put("event_date", errDate);
			
			if(sgInfo.get("material_kind").equals("IMAGE")) {
				if(randNum == 0) { // 이상 데이터
					startEventKind = imgEventKind[rand.nextInt(imgEventKind.length)];
				} else if(randNum == 1) { // 오류 데이터 +3초 이상
					startEventKind = "DS";
					endEvent.put("event_kind", "DE");
				}
			} else if(sgInfo.get("material_kind").equals("VIDEO")) {
				if(randNum == 0) { // 이상 데이터
					startEventKind = vidEventKind[rand.nextInt(vidEventKind.length)];
				} else if(randNum == 1) { // 오류 데이터 +3초 이상
					startEventKind = "PS";
					endEvent.put("event_kind", "PE");
				}
			}
			startEvent.put("event_kind", startEventKind);
			
			Calendar cal = Calendar.getInstance();
	        cal.setTime(errDate);
			
	        int exposureTime = (int) sgInfo.get("exposure_time") * 1000;
			cal.add(Calendar.MILLISECOND, exposureTime + (int) (Math.random() * 60000) + 3000);
			endEvent.put("event_date", sdf.format(cal.getTime()));
			
			j++;
			Map<String, Object> startLogData = logMapper.addDumyLog(startEvent);
			if(j == errCount) {
				break;
			}
			if(randNum == 1) {
				j++;
				endEvent.putAll(startLogData);
				logMapper.addDumyLog(endEvent);
			}
		}
		
		int i = errCount;
		while(i<count) { // 정상 데이터
			Map<String, Object> sgInfo = sgList.get(rand.nextInt(sgList.size()));
			if(sgInfo.get("sg_kind").equals("A")) {
				Map<String, Object> cpoint = logMapper.getRandomCpoint(sgInfo);
				sgInfo.putAll(cpoint);
			}
			sgInfo.putAll(param);
			
			Map<String, Object> startEvent = new HashMap<>();
			Map<String, Object> endEvent = new HashMap<>();
			startEvent.putAll(sgInfo);
			endEvent.putAll(sgInfo);
			
			String startEventKind = "";
			
			// 정상 데이터 로그
			if(sgInfo.get("material_kind").equals("IMAGE")) {
				startEventKind = "DS";
				endEvent.put("event_kind", "DE");
			} else if(sgInfo.get("material_kind").equals("VIDEO")) {
				startEventKind = "PS";
				endEvent.put("event_kind", "PE");
			}
			startEvent.put("event_kind", startEventKind);
			// 랜덤 날짜
			Date date = randomDate(param);
			startEvent.put("event_date", date);
			
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        
	        int exposureTime = (int) sgInfo.get("exposure_time") * 1000;
        	cal.add(Calendar.MILLISECOND, exposureTime + (int) (Math.random() * 3000));
        	endEvent.put("event_date", sdf.format(cal.getTime()));
			
        	i++;
			Map<String, Object> startLogData = logMapper.addDumyLog(startEvent);
			if(i==count) {
				break;
			}
			i++;
			endEvent.putAll(startLogData);
			logMapper.addDumyLog(endEvent);
		}
		return respMap.getResponse();
	}
	
	// 랜덤 시간
	public Date randomDate(Map<String, Object> param) throws ParseException {
		String dateMin = (String) param.get("event_date_min");
		String dateMax = (String) param.get("event_date_max");

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String event_day = "";
		
		if(!dateMin.equals(dateMax)) {
			LocalDate startDate = LocalDate.parse(dateMin, dtf);
			LocalDate endDate = LocalDate.parse(dateMax, dtf);
			
			List<LocalDate> list = DateUtil.getDatesBetweenTwoDates(startDate, endDate);
			if(list == null) { // min값이 더 큰 경우 error
				return null;
			}
			event_day = list.get((int) (Math.random() * list.size())).toString();
		} else {
			event_day = dateMin;
		}
		
		// 랜덤 시간
		String randomTime = (int) (Math.random() * 24) + ":" + 
							(int) (Math.random() * 60) + ":" + 
							(int) (Math.random() * 60) + "." +
							(int) (Math.random() * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = sdf.parse(event_day + " " + randomTime);
		
		return date;
	}
	
	
	/**
	 * ad_event_traffic -> ad_event_log 로 수시로 동작할 배치
	 * @param param
	 * @return
	 */
	public Map<String, Object> sgEventLogBatch(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		param.put("batch_code", "BC0001");
		
		Map<String,Object> batchMonitor = logMapper.getBatchMonitor(param);
		if (CommonUtil.checkIsNull(batchMonitor)) {
			return responseMap.getErrResponse(Code.BAD_REQUEST);
		}
		
		// batch_monitor의 last_index 는 event_date로 설정하는 것이 좋을 듯..
		String lastIndex = String.valueOf(batchMonitor.get("last_index"));
		String recordSize = String.valueOf(batchMonitor.get("record_size"));
		if (!recordSize.equals("0")) {
			param.put("record_size", recordSize);
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime currentTime = LocalDateTime.now();
		String batchDate = currentTime.format(dtf);
		
		if (!CommonUtil.checkIsNull(param, "daily")) {
			lastIndex = String.valueOf(param.get("today_last_index"));
			batchDate = String.valueOf(param.get("today_batch_date"));
		}
		
		if (lastIndex.equals("null")) {
			// 배치 관리 테이블에 마지막 배치일이 없을 시 전날부터 배치
			lastIndex = currentTime.minusDays(1).format(dtf);
		}
		
		param.put("last_index", lastIndex);
		param.put("batch_date", batchDate);
		param.put("traffic_status", "R");

		LogManager logManager = new LogManager();
		
		List<Map<String,Object>> sgEventTrafficList = logMapper.getSgEventTrafficList(param);
		
		for (Map<String, Object> eventMap : sgEventTrafficList) {
			// 로그 저장
			trafficService.saveEventTraffic(logManager, eventMap);

			// 시작 - 종료 pair가 맞으면 처리
			if (logManager.isResult()) {
				trafficService.processingEventTraffic(logManager);
			}
		}
		
		// 일일 배치 시 pair가 맞지 않았던 데이터까지 모두 처리 
		if (!CommonUtil.checkIsNull(param, "daily")) {
			trafficService.processingRemainLog(logManager);
			logManager.setLastIndex(batchDate);
		} else {
			// 배치에 처리된 로그가 하나도 없으면 마지막 라스트 인덱스 유지(네트워크 끊김으로 로그가 안올 수 있음)
			if (logManager.getTotalRecord() < 1) {
				logManager.setLastIndex(lastIndex);
			} else {
				trafficService.setLastIndex(logManager, currentTime, dtf);
			}
		}
		
		// 배치 모니터 갱신
		Map<String, Object> batchMap = Map.of(
				"batch_code", String.valueOf(batchMonitor.get("batch_code")),
				"result", "Success",
				"result_message", LOG_SUCCESS_MESSAGE,
				"total_record", logManager.getTotalRecord(),
				"last_index", logManager.getLastIndex(),
				"log_path", String.valueOf(batchMonitor.get("log_path"))
		);
		
		if (logMapper.modifyBatchMonitor(batchMap) < 1) {
			throw new RuntimeException();
		}
		
		// 로그 파일 저장
		try {
			logClassificationService.writeClassifiyLog(batchMap, new HashMap<>());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return responseMap.getResponse();
	}

	/**
	 * ad_event_traffic -> ad_event_log 로 하루에 한번 동작할 배치
	 * @param param
	 * @return
	 */
	public Map<String, Object> sgEventLogBatchAll(Map<String, Object> param) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		LocalDateTime currentTime = LocalDateTime.now();
		String todayLastIndex = LocalDateTime.of(currentTime.toLocalDate().minusDays(1), LocalTime.of(0, 0, 0)).format(dtf);
		String todayBatchDate = LocalDateTime.of(currentTime.toLocalDate(), LocalTime.of(0, 0, 0)).format(dtf);
		
		param.put("today_last_index", todayLastIndex);
		param.put("today_batch_date", todayBatchDate);
		param.put("daily", true);
		
		return sgEventLogBatch(param);
	}
	
	/**
	 * ad_event_log => ad_count 배치
	 * @return
	 */
	public Map<String, Object> sgCountLogBatch(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		param.put("batch_code", "BC0002");
		
		// 배치코드로 배치 모니터 테이블 조회
		Map<String, Object> batchMonitor = logMapper.getBatchMonitor(param);
		if (CommonUtil.checkIsNull(batchMonitor)) {
			return responseMap.getErrResponse(Code.BAD_REQUEST);
		}
		
		DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		String startDate = "";
		String endDate = LocalDateTime.now().format(datePattern);
		
		// 배치 모니터 테이블에 last_index 유무 확인
		if (!CommonUtil.checkIsNull(batchMonitor, "last_index")) {
			startDate = (String) batchMonitor.get("last_index");
		} else { 
			startDate = logMapper.getOldestSgEventLog();
		}
		
		if (startDate == null || startDate.equals("")) {
			// 배치 관리 테이블에 마지막 배치일이 없을 시 전날부터 배치
			startDate = LocalDateTime.now().minusDays(1).format(datePattern);
		}
		
		// 시작일의 존재유무와 종료일보다 앞인지 체크
		logClassificationService.dateCheck(startDate, endDate, datePattern);
		
		// 배치 시작 ~ 종료 리스트
		List<LocalDate> fromToDates = logClassificationService.getFromToDate(startDate, endDate, datePattern);
		
		String logPath = String.valueOf(batchMonitor.get("log_path"));;
		String batchCode = String.valueOf(batchMonitor.get("batch_code"));
		Code code = Code.OK;
		String resultMessage = LOG_SUCCESS_MESSAGE;
		int totalRecord = 0;
		String lastModifiedIndex = startDate;
		
		// 시작일 -> 종료일 배치
		for (LocalDate fromToDate : fromToDates) {
			param.put("event_date", fromToDate);
			lastModifiedIndex = fromToDate.format(datePattern);
			List<Map<String, Object>> replaceSgMapList = new ArrayList<>();
			
			// 해당 일자의 이전 데이터 제거
			logMapper.removeSgCount(param);
			
			// 일자별 ad_event_log 배치 
			List<Map<String,Object>> sgEventLogList = logMapper.getSgEventLogList(param);
			
			for (Map<String, Object> sgEventLog : sgEventLogList) {
				// ad_count log_status R인 것 S로 업데이트 필요
				String logStatus = String.valueOf(sgEventLog.get("log_status"));
				String sgKind = String.valueOf(sgEventLog.get("sg_kind"));
				
				int result = logMapper.saveSgCount(sgEventLog);
				
				// 정상 처리로 변경된 로그 
				if (logStatus.equals("R")) {
					if (sgKind.equals(SgKind.AREA.getDesc()) || sgKind.equals(SgKind.TIME.getDesc()) || sgKind.equals(SgKind.CPM.getDesc())) { // CPM일 시 광고 수 차감
						// R 상태의 광고가 존재하면 schedule_table_sg, dsp_sg_manager의 해당 광고 카운트를 갱신해줘야
						Map<String, Object> replaceLogMap = new HashMap<>();
						replaceLogMap.put("sg_id", sgEventLog.get("sg_id"));
						replaceLogMap.put("event_date", sgEventLog.get("event_date"));
						replaceLogMap.put("display_end_date", sgEventLog.get("display_end_date"));
						replaceSgMapList.add(replaceLogMap);
					}
				}
				
				String status = String.valueOf(sgEventLog.get("status"));
				if (status != null && status.equals("R")) {
					// ad_event_log status = C 로 변환
					sgEventLog.put("change_status", "C");
					if (logMapper.modifyAdEventLog(sgEventLog) < 1) {
						throw new RuntimeException();
					}
				}
				
				if (result < 1) {
					code = Code.ERROR;
					resultMessage = Code.BATCH_UPDATE_FAIL.msg;
					break;
				}
				
				totalRecord += result;
			}

			// 중간에 실패 시 배치 갱신 종료
			if (code == Code.ERROR) {
				break;
			}
			
			modifySgRemainCount(fromToDate, replaceSgMapList);
		}
		
		String resultCode = code == Code.OK ? "Success" : "Fail";
		
		// 배치 모니터 갱신
		Map<String, Object> batchMap = Map.of(
				"batch_code", batchCode,
				"result", resultCode,
				"result_message", resultMessage,
				"total_record", totalRecord,
				"last_index", lastModifiedIndex,
				"log_path", logPath
				);

		if (logMapper.modifyBatchMonitor(batchMap) < 1) {
			throw new RuntimeException();
		}
		
		// 로그 파일 저장
		try {
			logClassificationService.writeClassifiyLog(batchMap, new HashMap<>());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return responseMap.getResponse();
	}

	/**
	 * log_status = R로 갱신된 광고의 노출 수량 갱신
	 * @param fromToDate
	 * @param replaceSgIdList
	 */
	private void modifySgRemainCount(LocalDate fromToDate, List<Map<String, Object>> replaceSgMapList) {
		// log_status = R 인 컬럼 광고 카운트 갱신
		if (replaceSgMapList.isEmpty()) {
			return;
		}
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("schedule_date", fromToDate);
		
		for (Map<String, Object> log : replaceSgMapList) {
			paramMap.put("sg_id", log.get("sg_id"));
			
			Map<String,Object> scheduleTableSg = apiDeviceMapper.getScheduleTableSg(paramMap);
			Map<String,Object> sgCount = logMapper.getSgCount(paramMap);
			
			if (CommonUtil.checkIsNull(scheduleTableSg)) {
				throw new RuntimeException();
			}
			
			if (CommonUtil.checkIsNull(sgCount)) {
				throw new RuntimeException();
			}
			
			int maxCount = Integer.valueOf(String.valueOf(scheduleTableSg.get("max_count"))); // 일자의 최대 노출 수
			int remainCount = Integer.valueOf(String.valueOf(scheduleTableSg.get("remain_count"))); // 일자의 남은 노출 수
			int totalCount = Integer.valueOf(String.valueOf(sgCount.get("total_count"))); // 일자의 총 노출 수 
			
			paramMap.put("prev_exposure_count", (maxCount - remainCount));
			// dsp_sg_manager remain_exposure_count 를 현재 일자의 차감수 만큼 원복
			if (logMapper.plusDspSgManagerRemainCount(paramMap) < 1) {
				throw new RuntimeException();
			}
			
			// 광고 노출량이 현재일자의 광고 재고량보다 작으면
			if (totalCount < maxCount) {
				paramMap.put("remain_count", (maxCount - totalCount));
				paramMap.put("remain_exposure_count", totalCount);
				// remain_count 차감
				if (logMapper.minusScheduleTableSgRemainCount(paramMap) < 1) {
					throw new RuntimeException();
				}
				
				Map<String, Object> currSg = logMapper.minusDspSgManagerRemainCount(paramMap);
				String sgStatus = String.valueOf(currSg.get("status"));
				int remainExposureCount = Integer.valueOf(String.valueOf(currSg.get("remain_exposure_count")));
				
				// 남은 광고량이 다시 남게됐을때 status가 광고 종료라면 display_end_date와 status를 다시 진행중인 광고로 수정 
				if (remainExposureCount > 0 && sgStatus.equals("8")) {
					paramMap.put("sgStatus", "1");
					paramMap.put("display_end_date", null);
					
					if (logMapper.modifyDspSgManagerEndDate(paramMap) < 1) {
						throw new RuntimeException();
					}
				}
				 
			} else { // 광고 노출량이 광고 노출 재고량 이상이면
				paramMap.put("remain_count", 0);
				paramMap.put("excsss_count", (totalCount - maxCount));
				paramMap.put("remain_exposure_count", maxCount);
				
				if (logMapper.minusScheduleTableSgRemainCount(paramMap) < 1) {
					throw new RuntimeException();
				}
				
				Map<String, Object> currSg = logMapper.minusDspSgManagerRemainCount(paramMap);
				
				String sgStatus = String.valueOf(currSg.get("status"));
				int remainExposureCount = Integer.valueOf(String.valueOf(currSg.get("remain_exposure_count")));
				
				// 남은 광고량이 없을 때 status가 진행중인 광고이며 display_end_date와 status를 종료 광고로 수정 
				if (remainExposureCount > 0 && sgStatus.equals("1")) {
					String logDate = String.valueOf(log.get("display_end_date")) == null
							? String.valueOf(log.get("event_date"))
							: String.valueOf(log.get("display_end_date"));
					
					paramMap.put("sgStatus", "8");
					paramMap.put("display_end_date", logDate);
					
					if (logMapper.modifyDspSgManagerEndDate(paramMap) < 1) {
						throw new RuntimeException();
					}
				}
			}
		}
	}
	
}