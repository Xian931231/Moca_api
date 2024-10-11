package com.mocafelab.api.enums;

public enum SgKind {
	
    CPP("P"),
    AREA("A"),
    TIME("T"),
    CPM("M"),
    DEFAULT("D"),
    PUBLIC("C")
    ;

    private String desc;

    private SgKind(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
    
    public static String getDesc(String sgKind) {
    	for (SgKind kind : SgKind.values()) {
    		if (sgKind.equals(kind.name())) {
    			return kind.getDesc();
    		}
    	}
    	
    	throw new RuntimeException();
    }

    public static SgKind getSgKind(String sgKind) {
    	for (SgKind kind : SgKind.values()) {
    		if (sgKind.equals(kind.name())) {
    			return kind;
    		}
    	}
    	
    	throw new RuntimeException();
    }
}
