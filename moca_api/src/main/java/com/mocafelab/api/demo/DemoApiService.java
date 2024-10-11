package com.mocafelab.api.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mocafelab.api.log.LogService;
import com.mocafelab.api.vo.BeanFactory;
import com.mocafelab.api.vo.ResponseMap;

@Service
public class DemoApiService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DemoApiMapper demoApiMapper;
	
	@Autowired
	private LogService logService;
	
	/**
	 * 지역 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyArea(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		String areaValue = (String) param.get("area_value");
		if(areaValue.equals("P")) {
			param.put("gu_name", "파주시");
		} else if(areaValue.equals("S")) {
			param.put("gu_name", "세종시");
		} else if(areaValue.equals("G")) {
			param.put("gu_name", "강남구");	
		}
		
		Map<String, Object> result = demoApiMapper.modifyArea(param);
		respMap.setBody("data", result);
		
		return respMap.getResponse();
	}
	
	/**
	 * 차량의 현재 지역
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getArea(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> data = demoApiMapper.getArea(param);
		respMap.setBody("data", data);
		
		List<Map<String, Object>> list = demoApiMapper.getDeviceList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 차량 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMotorList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = demoApiMapper.getMotorList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}

	public Map<String, Object> addExcelArea(Map<String, Object> param, MultipartFile mFile) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		try (InputStream is = mFile.getInputStream(); ReadableWorkbook wb = new ReadableWorkbook(is)) {
			
//			List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
			
			List<Map<String, Object>> wrongData = new ArrayList<>();
			
			Iterator<Sheet> sheets = wb.getSheets().iterator();
			
			while(sheets.hasNext()) {
//				Sheet sheet = wb.getFirstSheet();
				boolean isError = false;
				
				Sheet sheet = sheets.next();
				String sheetName = sheet.getName();
				
				try (Stream<Row> rows = sheet.openStream()) {
					
					Iterator<Row> rIterator = rows.iterator();
					
					while(rIterator.hasNext()) {
						Row r = rIterator.next();
						
						int rownum = r.getRowNum();
			        	
			        	// cell
			        	if(rownum > 2) {
			        		
			        		
			        		BigDecimal cellValue = r.getCellAsNumber(1).orElse(null);
			        		if(cellValue != null && !cellValue.equals(BigDecimal.ZERO)) {
//			        			System.out.println(r);
			        			
			        			// 저장 데이터 
					        	Map<String, Object> dummyParam = new HashMap<>();
					        	
					        	int cellNum = 5;
					        	
					        	// 5 
					        	dummyParam.put("car_number", r.getCellAsString(cellNum++).get());
					        	
					        	// 6
					        	dummyParam.put("cip", r.getCellAsString(cellNum++).get());
					        	
					        	// 7
//					        	System.out.println(r.getCellAsNumber(cellNum).get().setScale(6, RoundingMode.HALF_UP));
					        	dummyParam.put("latitude", r.getCellAsNumber(cellNum++).get().setScale(6, RoundingMode.HALF_UP));
					        	
					        	// 8
//					        	System.out.println(r.getCellAsNumber(cellNum).get().setScale(6, RoundingMode.HALF_UP));
					        	dummyParam.put("longitude", r.getCellAsNumber(cellNum++).get().setScale(6, RoundingMode.HALF_UP));
					        	
					        	// 9
					        	dummyParam.put("count", r.getCellAsNumber(cellNum++).get());
					        	
					        	dummyParam.put("sg_id", 17);
					        	dummyParam.put("slot_id", 20);
					        	
					        	try {
//					        		System.out.println(dummyParam);
//					        		isError true;
					        		logService.addTestData(dummyParam);
					        	} catch(Exception e) {
					        		e.printStackTrace();
//					        		isError = true;
					        		
					        		Map<String, Object> wData = new HashMap<>();
					        		
					        		wData.put("sheet", sheetName);
					        		wData.put("car_number", dummyParam.get("car_number"));
					        		wData.put("latitude", dummyParam.get("latitude"));
					        		wData.put("longitude", dummyParam.get("longitude"));
					        		wrongData.add(wData);
					        		
//					        		break;
					        	}
			        		}
			        	}
					}
			    }
				if(wrongData.size() > 0) {
					String filePath = "/Users/mocafe";
					
					File file = new File(filePath, "dummy.log");
					if(file.exists()) {
						file.deleteOnExit();
					} 
					
//					file.mkdirs();
					file.createNewFile();
					
					FileWriter fw = new FileWriter(file);
					BufferedWriter writer = new BufferedWriter(fw);
					
					for(Map<String, Object> wData : wrongData) {
						writer.write("sheet = " + wData.get("sheet"));
						writer.write(", lat = " + wData.get("latitude"));
						writer.write(", lng = " + wData.get("longitude"));
						writer.write(", car_number = " + wData.get("car_number") + "\r\n");
					}
					
					writer.close();
				 }
				
				if(isError) {
					break;
				}
			}
		}

		
		return respMap.getResponse();
	}
}
