package com.mocafelab.api.demo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoApiMapper {

	// 위치 변경
	public Map<String, Object> modifyArea(Map<String, Object> param);
	
	// 차량의 현재 지역
	public Map<String, Object> getArea(Map<String, Object> param);
	
	// 차량의 디바이스 리스트
	public List<Map<String, Object>> getDeviceList(Map<String, Object> param);
	
	// 차량 리스트
	public List<Map<String, Object>> getMotorList(Map<String, Object> param);
	
}
