package com.mocafelab.api.schedule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.api.enums.SlotSortType;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
public class ScheduleBatchService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ScheduleBatchMapper scheduleBatchMapper;
	
	/**
	 * 스케줄 생성
	 * @param param
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	@SuppressWarnings({ "unchecked", "serial" })
	public Map<String, Object> createSchedule(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		
		// 배치일의 다음 날짜를 기본 일자로 설정
		if(CommonUtil.checkIsNull(param, "schedule_date")) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.now().plusDays(1);
			param.put("schedule_date", dtf.format(localDate));
		}
		
//        // 스케줄 날짜가 오늘 날짜 이전의 스케줄은 생성 불가		
//		DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		String scheduleDate = String.valueOf(param.get("schedule_date"));
//		
//        LocalDate scheduleDt = LocalDate.parse(scheduleDate, datePattern);
//        LocalDate currDt = LocalDate.parse(LocalDate.now().format(datePattern), datePattern);
//		
//        if (scheduleDt.compareTo(currDt) < 0) {
//        	return respMap.getErrResponse();
//        }
        
		/**
		 * 이미 스케줄을 생성한 기록이 있을 때 이전 정보를 remove
		 */
		List<Map<String,Object>> scheduleTableIdList = scheduleBatchMapper.getScheduleTableIdList(param);
		if (scheduleTableIdList != null && !scheduleTableIdList.isEmpty()) {
			for (Map<String, Object> tableIdMap : scheduleTableIdList) {
				param.put("schedule_table_id" , tableIdMap.get("schedule_table_id"));
				
				List<Map<String,Object>> scheduleTableSlotIdList = scheduleBatchMapper.getScheduleTableSlotIdList(param);
				for (Map<String, Object> tableSlotIdMap : scheduleTableSlotIdList) {
					param.put("schedule_table_slot_id" , tableSlotIdMap.get("schedule_table_slot_id"));
					
					scheduleBatchMapper.removeScheduleTableBlock(param);
				}
				
				scheduleBatchMapper.removeScheduleTableSlot(param);
			}
			
			scheduleBatchMapper.removeScheduleTableSg(param);
			scheduleBatchMapper.removeScheduleTable(param);
		}
		
		/**
		 * 1. 오늘 날짜(schedule_date) 에 해당하는 상품 목록 조회 (스케쥴에 반영된, 슬롯이 할당된 상품만) 
		 * -> ssp_product, schedule_product, schedule_product_slot, schedule_product_slot_sg 
		 * 2. 해당 상품들이 반영된 스케쥴의 슬롯 조회 
		 * 3. CPP 슬롯이라면 오늘날짜에 해당 광고가 나가야하는지 검사하여 schedule_table_block에 추가 
		 * 4. CPM 슬롯이라면 sort_info에 맞춰 (지역,시간,디폹트,공익,CPM) schedule_table_block에 추가 
		 */
		
		// insert schedule_table
		List<Map<String, Object>> todayProductList = scheduleBatchMapper.getTodayProductList(param);
		for(Map<String, Object> todayProductMap : todayProductList) {
			todayProductMap.put("schedule_date", param.get("schedule_date"));
			scheduleBatchMapper.addScheduleTable(todayProductMap);
		}
		
		// insert schedule_table_slot
		scheduleBatchMapper.addScheduleSlot(param);
		
		// insert schedule_table_block
		List<Map<String, Object>> targetSgList = new ArrayList<>();
		
		int cppOrder = 0;
		int cpmOrder = 0;
		
		List<Map<String, Object>> slotList = scheduleBatchMapper.getScheduleTableSlotList(param);
		for(Map<String, Object> slotMap : slotList) {
			String slotType = (String) slotMap.get("slot_type");
			String sortInfoJson = (String) slotMap.get("sort_info");
			
			int scheduleTableSlotId = Integer.valueOf(String.valueOf(slotMap.get("schedule_table_slot_id")));
			int scheduleId = Integer.valueOf(String.valueOf(slotMap.get("schedule_id")));
			int productId = Integer.valueOf(String.valueOf(slotMap.get("product_id")));;
			int slotId = Integer.valueOf(String.valueOf(slotMap.get("slot_id")));;
			
			param.put("schedule_table_slot_id", scheduleTableSlotId);
			param.put("schedule_id", scheduleId);
			param.put("product_id", productId);
			param.put("slot_id", slotId);
			
			if("C".equals(slotType)) {
				// CPP
				Map<String, Object> cppDetail = scheduleBatchMapper.getCppBySlot(param);
				if(!CommonUtil.checkIsNull(cppDetail)) {
					
					cppDetail.put("sg_kind", SlotSortType.CPP.getSgKind());
					cppDetail.put("send_order", ++cppOrder);
					cppDetail.put("schedule_table_slot_id", scheduleTableSlotId);
					
					targetSgList.add(cppDetail);
				}
			} else if ("B".equals(slotType) && !CommonUtil.checkIsNull(slotMap, "sort_info")) {
				// CPM
				Map<String, Object> productParam = new HashMap<>() {{
					put("product_id", productId);
				}};
				
				Map<String, Object> productDetail = scheduleBatchMapper.getProductDetail(productParam);
				
				if(!CommonUtil.checkIsNull(productDetail)) {
					param.put("deny_category_code1", productDetail.get("deny_category_code1"));
					param.put("deny_category_code2", productDetail.get("deny_category_code2"));
					param.put("deny_category_code3", productDetail.get("deny_category_code3"));
					param.put("page_size_code", productDetail.get("screen_resolution"));
					
					String cpmSortType = "";
					
					List<Map<String, Object>> sortInfoList = CommonUtil.jsonArrayToList(sortInfoJson);
					for(Map<String, Object> sortInfoMap : sortInfoList) {
						List<Map<String, Object>> cpmSgList = new ArrayList<>();
						
						String sortType = (String) sortInfoMap.get("sort_type");
						String useYn = (String) sortInfoMap.get("use_yn");
						
						if(!cpmSortType.equals(sortType)) {
							cpmOrder = 0;
							cpmSortType = sortType;
						} 
						
						SlotSortType slotSortType = SlotSortType.getName(sortType);
						
						if("Y".equals(useYn)) {
							param.put("sg_kind", slotSortType.getSgKind());
							switch(slotSortType) {
								case AREA:
									cpmSgList = scheduleBatchMapper.getAreaSgList(param);
									break;
								case TIME:
									cpmSgList = scheduleBatchMapper.getTimeSgList(param);
									break;
								case CPM:
									cpmSgList = scheduleBatchMapper.getCpmSgList(param);
									break;
								case DEFAULT:
									param.put("ad_type", "D");
									cpmSgList = scheduleBatchMapper.getDspServiceAdList(param);
									break;
								case PUBLIC:
									param.put("ad_type", "P");
									cpmSgList = scheduleBatchMapper.getDspServiceAdList(param);
								default: 
									break;
							}
							
							if(cpmSgList.size() > 0) {
								for(Map<String, Object> cpmSgMap : cpmSgList) {
									cpmSgMap.put("schedule_table_slot_id", scheduleTableSlotId);
									cpmSgMap.put("send_order", ++cpmOrder);
									cpmSgMap.put("slot_id", slotId);
								}
								
								targetSgList.addAll(cpmSgList);
							}
						}
					}
				}
			}
		}
		
		List<Integer> targetIdList = new ArrayList<>();
		
		Map<Long, Object> cpmMap = new HashMap<>(); 
		
		// schedule_table_block 테이블에 들어갈 데이터 리스트 
		for(Map<String, Object> targetSgMap : targetSgList) {
			String sgKind = String.valueOf(targetSgMap.get("sg_kind"));
			
			if (sgKind.equals("A") || sgKind.equals("T") || sgKind.equals("M")) {
				cpmMap.put(Long.parseLong(String.valueOf(targetSgMap.get("sg_id"))), targetSgMap);
			}
			
			Integer scheduleTableBlockId = scheduleBatchMapper.addScheduleBlock(targetSgMap); 
			if(scheduleTableBlockId != null && scheduleTableBlockId > 0) {
				targetIdList.add(scheduleTableBlockId);
			}
		}
		
		// schedule_table_sg 데이터 삽입
		for (Entry<Long, Object> uniqueSgMap : cpmMap.entrySet()) {
			Map<String,Object> sgParam = (Map<String, Object>) uniqueSgMap.getValue();
			sgParam.put("schedule_date", param.get("schedule_date"));
			sgParam.put("excess_count", 0);
			
			if (scheduleBatchMapper.addScheduleSg(sgParam) < 1) {
				throw new RuntimeException();
			}
		}
		
		//respMap.setBody("sg_size", targetIdList.size());
		
		return respMap.getResponse();
	}
}
