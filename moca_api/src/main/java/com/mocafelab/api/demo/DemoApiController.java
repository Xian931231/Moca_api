package com.mocafelab.api.demo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.api.schedule.ScheduleBatchService;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 데모 시연용 API 작성
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/demo")
public class DemoApiController {
	
	@Autowired
	private DemoApiService demoApiService;
	
	@Autowired
	private ScheduleBatchService scheduleBatchService;
	
	/**
	 * 위치 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/area")
	public Map<String, Object> modifyArea(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demoApiService.modifyArea(param);
	}
	
	/**
	 * 차량의 현재 지역
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/area")
	public Map<String, Object> getArea(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demoApiService.getArea(param);
	}
	
	/**
	 * 차량 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/list")
	public Map<String, Object> getMotorList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demoApiService.getMotorList(param);
	}
	
	/**
	 * 특정 일자의 스케즐 생성
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/create/schedule")
	public Map<String, Object> createSchedule(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return scheduleBatchService.createSchedule(param);
	}
	
	/**
	 * 특정 일자의 스케즐 생성
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add/excelArea")
	public Map<String, Object> addExcelArea(RequestMap requestMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(mRequest, "file");
		
		MultipartFile mFile = mRequest.getFile("file");
		
		return demoApiService.addExcelArea(param, mFile);
	}
}
