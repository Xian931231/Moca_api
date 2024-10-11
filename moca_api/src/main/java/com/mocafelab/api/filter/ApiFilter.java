package com.mocafelab.api.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mocafelab.api.device.ApiDeviceMapper;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.Code;
import com.mocafelab.api.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

@Slf4j
@Component
@Order(5)
public class ApiFilter extends OncePerRequestFilter {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ApiDeviceMapper apiDeviceMapper;
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		
		log.info("requestURI : {}", requestUri);
		
		// 세션 사용하지 않도록 변경
//		HttpSession session = request.getSession();
//		String secretKey = (String) session.getAttribute("secret_key");
//		if(secretKey == null || secretKey.equals("")) {
//			session.setAttribute("secret_key", SECRET_DEFAULT_KEY);
//		}
		
		headerLog(request);
		parameterLog(request);
		
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		if (requestUri.startsWith("/api/v1/device/motor/gps") ||
				requestUri.startsWith("/api/v1/log") ||
				requestUri.startsWith("/api/v1/sg") ||
				requestUri.startsWith("/api/v1/demo") ||
				requestUri.startsWith("/api/v1/schedule") ||
				requestUri.startsWith("/api/v1/device/motor/remove/location") ) {
			filterChain.doFilter(request, response);
			return;
		}
		
		try {
			boolean apiRequestVerify = apiRequestVerify(request);
			
			if(!apiRequestVerify) { // 검증 실패 시 
				PrintWriter pw = response.getWriter();
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				pw.print(CommonUtil.mapToJson(responseMap.getErrResponse(Code.UNAUTHORIZED)));
				return;
			}
			
			filterChain.doFilter(request, response);
			
		} catch (Exception e) { // 에러 발생 시 500 error
			throw new ServletException(e);
		}
	}
	
	/**
	 * 헤더 로그
	 * @param request
	 */
	private void headerLog(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		
		StringBuilder sb = new StringBuilder();
		while(headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			
			sb.append(name + ": " + request.getHeader(name) + ",");
		}
		
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		log.info("{}", sb.toString());
	}
	
	/**
	 * 파라미터 로그
	 * @param request
	 */
	private void parameterLog(HttpServletRequest request) {
		Enumeration<String> parameterNames = request.getParameterNames();
		StringBuilder sb = new StringBuilder();
		while(parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			
			sb.append(name + ": " + request.getParameter(name) + ",");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		log.info("{}", sb.toString());
	}

	/**
	 * 요청이 상품에 등록된 api_key와 일치하는지 확인
	 * @param request
	 * @return
	 */
	public boolean apiRequestVerify(HttpServletRequest request) {
		Map<String, Object> param = new HashMap<>();
		param.put("api_key", request.getHeader("app-key"));
		
		Map<String, Object> getProductInfo = apiDeviceMapper.getProduct(param);
		if (getProductInfo == null || getProductInfo.isEmpty()) {
			return false;
		}
		
		String osName = (String) getProductInfo.get("os");
		String packageId = (String) getProductInfo.get("package_id");
		
		String headerDeviceOS = request.getHeader("device-os");
		
		// Android
		if (headerDeviceOS != null && headerDeviceOS.equals("A")) {
			if (!osName.startsWith("A")) { // A_11, A_13
				return false;
			} 
			
			if (!packageId.equals(request.getHeader("package-name"))) {
				return false;
			}
			
			return true;
		} 
		// Web
		else if (headerDeviceOS != null && headerDeviceOS.equals("W")) {
			if (!packageId.contains(request.getHeader("referer"))) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}
}
