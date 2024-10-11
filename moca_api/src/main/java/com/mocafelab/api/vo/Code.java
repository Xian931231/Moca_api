package com.mocafelab.api.vo;

public enum Code {
	/**
	 * 정상 
	 */
	OK(200, "code.ok"),
	
	/**
	 * Client ERROR
	 */
	BAD_REQUEST(400, "code.bad.request"),
	UNAUTHORIZED(401, "code.unauthorized"),
	
	/**
	 * NOT FOUND 
	 */
	NOT_FOUND(404, "code.not.found"),
	
	/**
	 * 데이터 미존재 
	 */
	NOT_EXIST_DATA(4000, "code.not.exist.data"),
	NOT_EXIST_DEVICE(4001, "code.not.exist.device"),
	NOT_EXIST_PRODUCT(4002, "code.not.exist.product"),
	NOT_EXIST_MOTOR(4003, "code.not.exist.motor"),
	
	/**
	 * 에러
	 */
	ERROR(500, "code.error"),
	
	// Api 2000 ~
	//MEMBER_NOT_(2000, "code.member.not.allow.id"),
	API_DOMAIN_FAIL(96, "code.api.domain.fail"),
	API_PLATFORM_FAIL(97, "code.api.platform.fail"),
	API_PACKAGE_FAIL(98, "code.api.package.fail"),
	API_PLATFORM_NOT_SUPPORT(99, "code.api.platform.not.support"),
	API_KEY_NOT_EXIST(999, "code.api.key.not.exist"),
	API_ERROR(510, "code.api.error"),
	
	LOG_WRITE_FAIL(90, "code.log.write.fail"),
	
	DUMMY_INVALID_PARAM(2000, "code.dummy.invalid.param"),
	
	BATCH_UPDATE_FAIL(3000, "code.batch.update.fail"),
	
	;
	
	
	public final int code;
	public final String msg;
	
	Code(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
}
