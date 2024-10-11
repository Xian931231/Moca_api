package com.mocafelab.api.device;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/device")
public class ApiDeviceController {

	@Autowired
	private ApiDeviceService apiDeviceService;
	
	/**
	 * 통신 확인
	 * @param request
	 * @return
	 */
	@PostMapping("/init")
	public Map<String, Object> init(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return apiDeviceService.init(param);
	}
	
	/**
	 * 차량 gps 정보
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/motor/gps")
	public ApiResponse<Map<String, Object>> addMotorGps(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		return new ApiResponse<>(apiDeviceService.addMotorGps(param));
	}
	
	/**
	 * 상품에 대한 스케줄 정보
	 * @param request
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/schedule")
	public Map<String, Object> getScheduleInfo(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkIsNull(param, "schedule_date");
		CommonUtil.checkIsNull(param, "device_number");
		
		return apiDeviceService.getScheduleInfo(param);
	}
	
	/**
	 * 스케줄 다운로드 확인
	 * @param request
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/verify/schedule")
	public Map<String, Object> verifyScheduleDownload(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkIsNull(param, "schedule_date");
		CommonUtil.checkIsNull(param, "device_number");
		
		return apiDeviceService.verifyScheduleDownload(param);
	}
	
	/**
	 * 노출 가능한 광고인지 확인
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/verify/sg")
	public Map<String, Object> verifySgInfo(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkIsNull(param, "sg_id");
		CommonUtil.checkIsNull(param, "sg_kind");
		
		return apiDeviceService.verifySgInfo(param);
	}
	
	/**
	 * 광고 로그 add
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/event")
	public Map<String, Object> addSgEventTraffic(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return apiDeviceService.addSgEventTraffic(param);
	}
	
	/**
	 * 차량 로그 갱신을 위한 비 암호화 api 응답 클래스 
	 * @author pps8853
	 */
	static class ApiResponse<T> {
		private Map<String, Object> header;
		private T body;
		
		public ApiResponse(HttpStatus httpStatus, T body) {
			header = Map.of(
					"msg", httpStatus.name(),
					"code", httpStatus.value()
			);
			
			this.body = body;
		}
		
		public ApiResponse(T body) {
			header = Map.of(
					"msg", HttpStatus.OK,
					"code", HttpStatus.OK.value()
			);
					
			this.body = body;
		}

		public Map<String, Object> getHeader() {
			return header;
		}

		public T getBody() {
			return body;
		}
		
	}
	
	/**
	 * 차량 위치 정보 삭제
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/motor/remove/location")
	public Map<String, Object> removeMotorLocation(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		return apiDeviceService.removeMotorLocation(param);
	}
}
