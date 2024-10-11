package com.mocafelab.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import net.newfrom.lib.vo.CustomProperty;

@Configuration
@PropertySource(value= {"classpath:/properties/common.properties"}, encoding = "UTF-8")
public class BeanConfig {

	/**
	 *  code관련 proerties파일의 빈 추가
	 * @return CustomProperty
	 */
	@Bean(name="codeProperty")
	public CustomProperty codeProperty() {
		return new CustomProperty("properties/code.properties");
	}
}
