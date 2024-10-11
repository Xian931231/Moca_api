package com.mocafelab.api.schedule;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/schedule")
public class ScheduleBatchController {

	@Autowired
	private ScheduleBatchService scheduleBatchService;
	
	@PostMapping("/create")
	public Map<String, Object> createSchedule(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return scheduleBatchService.createSchedule(param);
	}
}
