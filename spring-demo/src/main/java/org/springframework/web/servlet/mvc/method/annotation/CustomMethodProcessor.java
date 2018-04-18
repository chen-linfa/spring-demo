package org.springframework.web.servlet.mvc.method.annotation;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.ReqMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;


/**
 * 方法参数处理器
 * @author zhenym
 * @date 2015-4-10
 */
public class CustomMethodProcessor extends RequestResponseBodyMethodProcessor{
	
	public CustomMethodProcessor(List<HttpMessageConverter<?>> messageConverters){
		super(messageConverters);
	}
	
	public CustomMethodProcessor(List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager){
		super(messageConverters, contentNegotiationManager);
	}
	
	public CustomMethodProcessor(List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager, List<Object> responseBodyAdvice){
		super(messageConverters, contentNegotiationManager, responseBodyAdvice);
	}
	
	@Override
	public boolean supportsParameter(MethodParameter parameter){
		return parameter.hasParameterAnnotation(RequestBody.class) || parameter.getMethodAnnotation(ReqMethod.class) != null;
	}
}