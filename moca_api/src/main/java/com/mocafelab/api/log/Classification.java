package com.mocafelab.api.log;

public enum Classification {

	AD_EVENT_LOG("BC0001"),
	AD_COUNT("BC0002"),
	AD_SG_PRODUCT_COUNT("BC0003"),
	DEMAND_COUNT_SG("BC0010"),
	DEMAND_COUNT_SG_AREA("BC0011"),
	SSP_AD_COUNT_PRODUCT("BC0020"),
	SSP_AD_COUNT_PRODUCT_DEVICE("BC0021"),
	;
	private String code;

	private Classification(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
