package org.springframework.web.servlet.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
/**
 * 全局异常处理器
 * @author zhenym
 * @date 2014-5-7
 */
public class CustomExceptionResolver extends SimpleMappingExceptionResolver{
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
		if(isAjaxRequest(request)){
			try{
				logException(ex, request);
				
				String message = ex.getMessage();
				String stack = ExceptionUtils.getStackTrace(ex);
				
				//jQuery.ajax响应error方法
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				//设置编码
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=UTF-8");
				
				PrintWriter writer = response.getWriter();
				writer.print("{\"message\":\"" + message + "\", \"stack\":\"" + stack + "\"}");
				writer.flush();
			}
			catch(IOException e){
				//log.error(e);
			}
		}
		else{
			return super.resolveException(request, response, handler, ex);
		}
		
		return null;
	}

	/**
	 * 判断是否为Ajax请求
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request){
		String requestType = request.getHeader("X-Requested-With");
		if(requestType != null && requestType.equals("XMLHttpRequest")){
			return true;
		}
		else{
			return false;
		}
	}
}