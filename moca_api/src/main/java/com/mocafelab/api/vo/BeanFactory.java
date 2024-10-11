package com.mocafelab.api.vo;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.newfrom.lib.vo.CustomProperty;

/**
 * 인스턴트 객체에 bean 객체를 사용할 경우 메소드로 등록해서 사용
 * @author asd
 *
 */
@Component
public class BeanFactory {

	/**
	 * codeProperty를 사용하는 ResponseMap을 리턴
	 */
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Resource(name="codeProperty")
	private CustomProperty codeProperty;
	
	public ResponseMap getResponseMap() {
		return new ResponseMap();
	}
}
