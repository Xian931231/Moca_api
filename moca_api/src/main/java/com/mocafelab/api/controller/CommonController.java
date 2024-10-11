package com.mocafelab.api.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {

	@GetMapping("/health")
	public void healthCheck(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_OK);
	}
}