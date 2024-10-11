package com.mocafelab.api.device;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.api.enums.EventKind;
import com.mocafelab.api.enums.DeviceBatchCode;
import com.mocafelab.api.enums.SgKind;
import com.mocafelab.api.enums.SgStatus;
import com.mocafelab.api.sg.SgService;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.AES256Util;
import net.newfrom.lib.util.CommonUtil;

@Service
public class ApiDeviceService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ApiDeviceMapper apiDeviceMapper;
	
	@Autowired
	private SgService sgService;
	
	@Value("${s3.path.default}")
	private String s3Url;
	
	@Value("${secret.motor.key}")
	private String secretMotorKey;
	
	@Value("${file.path.batch.motor.remove.location}")
	private String REMOVE_MOTOR_LOCATION;
	
	/** 
	 * 통신 확인
	 * @return
	 */
	public Map<String, Object> init(Map<String, Object> param) {
		return beanFactory.getResponseMap().getResponse();
	} 
	
	/**
	 * 차량 정보 로그 등록
	 * @param param
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> addMotorGps(Map<String, Object> param) throws Exception {
		Map<String, Object> responseMap = new HashMap<>();

		String latitude = String.valueOf(param.get("latitude")); // 위도
		String longitude = String.valueOf(param.get("longitude")); // 경도
		
		AES256Util aes256Util = new AES256Util(secretMotorKey);
		
		// 위도, 경도 암호화
		param.put("encrypt_latitude", aes256Util.encrypt(latitude));
		param.put("encrypt_longitude", aes256Util.encrypt(longitude));
		
		// 위도 경도의 지점 
		String point = "POINT(" + longitude + " " + latitude + ")";
		param.put("point", point);
		
		Map<String,Object> areaCode = apiDeviceMapper.getAreaCode(param);
		param.putAll(areaCode);
		
		apiDeviceMapper.addMotorGpsLog(param);
		apiDeviceMapper.addMotorLocation(param);
		
		if (CommonUtil.checkIsNull(areaCode)) {
			responseMap.put("result", false);
		} else {
			apiDeviceMapper.modifyMotor(param);
			responseMap.put("result", true);
		}
		
		return responseMap;
	}

	/**
	 * 디바이스로 보낼 상품의 편성표 정보
	 * @param param
	 * @return
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> getScheduleInfo(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		param.put("device", param.get("device_number"));
		Map<String, Object> scheduleMap = new LinkedHashMap<>();
		
		/**
		 * 1. 디바이스의 식별 번호로 디바이스가 존재하는 지 확인
		 * 2. api-key와 디바이스의 상품번호로 상품을 조회해서 product_id 얻어옴
		 * 3. 해당 상품에 대한 스케쥴 목록 조회
		 * 4. CPP, BLOCK 광고 목록 조회
		 * 5. device로 보낼 정보를 json 형식으로 가공
		 */
		
		// 디바이스 정보 확인
		Map<String,Object> sspDevice = apiDeviceMapper.getSspDevice(param);
		if (CommonUtil.checkIsNull(sspDevice)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_DEVICE);
		}
		
		param.put("check_product", true);
		param.put("device_product_id", sspDevice.get("product_id"));
		Map<String,Object> productDetail = apiDeviceMapper.getProduct(param);
		if (CommonUtil.checkIsNull(productDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_PRODUCT);
		}
		
		param.putAll(productDetail);
		Map<String, Object> motorDetail = apiDeviceMapper.getMotor(param);
		if (CommonUtil.checkIsNull(motorDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_MOTOR);
		}
		
		Map<String,Object> productSchedule = apiDeviceMapper.getScheduleTable(param);
		// 상품의 스케쥴 정보가 없으면 빈 맵 리턴
		if (CommonUtil.checkIsNull(productSchedule)) {
			responseMap.setBody("schedule_info", new HashMap<>());
			return responseMap.getResponse();
		}

		param.put("schedule_table_id", productSchedule.get("schedule_table_id"));
		
		scheduleMap.put("product_id", productSchedule.get("product_id"));
		scheduleMap.put("schedule_id", productSchedule.get("schedule_id"));
		scheduleMap.put("schedule_date", param.get("schedule_date"));
		scheduleMap.put("base_download_path", s3Url);
		scheduleMap.put("total_size", 0);
		
		// 상품의 슬롯 정보 
		List<Map<String, Object>> scheduleTableSlotList = apiDeviceMapper.getScheduleTableSlotList(param);
		
		// schedule_info
		List<Map<String, Object>> scheduleInfoList = new ArrayList<>();
		// ad_cpp_info
		Map<String, Object> advertCppInfoMap = new LinkedHashMap<>();
		// ad_block_info
		Map<String, Object> advertBlockInfoMap = new LinkedHashMap<>();

		Map<String, Object> areaMap = new HashMap<>();
		Map<String, Object> timeMap = new HashMap<>();
		Map<String, Object> defaultMap = new HashMap<>();
		Map<String, Object> publicMap = new HashMap<>();
		Map<String, Object> cpmMap = new HashMap<>();
		
		// 슬롯 정보에대해 매핑된 광고 루프
		for (Map<String, Object> tableSlot : scheduleTableSlotList) {
			// 스케쥴 정보 설정
			Map<String, Object> scheduleInfoMap = new HashMap<>();
			
			String slotType = String.valueOf(tableSlot.get("slot_type"));
			long slotId = Long.valueOf(String.valueOf(tableSlot.get("slot_id")));
			
			scheduleInfoMap.put("slot_id", slotId);
			scheduleInfoMap.put("slot_type", slotType);
			scheduleInfoMap.put("slot_order", tableSlot.get("slot_order"));
			
			if (slotType.equals("B") && !CommonUtil.checkIsNull(tableSlot, "sort_info")) {
				String sortInfo = (String) tableSlot.get("sort_info");
				List<Map<String,Object>> sortInfoArray = CommonUtil.jsonArrayToList(sortInfo);
				List<String> orderList = new ArrayList<>();
				
				// Block 타입일 시 block_order 셋팅
				for (Map<String, Object> blockOrder : sortInfoArray) {
					if (blockOrder.get("use_yn").equals("Y")) {
						orderList.add(String.valueOf(blockOrder.get("sort_type")).toLowerCase());
					}
				}
				scheduleInfoMap.put("block_order", orderList);
			}
			
			scheduleInfoList.add(scheduleInfoMap);
			
			// 광고 정보
			long scheduleTableSoltId = Long.valueOf(String.valueOf(tableSlot.get("schedule_table_slot_id")));
			param.put("schedule_table_slot_id", scheduleTableSoltId);
			
			if (slotType.equals("C")) { // CPP
				List<Map<String,Object>> scheduleTableBlock = apiDeviceMapper.getScheduleTableBlock(param);
				createCppAdvertInfo(scheduleMap, advertCppInfoMap, scheduleTableBlock, slotId);
			} else if (slotType.equals("B")) { // BLOCK
				
				/* Area 광고 */
				if (areaMap.isEmpty()) {
					List<Map<String,Object>> sameAreaList = apiDeviceMapper.getSameAreaList(param);
					
					for (Map<String, Object> sameAreaMap : sameAreaList) {
						// area_[구코드]
						areaMap.put("area_" + sameAreaMap.get("gu_code"), new HashMap<>() {{
							put("area_name", sameAreaMap.get("gu_name"));
						}});
					}
					
					List<Map<String,Object>> scheduleTableBlockTypeAreaList = apiDeviceMapper.getScheduleTableBlockTypeAreaList(param);
					
					for (Map<String, Object> typeAreaMap : scheduleTableBlockTypeAreaList) {
						String guCode = String.valueOf(typeAreaMap.get("gu_code"));
						Map<String, Object> guAreaMap = (Map<String, Object>) areaMap.get("area_" + guCode);
						
						createBlockAdvertInfo(scheduleMap, typeAreaMap, guAreaMap);
					}
					
					advertBlockInfoMap.put("area", areaMap);
				}
				
				/* Time 광고 */
				if (timeMap.isEmpty()) {
					List<Map<String, Object>> scheduleTableBlockTypeWeekList = apiDeviceMapper.getScheduleTableBlockTypeWeekList(param);
					for (Map<String, Object> typeWeekMap : scheduleTableBlockTypeWeekList) {
						int hour = 24;
						
						// hourMap 에 광고 set
						for (int i = 0; i < hour; i++) {
							String hourStr =  i < 10 ? "0" + i : "" + i;
							String hourValue = (String.valueOf(typeWeekMap.get("hour_" + hourStr)));
							
							if (hourValue.equals("1")) {
								Map<String, Object> hourMap = (Map<String, Object>) timeMap.get("hour_" + hourStr) == null 
										? new HashMap<>()
										: (Map<String, Object>) timeMap.get("hour_" + hourStr);
								
								createBlockAdvertInfo(scheduleMap, typeWeekMap, hourMap);
								
								timeMap.put("hour_" + hourStr, hourMap);
							}
						}
					}
					
					advertBlockInfoMap.put("time", timeMap);
				}
				
				/* default 광고 */
				if (defaultMap.isEmpty()) {
					List<Map<String, Object>> scheduleTableBlockTypeDefaultList = apiDeviceMapper.getScheduleTableBlockTypeDefaultList(param);
					
					for (Map<String, Object> typeDefaultMap : scheduleTableBlockTypeDefaultList) {
						createBlockAdvertInfo(scheduleMap, typeDefaultMap, defaultMap);
					}
					
					advertBlockInfoMap.put("default", defaultMap);
				}
				
				/* public 광고 */
				if (publicMap.isEmpty()) {
					List<Map<String,Object>> scheduleTableBlockTypePublicList = apiDeviceMapper.getScheduleTableBlockTypePublicList(param);
					for (Map<String, Object> typePublicMap : scheduleTableBlockTypePublicList) {
						createBlockAdvertInfo(scheduleMap, typePublicMap, publicMap);
					}
					
					advertBlockInfoMap.put("public", publicMap);
				}
				
				/* cpm 광고 */
				if (cpmMap.isEmpty()) {
					List<Map<String,Object>> scheduleTableBlockTypeCpmList = apiDeviceMapper.getScheduleTableBlockTypeCpmList(param);
					for (Map<String, Object> typeCpmMap : scheduleTableBlockTypeCpmList) {
						createBlockAdvertInfo(scheduleMap, typeCpmMap, cpmMap);
					}
					
					advertBlockInfoMap.put("cpm", cpmMap);
				}
			}
		}
		
		scheduleMap.put("schedule_info", scheduleInfoList);
		scheduleMap.put("ad_cpp_info", advertCppInfoMap);
		scheduleMap.put("ad_block_info", advertBlockInfoMap);
		responseMap.setBody("schedule", scheduleMap);
		
		String scheduleJson = CommonUtil.mapToJson(scheduleMap);
		param.put("schedule_json", scheduleJson);
		param.put("send_status", "R");
		
		// 스케줄 전송 로그 저장 => 이미 있으면 업데이트
		if (apiDeviceMapper.hasScheduleSendLog(param) > 0) {
			if (apiDeviceMapper.modifySameScheduleSendLog(param) < 1) {
				throw new RuntimeException();
			}
		} else {
			if (apiDeviceMapper.addScheduleSendLog(param) < 1) {
				throw new RuntimeException();
			}
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * block 광고 정보 생성
	 * @param typeMap 타입별 광고 List의 iterate 하는 Map
	 * @param advertMap 광고 리스트를 wrapping하는 Map
	 */
	@SuppressWarnings("unchecked")
	private void createBlockAdvertInfo(Map<String, Object> scheduleMap, Map<String, Object> typeMap, Map<String, Object> advertMap) {
		List<Map<String, Object>> sgList = (List<Map<String, Object>>) advertMap.get("sg_list") == null 
				? new ArrayList<>() 
				: (List<Map<String, Object>>) advertMap.get("sg_list");;
		
		Map<String, Object> sgMap = new HashMap<>();
		
		sgMap.put("sg_id", typeMap.get("sg_id"));
		sgMap.put("sg_name", typeMap.get("sg_name"));
		sgMap.put("ad_kind", typeMap.get("ad_kind"));
		sgMap.put("play_time", typeMap.get("play_time"));
		sgMap.put("file_path", typeMap.get("file_path"));
		sgMap.put("file_name", typeMap.get("file_name"));		
		sgMap.put("file_resolution", typeMap.get("page_size_code"));
		sgMap.put("file_size", String.valueOf(typeMap.get("file_size")));
		sgMap.put("exposure_limit", typeMap.get("exposure_limit"));
		
		Double fileSize = Double.valueOf(String.valueOf(scheduleMap.get("total_size"))) + Double.valueOf(String.valueOf(typeMap.get("file_size")));
		BigDecimal bigDecimal = new BigDecimal(fileSize);
		scheduleMap.put("total_size", String.valueOf(bigDecimal));
		
		sgList.add(sgMap);
		advertMap.put("sg_list", sgList);
		advertMap.put("view_index", 0);
	}
	
	/**
	 * 슬롯의 CPP 광고 정보 생성
	 * @param advertInfo
	 * @param scheduleTableBlock
	 * @param slotId
	 */
	private void createCppAdvertInfo(Map<String, Object> scheduleMap, Map<String, Object> advertInfo, List<Map<String, Object>> scheduleTableBlock, long slotId) {
		Map<String, Object> slotMap = new HashMap<>();
		
		for (Map<String, Object> cpp : scheduleTableBlock) {
			slotMap.put("sg_id", cpp.get("sg_id"));
			slotMap.put("sg_name", cpp.get("sg_name"));
			slotMap.put("ad_kind", cpp.get("ad_kind"));
			slotMap.put("play_time", cpp.get("play_time"));
			slotMap.put("file_path", cpp.get("file_path"));
			slotMap.put("file_name", cpp.get("file_name"));
			slotMap.put("file_resolution", cpp.get("page_size_code"));
			slotMap.put("file_size", String.valueOf(cpp.get("file_size")));
			slotMap.put("exposure_limit", cpp.get("exposure_limit"));
			Double fileSize = Double.valueOf(String.valueOf(scheduleMap.get("total_size"))) + Double.valueOf(String.valueOf(cpp.get("file_size")));
			BigDecimal bigDecimal = new BigDecimal(fileSize);
			
			scheduleMap.put("total_size", String.valueOf(bigDecimal));
		}
		
		advertInfo.put("SLOT_" + slotId, slotMap);
	}
	
	/**
	 * 디바이스에서 스케쥴의 광고 소재를 다운로드 받고 완료한 것을 확인
	 * @param param
	 * @return
	 */
	public Map<String, Object> verifyScheduleDownload(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		param.put("device", param.get("device_number"));
		
		// 디바이스 정보 확인
		Map<String,Object> sspDevice = apiDeviceMapper.getSspDevice(param);
		if (CommonUtil.checkIsNull(sspDevice)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_DEVICE);
		}
		
		param.put("check_product", true);
		param.put("device_product_id", sspDevice.get("product_id"));
		Map<String,Object> productDetail = apiDeviceMapper.getProduct(param);
		if (CommonUtil.checkIsNull(productDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_PRODUCT);
		}
		
		param.putAll(productDetail);
		Map<String, Object> motorDetail = apiDeviceMapper.getMotor(param);
		if (CommonUtil.checkIsNull(motorDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_MOTOR);
		}

		param.put("send_status", "R");
		// 스케쥴을 다운로드 받은 로그가 존재하지 않으면 
		if (apiDeviceMapper.hasScheduleSendLog(param) < 1) {
			return responseMap.getErrResponse(Code.BAD_REQUEST);
		}
		
		setLocationInfo(responseMap, motorDetail);
		
		param.put("send_status", "S");
		if (apiDeviceMapper.modifyScheduleSendLog(param) < 1) {
			throw new RuntimeException();
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고가 노출 가능한 광고인지 확인
	 * @param param
	 * @return
	 */
	public Map<String, Object> verifySgInfo(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		param.put("device", param.get("device_number"));
		
		// 디바이스 정보 확인
		Map<String,Object> sspDevice = apiDeviceMapper.getSspDevice(param);
		if (CommonUtil.checkIsNull(sspDevice)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_DEVICE);
		}
		
		param.put("check_product", true);
		param.put("device_product_id", sspDevice.get("product_id"));
		Map<String,Object> productDetail = apiDeviceMapper.getProduct(param);
		if (CommonUtil.checkIsNull(productDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_PRODUCT);
		}
		
		param.putAll(productDetail);
		Map<String, Object> motorDetail = apiDeviceMapper.getMotor(param);
		if (CommonUtil.checkIsNull(motorDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_MOTOR);
		}
		
		// 광고 종류
		SgKind sgKind = SgKind.getSgKind(String.valueOf(param.get("sg_kind")));
		
		switch (sgKind) {
			case CPP:
				Map<String, Object> dspSgManager = apiDeviceMapper.getDspSgManager(param);
				if (CommonUtil.checkIsNull(dspSgManager)) {
					//return responseMap.getErrResponse(Code.BAD_REQUEST);
					responseMap.setBody("playable", false);
					break;
				}
				
				int status1 = Integer.valueOf(String.valueOf(dspSgManager.get("status")));
				SgStatus sgStatus1 = SgStatus.getSgStatus(status1);
				
				setSgInfo(responseMap, sgStatus1);
				break;
			case CPM: case AREA: case TIME:
				Map<String,Object> scheduleTableSg = apiDeviceMapper.getScheduleTableSg(param);
				if (CommonUtil.checkIsNull(scheduleTableSg)) {
					//return responseMap.getErrResponse(Code.BAD_REQUEST);
					responseMap.setBody("playable", false);
					break;
				}
				
				int status2 = Integer.valueOf(String.valueOf(scheduleTableSg.get("status")));
				int remainCount = Integer.valueOf(String.valueOf(scheduleTableSg.get("remain_count")));
				SgStatus sgStatus2 = SgStatus.getSgStatus(status2);
				
				setSgInfo(responseMap, sgStatus2, remainCount);
				break;
			case PUBLIC: case DEFAULT: 
				String adType = sgKind.equals(SgKind.PUBLIC) ? "P" : "D"; // ad_type 설정
				param.put("ad_type", adType);
				
				Map<String,Object> sgService = apiDeviceMapper.getSgService(param);
				if (CommonUtil.checkIsNull(sgService)) {
					return responseMap.getErrResponse(Code.BAD_REQUEST);
				}
				
				String status3 =String.valueOf(sgService.get("status"));
				
				setSgInfo(responseMap, status3);
				break;
			default:
				return responseMap.getErrResponse(Code.BAD_REQUEST);
		}

		setLocationInfo(responseMap, motorDetail);
		
		return responseMap.getResponse();
	}

	private void setLocationInfo(ResponseMap responseMap, Map<String, Object> motorDetail) {
		if (!CommonUtil.checkIsNull(motorDetail, "cpoint")) {
			String point = String.valueOf(motorDetail.get("cpoint"));
			String[] splitPoint = point.substring(1, point.length() -1).split(",");
			responseMap.setBody("latitude", splitPoint[0]);
			responseMap.setBody("longitude", splitPoint[1]);
			
			// 위치 정보
			responseMap.setBody("si_code", motorDetail.get("si_code"));
			responseMap.setBody("gu_code", motorDetail.get("gu_code"));
			responseMap.setBody("dong_code", motorDetail.get("dong_code"));
			responseMap.setBody("local_name", motorDetail.get("local_name"));
		} else {
			responseMap.setBody("latitude", 0);
			responseMap.setBody("longitude", 0);
		}
	}
	
	/** 
	 * CPP 광고 노출 정보 set
	 * @param responseMap
	 * @param sgStatus
	 */
	private void setSgInfo(ResponseMap responseMap, SgStatus sgStatus) {
		responseMap.setBody("playable", false);
		
		// 광고가 진행중일 때
		if (sgStatus.equals(SgStatus.APPROVE_COMPLETE)) {
			responseMap.setBody("playable", true);
		}
	}
	
	/** 
	 * 디폴트, 공익 광고 노출 정보 set
	 * @param responseMap
	 * @param sgStatus
	 */
	private void setSgInfo(ResponseMap responseMap, String status) {
		responseMap.setBody("playable", false);
		
		// 광고가 진행중일 때
		if (status.equals("C")) {
			responseMap.setBody("playable", true);
		}
	}
	
	/** 
	 * CPM 광고 노출 정보 set
	 * @param responseMap
	 * @param sgStatus
	 */
	private void setSgInfo(ResponseMap responseMap, SgStatus sgStatus, int remainCount) {
		responseMap.setBody("playable", false);
		responseMap.setBody("exposure_limit", 0);
		
		// 광고가 진행중이고 노출 수량이 남아있을 때
		if (sgStatus.equals(SgStatus.APPROVE_COMPLETE) && remainCount > 0) {
			responseMap.setBody("playable", true);
			responseMap.setBody("exposure_limit", remainCount);
		}
	}
	
	/**
	 * 디바이스에서 넘어온 광고 로그 저장
	 * @param param
	 * @return
	 */
	public Map<String, Object> addSgEventTraffic(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		param.put("device", param.get("device_number"));
		
		// 디바이스 정보 확인
		Map<String,Object> sspDevice = apiDeviceMapper.getSspDevice(param);
		if (CommonUtil.checkIsNull(sspDevice)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_DEVICE);
		}
		
		param.put("check_product", true);
		param.put("device_product_id", sspDevice.get("product_id"));
		Map<String,Object> productDetail = apiDeviceMapper.getProduct(param);
		if (CommonUtil.checkIsNull(productDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_PRODUCT);
		}
		
		param.putAll(productDetail);
		Map<String, Object> motorDetail = apiDeviceMapper.getMotor(param);
		if (CommonUtil.checkIsNull(motorDetail)) {
			return responseMap.getErrResponse(Code.NOT_EXIST_MOTOR);
		}
		
		String sgKind = SgKind.getDesc(String.valueOf(param.get("sg_kind")));
		EventKind.verifyEventKind(String.valueOf(param.get("event_kind")));

		param.put("cpoint", motorDetail.get("cpoint"));
		param.put("sg_kind", sgKind);
		param.put("status", "R");
		
		if (apiDeviceMapper.addSgEventTraffic(param) < 1) {
			throw new RuntimeException();
		}
		
		return responseMap.getResponse();	
	}
	
	/**
	 * 차량 위치 정보 삭제
	 * @param param
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> removeMotorLocation(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		String resultCode = "Success";
		
		// 삭제 성공 레코드
		int totalRecord = 0;
		
		// 총 삭제할 개수
		int totalRemoveCnt = apiDeviceMapper.getRemoveMotorLocationCnt();
		
		if(totalRemoveCnt > 0) {
			totalRecord = apiDeviceMapper.removeMotorLocation();
		
			if(totalRecord != totalRemoveCnt) {
				resultCode = "Fail";
			} 
		}
		String result = resultCode + "(총 개수 : " + totalRemoveCnt + ", 삭제 성공 개수 : " + totalRecord + ")";
		
		// 배치 모니터 업데이트
		sgService.modifyBatchMonitor(DeviceBatchCode.REMOVE_MOTOR_LOCATION.getCode(), resultCode);
		
		// 로그 기록
		sgService.writeResultLog(result, totalRecord, REMOVE_MOTOR_LOCATION);
		
		return respMap.getResponse();	
	}
}
