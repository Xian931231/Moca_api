package com.mocafelab.api.schedule;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleBatchMapper {

	////////// 스케줄 생성 관련 //////////
	
	// 같은 날짜의 스케쥴 제거 용
	List<Map<String, Object>> getScheduleTableIdList(Map<String, Object> param);
	List<Map<String, Object>> getScheduleTableSlotIdList(Map<String, Object> param);
	int removeScheduleTable(Map<String, Object> param);
	int removeScheduleTableSlot(Map<String, Object> param);
	int removeScheduleTableBlock(Map<String, Object> param);
	int removeScheduleTableSg(Map<String, Object> param);
	
	List<Map<String, Object>> getTodayProductList(Map<String, Object> param);
	List<Map<String, Object>> getScheduleTableSlotList(Map<String, Object> param);
	Map<String, Object> getCppBySlot(Map<String, Object> param);
	Map<String, Object> getProductDetail(Map<String, Object> param);
	
	int addScheduleTable(Map<String, Object> param);
	int addScheduleSlot(Map<String, Object> param);
	Integer addScheduleBlock(Map<String, Object> param);
	int addScheduleSg(Map<String, Object> param);
	
	List<Map<String, Object>> getAreaSgList(Map<String, Object> param);
	List<Map<String, Object>> getTimeSgList(Map<String, Object> param);
	List<Map<String, Object>> getCpmSgList(Map<String, Object> param);
	List<Map<String, Object>> getDspServiceAdList(Map<String, Object> param);
	
	//////////스케줄 생성 관련 //////////
}
