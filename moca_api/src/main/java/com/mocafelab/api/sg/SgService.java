package com.mocafelab.api.sg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mocafelab.api.enums.SgBatchCode;
import com.mocafelab.api.log.LogClassificationService;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

@Service
public class SgService {

	@Autowired
	private SgMapper sgMapper;
	
	@Autowired
	private LogClassificationService logClassificationService;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Value("${file.path.batch.sg.modify.end.log}")
	private String SG_END_LOG_PATH;
	
	@Value("${file.path.batch.sg.modify.notpaid.log}")
	private String SG_NOT_PAID_LOG_PATH;
	
	@Value("${file.path.batch.sg.modify.notapproval.log}")
	private String SG_NOT_APPROVAL_LOG_PATH;
	
	@Value("${log.message.success}")
	private String LOG_SUCCESS_MESSAGE;
	
	/**
	 * 종료날짜가 지난 CPP 광고 상태 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyCppStatusEnd(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		String resultCode = "Success";
		
		// 업데이트 성공 레코드
		int totalRecord = 0;
		
		// 종료 상태로 변경할 CPP 광고 개수
		int endCppCnt = sgMapper.getEndCppCnt();
		
		if(endCppCnt > 0) {
			totalRecord = sgMapper.modifyCppStatusEnd();
		
			if(totalRecord != endCppCnt) {
				resultCode = "Fail";
			} 
		}
		String result = resultCode + "(총 개수 : " + endCppCnt + ", 업데이트 성공 개수 : " + totalRecord + ")";
		
		// 배치 모니터 업데이트
		modifyBatchMonitor(SgBatchCode.MODIFY_CPP_END.getCode(), resultCode);
		
		// 로그 기록
		writeResultLog(result, totalRecord, SG_END_LOG_PATH);
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 승인 요청 익일까지 미 입금 시 승인 거부
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifySgStatusNotPaid(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		String resultCode = "Success";
		
		// 미 입금 개수
		int notPaidCnt = sgMapper.getNotPaidSgCnt();
		
		// 업데이트 성공 레코드
		int totalRecord = 0;
		
		if(notPaidCnt > 0) {
			param.put("reject_type", "notPaid");
			param.put("reject_reason", "광고비 미 입금");
			
			totalRecord = sgMapper.modifySgStatusRejection(param);
		
			if(totalRecord != notPaidCnt) {
				resultCode = "Fail";
			}
		}
		String result = resultCode + "(총 개수 : " + notPaidCnt + ", 업데이트 성공 개수 : " + totalRecord + ")";
		
		// 배치 모니터 업데이트
		modifyBatchMonitor(SgBatchCode.MODIFY_NOT_PAID.getCode(), resultCode);
		
		// 로그 기록
		writeResultLog(result, totalRecord, SG_NOT_PAID_LOG_PATH);
		
		return respMap.getResponse();
	}
	
	/**
	 * 입금 완료 후 광고 시작일까지 승인이 안된 경우 자동 승인거부
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifySgStatusNotApproval(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		String resultCode = "Success";
		
		// 입금은 되었지만 광고 시작일까지 미 승인된 개수
		int notApprovalCnt = sgMapper.getPaidSgAndNotApprovalCnt();
		
		// 업데이트 성공 레코드
		int totalRecord = 0;
		
		if(notApprovalCnt > 0) {
			param.put("reject_type", "notApproval");
			param.put("reject_reason", "입금 후 광고 시작일까지 미 승인");
			param.put("pay_status_code", "REFUND_WAIT");
			
			totalRecord = sgMapper.modifySgStatusRejection(param);
		
			if(totalRecord != notApprovalCnt) {
				resultCode = "Fail";
			}
		}
		String result = resultCode + "(총 개수 : " + notApprovalCnt + ", 업데이트 성공 개수 : " + totalRecord + ")";

		// 배치 모니터 업데이트
		modifyBatchMonitor(SgBatchCode.MODIFY_NOT_APPROVAL.getCode(), resultCode);
		
		// 로그 기록
		writeResultLog(result, totalRecord, SG_NOT_APPROVAL_LOG_PATH);
		
		return respMap.getResponse();
	}
	
	// 배치 모니터링 업데이트 
	public void modifyBatchMonitor(String batchCode, String result) {
		Map<String, Object> param = new HashMap<>();
		param.put("batch_code", batchCode);
		param.put("result", result);
		param.put("result_message", result.equals("Success") ? LOG_SUCCESS_MESSAGE : Code.BATCH_UPDATE_FAIL.msg);
		
		if(result.equals("Success")) {
			String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			param.put("last_index", nowDate);
		}
		
		sgMapper.modifyBatchMonitor(param);
	}
	
	// 결과 로그 기록
	public void writeResultLog(String result, int totalRecord, String logPath) throws Exception {
		Map<String, Object> param = new HashMap<>();
		param.put("result", result);
		param.put("total_record", totalRecord);
		param.put("log_path", logPath);
		
		logClassificationService.writeClassifiyLog(param, null);
	}
}
