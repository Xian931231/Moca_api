/**===========================================================================================================  
 * @Project		: Common Lib Project
 * @Source		: h.comm.js
 * @Description	: 통신 관련 설정
 * @Version		: v0.8.8
 * 
 * Copyright(c) 2011 NewFrom All rights reserved
 * ===========================================================================================================  
 *  No        DATE      Author	                Description
 * ===========================================================================================================        
 *  0.8.0 	2014/05/26	hpiece@gmail.com		Initial Coding
 *  0.8.8 	2014/06/17	hpiece@gmail.com		Modify And Add Coding
 * ===========================================================================================================
 */
// [Console로그 및 화면 디버그용 사용]
var _hLog = function (s) {
//	return;
	console.log(s);
};

var comm = {
	timeout:60,					// timeout 시간 설정
	callbackQueue: new Array(),	// 콜백함수 큐
	isReceive: false,			// 큐 대기열 트리거
	prefixUrl: "/api/v1",		// url 앞에 붙을 고정 URL 
	errorMsg: "SERVER ERROR!",	// 기본 에러 메세지
	
	loading: false,				// ajax 통신 요청중이면 true.
	useLoading: false,			// 로딩 모달 사용 여부
	isTimeout: false,			// 타임아웃 여부 체크 
	
	// 통신 함수
	send: function(url, data, method, sucCallback, errCallback, option){
		
		if(url) {
			url = comm.prefixUrl + url;
		} else {
			return;
		}
		
		// uuid 생성
		var _uuid = util.getUUID();
		
		// 콜백 정보 생성
		var callbackObj = {
				isComplete: false,
				uuid: _uuid,
				successCallback: sucCallback,
				errorCallback: errCallback
		};
		
		// 콜백 큐에 정보 입력
		comm.callbackQueue.push(callbackObj);
		
		// 데이터 변환
		if(data != null) {
			var json = JSON.stringify(data);
			data = "e=" + encodeURIComponent(util.aesEncode(json)); 
		}
		
		// ajax 통신객체 설정
		var jqxhr = $.ajax({
			url,
			type: method,
			data,
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			/*
			contentType: "application/json; charset=UTF-8",
			contentType: "text; charset=UTF-8",
			 */
			// cors 설정
			/*
			xhrFields: {
			      withCredentials: true
			},
			*/
			success: function(response, status, xhr){
				if(response.e) {
					response = JSON.parse(util.aesDecode(response.e));
				}
				var code = response.header.code;
				
				if(code != 200) {
					var msg = response.header.msg;
					// error 
					comm.recv("error", _uuid, {xhr: xhr, status: status, msg: msg, code: code});
				} else {
					// success
					comm.recv("success", _uuid, {response: response.body});
				}
			},
			
			error: function(xhr, status, error){
				var json = xhr.responseJSON;
				var code = 500;
				var msg = comm.errorMsg;
				if(json) {
					if(json.header != null && json.header.msg) {
						code = json.header.code;
						msg = json.header.msg;
					}
				}
				
				comm.recv("error", _uuid, { xhr, status, error, msg, code, callback: errCallback });
			},
			
			complete: function(){
				// 로딩 관련 설정
				/*
				window.setTimeout(function() {
					if(comm.useLoading == true || instantLoading == true){
						comm.showLoading(false);
					}
					else{
						comm.loading = false;
					}
				}, 500);
				*/
			},
		});
	},
	
	//파일 업로드 ajax 
	sendFile: function(url, data, method, sucCallback, errCallback, option){
		
		if(url) {
			url = comm.prefixUrl + url;
		} else {
			return;
		}
		
		// uuid 생성
		var _uuid = util.getUUID();
		
		// 콜백 정보 생성
		var callbackObj = {
				isComplete: false,
				uuid: _uuid,
				successCallback: sucCallback,
				errorCallback: errCallback
		};
		
		// 콜백 큐에 정보 입력
		comm.callbackQueue.push(callbackObj);
		
		if(comm.useLoading && comm.loading == false) {
			comm.showLoading(true);
		}
		
		var fData = new FormData();
		if(data instanceof FormData) {
			var entries = data.entries();
			var obj = {};
			for(var pair of entries) {
				var key = pair[0];
				var value = pair[1];
				
				if(value instanceof File) {
					fData.append(key, value);
				} else {
					obj[key] = value;
				}
			}
			fData.append("e", util.aesEncode(JSON.stringify(obj)));
		}
		
		var jqxhr = $.ajax({
		    url,
		    type: method,
		    data: fData,
		    contentType : false,
		    processData: false,
		    enctype: "multipart/form-data",
		    xhrFields: {
		          withCredentials: true
		    },
		    cache: false,
		    
		    success: function(response, status, xhr){
				
				var header = response.header;
				var body = response.body;
				
				if(body.result) {
					// success
					comm.recv("success", _uuid, {response: body});
				} else {
					var msg = body.msg;
					var code = body.code;
					
					// error 
					comm.recv("error", _uuid, {xhr: xhr, status: status, msg: msg, code: code, callback: errCallback});
				}
			},
		    error: function(xhr, status, error){
				var json = xhr.responseJSON;
				var code = 500;
				var msg = comm.errorMsg;
				if(json) {
					if(json.header != null && json.header.msg) {
						code = json.header.code;
						msg = json.header.msg;
					}
				}
				comm.recv("error", _uuid, { xhr, status, error, msg, code});
			},
			
			complete: function(){
				window.setTimeout(function() {
					if(comm.useLoading == true){

						comm.showLoading(false);
					}
					else{
						comm.loading = false;
					}
				}, 500);
				
			},
		});

	},

	// 콜백 함수가 통신 시작한 순서대로 실행되기 위한 큐 로직
	recv: function(type, uuid, option){
		// 큐 대기 위한 설정
		if(comm.isReceive == true){
			setTimeout(function(){
				comm.recv(type, uuid);
			}, 500);
			return;
		}
		
		comm.isReceive = true;
		
		// 콜백 큐에 완료 처리
		for(var i = 0; i < comm.callbackQueue.length; i++){
			var cbInfo = comm.callbackQueue[i];
			if(cbInfo.uuid == uuid){
				cbInfo.isComplete = true;
				if(type == "success"){
					cbInfo.type = "success";
					cbInfo.response = option.response;
				}
				else if(type == "error"){
					cbInfo.type = "error";
					cbInfo.xhr = option.xhr;
					cbInfo.status = option.status;
					cbInfo.errorCallback = option.callback;
					cbInfo.code = option.code;
					cbInfo.msg = option.msg;
				}
				break;
			}
		}
		
		var cloneQueue = new Array().concat(comm.callbackQueue);
		
		// 콜백 큐에서 순서대로 완료된 통신 콜백 처리, 중간 순서에 완료되지 않으면 리턴
		for(var i = 0; i < cloneQueue.length; i++){
			var cbInfo = cloneQueue[i];
			if(cbInfo.isComplete == true){
				if(cbInfo.type == "success"){
					if(typeof cbInfo.successCallback == "function"){
						cbInfo.successCallback(cbInfo.response);
					}
				}
				else if(cbInfo.type == "error"){
					alert(cbInfo);
				}
				comm.callbackQueue.shift();
			}
			else{
				break;
			}
		}
		
		comm.isReceive = false;
	},
}