package com.ztesoft.spring.common.mybatis;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import com.ztesoft.spring.common.Const;
import com.ztesoft.spring.utils.PropertiesUtil;
import com.ztesoft.spring.utils.StringUtil;

/**
 * mybatis拦截器，编码转换
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
        Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
        Object.class, RowBounds.class, ResultHandler.class})})
public class StringInterceptor implements Interceptor {
  public final static String classPathFileName = "config.properties.param";
  public final static String fileName = "param.properties";

  private static final String TYPE_IN = "in";
  private static final String TYPE_OUT = "out";

  private String interceptorSwitch;// 拦截器开关， on：开 off：关

  @SuppressWarnings("unchecked")
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    if (!"on".equals(this.interceptorSwitch)) {// 拦截器开关
      return invocation.proceed();
    }

    String method = invocation.getMethod().getName();// 拦截方法名称，update or query

    /**
     * SQL入参编码转换
     */
    Object[] obj = invocation.getArgs();
    if (obj.length > 1) {
      Object parameter = invocation.getArgs()[1];// 参数
      if (parameter instanceof Map) {
        converString((Map<String, Object>) parameter, TYPE_IN);
      } else if (parameter instanceof String) {
        String str = (String) parameter;
        if (Const.ENCODING_GBK.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
          invocation.getArgs()[1] =
              new String(str.getBytes(Const.ENCODING_GBK), Const.ENCODING_ISO88591);
      } else {
        converString(parameter, TYPE_IN);
      }
    }

    Object object = invocation.proceed();

    /**
     * 查询结果编码转换
     */
    if ("query".equals(method)) {
      if (object instanceof List) {
        List<Object> list = (List<Object>) object;
        for (int i = 0; i < list.size(); i++) {
          Object element = list.get(i);
          if (element instanceof Map) {
            converString((Map<String, Object>) element, TYPE_OUT);
          } else if (element instanceof String) {
            String str = (String) element;
            if (Const.ENCODING_ISO88591.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
              list.set(i, new String(str.getBytes(Const.ENCODING_ISO88591), Const.ENCODING_GBK));
          } else {
            converString(element, TYPE_OUT);
          }
        }
      } else if (object instanceof Map) {
        converString((Map<String, Object>) object, TYPE_OUT);
      } else if (object instanceof String) {
        String str = (String) object;
        if (Const.ENCODING_ISO88591.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
          object = new String(str.getBytes(Const.ENCODING_ISO88591), Const.ENCODING_GBK);
      } else {
        converString(object, TYPE_OUT);
      }
    }

    return object;
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {
    // 获取param.properties配置文件中的mybatis.interceptor.switch值
    ResourceBundle resource =
        PropertiesUtil.getBundle(Const.VM_CONFIG_PATH, classPathFileName, fileName);
    if (resource != null) {
      this.interceptorSwitch = PropertiesUtil.getString(resource, "mybatis.interceptor.switch");// 拦截器开关
    }
  }

  public void converString(Object object, String type) throws Exception {
    if (object == null || isSimpleType(object)) {
      return;
    }

    BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    for (PropertyDescriptor property : propertyDescriptors) {
      String key = property.getName();

      // 过滤class属性
      if ("class".equals(key)) {
        continue;
      }

      // 得到property对应的get方法
      Method get = property.getReadMethod();
      // 得到property对应的set方法
      Method set = property.getWriteMethod();
      if (get == null) {
        continue;
      }

      try {
        Object value = get.invoke(object);
        if (value instanceof String) {
          String str = (String) value;
          if (TYPE_OUT.equals(type)) {
            // 查询返回结果，将ISO88591转GBK
            if (Const.ENCODING_ISO88591.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
              set.invoke(object, new String(str.getBytes(Const.ENCODING_ISO88591),
                  Const.ENCODING_GBK));
          } else {
            // SQL入参，将GBK转ISO8891
            if (Const.ENCODING_GBK.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
              set.invoke(object, new String(str.getBytes(Const.ENCODING_GBK),
                  Const.ENCODING_ISO88591));
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void converString(Map<String, Object> map, String type) throws Exception {
    if (map == null) {
      return;
    }

    Iterator<String> itr = map.keySet().iterator();
    while (itr.hasNext()) {
      String key = itr.next();
      Object value = map.get(key);
      if (value instanceof String) {
        String str = (String) value;
        if (TYPE_OUT.equals(type)) {
          // 查询返回结果，将ISO88591转GBK
          if (Const.ENCODING_ISO88591.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
            map.put(key, new String(str.getBytes(Const.ENCODING_ISO88591), Const.ENCODING_GBK));
        } else {
          // SQL入参，将GBK转ISO8891
          if (Const.ENCODING_GBK.equals(StringUtil.getEncode(str)))// 避免二次编码转换(转了又转)
            map.put(key, new String(str.getBytes(Const.ENCODING_GBK), Const.ENCODING_ISO88591));
        }
      }
    }
  }

  public static boolean isSimpleType(Object object) {
    if (object == null) {
      return false;
    }

    if ((int.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Integer.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((long.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Long.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((float.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Float.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((double.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Double.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((boolean.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Boolean.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((short.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Short.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((byte.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((Byte.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((String.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    if ((char.class).isAssignableFrom(object.getClass())) {
      return true;
    }

    return false;
  }
}
