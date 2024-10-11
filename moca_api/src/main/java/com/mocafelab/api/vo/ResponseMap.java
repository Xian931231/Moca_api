package com.mocafelab.api.vo;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.newfrom.lib.impl.ResponseMapImpl;
import net.newfrom.lib.util.AES256Util;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.CustomProperty;

/**
 * 공통 응답 객체
 * @author mure96
 * @version 1.0.0
 */
public class ResponseMap implements ResponseMapImpl {
	/**
	 * 공통 결과 Header
	 */
	protected Map<String, Object> header;
	
	/**
	 * 공통 결과 Body
	 */
	protected Map<String, Object> body;

	
	protected CustomProperty property;
	
	private String SECRET_KEY;
	
	/**
	 * 공통 응답 
	 */
	public ResponseMap() {
		this.header = new HashMap<String, Object>();
		this.body = new HashMap<String, Object>();
		this.property = new CustomProperty("properties/code.properties");
		
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
//		HttpSession session = request.getSession();
		
//		String secretKey = (String) session.getAttribute("secret_key");
//		this.SECRET_KEY = secretKey;
		this.SECRET_KEY = "lTZiXyjWmtHsglnPkbtPi80wtUXwwMz1";
	}

	
	@Override
	public Map<String, Object> getBody() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setBody(Map<String, Object> data) {
		// TODO Auto-generated method stub
		this.body.putAll(data);
	}

	@Override
	public void setBody(String key, Object data) {
		// TODO Auto-generated method stub
		this.body.put(key, data);
	}

	@Override
	public Map<String, Object> getResponse() {
		// TODO Auto-generated method stub
		return getResponse(null);
	}
	
	public Map<String, Object> getResponse(Code code) {
		Map<String, Object> encMap = new HashMap<>();
		Map<String, Object> response = new HashMap<String, Object>();

		try {
			header.put("code", Code.OK.code);
			header.put("msg", property.getProperty(Code.OK.msg));
			
			if (code != null) {
				body.put("result", false);
				body.put("code", code.code);
				body.put("msg", property.getProperty(code.msg));
			} else {
				body.put("result", true);
			}
			
			response.put("header", header);
			response.put("body", body);
			
			AES256Util aes256Util = new AES256Util(SECRET_KEY);
			encMap.put("e", aes256Util.encrypt(CommonUtil.mapToJson(response)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return encMap;
	}
	
	@Override
	public Map<String, Object> getErrResponse() {
		return getErrResponse("");
	}
	
	public Map<String, Object> getErrResponse(Code code) {
		String msg = "";
		if(code != null) {
			msg = property.getProperty(code.msg);
		}
		
		return getErrResponse(code, msg);
	}
	
	public Map<String, Object> getErrResponse(Code code, String msg) {
		Map<String, Object> encMap = new HashMap<>();
		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			
			header.put("code", code.code);
			
			if(msg == null || msg.equals("")) {
				header.put("code", Code.ERROR.code);
				msg = property.getProperty(Code.ERROR.msg);
			}
			
			header.put("msg", msg);
			body.put("result", false);
			
			response.put("header", header);
			response.put("body", body);
			
			AES256Util aes256Util = new AES256Util(SECRET_KEY);
			encMap.put("e", aes256Util.encrypt(CommonUtil.mapToJson(response)));
//			response.putAll(encMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return encMap;

	}

	@Override
	public Map<String, Object> getErrResponse(String msg) {
		Map<String, Object> encMap = new HashMap<>();
		Map<String, Object> response = new HashMap<String, Object>();
		
		try {
			
			if(msg == null || msg.equals("")) {
				msg = property.getProperty(Code.ERROR.msg);
				body.put("result", false);
			}
			
			header.put("code", Code.ERROR.code);
			header.put("msg", msg);
			
			response.put("header", header);
			response.put("body", body);
			AES256Util aes256Util = new AES256Util(SECRET_KEY);
			encMap.put("e", aes256Util.encrypt(CommonUtil.mapToJson(response)));
//			response.putAll(encMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return encMap;

	}

}