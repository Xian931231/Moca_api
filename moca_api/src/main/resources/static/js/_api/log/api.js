var api = 
{	
	"option":{"urlCustom":"N"},
	"cls": [
		{
			"name": "엑셀 데이터 더미 로그 생성",
			"desc": "엑셀 데이터 더미 로그 생성",
			"method": [
				{
					"name": "엑셀 데이터 더미 로그 생성",
					"label": "success", 	
					"desc": "엑셀 데이터 더미 로그 생성",
					"usage":"",
					"progress":{ 
						"rate":"0",
						"desc":""
					},
					"info": {
						"url": "/demo/add/excelArea",
						"reqParam": [
							{
								"name":"file", "type":"file", "req":"yes", "value":"0",
								"desc":"로그 쌓을 데이터 파일"
							},
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
			],
			
		},
		{
			"name": "광고 디바이스 로그 API",
			"desc": "저장된 광고의 로그를 분류하는 API",
			"method": [
				{
					"name": "시연용 데이터 생성",
					"label": "success", 	
					"desc": "시연용 데이터 생성",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/testData/add",
						"reqParam": [
							{
								"name":"count", "type":"int", "req":"yes", "value":"0",
								"desc":"로그 쌓을 데이터 갯수"
							},
							{
								"name":"car_number", "type":"int", "req":"yes", "value":"980바7934",
								"desc":"차량번호"
							},
							{
								"name":"cip", "type":"string", "req":"yes", "value":"60.159.242.35",
								"desc":"차량 ip"
							},
							{
								"name":"latitude", "type":"float", "req":"yes", "value":"36.580123",
								"desc":"위도"
							},
							{
								"name":"longitude", "type":"float", "req":"yes", "value":"127.292345",
								"desc":"경도"
							},
							{
								"name":"sg_id", "type":"int", "req":"yes", "value":"17",
								"desc":"광고 id"								
							},
							{
								"name":"slot_id", "type":"int", "req":"yes", "value":"20",
								"desc":"슬롯 id"
							}
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "adEventTraffic dummyData",
					"label": "success", 	
					"desc": "adEventLog 더미데이터 생성 (테스트 데이타 생성을 위한 처리)",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/addDummyLog",
						"reqParam": [
							{
								"name":"count", "type":"int", "req":"yes", "value":"0",
								"desc":"로그 쌓을 데이터 갯수"
							},
							{
								"name":"err_count", "type":"int", "req":"yes", "value":"0",
								"desc":"이상/에러 데이터 갯수"
							},
							{
								"name":"event_date_min", "type":"string", "req":"yes", "value":"2023/07/01",
								"desc":"날짜(min) 형식: yyyy/mm/dd"
							},
							{
								"name":"event_date_max", "type":"string", "req":"yes", "value":"2023/07/31",
								"desc":"날짜(max) 형식: yyyy/mm/dd"
							},
							{
								"name":"cip", "type":"string", "req":"yes", "value":"127.0.0.1",
								"desc":"ip ex) 127.0.0.1 (빈칸 시 랜덤) "
							},
							{
								"name":"latitude_min", "type":"int", "req":"yes", "value":"37.5",
								"desc":"위도(min) // 지역 광고는 알맞은 랜덤 값으로 들어갑니다."
							},
							{
								"name":"latitude_max", "type":"int", "req":"yes", "value":"38.5",
								"desc":"위도(max)"
							},
							{
								"name":"longitude_min", "type":"int", "req":"yes", "value":"127.5",
								"desc":"경도(min)"
							},
							{
								"name":"longitude_max", "type":"int", "req":"yes", "value":"128.5",
								"desc":"경도(max)"
							},
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "ad_event_log batch",
					"label": "success", 	//success, primary, danger, warning
					"desc": "상시로 돌아가며 ad_event_traffic => ad_event_log로 이동시키는 배치",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/sg/event",
						"reqParam": [
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "ad_event_log batch all",
					"label": "success", 	//success, primary, danger, warning
					"desc": "하루에 한번 ad_event_traffic의 돌려지지 않은 부분을 정리하는 배치",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/sg/event/all",
						"reqParam": [
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "ad_count batch",
					"label": "success", 	//success, primary, danger, warning
					"desc": "하루에 한번 ad_event_log에서 배치를 돌려 ad_count로 데이터를 쌓는 배치",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/sg/count",
						"reqParam": [
						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
			]
		},
		
		{
			"name": "classify",
			"desc": "로그 분류 (ad_event_log를 기준으로 필요할 로그 추출을 위한 배치 작업)",
			"method": [
				{
					"name": "classify traffic.ad_sg_product_count",
					"label": "success", 	//success, primary, danger, warning
					"desc": "ad_sg_product_count 분류",
					"usage":"",
					"progress":{ 
						"rate":"0",
						"desc":""
					},
					"info": {
						"url": "/log/classify/sg/product",
						"reqParam": [

						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "classify dsp_report.count_sg",
					"label": "success", 	//success, primary, danger, warning
					"desc": "count_sg 분류",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/classify/demand/sg",
						"reqParam": [

						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "classify dsp_report.count_sg_area",
					"label": "success", 	//success, primary, danger, warning
					"desc": "count_sg_area 분류",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/classify/demand/sg/area",
						"reqParam": [

						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "classify ssp_report.ad_count_proudct",
					"label": "success", 	//success, primary, danger, warning
					"desc": "ad_count_proudct 분류",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/classify/ssp/product",
						"reqParam": [

						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
				{
					"name": "classify ssp_report.ad_count_proudct_device",
					"label": "success", 	//success, primary, danger, warning
					"desc": "ad_count_proudct_device 분류",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/log/classify/ssp/product/device",
						"reqParam": [

						],
						"respParam": [
						]
					},
					"sample": {
						"desc": "정상적으로 DB의 select 여부를 보여줍니다 .",
						"resp": '{"header": { "code": "", "msg": "" },"body": {}}'
					}
				},
			]
		},
	]
};