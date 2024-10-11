package com.mocafelab.api.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.AES256Util;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;
import com.mocafelab.api.vo.BeanFactory;

/**
 * 일반 파라미터용 resolver
 * @author asd
 *
 */
@Slf4j
public class CustomArgumentResolver implements HandlerMethodArgumentResolver {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		// 현재 세션 미사용
//		HttpSession session = request.getSession();
//		
//		String secretKey = (String) session.getAttribute("secret_key");
//		if(secretKey == null || secretKey.equals("")) {
//			// 세션에 새로 발급받은 암호화키가 없다면 디폴트 암호화 키로 복호화 
//			secretKey = SECRET_DEFAULT_KEY;
//		}
		
		String secretKey = SECRET_DEFAULT_KEY;
		
		AES256Util aes256Util = new AES256Util(secretKey);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		
		headerMap.put("remote_ip", CommonUtil.getRemoteIP(request));
		headerMap.put("api_key", request.getHeader("app-key"));
		headerMap.put("referer", request.getHeader("referer"));
		
		// 헤더
		/*Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String value = request.getHeader(key);
			if(value != null) {
				headerMap.put(key, value);
			}
		}*/
		// 파라미터 
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			String[] values = request.getParameterValues(key);

			if (values != null) {
				if(key.equals("e")) {
					String decryptStr = aes256Util.decrypt(values[0]);
					
					if(decryptStr != null && !decryptStr.equals("") && !decryptStr.equals("\"\"")) {
						Map<String, Object> encMap = CommonUtil.jsonToMap(decryptStr);
						bodyMap.putAll(encMap);
					}
				} else if (values.length == 1) {
					bodyMap.put(key, values[0]);
				} else {
					bodyMap.put(key, values);
				}
			}
		}
		
		return new RequestMap(headerMap, bodyMap);
	}
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// TODO Auto-generated method stub;
		return parameter.getParameterType().isAssignableFrom(RequestMap.class);
	}
}
