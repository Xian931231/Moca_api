package com.mocafelab.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

import com.mocafelab.api.interceptor.CustomInterceptor;
import com.mocafelab.api.filter.ApiFilter;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Autowired
	private ApiFilter apiFilter;
	
	@Bean
	public FilterRegistrationBean<Filter> getApiFilter() {
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(apiFilter);
		registrationBean.addUrlPatterns("/api/v1/*");
		return registrationBean;
	}
	
	/**
	 * ArgumentResolver 설정 
	 */	
	
	@Bean
	public CustomArgumentResolver customArgumentResolver() {
		return new CustomArgumentResolver();
	}
	
	@Bean
	public CustomInterceptor customInterceptor() {
		return new CustomInterceptor();
	}
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		// TODO Auto-generated method stub
		
		resolvers.add(customArgumentResolver());
	}
	
	// 인터셉터 제외 (세션, 시크릿 키 미사용) 
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry
//			.addInterceptor(customInterceptor())
//			.excludePathPatterns("/assets/**", "/js/**", "/css/**", "/favicon.ico")
//		;
//	}
}
