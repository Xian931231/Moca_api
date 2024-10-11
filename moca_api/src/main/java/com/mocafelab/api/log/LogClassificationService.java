package com.mocafelab.api.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.DateUtil;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

@Service
@Transactional
public class LogClassificationService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private LogMapper logMapper;
	
	@Value("${batch.path.default}")
	private String DEFAULT_FILE_PATH;
	
	@Value("${log.message.success}")
	private String LOG_SUCCESS_MESSAGE;
	
	/**
	 * 배치 실행
	 * @param param
	 * @return 
	 * @throws IOException 
	 */
	public Map<String, Object> adCountClassification(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		Classification classification = (Classification) param.get("batch_code_enum");
		if(classification != null) {
			param.put("batch_code", classification.getCode());
		} else {
			throw new RuntimeException("베치 코드는 필수입니다.");
		}
		
		// 배치코드로 배치 모니터 테이블 조회
		Map<String, Object> getBatchMonitor = logMapper.getBatchMonitor(param);
		
		batchMonitorCheck(getBatchMonitor);
		
		DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		String startDate = "";
		String endDate = LocalDateTime.now().format(datePattern);
		
		// 배치 모니터 테이블에 last_index 유무 확인
		if (!CommonUtil.checkIsNull(getBatchMonitor, "last_index")) {
			startDate = (String) getBatchMonitor.get("last_index");
		} else { 
			startDate = logMapper.getOldestAdCount();
		}
		
		if (startDate == null || startDate.equals("")) {
			// 배치 관리 테이블에 마지막 배치일이 없을 시 전날부터 배치
			startDate = LocalDateTime.now().minusDays(1).format(datePattern);
		}
		
		// 시작일의 존재유무와 종료일보다 앞인지 체크
		dateCheck(startDate, endDate, datePattern);
		
		// 배치 시작 ~ 종료 리스트
		List<LocalDate> fromToDates = getFromToDate(startDate, endDate, datePattern);
		
		String logPath = (String) getBatchMonitor.get("log_path");
		String batchCode = (String) param.get("batch_code");
		Code code = Code.OK;
		String resultMessage = LOG_SUCCESS_MESSAGE;
		int totalRecord = 0;
		String lastModifiedIndex = startDate;
		
		// 배치 시작일 -> 종료일까지 배치 돌리기
		for (LocalDate fromtoDate : fromToDates) {
			String batchDate = fromtoDate.format(datePattern);
			
			param.put("batch_date", batchDate);
			
			lastModifiedIndex = batchDate;
			
			List<Map<String, Object>> adCountList = getAdCountListByDate(param);
			
			for (Map<String, Object> adCountMap : adCountList) {
				
				adCountMap.put("batch_code_enum", classification);
				
				// insert or update
				int result = saveLogClassification(adCountMap);
				
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
		}
		
		String resultCode = code == Code.OK ? "Success" : "Fail";
		
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
		
		try {
			writeClassifiyLog(batchMap, new HashMap<>());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return responseMap.getResponse();
	}

	/**
	 * 시작날짜의 존재유무와 시작 날짜가 종료 날짜보다 앞서는지 체크
	 * @param startDate
	 * @param endDate
	 */
	public void dateCheck(String startDate, String endDate, DateTimeFormatter datePattern) {
		if (startDate == null) {
			throw new RuntimeException("not exists start date");
		}
		
		LocalDate fromDate = LocalDate.parse(startDate, datePattern);
		LocalDate toDate = LocalDate.parse(endDate, datePattern);
		
		if (fromDate.compareTo(toDate) > 0) {
			throw new RuntimeException("Start date cannot be greater than end date!");
		}
	}

	/**
	 * 배치 모니터 존재여부 체크
	 * @param getBatchMonitor
	 */
	private void batchMonitorCheck(Map<String, Object> getBatchMonitor) {
		if (getBatchMonitor == null) {
			throw new RuntimeException("comfirm batch_code!");
		}
	}

	/**
	 * 분류 테이블에 맞는 리스트 조회
	 * @param tableName
	 * @param param
	 * @return
	 */
	private List<Map<String,Object>> getAdCountListByDate(Map<String, Object> param) {
		Classification batchCode = (Classification) param.get("batch_code_enum");
		switch(batchCode) {
			case AD_SG_PRODUCT_COUNT:
				return logMapper.getAdSgProductCount(param);
			case DEMAND_COUNT_SG:
				return logMapper.getDemandCountSg(param);
			case DEMAND_COUNT_SG_AREA:
				return logMapper.getDemandCountSgArea(param);
			case SSP_AD_COUNT_PRODUCT:
				return logMapper.getSspAdCountProduct(param);
			case SSP_AD_COUNT_PRODUCT_DEVICE:
				return logMapper.getSspAdCountProductDevice(param);
			default:
				throw new RuntimeException("comfirm batch_monitor method!");
		}
	}
	
	/**
	 * 로그 분류 insert or update
	 * @param tableName
	 * @param param
	 * @return
	 */
	private int saveLogClassification(Map<String, Object> param) {
		Classification batchCode = (Classification) param.get("batch_code_enum");
		switch(batchCode) {
			case AD_SG_PRODUCT_COUNT: 
				return logMapper.saveAdSgProductCount(param);
			case DEMAND_COUNT_SG:
				return logMapper.saveDemandCountSg(param);
			case DEMAND_COUNT_SG_AREA:
				return logMapper.saveDemandCountSgArea(param);
			case SSP_AD_COUNT_PRODUCT:
				return logMapper.saveSspAdCountProduct(param);
			case SSP_AD_COUNT_PRODUCT_DEVICE:
				return logMapper.saveSspAdCountProductDevice(param);
			default:
				throw new RuntimeException("comfirm batch_monitor method!");
		}
	}
	
	/**
	 * 로그 파일 쓰기
	 * @param batchMap
	 * @param nativeParam
	 * @throws IOException
	 */
	public void writeClassifiyLog(Map<String, Object> batchMap, Map<String, Object> nativeParam) throws IOException {
		String logPath = DEFAULT_FILE_PATH + (String) batchMap.get("log_path");
		String currDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String result = (String) batchMap.get("result");
		String logParam = createParam(nativeParam);
		int totalRecord = (int) batchMap.get("total_record");
		
		String log = System.lineSeparator() + currDate + "," + result + "," + logParam + "," + totalRecord;
		
		writeLog(log, logPath);
	}

	/**
	 * 로그 파일 쓰기
	 * @param log
	 * @param logPath
	 * @throws IOException
	 */
	 private void writeLog(String log, String logPath) throws IOException {
		File logFile = new File(createLogFormat(logPath));
		
		if(!logFile.getParentFile().exists()) {
			logFile.getParentFile().mkdirs();
		}
		
		if (!logFile.exists()) {
    		log = "date,result,param,total" + log; 
		}
		
        BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
        
        bw.write(log);
        bw.close();
	}
	
	/**
	 * 로그 포맷 생성
	 * @param logPath
	 * @return
	 */
	private String createLogFormat(String logPath) {
		if(logPath.indexOf("_") <= 0) {
			return logPath;
		}
		int a = logPath.lastIndexOf("_");
        int b = logPath.lastIndexOf(".");
        
        String format = logPath.substring(a + 1, b);

        String currDate = LocalDate.now().format(DateTimeFormatter.ofPattern(format));
        
        return logPath.replaceAll(format, currDate);
	}

	/**
	 * 파라미터 양식 생성
	 * @param param
	 * @return
	 */
	private String createParam(Map<String, Object> param) {
        if (param == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (Map.Entry<String, Object> map : param.entrySet()) {
            sb.append(map.getKey() + "=" + map.getValue());
            sb.append("&");
        }
        
        if (sb.charAt(sb.length() - 1) != '[') {
        	sb.deleteCharAt(sb.length() - 1);
        }
        
        sb.append("]");

        return sb.toString();
    }

	/**
	 * 이전 날짜부터 현재날짜까지 리스트로 얻기
	 * @param prevDate
	 * @return
	 */
	public List<LocalDate> getFromToDate(String prevDate, String criterionDate , DateTimeFormatter datePattern) {
		// 시작 ~ 종료일
		LocalDate fromDate = LocalDate.parse(prevDate, datePattern);
		LocalDate toDate = LocalDate.parse(criterionDate, datePattern).plusDays(1);
		
		return DateUtil.getDatesBetweenTwoDates(fromDate, toDate);
	}

}
