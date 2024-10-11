var error = {
		"cls": [
			{
				"code": "-1",
				"message": "기타 에러",
			},
			{
				"code": "-99",
				"message": "SQL error",
			},
			{
				"code": "-404",
				"message": "정보가 존재하지 않습니다.",
			},
			{
				"code": "-599",
				"message": "이미 존재하는 정보입니다.",
			},
			{
				"code": "-996",
				"message": "비정상적인 접근입니다.",
			},
			{
				"code": "-998",
				"message": "잘못된 경로로 접근하였습니다.",
			},
			{
				"code": "-999",
				"message": "Who Are You!",
			},
			{
				"code": "-9999",
				"message": "잘못된 경로로 접근하였습니다.",
			},
			{
				"code": "1000",
				"message": "Success!",
			},
		]
}

window.onload = function(){
	var table_o = document.getElementById("errorBody");
	
	var cls = error.cls;
	
	for(var i=0; i<cls.length; i++){
		var obj = cls[i];
		
		var code = obj.code;
		var message = obj.message;
		
		var tr_o = document.createElement("tr");
		tr_o.className = "success";
		
		var td_o = document.createElement("td");
		td_o.innerHTML = code;
		tr_o.appendChild(td_o);
		
		td_o = document.createElement("td");
		td_o.innerHTML = message;
		tr_o.appendChild(td_o);
		
		table_o.appendChild(tr_o);
	}
}