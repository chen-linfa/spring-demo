/**
 * Ajax请求
 * 依赖jquery.min.js，Utils.js，json2.js
 */
function Invoker(){
	
}

/**
 * 异步请求
 * @param action
 * @param method
 * @param params
 * @param success
 * @param error
 * @param loading
 */
Invoker.prototype.async = function(action, method, params, success, error, loading){
	this._ajaxCall(true, action, method, params, success, error, loading);
};

/**
 * 同步请求
 * @param action
 * @param method
 * @param params
 * @param success
 * @param error
 * @param loading
 */
Invoker.prototype.sync = function(action, method, params, success, error, loading){
	this._ajaxCall(false, action, method, params, success, error, loading);
};

/**
 * ajax请求
 * @param async
 * @param action
 * @param method
 * @param params
 * @param success
 * @param error
 * @param loading
 */
Invoker.prototype._ajaxCall = function(async, action, method, params, success, error, loading){
	var _self = this;
	
	//第6、7个是可选参数
	if(typeof arguments[5] === "boolean"){
		_self.loading = arguments[5];
	}
	else{
		_self.loading = loading;
	}
	
	_self._loading();
	
	var context_path = Utils.getContextPath();
	var url = context_path + "/" + action + "/" + method + ".do";
	
	if(!params || (typeof(params) === "string" && !jQuery.trim(params))){
		params = "{}";
	}
	
	try{
		params = typeof(params) !== "string" ? JSON.stringify(params) : params;
	}
	catch(e){}
	
	jQuery.ajax({
		url: url,
		async: async,
		contentType: "application/json;charset=UTF-8",
		data: params,
		dataType: "text",
		type: "POST",
		dataFilter: function(data, type){
			try{
				data = jQuery.parseJSON(data);
			}
			catch(e){}
			
			return data;
		},
		success: function(data){
			_self._loaded();
			if(jQuery.isFunction(success)){
				success.call(_self, data);
			}
		},
		error: function(XMLHttpRequest, textStatus, errorThrown){
			_self._loaded();

			var status = XMLHttpRequest.status;
			switch(status){
				case 400 :
					_self._badRequestHandler(status, action, method);
					break;
				case 403 :
					_self._forbiddenHandler(status, action, method);
					break;
				case 404 :
					_self._notFoundHandler(status, action, method);
					break;
				case 500 :
					_self._serverErrorHandler(XMLHttpRequest);
					break;
				case 503 :
					_self._serviceUnavailableHandler(status, action, method);
					break;
				case 999:
					_self._loginTimeoutHandler();
					break;
				default :
					_self._serviceUnavailableHandler(status, action, method);
			}
			
			if(jQuery.isFunction(error)){
				error.apply(_self, arguments);
			}
		}
	});
};

/**
 * 显示loading信息
 */
Invoker.prototype._loading = function(){
	var _self = this;
	if(_self.loadcount === undefined){
		_self.loadcount = 0;
	}
	//loading计数器，防止重复显示loading
	_self.loadcount++;
	
	if(_self.loading !== false && _self.loadcount == 1){
		//在顶层页面显示loadding
		window.top.layer.load();
	}
};

/**
 * 移除loading信息
 */
Invoker.prototype._loaded = function(){
	var _self = this;
	if(_self.loadcount === undefined){
		_self.loadcount = 0;
	}
	//loading计数器，防止重复显示loading
	_self.loadcount--;
	
	if(_self.loadcount < 1){
		window.top.layer.closeAll("loading");
	}
};

Invoker.prototype._badRequestHandler = function(status, action, method){
	window.top.Utils.show("状态码：" + status + "\n服务：" + action + "." + method + "\n信息：请求的服务不存在", 5000);
};

Invoker.prototype._forbiddenHandler = function(status, action, method){
	window.top.Utils.show("状态码：" + status + "\n服务：" + action + "." + method + "\n信息：请求被禁止", 5000);
};

Invoker.prototype._notFoundHandler = function(status, action, method){
	window.top.Utils.show("状态码：" + status + "\n服务：" + action + "." + method + "\n信息：请求的资源不可用", 5000);
};

Invoker.prototype._serverErrorHandler = function(XMLHttpRequest){
	var response_text = XMLHttpRequest.responseText;
	response_text = response_text.replace(/\n/g, "\\n");//回车
	response_text = response_text.replace(/\r/g, "\\r");//换行
	response_text = response_text.replace(/\t/g, "\\t");//水平制表符
	var index = response_text.lastIndexOf("}");
	if(index != response_text.length -1){
		response_text = response_text.substring(0, index + 1);
	}
	
	try{
		var response = jQuery.parseJSON(response_text);
		//alert(response.message);
		//alert(response.stack);
		var more_id = "more_" + Math.round(Math.random() * 10000);
		var more = "<img id=" + more_id + " src='" + Utils.getContextPath() + "/public/images/more.jpg' style='vertical-align:middle;cursor:pointer;margin-left:5px;'/>";
		//在顶层页面显示，防止在小的iframe页面里显示不全
		window.top.Utils.show(response.message + more, {title: "异常信息", time: 5000});
		
		//点击more显示堆栈信息
		window.top.$("#" + more_id).bind("click", {stack: response.stack}, function(e){
			var content = "<div style='overflow:hidden;'><textarea style='overflow:auto;width:594px;height:351px;border:none;color:#000000;' readonly='readonly'>" + e.data.stack + "</textarea></div>";
			window.top.Utils.showDialog(content, {title: "堆栈信息", width: "600px", height: "400px"});
		});
	}
	catch(e){
		alert(response_text);
	}
};

Invoker.prototype._serviceUnavailableHandler = function(status, action, method){
	window.top.Utils.show("状态码：" + status + "\n服务：" + action + "." + method + "\n信息：服务器不可用", 5000);
};

Invoker.prototype._loginTimeoutHandler = function(status, action, method){
	var _self = this;
	if(_self.timeoutcount === undefined){
		_self.timeoutcount = 0;
	}
	
	_self.timeoutcount++;
	if(_self.timeoutcount < 2){
		Utils.alert("登录超时或者用户未登录", {end: function(){
			window.top.location.href = Utils.getContextPath() + "/login.html";
		}});
	}
};

/**
 * 文件上传
 * @param $form
 * @param action
 * @param method
 * @param success
 * @param error
 */
Invoker.prototype.upload = function($form, action, method, success, error){
	/**
	 * 依赖的js
	 * /public/common/Utils.js
	 * /public/lib/easyui/plugins/jquery.parser.js
	 * /public/lib/easyui/plugins/jquery.form.js
	 */
	var _self = this;
	_self._loading();
	$form.form("submit", {
		url: Utils.getContextPath() + "/" + action + "/" + method + ".do",
		success: function(data){
			_self._loaded();
			
			if(data){
				data = data.replace(/\n/g, "\\n");//回车
				data = data.replace(/\r/g, "\\r");//换行
				data = data.replace(/\t/g, "\\t");//水平制表符
				var index = data.lastIndexOf("}");
				if(index != data.length -1){
					data = data.substring(0, index + 1);
				}
				
				try{
					var response = jQuery.parseJSON(data);
					var code = response.code;
					if(code){
						switch(code){
							case "400" :
								_self._badRequestHandler(code, action, method);
								break;
							case "403" :
								_self._forbiddenHandler(code, action, method);
								break;
							case "404" :
								_self._notFoundHandler(code, action, method);
								break;
							case "500" :
								var object = {};
								object.responseText = data;
								_self._serverErrorHandler(object);
								break;
							case "503" :
								_self._serviceUnavailableHandler(code, action, method);
								break;
							case "999":
								_self._loginTimeoutHandler();
								break;
							default :
								_self._serviceUnavailableHandler(code, action, method);
						}
						
						if(jQuery.isFunction(error)){
							error.apply(_self, response);
						}
					}
					else if(jQuery.isFunction(success)){
						success.call(_self, response);
					}
				}
				catch(e){
					if(jQuery.isFunction(success)){
						success.call(_self, data);
					}
				}
			}
		}
	});
};

var Invoker = new Invoker();