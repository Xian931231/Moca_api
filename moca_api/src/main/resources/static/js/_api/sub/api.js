var api = 
{	
	"option":{"urlCustom":"N"},
	"cls": [
		{
			"name": "광고 디바이스 연동 API",
			"desc": "광고 송출을 하는 디바이스에서 moca 플랫폼과 통신하는 API <br/><span class='block'></span> 현재는 안드로이드 태블릿 기준으로만 준비.",
			"method": [
				{
					"name": "서비스 가능 여부 확인",
					"label": "success", 	//success, primary, danger, warning
					"desc": "정상적으로 서비스가 가능한(허용 또는 등록된) 타겟 디바이스 또는 서비스인지 여부 체크",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/init",
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
					"name": "광고 편성표 정보 조회",
					"label": "success", 	//success, primary, danger, warning
					"desc": "디바이스에서 특정 날짜에 노출해야할 편성표 정보를 조회",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/schedule",
						"reqParam": [
							{
								"name":"schedule_date", "type":"string", "req":"yes", "value":"2023-12-20",
								"desc":"편성표 정보를 요청할 날쨔"
							},
							{
								"name":"device_number", "type":"string", "req":"yes", "value":"inno_Display_01",
								"desc":"디바이스 식별 번호"
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
					"name": "광고 소재 다운로드 완료 여부 저장",
					"label": "success", 	//success, primary, danger, warning
					"desc": "디바이스에서 편성표에 존재하는 광고 소재를 모두 다운로드 받았을때 호출하여 다운로드 완료 여부를 저장",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/verify/schedule",
						"reqParam": [
							{
								"name":"schedule_date", "type":"string", "req":"yes", "value":"2023-12-20",
								"desc":"편성표 정보를 요청할 날쨔"
							},
							{
								"name":"device_number", "type":"string", "req":"yes", "value":"inno_Display_01",
								"desc":"디바이스 식별 번호"
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
					"name": "광고 노출 가능 여부 확인",
					"label": "success", 	//success, primary, danger, warning
					"desc": "디바이스에서 광고를 노출해야할때 호출해서 광고의 노출 가능 여부 확인",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/verify/sg",
						"reqParam": [
							{
								"name":"sg_id", "type":"int", "req":"yes", "value":"493",
								"desc":"광고 아이디"
							},
							{
								"name":"sg_kind", "type":"int", "req":"yes", "value":"CPP",
								"desc":"광고 종류(CPP, AREA, TIME, CPM, PUBLIC, DEFAULT)"
							},
							{
								"name":"schedule_date", "type":"int", "req":"yes", "value":"2023-12-20",
								"desc":"스케쥴 일자"
							},
							{
								"name":"device_number", "type":"string", "req":"yes", "value":"inno_Display_01",
								"desc":"디바이스 식별 번호"
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
					"name": "광고 노출 로그 저장",
					"label": "success", 	//success, primary, danger, warning
					"desc": "디바이스에서 광고 노출 시 발생되는 이벤트 로그 저장",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/sg/event",
						"reqParam": [
							{
								"name":"event_date", "type":"string", "req":"yes", "value":"2023-12-20 16:38:26.122",
								"desc":"해당 광고에 대한 이벤트 실제 발생 시간"
							},
							{
								"name":"sg_id", "type":"int", "req":"yes", "value":"493",
								"desc":"광고 아이디"
							},
							{
								"name":"sg_kind", "type":"string", "req":"yes", "value":"CPP",
								"desc":"광고 종류(C:CPP, A:AREA, T:TIME, M:CPM, P:PUBLIC, D:DEFAULT)"
							},
							{
								"name":"slot_id", "type":"int", "req":"yes", "value":"816",
								"desc":"편성표 슬롯 코드"
							},
							{
								"name":"device_number", "type":"string", "req":"yes", "value":"inno_Display_01",
								"desc":"디바이스 식별번호(시리얼번호)"
							},
							{
								"name":"event_kind", "type":"string", "req":"yes", "value":"PS",
								"desc":"C: 클릭(CPC 대응위함), DS: 이미지 노출 시작, DE: 이미지 노출 종료, PS: 재생시작, PE: 재생종료, PP: 재생 중지, PR: 중단후 다시 시작, PC: 중단(기타 이유로)"
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

			]
		},
		
	]
};