var api = 
{	
	"option":{"urlCustom":"N"},
	"cls": [
		{
			"name": "광고 상태 변경",
			"desc": "상황에 따른 광고 상태 변경",
			"method": [
				{
					"name": "종료날짜가 지난 CPP 광고 상태 변경",
					"label": "success", 	//success, primary, danger, warning
					"desc": "종료날짜가 지난 CPP 광고 상태 변경",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/sg/modify/status/end",
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
					"name": "광고 신청 후 익일까지 미 입금 시 승인 거부",
					"label": "success", 	//success, primary, danger, warning
					"desc": "광고 신청 후 익일까지 미 입금 시 승인 거부",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/sg/modify/status/notpaid",
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
					"name": "입금 완료 후 광고 시작일까지 승인이 안된 경우 자동 승인거부",
					"label": "success", 	//success, primary, danger, warning
					"desc": "입금 완료 후 광고 시작일까지 승인이 안된 경우 자동 승인거부",
					"usage":"",
					"progress":{ 
						"rate":"100",
						"desc":""
					},
					"info": {
						"url": "/sg/modify/status/notapproval",
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