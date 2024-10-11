package com.mocafelab.api.device;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiDeviceMapper {
	
	// 상품 정보
	Map<String, Object> getProduct(Map<String, Object> param);
	// 디바이스 정보
	Map<String, Object> getSspDevice(Map<String, Object> param);
	// 위도 경도에 따른 위치 조회
	Map<String, Object> getAreaCode(Map<String, Object> param);
	// 상품의 차량 정보
	Map<String, Object> getMotor(Map<String, Object> param);
	
	// 차량 GPS 로그 등록
	int addMotorGpsLog(Map<String, Object> param);
	// 암호화한 차량 위치 등록
	int addMotorLocation(Map<String, Object> param);
	// ssp_motor 차량 위치 정보 갱신
	int modifyMotor(Map<String, Object> param);
	
	// 광고 집행 날짜의 스케줄 정보
	Map<String, Object> getScheduleTable(Map<String, Object> param);
	// 스케쥴 슬롯 정보
	List<Map<String, Object>> getScheduleTableSlotList(Map<String, Object> param);
	// 슬롯별 광고 정보 목록
	List<Map<String, Object>> getScheduleTableBlock(Map<String, Object> param);
	// cpm 광고가 노출 가능한지에 대한 정보
	Map<String, Object> getScheduleTableSg(Map<String, Object> param);
	// cpp 광고에 대한 정보
	Map<String, Object> getDspSgManager(Map<String, Object> param);
	// default, public 광고에 대한 정보
	Map<String, Object> getSgService(Map<String, Object> param);
	int hasScheduleSendLog(Map<String, Object> param);
	// 스케쥴 전송 로그 등록
	int addScheduleSendLog(Map<String, Object> param);
	// 스케쥴 전송 로그 수정
	int modifyScheduleSendLog(Map<String, Object> param);
	// 같은 스케쥴을 요청했을 때 기존 데이트를 덮어쓰기 
	int modifySameScheduleSendLog(Map<String, Object> param);
	
	// 지역 옵션에서 같은 구 조회
	List<Map<String, Object>> getSameAreaList(Map<String, Object> param);
	// Block타입의 지역 옵션 광고 목록
	List<Map<String, Object>> getScheduleTableBlockTypeAreaList(Map<String, Object> param);
	// Block타입의 시간 옵션 광고 목록
	List<Map<String, Object>> getScheduleTableBlockTypeWeekList(Map<String, Object> param);
	// Block타입의 기본 옵션 광고 목록
	List<Map<String, Object>> getScheduleTableBlockTypeDefaultList(Map<String, Object> param);
	// Block타입의 공익 옵션 광고 목록
	List<Map<String, Object>> getScheduleTableBlockTypePublicList(Map<String, Object> param);
	// Block타입의 CPM 옵션 기본 목록 
	List<Map<String, Object>> getScheduleTableBlockTypeCpmList(Map<String, Object> param);
	
	// 광고 노출 시 쌓이는 로그 저장
	int addSgEventTraffic(Map<String, Object> param);
	
	// 삭제할 6개월 전 motor location 개수
	int getRemoveMotorLocationCnt();
	// 6개월 전 motor location
	int removeMotorLocation();
}
