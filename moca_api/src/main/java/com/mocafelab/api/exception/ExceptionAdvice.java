package com.mocafelab.api.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
	
	@Autowired
	private BeanFactory beanFactory;

	@ExceptionHandler(RuntimeException.class)
	protected Map<String, Object> Exception(RuntimeException e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		log.error("{}", stacktrace);
		
		ResponseMap respMap = beanFactory.getResponseMap();
		e.printStackTrace();
		return respMap.getErrResponse(Code.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	protected Map<String, Object> Exception(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		log.error("{}", stacktrace);
		
		ResponseMap respMap = beanFactory.getResponseMap();
		e.printStackTrace();
		
		return respMap.getErrResponse(Code.API_ERROR);
	}
}
