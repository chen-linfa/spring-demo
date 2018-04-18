package com.ztesoft.spring.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

/**
 * 配置文件读取工具类
 */
public class PropertiesUtil {

  /**
   * 
   * @param vmName 启动应用是通过 -D设置的参数名称，如 CONFING_PATH、dubbo.protocol.port等
   * @param classPathFileName 在classpath 下面的文件名，不带后缀，如 config.rocketmq
   * @param fileName 文件全名，带后缀，如rocketmq.properties
   * @return
   */
  public static ResourceBundle getBundle(String vmName, String classPathFileName, String fileName) {
    ResourceBundle resource = null;
    String configPath = System.getProperty(vmName);// 获取vm动态参数
    if (configPath == null) {// 如果没有配置vm参数，根据classpath方式来获取
      resource = ResourceBundle.getBundle(classPathFileName, Locale.CHINA);
    } else {
      try {
        // 根据jvm参数配置的绝对路径获取
        if (configPath.startsWith("file:")) {
          configPath = configPath.substring("file:".length()) + File.separator + fileName;
        } else if (configPath.startsWith("classpath:")) {
          configPath += "/" + fileName;
        }
        resource = getResourceBundle(configPath);
      } catch (Exception e) {
      }
    }
    return resource;
  }


  /**
   * 注意使用完关闭inputstream流
   * 
   * @param vmName 启动应用是通过 -D设置的参数名称，如 CONFING_PATH、dubbo.protocol.port等
   * @param fileName 文件全名，带后缀，如rocketmq.properties
   * @return
   */
  public static InputStream getBundleInputStream(String vmName, String fileName) {
    InputStream in = null;
    String configPath = System.getProperty(vmName);// 获取vm动态参数
    if (configPath == null) {// 如果没有配置vm参数，根据classpath方式来获取
      // resource = ResourceBundle.getBundle(classPathFileName, Locale.CHINA);
    } else {
      try {
        // 根据jvm参数配置的绝对路径获取
        if (configPath.startsWith("file:")) {
          configPath = configPath.substring("file:".length()) + File.separator + fileName;
        } else if (configPath.startsWith("classpath:")) {
          configPath += "/" + fileName;
        }
        in = getResourceInputStream(configPath);
      } catch (Exception e) {
      }
    }
    return in;
  }

  /**
   * 获取配置文件值（优先获取vm参数的值）
   * 
   * @param resource
   * @param key
   * @return
   */
  public static String getString(ResourceBundle resource, String key) {
    try {
      String vmKey = System.getProperty(key);// 获取vm动态参数
      if (vmKey != null) {
        return vmKey;// 如果vm参数配置了该key，则优取该值
      }
      return resource.getString(key);
    } catch (Exception e) {
    }
    return null;
  }


  /**
   * 支持读取在jar中的资源文件
   * 
   * @param resourceLocation
   * @return
   * @throws IOException
   * @author SvenAugustus(蔡政滦) e-mail: SvenAugustus@outlook.com
   * @version 2016年7月30日
   */
  public static ResourceBundle getResourceBundle(String resourceLocation) throws IOException {
    Assert.notNull(resourceLocation, "Resource location must not be null");
    ResourceBundle resource = null;

    InputStream in = null;
    try {
      in = getResourceInputStream(resourceLocation);
      resource = new PropertyResourceBundle(in);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }
    return resource;
  }

  /**
   * 支持读取在jar中的资源文件
   * 
   * @param resourceLocation
   * @return
   * @throws IOException
   * @author SvenAugustus(蔡政滦) e-mail: SvenAugustus@outlook.com
   * @version 2016年7月30日
   */
  public static InputStream getResourceInputStream(String resourceLocation) throws IOException {
    Assert.notNull(resourceLocation, "Resource location must not be null");
    InputStream in = null;

    File resourceFile = null;

    if (resourceLocation.startsWith("classpath:")) {
      String path = resourceLocation.substring("classpath:".length());
      String description =
          (new StringBuilder()).append("class path resource [").append(path).append("]").toString();
      ClassLoader cl = ClassUtils.getDefaultClassLoader();
      URL url = cl == null ? ClassLoader.getSystemResource(path) : cl.getResource(path);
      if (url == null)
        throw new FileNotFoundException((new StringBuilder()).append(description)
            .append(" cannot be resolved to absolute file path because it does not exist")
            .toString());
      else {
        Assert.notNull(url, "Resource URL must not be null");
        if ("jar".equals(url.getProtocol())) {
          org.springframework.core.io.Resource fileRource = new ClassPathResource(path);
          in = fileRource.getInputStream();
        } else if ("file".equals(url.getProtocol())) {
          try {
            resourceFile = new File(ResourceUtils.toURI(url).getSchemeSpecificPart());
          } catch (URISyntaxException ex) {
            resourceFile = new File(url.getFile());
          }
        } else {
          throw new FileNotFoundException((new StringBuilder()).append(description)
              .append(" cannot be resolved to absolute file path ")
              .append("because it does not reside in the file system: ").append(url).toString());
        }
      }
    }
    if (in == null) {
      if (resourceFile == null) {
        try {
          resourceFile = ResourceUtils.getFile(new URL(resourceLocation));
        } catch (MalformedURLException ex) {
          resourceFile = new File(resourceLocation);
        }
      }
      in = new FileInputStream(resourceFile);
    }
    return in;
  }

}
