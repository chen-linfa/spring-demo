
var index = {

	initEvent : function() {
		var me = this;
		Invoker.async("LoginController", "queryMenus", {name:"admin",password:"123456"}, function(result) {
			//alert("1111");
		});
	},
	
	init : function() {
		var me = this;
		me.initEvent();
	}
};

$(function(){	
    index.init();
});