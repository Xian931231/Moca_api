var apiList = ["api.js"];

function syntaxHighlight(json) {
    if (typeof json != 'string') {
         json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
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


function drawStatus(api, callback) {    	
	// ************************************************** //
	// Start : 동적 생성하는 부분 
	var cls = api.cls;
	var trString = "";
	//_hLog("cls.length : "+ cls.length);
	
	var no = 0;
	var objs = document.querySelectorAll("#progTable tr");
	no = objs.length;
	for (var i=0; i<cls.length; i++) {		
		var depth1 = cls[i].name;
				
		for (var j=0; j<cls[i].method.length; j++) {
			no = no + 1;
			var depth2 = cls[i].method[j].name;
			var title = cls[i].method[j].desc;
			var rate = cls[i].method[j].progress.rate;
			var bigo = cls[i].method[j].progress.desc;
			
			var style = "";
			var iRate = parseInt(rate);
			if (iRate == 100) {
				rate = "완료";
				style = "success";
			} else if (iRate <= 100 && iRate >90) {
				style = "primary"; 
            } else if (iRate <= 90 && iRate > 50) {
                style = "warning";
            } else if (iRate <= 50 && iRate > 0) {
                style = "default";
			} else if (iRate == 0) {
				rate = "미진행";
				style = "danger";
			}

			trString += statusClass.replace(/{prog.no}/gi, no)
								.replace(/{prog.1depth}/gi, depth1)
								.replace(/{prog.2depth}/gi, depth2)
								.replace(/{prog.title}/gi, title)
								.replace(/{prog.rate}/gi, rate);
								
								
		
			var progTable = document.getElementById("progTable");
			var trObj = document.createElement("tr");
			trObj.setAttribute("class", style);
			trObj.innerHTML = trString;
			progTable.appendChild(trObj);
			trString = "";
			trObj = null;
		}
	}    	
	// End : 동적 생성하는 부분 
	// ************************************************** //
	if(typeof(callback) == "function") {
		callback();
	}
	
	
	

    $('#php-apidoctab a').click(function (e) {
        e.preventDefault()
        $(this).tab('show')
    });

    $('.tooltipP').tooltip({placement:'bottom'});

    $('code[id^=response]').hide();


    $.each($('pre[id^=sample_response]'), function(){        	
        //_hLog($(this).html());
        if($(this).html() == 'NA') {
            return;
        }
        var str = JSON.stringify(JSON.parse($(this).html().replace(/'/g,'"')), undefined, 6);
        //_hLog(str);
        $(this).html(syntaxHighlight(str));
    });

    $('body').on('click', '.send', function(e){
        e.preventDefault();
        var form = $(this).closest('form');
        var matchedParamsInRoute = $(form).attr('action').match(/{\w+}$/);
        var theId = $(this).attr('rel');
        if(matchedParamsInRoute) {
            $("form#"+$(form).attr('id')+" input[type=text]").each(function(){
                var input = $(this);

                var index;
                for (index = 0; index < matchedParamsInRoute.length; ++index) {
                    try {
                        var tmp = matchedParamsInRoute[index].replace('{','').replace('}','');
                        if($(this).attr('id') == tmp) {
                            var newFormAction = $(form).attr('action').replace(matchedParamsInRoute[index], $(this).val());
                            $(form).attr('action', newFormAction);
                        }
                    } catch(err) {
                        console.log(err);
                    }
                }
            });
        };

        var st_headers = {};
        st_headers[$('#apikey_key').val()] = $('#apikey_value').val();
        st_headers["tank"] = "mansu";

        $.ajax({
            url: $('#apiUrl').val()+$(form).attr('action'),
            data: $(form).serialize(),
            type: ''+$(form).attr('method')+'',
            dataType: 'json',
            headers: st_headers
        }).done(function (data) {
            if(typeof data === 'object') {
            	data = JSON.stringify(data, undefined, 6);
            	$('#response'+theId).html(syntaxHighlight(data));
            } else {
                $('#response'+theId).html(syntaxHighlight(data));
            }
            $('#response'+theId).show();
        }).error(function (xhr, textStatus, error){
            $('#response'+theId).html(textStatus +"["+ error +"]\n ResponseText :\n"+ (xhr.responseText).trim());
            $('#response'+theId).show();
        });
        
        return false;
    });

}

function loadJSObject (jspage, callback) {
	var url = "/js/_api/" + jspage;
    jqxhr = $.ajax({
        type: "GET",
        url: url,
        processData: false,
        contentType: false,
        cache: false,
        timeout: 600000,
        dataType: 'text',
    }).done(function(data) {
        data = data + " return api;";
        
	var result = eval('(function() {' + data + '}())');            
		if(typeof(callback) == "function") {
			callback(result);
		}
    }).fail(function() {
		callback(null);
    });
}

var pos = 0;
function drawList() {
	if(pos < apiList.length) {
		var js = apiList[pos];
		pos++;
		loadJSObject(js, function(jsObj) {
			drawStatus(jsObj, drawList);	
		});
	}
	return;
}

window.onload=function(){
	var progTable = document.getElementById("progTable");
	progTable.innerHTML = "";
	
	pos = 0;
	drawList();
};


