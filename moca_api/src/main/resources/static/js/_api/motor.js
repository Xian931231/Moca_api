var api = 
{	
	"option":{"urlCustom":"N"},
	"cls": [
		{
			"name": "광고 디바이스 연동 API",
			"desc": "광고 송출을 하는 디바이스에서 moca 플랫폼과 통신하는 API <br/><span class='block'></span> 현재는 안드로이드 태블릿 기준으로만 준비.",
			"method": [
				{
					"name": "매체의 차량의 위치 로그 갱신",
					"label": "success", 	//success, primary, danger, warning
					"desc": "초마다 차량 및 현재 지역 정보를 얻어와서 저장",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/motor/gps",
						"reqParam": [
							{
								"name":"car_number", "type":"string", "req":"yes", "value":"32자 5321",
								"desc":"차량 번호"
							},
							{
								"name":"latitude", "type":"string", "req":"yes", "value":"37.55237965910342",
								"desc":"위도"
							},
							{
								"name":"longitude", "type":"string", "req":"yes", "value":"126.91646307706833",
								"desc":"경도"
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
					"name": "매체의 차량의 위치 로그 삭제",
					"label": "success", 	//success, primary, danger, warning
					"desc": "6개월전 로그 삭제",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/device/motor/remove/location",
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