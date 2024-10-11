package com.mocafelab.api.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 처리해야 할 traffic 로그 데이터
 */
public class LogResponse {

    private List<Map<String, Object>> processingList = new ArrayList<>();
    private Map<String, Object> paramMap;
    private LogStatusCode logStatusCode;

    private LogResponse(List<Map<String, Object>> processingList, Map<String, Object> paramMap, LogStatusCode logStatusCode) {
        this.processingList = processingList;
        this.paramMap = paramMap;
        this.logStatusCode = logStatusCode;
    }

    public static LogResponse of(List<Map<String, Object>> processingList, Map<String, Object> paramMap, LogStatusCode logStatusCode) {
        return new LogResponse(processingList, paramMap, logStatusCode);
    }

    public List<Map<String, Object>> getProcessingList() {
        return processingList;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public LogStatusCode getLogStatusCode() {
        return logStatusCode;
    }
}
