package com.mocafelab.api.log;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;
import net.newfrom.lib.util.DateUtil;

@RestController
@RequestMapping("${apiPrefix}/log")
public class LogController {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         

	@Autowired
	private LogService logService;
	
	@Autowired
	private LogClassificationService logClassificationService;
	
	@RequestMapping("/testData/add")
	public Map<String, Object> addTestData (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return logService.addTestData(param);
	}
	
	// 단일 로그 생성
	@RequestMapping("/addSingleLog")
	public Map<String, Object> addSingleLog (RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "event_date");
		CommonUtil.checkNullThrowException(param, "sg_id");
		CommonUtil.checkNullThrowException(param, "paper_id");
		CommonUtil.checkNullThrowException(param, "device_id");
		CommonUtil.checkNullThrowException(param, "event_kind");
		CommonUtil.checkNullThrowException(param, "longitude");
		CommonUtil.checkNullThrowException(param, "latitude");
		
		return logService.addSingleLog(param, request);
	}
	
	// 설정 기간 사이의 로그 생성 
	@RequestMapping("/addDummyLog")
	public Map<String, Object> addDumyLog (RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		DateUtil.compareNum(param, "latitude_max", "latitude_min");
		DateUtil.compareNum(param, "longitude_max", "longitude_min");
		
		return logService.addDumyLog(param, request);
	}
	
	/**
	 * 상시로 돌아가며 ad_event_traffic => ad_event_log로 이동시키는 배치
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/event")
	public Map<String, Object> sgEventLogBatch(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return logService.sgEventLogBatch(param);
	}
	
	/**
	 * 하루에 한번 ad_event_traffic의 돌려지지 않은 부분을 정리하는 배치
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/event/all")
	public Map<String, Object> sgEventLogBatchAll(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return logService.sgEventLogBatchAll(param);
	}
	
	/**
	 * 하루에 한번 ad_eventLog에서 배치를 돌려 ad_count로 데이터를 쌓는 배치
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/count")
	public Map<String, Object> sgCountLogBatch(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return logService.sgCountLogBatch(param);
	}
	
	// 로그 테이블별 api 
	/* 
	 * 
	@RequestMapping("/classify/adCount") // ad_event_log to ad_count 
	@RequestMapping("/classify/campaign") // ad_count to ad_count_campaign
	@RequestMapping("/classify/campaignPaper") // ad_count to ad_count_campaign_paper
	@RequestMapping("/classify/campaignArea") // ad_count to ad_count_campaign_area
	@RequestMapping("/classify/campaignMaterial") // ad_count to ad_count_campaign_material
	@RequestMapping("/classify/sg") // ad_count to ad_count_sg
	@RequestMapping("/classify/service") // ad_count to ad_count_service
	@RequestMapping("/classify/serviceApp") // ad_count to ad_count_service_app
	@RequestMapping("/classify/servicePaper") // ad_count to ad_count_service_paper
	*/


	/**
	 * ad_count to ad_count_campaign
	 * @param reqMap
	 * @param request
	 * @return
	 */
	@RequestMapping("/classify/sg/product")
	public Map<String, Object> classifyadSgProductCount (RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		param.put("batch_code_enum", Classification.AD_SG_PRODUCT_COUNT);
		return logClassificationService.adCountClassification(param);
	}
	
	/**
	 * ad_count to dsp_report.count_sg
	 * @param reqMap
	 * @param request
	 * @return
	 */
	@RequestMapping("/classify/demand/sg")
	public Map<String, Object> classifyDemandCountSg (RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		param.put("batch_code_enum", Classification.DEMAND_COUNT_SG);
		return logClassificationService.adCountClassification(param);
	}
	
	/**
	 * ad_count to dsp_report.count_sg_area
	 * @param reqMap
	 * @param request
	 * @return
	 */
	@RequestMapping("/classify/demand/sg/area")
	public Map<String, Object> classifyDemandCountSgArea (RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		param.put("batch_code_enum", Classification.DEMAND_COUNT_SG_AREA);
		return logClassificationService.adCountClassification(param);
	}
	
	/**
	 * ad_count to ssp_report.ad_count_product
	 * @param reqMap
	 * @param request
	 * @return
	 */
	@RequestMapping("/classify/ssp/product")
	public Map<String, Object> classifySspAdCountProudct(RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		param.put("batch_code_enum", Classification.SSP_AD_COUNT_PRODUCT);
		return logClassificationService.adCountClassification(param);
	}
	
	/**
	 * ad_count to ssp_report.ad_count_product_device
	 * @param reqMap
	 * @param request
	 * @return
	 */
	@RequestMapping("/classify/ssp/product/device")
	public Map<String, Object> classifySspAdCountProudctDevice(RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		param.put("batch_code_enum", Classification.SSP_AD_COUNT_PRODUCT_DEVICE);
		return logClassificationService.adCountClassification(param);
	}
}
