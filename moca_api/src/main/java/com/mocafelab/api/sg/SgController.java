package com.mocafelab.api.sg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/sg")
public class SgController {

	@Autowired
	private SgService sgService;
	
	/**
	 * 종료날짜가 지난 CPP 광고 상태 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/status/end")
	public Map<String, Object> modifyCppStatusEnd(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.modifyCppStatusEnd(param);
	}
	
	/**
	 * 광고비 미납 승인 거부 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/status/notpaid")
	public Map<String, Object> modifySgStatusNotPaid(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.modifySgStatusNotPaid(param);
	}
	
	/**
	 * 광고 승인 요청 익일까지 미 입금 시 승인 거부
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/status/notapproval")
	public Map<String, Object> modifySgStatusNotApproval(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.modifySgStatusNotApproval(param);
	}
}
