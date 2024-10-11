package com.mocafelab.api.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

@Slf4j
@Component
@Order(1)
public class FilterExceptionHandler extends OncePerRequestFilter{

	@Autowired 
	private BeanFactory beanFactory;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.debug("FilterExceptionHandler Exception Catch");
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			log.error("{}", stacktrace);
			
			ResponseMap responseMap = beanFactory.getResponseMap();
			PrintWriter pw = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			pw.print(CommonUtil.mapToJson(responseMap.getErrResponse(Code.API_ERROR)));
			return;
		}
	}

}
