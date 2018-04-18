(function($){
	/**
	 * 前台工具
	 */
	window.Utils = {
		getContextPath: function(){
			var pathname = document.location.pathname;
			var index = pathname.substr(1).indexOf("/");
			var context_path = "";
			
			if(index !== -1){
				context_path = pathname.substr(0, index + 1);
			}
			
			if(context_path && context_path.substr(0, 1) != "/"){
				context_path = "/" + context_path;
			}
			
			return context_path;
		},
		getScript: function(url, success, error){
			//同步取javascript
			jQuery.ajax({
				url: url,
				async: false,
				cache: true,
				dataType: "script",
				success: success,
				error: error
			});
		},
		createInst: function(class_name, param){
			if(param){
				return (new Function("return new " + class_name + "(arguments[0]);")(param));
			}
			else{
				return (new Function("return new " + class_name + "();")());
			}
		},
		/**
		 * 提示信息
		 * options：{title: "标题", time: "自动关闭时间", yes: "确定按钮事件", cancel: "右上角关闭事件", end: "关闭事件"}
		 */
		alert: function(message, options){
			try{
				var defaults = {title: "信息", time: 0};
				options = $.extend({}, defaults, options);
				message = message.replace(/\n/g, "<br>");//回车
				
				layer.alert(message, {
					title: options.title,
					time: options.time,
					yes: function(index, layero){
						if($.isFunction(options.yes)){
							options.yes();
						}
						
						layer.close(index);
					},
					cancel: function(index){
						if($.isFunction(options.cancel)){
							options.cancel();
						}
					},
					end: function(){
						if($.isFunction(options.end)){
							options.end();
						}
					}
				});
			}
			catch(e){
				message = message.replace(/<br>|<\/br>/g, "\n");//回车
				alert(message);
			}
		},
		/**
		 * 右下角提示信息
		 * content：提示内容(字符串)
		 * options：{title: "标题", time: "自动关闭时间"}
		 */
		show: function(content, options){
			var defaults = {title: "信息", time: 0};
			options = $.extend({}, defaults, options);
			layer.open({
				type : 1,
				shade: 0,
				shift: 2,
				title: options.title,
				time: options.time,
				offset : "rb",
				content : "<div style='padding:20px;word-break:break-all;color:#404040;font-size:12px;width:220px;'>" + content + "</div>"
			});
		},
		/**
		 * 确认信息 options：{title: "标题", yes: "确定按钮事件", cancel: "取消按钮事件"}
		 */
		confirm: function(message, options){
			var defaults = {title: "信息"};
			options = $.extend({}, defaults, options);
			message = message.replace(/<br>|<\/br>/g, "\n");//回车
			layer.confirm(message, {title: options.title}, function(index){
				if($.isFunction(options.yes)){
					options.yes();
				}
				layer.close(index);
			}, function(index){
				if($.isFunction(options.cancel)){
					options.cancel();
				}
			});
		},
		/**
		 * iframe弹出框(适用弹出选择页面)
		 * url：iframe地址
		 * options：{title: "标题", width: "宽度", height: "高度", autoClose: "点击确定是否自动关闭", yes: "确定按钮方法，iframe中callback方法返回值做为参数", cancel: "取消按钮方法", success: "层弹出后的成功回调方法", end: "层销毁后触发的回调"}
		 */
		openDialog: function(url, options){
			var defaults = {title: "信息", width: "700px", height: "500px", autoClose: true};
			options = $.extend({}, defaults, options);
			
			var settings = {
				type: 2,
				content: url,
				title: options.title,
				area: [options.width, options.height]
			};
			
			var btn = [];
			if($.isFunction(options.yes)){
				btn.push("确定");
				settings.yes = function(index, layero){
					var frame_id = layero.find("iframe").attr("id");
					var frm = window.frames[frame_id];
					//调用iframe中的callback方法获取数据并做为yes方法的参数
					if(frm && $.isFunction(frm.callback)){
						var data = frm.callback(index);
						options.yes(data, index);
					}
					
					if(options.autoClose){
						layer.close(index);
					}
				};
			}
			
			if($.isFunction(options.cancel)){
				btn.push("取消");
				settings.cancel = function(index){
					options.cancel();
				};
			}
			
			if($.isFunction(options.success)){
				settings.success = options.success;
			}
			
			if($.isFunction(options.end)){
				settings.end = options.end;
			}
			
			if(btn.length > 0){
				settings.btn = btn;
			}
			
			layer.open(settings);
		},
		/**
		 * 弹出框
		 * content：可以是html内容，也可以是dom(如：$("#id"))对象
		 * options：{title: "标题", width: "宽度", height: "高度", autoClose: "点击确定是否自动关闭", yes: "确定按钮方法", cancel: "取消按钮方法", success: "层弹出后的成功回调方法"}
		 */
		showDialog: function(content, options){
			var defaults = {title: "信息", width: "700px", height: "500px", autoClose: true};
			options = $.extend({}, defaults, options);
			
			var settings = {
				type: 1,
				content: content,
				title: options.title,
				area: [options.width, options.height]
			};
			
			var btn = [];
			if($.isFunction(options.yes)){
				btn.push("确定");
				settings.yes = function(index, layero){
					options.yes(index, layero);
					
					if(options.autoClose){
						layer.close(index);
					}
				};
			}
			
			if($.isFunction(options.cancel)){
				btn.push("取消");
				settings.cancel = function(index){
					options.cancel();
				};
			}
			
			if($.isFunction(options.success)){
				settings.success = function(layero, index){
					options.success(layero, index);
				};
			}
			
			if(btn.length > 0){
				settings.btn = btn;
			}
			
			layer.open(settings);
		},
		initSelect: function(select, code, blankValue, blankName, callback){
			Invoker.sync("CacheController", "getAttrValues", code, function(data){
				data = data || [];
				var html = [];
				
				if(blankValue != undefined && blankName != undefined){
					html.push("<option value='" + blankValue + "'>" + blankName + "</option>");
				}
				
				$.each(data, function(i, attrValue){
					html.push("<option value='" + attrValue.attr_value + "'>" + attrValue.attr_value_name + "</option>");
				});
				
				select.html(html.join(""));
				
				if($.isFunction(callback)){
					callback.call(select);
				}
			}, false);
		},
		/**获取URL参数**/
		getUrlParams: function(name){
			var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
			var r = window.location.search.substr(1).match(reg);  //匹配目标参数
			if (r!=null) {
				return unescape(r[2]);
			} 
			return null; //返回参数值
		},
		//获取配置的系统参数
		getParamVal: function(code){
			var result = null;
			Invoker.sync("CacheController", "getParamVal", code, function(data){
				result = data;
			});
			return result;
		},
		/**加载iframe，加载完毕触发callback
		 * frm 可以是iframe的id，也可以可以iframe的jquery对象，也可以是iframe的dom对象
		 * src iframe的url
		 * callback 加载完毕回调函数
		 */
		loadFrame: function(frm, src, callback){
			if(typeof(frm) === "string"){
				frm = $("#" + frm).get(0);
			}
			
			if(frm && frm.size &&  frm.size() > 0){
				frm = frm.get(0);
			}
			
			if(frm){
				frm.onload = frm.onreadystatechange = function(){
					if(this.readyState && this.readyState != "complete"){
						return;
					}
					else{
						if($.isFunction(callback)){
							callback.call(frm);
						}
					}
				};
				
				$(frm).attr("src", src);
			}
		},
		/**刷新iframe的高度
		 * frm 可以是iframe的id，也可以可以iframe的jquery对象，也可以是iframe的dom对象
		 */
		refreshFrameHeight: function(frm, height){
			if(typeof(frm) === "string"){
				frm = $("#" + frm).get(0);
			}
			
			if(frm && frm.size &&  frm.size() > 0){
				frm = frm.get(0);
			}
			
			if(frm && frm.contentDocument){
				if(!height){
					height = frm.contentDocument.body.scrollHeight;
				}
				
				frm.height = height;
			}
		},
		/**
		 * 判断str1是否以str2开始
		 */
		startWith: function(str1, str2){
			if(str1 == null || str1.length == 0){
				return false;
			}
			
			if(str2 == null || str2 == "" || str2.length > str1.length){
				return false;
			}
				
			if(str1.substr(0, str2.length) == str2){
				return true;
			}
			else{
				return false;
			}
			
			return true; 
		},
		/**
		 * 判断str1是否以str2结束
		 */
		endWidth: function(str1, str2){
			if(str1 == null || str1.length == 0){
				return false;
			}
			
			if(str2 == null || str2 == "" || str2.length > str1.length){
				return false;
			}
			
			if (str1.substring(str1.length - str2.length) == str2){
				return true;
			}
			else{
				return false;
			}
			
			return true;
		},
		/**
		 * 根据property从对象中获取对应value值
		 * @param obj
		 * @param key
		 * @returns
		 */
		getObjVal: function(obj, key){
			var me = this;
			if(me.isEmpty(obj)){
				return "";
			}
			
			var v = obj[key];
			if(me.isEmpty(v)){
				return "";
			}
				
			return v;
		},
		/**
		 * 判断对象or字符是否非空
		 * @param str
		 * @returns {Boolean}
		 */
		isEmpty: function(str) {
			if(null == str || 'undefined' == str || '' === $.trim(str)){
				return true;
			}
				
			return false;
		},
		/**
		 * 追加元素
		 */
		appendElement: function($ulJQ, $liJQ, eleType){
			var c = $ulJQ.children(eleType).size();
			if(0 == c){
				$ulJQ.append($liJQ);
			}
			else {
				$ulJQ.children(eleType).last().after($liJQ);
			}
		},
		/**
		 * 删除元素
		 */
		removeElement: function($ulJQ, eleType){
			//第一个元素是复制用的，为方便处理，永远静态存在
			$ulJQ.children(eleType + ":gt(0)").each(function(i){
				$(this).remove();
			});
		},
		
		/**
		 * IP校验
		 */
		vilidationIp : function(ip) {
			var vilidationState = false;
			var index = ip.indexOf("-");
			if (index > -1) {
				var re = /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;
				if (!re.test(ip.substr(0, index))) {
					vilidationState = true;
				}
				var re = /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])$/;
				if (!re.test(ip.substr(index + 1, _ip.length))) {
					vilidationState = true;
				}
			} else {
				var re = /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]|[*])$/;
				if (!re.test(ip)) {
					vilidationState = true;
				}
			}
			return vilidationState;
		},

		checkPhoneNumber: function(phoneNumber){
			var reg  = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1})|(17[0-9]{1}))+\d{8})$/;
			if(reg.test(phoneNumber)){
				return true;
			}
			return false;
		},
		encrypt: function(str){
			var encrypt = new JSEncrypt();
			var public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyfEuMt351kG8e2ZSN47rp95dY\nTX4rGHdGXrx+mFwm5W7VZlrmtY07QJNUJ0myQragBUiYLSNeVSysSLbNOUDBAs1i\nPcb/QG++UzmJhCDjf1+GJANHtfLI7CDaFM1aoa/7C09qWkds7n0QdpT5iLPCOS6C\nVYq5DBv5mDgUeAwleQIDAQAB\n";
			encrypt.setPublicKey(public_key);
			return encrypt.encrypt(str);
		},
		/**获取本地缓存*/
		getItem: function(key){
			if(window.localStorage){
				var value = window.localStorage.getItem(key);
				if(value != null){
					try{
						value = $.parseJSON(value);
					}
					catch(e){
						
					}
				}
				return value;
			}
			
			return null;
		},
		/**添加本地缓存*/
		setItem: function(key, value){
			if(window.localStorage){
				if(typeof(value) !== "string"){
					value = JSON.stringify(value);
				}
				window.localStorage.setItem(key, value);
			}
		},
		/**删除本地缓存*/
		removeItem: function(key){
			if(window.localStorage){
				window.localStorage.removeItem(key);
			}
		},
		/**清空本地缓存*/
		clear: function(){
			if(window.localStorage){
				window.localStorage.clear();
			}
		},
		/**校验密码复杂度**/
		checkPasswdStrength : function(passWord){
			var me = this;
			Modes = 0;
			for(var i = 0; i < passWord.length; i++) {
			    //测试每一个字符的类别并统计一共有多少种模式.
			    Modes |= me.CharMode(passWord.charCodeAt(i));
			}
			return me.bitTotal(Modes);
		},
		
		//测试某个字符是属于哪一类
		CharMode : function(iN){
		   if(iN>=48 && iN <=57){
			   return 1; //数字
		   }else if(iN>=65 && iN <=90){
			   return 2; //大写字母
		   }else if(iN>=97 && iN <=122){
			   return 4; //小写
		   }else{
			   return 8; //特殊字符
		   }
		},

		//计算出当前密码当中一共有多少种模式
		bitTotal : function(num){
		   var modes=0;
		   for(var i=0; i<4; i++){
			   if((num & 1) > 0){
				   modes++; 
			   }
			   num>>>=1;
		   }
		   return modes;
		}
	};
	
	$(document).keydown(function(e){
		try{
			if(e.which === 8){//退格
				var doPrevent = true;
				if(e.target.nodeName.toLowerCase() === "input" || e.target.nodeName.toLowerCase() === "textarea"){
					if(!$(e.target).prop("readonly") && !$(e.target).prop("disabled")){
						doPrevent = false;
					}
				}
				
				if(doPrevent){
					e.preventDefault();
				}
			}
		}
		catch(exception){
			
		}
	});
})(jQuery);