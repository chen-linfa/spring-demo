package org.springframework.web.servlet.mvc.method.annotation;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ReqMethod;
import org.springframework.web.bind.annotation.ReqProxy;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


/**
 * 请求方法映射器
 * @author zhenym
 * @date 2015-4-10
 */
public class CustomHandlerMapping extends RequestMappingHandlerMapping{
	
	@Override
	protected boolean isHandler(Class<?> beanType){
		//此映射处理器支持有(@Controller、@RestController、@ControllerAdvice)和@ReqProxy注解的类
		return ((AnnotationUtils.findAnnotation(beanType, Controller.class) != null || 
			AnnotationUtils.findAnnotation(beanType, RestController.class) != null || 
			AnnotationUtils.findAnnotation(beanType, ControllerAdvice.class) != null) && 
			(AnnotationUtils.findAnnotation(beanType, ReqProxy.class) != null));
	}
	
	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType){
		RequestMappingInfo info = null;
		//查找有@ReqMethod注解的方法
		ReqMethod methodAnnotation = AnnotationUtils.findAnnotation(method, ReqMethod.class);
		if(methodAnnotation != null){
			info = createRequestMappingInfo(method, handlerType);
		}
		return info;
	}
	
	protected RequestMappingInfo createRequestMappingInfo(Method method, Class<?> handlerType){
		//以类名和方法名映射请求，参照@RequestMapping，但是不需要为注解@ReqProxy和@ReqMethod添加任何参数(如：/className/methodName.do)
		return new RequestMappingInfo(new PatternsRequestCondition("/" + handlerType.getSimpleName() + "/" + method.getName() + ".do"),
			null, null, null, null, null, null);
	}
}