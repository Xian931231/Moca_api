package com.mocafelab.api.interceptor;


import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.CookieUtil;

@Slf4j
public class CustomInterceptor implements HandlerInterceptor {
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/**
		 * 세션에 저장된 암호화키가 없다면 디폴트 암호화키로 복호화를 하고 이후 새로운 암호화키를 생성하여 세션에 저장한다. 클라이언트에는 쿠키로 전달.  
		 * 있다면 세션에 있는 암호화키로 복호화를 한다. 
		 */
		try {
			HttpSession session = request.getSession();
			session.setAttribute("secret_key", SECRET_DEFAULT_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}
}