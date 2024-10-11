function syntaxHighlight(json) {
    if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

window.onload = function() {

    //    	if(location.pathname != "/_api") return;
    $("input[name='mode']").on("change", function(ev) {
		var o = document.querySelector(".non-web-opt");
		if(o) {
			if( document.getElementById("mode_nonweb").checked) {
				o.style.display = "";
			}
			else {
				o.style.display = "none";
			}
		}
	});

    $("#apiUrl").val(location.origin + "/api/v1");
    //$("#package_name_value").val(location.origin);

    // ************************************************** //
    // Start : 동적 생성하는 부분 
    var idxMethod = 0;
    var cls = api.cls;
    var option = api.option;

    var divMethod = divMethodOrg;
    if (option != null && option.urlCustom == "Y") {
        divMethod = divMethodUrl;
    }

    for (var i = 0; i < cls.length; i++) {
        var tmpDivClass = "";
        tmpDivClass = divClass.replace(/{cls.name}/gi, cls[i].name)
            .replace(/{cls.desc}/gi, cls[i].desc);

        var tmpDivMethod = "";
        for (var j = 0; j < cls[i].method.length; j++) {

            var tmpReqParam = "";
            var tmpRespParam = "";
            var tmpInputSandbox = "";
            //_hLog(reqParam);	_hLog(respParam);
            for (var k = 0; k < cls[i].method[j].info.reqParam.length; k++) {
                //_hLog(tmpReqParam);

                tmpReqParam += reqParam.replace(/{cls.method.info.reqParam.name}/gi, cls[i].method[j].info.reqParam[k].name)
                    .replace(/{cls.method.info.reqParam.type}/gi, cls[i].method[j].info.reqParam[k].type)
                    .replace(/{cls.method.info.reqParam.req}/gi, cls[i].method[j].info.reqParam[k].req)
                    .replace(/{cls.method.info.reqParam.desc}/gi, cls[i].method[j].info.reqParam[k].desc)
                    .replace(/{j}/gi, idxMethod);
                // input type=file
                if (cls[i].method[j].info.reqParam[k].type == "file") {
                    tmpInputSandbox += inputSandbox_file.replace(/{cls.method.sandbox.name}/gi, cls[i].method[j].info.reqParam[k].name)
                        .replace(/{cls.method.sandbox.value}/gi, cls[i].method[j].info.reqParam[k].value)
                        .replace(/{j}/gi, idxMethod);
                } else { // input type=text
                    tmpInputSandbox += inputSandbox.replace(/{cls.method.sandbox.name}/gi, cls[i].method[j].info.reqParam[k].name)
                        .replace(/{cls.method.sandbox.value}/gi, cls[i].method[j].info.reqParam[k].value)
                        .replace(/{j}/gi, idxMethod);
                }
            }

            for (var k = 0; k < cls[i].method[j].info.respParam.length; k++) {
                var enterArrowString = "";

                if (cls[i].method[j].info.respParam[k].depth == "1")
                    enterArrowString += "&nbsp;<img src='./enter_arrow.png' width='14' height='14'>&nbsp;";
                else if (cls[i].method[j].info.respParam[k].depth == "2")
                    enterArrowString += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src='./enter_arrow.png' width='14' height='14'>&nbsp;";
                else if (cls[i].method[j].info.respParam[k].depth == "3")
                    enterArrowString += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src='./enter_arrow.png' width='14' height='14'>&nbsp;";
                else if (cls[i].method[j].info.respParam[k].depth == "4")
                    enterArrowString += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src='./enter_arrow.png' width='14' height='14'>&nbsp;";

                tmpRespParam += respParam.replace(/{cls.method.info.respParam.name}/gi, enterArrowString + cls[i].method[j].info.respParam[k].name)
                    .replace(/{cls.method.info.respParam.type}/gi, cls[i].method[j].info.respParam[k].type)
                    .replace(/{cls.method.info.respParam.desc}/gi, cls[i].method[j].info.respParam[k].desc)
                    .replace(/{j}/gi, idxMethod);
            }

            var tmpDivSample = "";
            tmpDivSample = divSample.replace(/{cls.method.sample.desc}/gi, cls[i].method[j].sample.desc)
                .replace(/{cls.method.sample.resp}/gi, cls[i].method[j].sample.resp)
                .replace(/{j}/gi, idxMethod);

            tmpDivMethod += divMethod.replace(/{cls.method.name}/gi, cls[i].method[j].name)
                .replace(/{cls.method.label}/gi, cls[i].method[j].label)
                .replace(/{cls.method.desc}/gi, cls[i].method[j].desc)
                .replace(/{cls.method.usage}/gi, cls[i].method[j].usage)
                .replace(/{cls.method.info.url}/gi, cls[i].method[j].info.url)
                .replace(/{reqParam}/gi, tmpReqParam)
                .replace(/{respParam}/gi, tmpRespParam)
                .replace(/{inputSandbox}/gi, tmpInputSandbox)
                .replace(/{divSample}/gi, tmpDivSample)
                .replace(/{j}/gi, idxMethod);

            if (cls[i].method[j].info.method) {
                tmpDivMethod = tmpDivMethod.replace(/{cls.method.info.method}/gi, cls[i].method[j].info.method);
            } else {
                tmpDivMethod = tmpDivMethod.replace(/{cls.method.info.method}/gi, "POST");
            }

            idxMethod++;
        }
        //_hLog("tmpDivMethod: "+ tmpDivMethod);

        tmpDivClass = tmpDivClass.replace(/{divMethod}/gi, tmpDivMethod);
        //_hLog("tmpDivClass: "+ tmpDivClass);

        var objCls = document.createElement("div");
        // objCls.setAttribute("class", "panel-group active");
        objCls.setAttribute("class", "panel-group");
        objCls.setAttribute("id", "accordion");
        objCls.innerHTML = tmpDivClass;
        document.getElementById("container").appendChild(objCls);
        document.getElementById("container").appendChild(document.createElement("hr"));
    }
    // End : 동적 생성하는 부분 
    // ************************************************** //

    $('#php-apidoctab a').click(function(e) {
        e.preventDefault();
        $(this).tab('show');
    });

    $('.tooltipP').tooltip({
        placement: 'bottom'
    });

    $('code[id^=response]').hide();


    $.each($('pre[id^=sample_response]'), function() {
        //_hLog($(this).html());
        if ($(this).html() == 'NA') {
            return;
        }
        var str = JSON.stringify(JSON.parse($(this).html().replace(/'/g, '"')), undefined, 6);
        //_hLog(str);
        $(this).html(syntaxHighlight(str));
    });

    $('body').on('click', '.toggle', function(e) {
        var o = this.parentNode.parentNode;
        if ($(o).hasClass("active")) {
            $(o).removeClass("active");
        } else {
            $(o).addClass("active");
        }
    });

    $('body').on('click', '.send', function(e) {
        console.log("option=" + JSON.stringify(option));
        if (option) {
            if (option.urlCustom == "Y") {
                var form = $(this).closest('form');
                var formId = $(form).attr('id');
                var formEl = document.querySelector("#" + formId);
                var url_o = formEl.parentNode.querySelector("#_url_org_");
                $(form).attr("action", url_o.value);
            }
        }
        e.preventDefault();
        var form = $(this).closest('form');
        var matchedParamsInRoute = $(form).attr('action').match(/{\w+}$/);
        var theId = $(this).attr('rel');
        var input;
        if (matchedParamsInRoute) {
            $("form#" + $(form).attr('id') + " input[type=text]").each(function() {
                input = $(this);
                var index;
                for (index = 0; index < matchedParamsInRoute.length; ++index) {
                    try {
                        var tmp = matchedParamsInRoute[index].replace('{', '').replace('}', '');

                        if ($(this).attr('id') == tmp) {
                            var newFormAction = $(form).attr('action').replace(matchedParamsInRoute[index], $(this).val());
                            $(form).attr('action', newFormAction);
                        }
                    } catch (err) {
                        console.log(err);
                    }
                }
            });
        };

        var st_headers = {};
        // API 인증키 헤더 추가 
        st_headers[$('#consumerKey_key').val()] = $('#consumerKey_value').val();
        st_headers[$('#consumerSec_key').val()] = $('#consumerSec_value').val();
        
        st_headers[$('#app_key').val()] = $('#app_key_value').val();
        st_headers[$('#package_name').val()] = $('#package_name_value').val();

        if ($('#accessToken_value').val()) {
            // accessToken 추가
            st_headers['Authorization'] = "Bearer " + $('#accessToken_value').val();
        }

        // Non-WEB 모드일 경우 Device 정보 추가 
        if ( document.getElementById("mode_nonweb").checked ) {

        	st_headers['session-id'] = $('#sessionId_value').val();
        	st_headers['device-uuid'] = $('#deviceUuid_value').val();
            st_headers["device-id"] = "It-is-device-id";
            st_headers["device-type"] = "It-is-device-type";
            st_headers["device-ver"] = $('#deviceVer_value').val();
            st_headers["device-model"] = $('#deviceModel_value').val();
            st_headers["device-langcode"] = $('#deviceLangcode_value').val();
            st_headers["device-token"] = $('#deviceToken_value').val();//
            st_headers["device-os"] = $('#deviceOs_value').val();//
            st_headers["device-appver"] = $('#deviceAppver_value').val();//
            st_headers["device-countrycode"] = $('#deviceCountrycode_value').val();
            st_headers["device-timezone"] = $('#deviceTimezone_value').val();
        }

        if ($(form).attr('id')) {
            var formId = $(form).attr('id');
            var formEl = document.querySelector("#" + formId);
            var inputEls = formEl.querySelectorAll("input[type=file]");

            var jqxhr = null;

            if (inputEls != null && inputEls.length > 0) {
                //file 객체가 있는 경우.

                var form = $(form)[0];
                var formData = new FormData();

                for (var inputEl of inputEls) {
                    formData.append(inputEl.name, inputEl.files[0]);
                }

                var json = JSON.stringify($(form).serializeObject());
                formData.append("e", AES_Encode(json));

                window.__formData = formData;

                jqxhr = $.ajax({
                    type: "POST",
                    enctype: 'multipart/form-data',
                    url: $('#apiUrl').val() + $(form).attr('action'),
                    data: formData,
                    processData: false,
                    contentType: false,
                    cache: false,
                    timeout: 600000,
                    dataType: 'text',
                    headers: st_headers
                }).done(function(data) {
                    data = eval("(" + data + ")");
                    data = JSON.parse(AES_Decode(data.e));
                    if (typeof data === 'object') {
                        data = JSON.stringify(data, undefined, 6);
                        $('#response' + theId).html(syntaxHighlight(data));
                    } else {
                        $('#response' + theId).html(syntaxHighlight(data));
                    }
                    $('#response' + theId).show();
                }).fail(function() {
                    alert("error");
                });
            } else { //file 객체가 없는 경우.

                /*
                 * {
                 * 	e: asdlfkjaldkfjaldkfjasdlkfj
                 * }
                 * 
                 * 
                 * */

                //    	            	var serialData = $(form).serializeObject();
                //    		            var secData = AES_Encode(JSON.stringify(serialData));

                var json = JSON.stringify($(form).serializeObject());

                console.log(json);

                jqxhr = $.ajax({
                    url: $('#apiUrl').val() + $(form).attr('action'),
                    data: "e=" + encodeURIComponent(AES_Encode(json)),
                    type: '' + $(form).attr('method') + '',
                    dataType: 'text',
                    headers: st_headers
                }).done(function(data) {
                    data = eval("(" + data + ")");
                    data = JSON.parse(AES_Decode(data.e));
                    if (typeof data === 'object') {
                        data = JSON.stringify(data, undefined, 6);
                        $('#response' + theId).html(syntaxHighlight(data));
                    } else {
                        $('#response' + theId).html(syntaxHighlight(data));
                    }
                    $('#response' + theId).show();
                }).fail(function() {
                    alert("error");
                });
            }
        }


        return false;
    });


};

jQuery.fn.serializeObject = function() {
    var obj = null;
    try {
        if (this[0].tagName && this[0].tagName.toUpperCase() == "FORM") {
            var arr = this.serializeArray();
            if (arr) {
                obj = {};
                jQuery.each(arr, function() {
                    var value = this.value;
                    if (this.value.indexOf("[") >= 0 && this.value.indexOf("]") >= 0) {
                        obj[this.name] = eval(value);
                    }
                    /*else if ( !isNaN(this.value)) {
               		 obj[this.name] = parseInt(value);
               	 } 
               	 */
                    else if (value != "") {
                        obj[this.name] = value;
                    }
                });
            }
        }
    } catch (e) {
        alert(e.message);
    } finally {}
    return obj;
}