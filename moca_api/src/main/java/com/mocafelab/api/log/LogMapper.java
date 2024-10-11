package com.mocafelab.api.log;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper {
	
	// ad_event_traffic 시연용 데이터
	public int addTestTrafficData(Map<String, Object> param);
	// ad_event_log 시연용 데이터
	public int addTestEventData(Map<String, Object> param);
	
	public Map<String, Object> getAreaCode(Map<String, Object> param);
	public Map<String, Object> getDeviceApiKey(Map<String, Object> param);
	
	// 로그 생성 ( 단일 )
	public int addSingleLog(Map<String, Object> param);
	
	// 로그 생성 ( 더미 )
	public List<Map<String, Object>> getSgInfo(Map<String, Object> param);
	public Map<String, Object> getRandomCpoint(Map<String, Object> param);
	
	public Map<String, Object> getRandomSspInfo(Map<String, Object> param);
	public Map<String, Object> addDumyLog(Map<String, Object> param);
	
	
	// 배치 모니터 조회
	Map<String, Object> getBatchMonitor(Map<String, Object> param);
	// 배치 모니터 갱신
	int modifyBatchMonitor(Map<String, Object> param);
	// ad_event_traffic 광고에 대한 이벤트 조회
	List<Map<String, Object>> getSgEventTrafficList(Map<String, Object> param);
	// ad_event_log 로그 등록
	int addSgEventLog(Map<String, Object> param);
	// ad_event_traffic 로그 상태 완료로 변경
	int modifySgEventTraffic(Map<String, Object> param);
	// CPM 광고 노출 수 관리 테이블 남은 노출 수 차감
	int minusScheduleTableSg(Map<String, Object> param);
	int minusScheduleTableSgRemainCount(Map<String, Object> param);
	// 초과 노출 수 + 1
	int plusExcessCount(Map<String, Object> param);
	// dsp_sg_manager remain_exposure_count 차감
	Integer minusRemainExposureCount(Map<String, Object> param);
	int plusDspSgManagerRemainCount(Map<String, Object> param);
	Map<String, Object> minusDspSgManagerRemainCount(Map<String, Object> param);
	
	// display_start, end_date 갱신
	int modifyDspSgManagerStartDate(Map<String, Object> param);
	int modifyDspSgManagerEndDate(Map<String, Object> param);
	
	// 가장 이전의 로그 일자
	String getOldestSgEventLog();
	// 일별 ad_event_log 조회
	List<Map<String, Object>> getSgEventLogList(Map<String, Object> param);
	// 기준 일자의 ad_count 저장 (insert or update)
	int saveSgCount(Map<String, Object> param);
	// log_status R => S 로 수정
	int modifyAdEventLog(Map<String, Object> param);
	// 기준 일자의 ad_count 제거
	int removeSgCount(Map<String, Object> param);
	// 해당 일자의 지정한 광고의 노출량
	Map<String, Object> getSgCount(Map<String, Object> param);
	
	// 공통 
	// 가장 이전의 ad_count
	String getOldestAdCount();
	// -- 공통 
	
	
	// ad_count > ad_sg_product_count
	List<Map<String, Object>> getAdSgProductCount(Map<String, Object> param);
	int saveAdSgProductCount(Map<String, Object> param);
	
	// ad_count > dsp_report.count_sg
	List<Map<String, Object>> getDemandCountSg(Map<String, Object> param);
	int saveDemandCountSg(Map<String, Object> param);
	// ad_traffic > dsp_report.count_sg_area
	List<Map<String, Object>> getDemandCountSgArea(Map<String, Object> param);
	int saveDemandCountSgArea(Map<String, Object> param);
	
	// ad_count > ssp_report.ad_count_proudct
	List<Map<String, Object>> getSspAdCountProduct(Map<String, Object> param);
	int saveSspAdCountProduct(Map<String, Object> param);
	// ad_count > ssp_report.ad_count_proudct_deivce
	List<Map<String, Object>> getSspAdCountProductDevice(Map<String, Object> param);
	int saveSspAdCountProductDevice(Map<String, Object> param);
}