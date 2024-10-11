package com.mocafelab.api.sg;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SgMapper {
	// 종료날짜가 지난 CPP 광고 개수 
	public int getEndCppCnt();
	// 종료날짜가 지난 CPP 광고 상태 변경
	public int modifyCppStatusEnd();
	// 광고 신청 후 익일까지 미입금 개수
	public int getNotPaidSgCnt();
	// 입금 완료 후 광고 시작일 까지 미승인된 개수
	public int getPaidSgAndNotApprovalCnt();
	// 광고 승인 거부
	public int modifySgStatusRejection(Map<String, Object> param);
	// 배치 모니터 업데이트
	public int modifyBatchMonitor(Map<String, Object> param);
}
