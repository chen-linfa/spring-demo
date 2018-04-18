package org.springframework.http.converter.json;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;

/**
 * 入参&返回数据转换器
 * @author zhenym
 * @date 2015-4-10
 */
public class CustomJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter{
	
	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException{
		JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
		JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
		try{
			if(object instanceof String){
				generator.writeRaw((String)object);
			}
			else{
				writePrefix(generator, object);
				Class<?> serializationView = null;
				Object value = object;
				if(value instanceof MappingJacksonValue){
					MappingJacksonValue container = (MappingJacksonValue)object;
					value = container.getValue();
					serializationView = container.getSerializationView();
				}
				if(serializationView != null){
					this.objectMapper.writerWithView(serializationView).writeValue(generator, value);
				}
				else{
					this.objectMapper.writeValue(generator, value);
				}
				writeSuffix(generator, object);
			}

			generator.flush();
			
		}
		catch(JsonProcessingException ex){
			throw new HttpMessageNotWritableException("Could not write content: " + ex.getMessage(), ex);
		}
	}
	
	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException{
		JavaType javaType = getJavaType(type, contextClass);
		if(javaType.getRawClass().equals(String.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return value;
		}
		else if(javaType.getRawClass().equals(int.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Integer.parseInt(value);
		}
		else if(javaType.getRawClass().equals(Integer.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Integer.valueOf(value);
		}
		else if(javaType.getRawClass().equals(long.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Long.parseLong(value);
		}
		else if(javaType.getRawClass().equals(Long.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Long.valueOf(value);
		}
		else if(javaType.getRawClass().equals(float.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Float.parseFloat(value);
		}
		else if(javaType.getRawClass().equals(Float.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Float.valueOf(value);
		}
		else if(javaType.getRawClass().equals(double.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Double.parseDouble(value);
		}
		else if(javaType.getRawClass().equals(Double.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Double.valueOf(value);
		}
		else if(javaType.getRawClass().equals(boolean.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Boolean.parseBoolean(value);
		}
		else if(javaType.getRawClass().equals(Boolean.class)){
			String value = IOUtils.toString(inputMessage.getBody());
			return Boolean.valueOf(value);
		}
		else{
			return readJavaType(javaType, inputMessage);
		}
	}
	
	private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage){
		try{
			return this.objectMapper.readValue(inputMessage.getBody(), javaType);
		}
		catch(IOException ex){
			throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
		}
	}
}