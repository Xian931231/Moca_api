var util = {
		getUUID: function(){
			// UUID용 변수
			var lut = []; 
			for (var i=0; i<256; i++) { 
				lut[i] = (i<16?'0':'')+(i).toString(16); 
			};
			
			var d0 = Math.random()*0xffffffff|0;
		    var d1 = Math.random()*0xffffffff|0;
		    var d2 = Math.random()*0xffffffff|0;
		    var d3 = Math.random()*0xffffffff|0;
		    return lut[d0&0xff]+lut[d0>>8&0xff]+lut[d0>>16&0xff]+lut[d0>>24&0xff]+'-'+
		      lut[d1&0xff]+lut[d1>>8&0xff]+'-'+lut[d1>>16&0x0f|0x40]+lut[d1>>24&0xff]+'-'+
		      lut[d2&0x3f|0x80]+lut[d2>>8&0xff]+'-'+lut[d2>>16&0xff]+lut[d2>>24&0xff]+
		      lut[d3&0xff]+lut[d3>>8&0xff]+lut[d3>>16&0xff]+lut[d3>>24&0xff];
		},
		
		aesEncode: function(plain_text){
			GibberishAES.size(256);
			var tmp = GibberishAES.aesEncrypt(plain_text, getSecKey());
			tmp = tmp.replace(/[\r|\n]/g, '');
		    return tmp;
		},

		aesDecode: function(base64_text){
			GibberishAES.size(256);
			var tmp = GibberishAES.aesDecrypt(base64_text, getSecKey());
		    return tmp;
		},
		
		
		getCookie: function(key){
			let result;
			const cookie = document.cookie.split(";");
			
			cookie.some((v) => {
				v = v.replaceAll(" ", "");
				
				let item = v.split("=");
				
				if(item[0] === key) {
					result = item[1];
					return true;
				}
			});
			
			return result;
		},
		
		setCookie: function(key, value, day){
			var cookie = key + "=" + encodeURI(value) + ";path=/ ;";
			
			if(day != null) {
				var expire = new Date();
				
				expire.setDate(expire.getDate() + day);
				
				cookie += "expires=" + expire.toGMTString() + ";";
			}
			
			document.cookie = cookie;
		}

}