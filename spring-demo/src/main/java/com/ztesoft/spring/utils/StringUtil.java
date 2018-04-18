package com.ztesoft.spring.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.soap.SOAPConstants;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.ztesoft.spring.common.Const;

/**
 * 字符串工具
 * 
 * @author zhenym
 * @date 2015-4-10
 */
public class StringUtil {

  public static final String allChar = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * 字符串比较(null和空或空格字符串认为相等)
   * 
   * @param str1
   * @param str2
   * @return
   */
  public static boolean equals(String str1, String str2) {
    if (StringUtils.isBlank(str1)) {
      str1 = "";
    }

    if (StringUtils.isBlank(str2)) {
      str2 = "";
    }

    return str1.equals(str2);
  }


  /**
   * 生成自定义主键 :时间戳+n位随机数
   * 
   * @param date 前缀：当前系统时间
   * @param n 后缀：n为随机数
   * @param max 随机数最大值
   * @return
   */
  public static String genCustomSeq(Date date, int n, int max) {
    // ID前缀 由时间戳构成
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String prefix_id = df.format(date);

    // ID后缀 由n位随机数构成
    Random random = new Random();
    int randVal = random.nextInt(max);
    String suffix_id = String.format("%0" + n + "d", randVal);// 0 代表前面补充0 3 代表长度为3 d代表参数为正数型

    return (prefix_id + suffix_id);
  }

  /**
   * 返回一个定长的随机字符串(只包含大小写字母、数字)
   * 
   * @param length 随机字符串长度
   * @return 随机字符串
   */
  public static String generateString(int length) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      sb.append(allChar.charAt(random.nextInt(allChar.length())));
    }
    return sb.toString();
  }


  /**
   * 判断字符串不为空
   * 
   * @param string
   * @return
   */
  public static boolean isNotEmpty(String string) {
    return string != null && !"null".equals(string) && string.length() > 0;
  }

  /**
   * 判断字符串为空
   * 
   * @param string
   * @return
   */
  public static boolean isEmpty(String string) {
    return string == null || "null".equals(string) || string.length() == 0;
  }

  /**
   * @param url
   * @return str 数据 str[0] 为ip str[1] 为端口，未配置端口时，默认为80
   */
  public static String[] getIpAndPortByUrl(String url) {
    String[] strs = new String[2];
    String regex = "//(.*?)(:\\d{1,5})?/";// "//(.*?):(\\d{1,5})?";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(url);
    while (m.find()) {
      strs[0] = m.group(1);
      String ports = m.group(2);
      if (isNotEmpty(ports)) {
        ports = ports.substring(1);
      } else {
        ports = "80";
      }
      strs[1] = ports;
    }
    return strs;
  }

  /**
   * 判断报文是否SOAP协议
   * 
   * @param content
   * @return
   */
  public static boolean isSoap(String content) {
    if (StringUtils.isBlank(content)) {
      return false;
    }

    return isSoap11(content) || isSoap12(content);
  }

  /**
   * 判断是否soap1.1协议
   * 
   * @param content
   * @return
   */
  public static boolean isSoap11(String content) {
    if (StringUtils.isBlank(content)) {
      return false;
    }

    if (content.indexOf(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE) != -1) {// SOAP1.1
      return true;
    }

    return false;
  }

  /**
   * 判断是否soap1.2协议
   * 
   * @param content
   * @return
   */
  public static boolean isSoap12(String content) {
    if (StringUtils.isBlank(content)) {
      return false;
    }

    if (content.indexOf(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE) != -1) {// SOAP1.2
      return true;
    }

    return false;
  }

  public static String join(Collection<?> collection, String separator) {
    if (collection == null) {
      return null;
    }
    return join(collection.iterator(), separator);
  }

  public static String join(Iterator<?> iterator, String separator) {
    if (iterator == null) {
      return null;
    }
    if (!(iterator.hasNext())) {
      return "";
    }
    Object first = iterator.next();
    if (!(iterator.hasNext())) {
      return ObjectUtils.toString(first);
    }

    StringBuffer buf = new StringBuffer(256);
    if (first != null) {
      buf.append(first);
    }

    while (iterator.hasNext()) {
      if (separator != null) {
        buf.append(separator);
      }
      Object obj = iterator.next();
      if (obj != null) {
        buf.append(obj);
      }
    }
    return buf.toString();
  }

  /**
   * 判断字符串是否XML
   * 
   * @param str
   * @return
   */
  public static boolean isXml(String str) {
    if (StringUtils.isNotBlank(str)) {
      str = StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeJava(str)).trim();
      if (str.startsWith("<") && str.endsWith(">")) {
        return true;
      }
    }

    return false;
  }

  /**
   * 判断字符串是否JSON
   * 
   * @param str
   * @return
   */
  public static boolean isJson(String str) {
    if (StringUtils.isNotBlank(str)
        && ((str.trim().startsWith("{") && str.trim().endsWith("}")) || (str.trim().startsWith("[") && str
            .trim().endsWith("]")))) {
      return true;
    }

    return false;
  }

  // 返回编码格式
  public static String getEncode(String str) {
    String encode = null;
    if (verifyEncode(str, Const.ENCODING_GBK)) {
      encode = Const.ENCODING_GBK;
    } else if (verifyEncode(str, Const.ENCODING_ISO88591)) {
      encode = Const.ENCODING_ISO88591;
    } else if (verifyEncode(str, Const.ENCODING_UTF8)) {
      encode = Const.ENCODING_UTF8;
    }

    return encode;
  }

  // 判断编码格式是否相符
  public static boolean verifyEncode(String str, String encode) {
    try {
      if (str.equals(new String(str.getBytes(encode), encode))) {
        return true;
      }
    } catch (UnsupportedEncodingException e) {

    }
    return false;
  }

  /**
   * 生成CRM接口的ExchangeId
   * 
   * @param client_id
   * @return
   */
  public static String genExchangeId(String client_id) {
    if (StringUtils.isBlank(client_id)) {
      client_id = "";
    } else {
      client_id = client_id.trim();
    }

    String date = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

    Random random = new Random();
    int randVal = random.nextInt(10000000);

    return client_id + date + String.format("%07d", randVal);
  }

  /**
   * 生成TSAP流水号(湖南)
   * 
   * @return
   */
  public static String genWorkOrderId() {
    return genWorkOrderId(new Date());
  }

  /**
   * 生成TSAP流水号(湖南)
   * 
   * @param curDate
   * @return
   */
  private static String genWorkOrderId(Date curDate) {
    String date = new SimpleDateFormat("yyyyMMddHHmmss").format(curDate);
    Random random = new Random();
    int randVal = random.nextInt(10000);

    return date + String.format("%04d", randVal);
  }

  /**
   * 生成TSAP调用时间(湖南)
   * 
   * @return
   */
  public static String genSendDateTime() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
  }

  /**
   * 生成TSAP流水号&调用时间(湖南)
   * 
   * @return
   */
  public static Map<String, String> genWorkOrderIdMap() {
    Date curDate = new Date();
    String workOrderId = genWorkOrderId(curDate);
    String sendDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate);

    Map<String, String> map = new HashMap<String, String>();
    map.put("workOrderId", workOrderId);
    map.put("sendDateTime", sendDateTime);

    return map;
  }

  /**
   * 手机号码校验
   * 
   * @param mobilePhone
   * @return
   */
  public static boolean validateMobilePhone(String mobilePhone) {
    if (isEmpty(mobilePhone)) {
      return false;
    }

    String REGEX_MOBILE = "^((13[0-9])|(15[^4,\\D])|(18[0,3,5-9]))\\d{8}$";
    return Pattern.matches(REGEX_MOBILE, mobilePhone);
  }

  /**
   * 电子邮箱地址校验
   * 
   * @param email
   * @return
   */
  public static boolean validateEmail(String email) {
    if (isEmpty(email)) {
      return false;
    }

    String REGEX_EMAIL =
        "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    return Pattern.matches(REGEX_EMAIL, email);
  }

  /**
   * 是否是数字
   * 
   * @param str
   * @return true 是， false 否
   */
  public static boolean isNumeric(String str) {

    if (isEmpty(str)) {
      return false;
    }

    String NUMERIC = "[0-9]*";
    return Pattern.matches(NUMERIC, str);
  }

  public static int checkPasswdStrength(String passWord) {
    int Modes = 0;
    for (int i = 0; i < passWord.length(); i++) {
      // 测试每一个字符的类别并统计一共有多少种模式
      Modes |= CharMode(passWord.charAt(i));
    }
    return bitTotal(Modes);
  }

  // 测试某个字符是属于哪一类
  public static int CharMode(int iN) {
    if (iN >= 48 && iN <= 57) {
      return 1; // 数字
    } else if (iN >= 65 && iN <= 90) {
      return 2; // 大写字母
    } else if (iN >= 97 && iN <= 122) {
      return 4; // 小写
    } else {
      return 8; // 特殊字符
    }
  }

  // 计算出当前密码当中一共有多少种模式
  public static int bitTotal(int num) {
    int modes = 0;
    for (int i = 0; i < 4; i++) {
      if ((num & 1) > 0) {
        modes++;
      }
      num >>>= 1;
    }
    return modes;
  }

  public static String getStrValue(Map m, String name) {
    if (m == null) {
      return "";
    }
    Object t = m.get(name);
    if (t == null)
      return "";
    return ((String) m.get(name).toString()).trim();
  }

  /**
   * 多字符参数非空判断
   * 
   * @param error_msg
   * @param strings
   */
  public static void valiteMultiStringNull(String error_msg, String... strings) {
    for (String str : strings) {
      if (isEmpty(str)) {
        throw new IllegalArgumentException(error_msg);
      }
    }
  }


  /**
   * 根据字节长度截取字符串
   * 
   * @param str
   * @param byteLen
   * @return
   */
  public static String substringByteLen(String str, int byteLen) {
    try {
      if (str != null) {
        byte[] bytes = str.getBytes("utf-8");
        if (bytes.length > byteLen) {
          byte[] bb = new byte[byteLen];
          for (int i = 0; i < bb.length; i++) {
            bb[i] = bytes[i];
          }
          str = new String(bb, "utf-8");
        }
      }
    } catch (Exception e1) {
    }
    return str;
  }

  public static String toStringBuilder(Object[] array) {
    if (array == null || array.length <= 0) {
      return "";
    }
    StringBuffer stringBuffer = new StringBuffer("[");
    String pre = "";
    for (int i = 0; i < array.length; i++) {
      Object obj = array[i];
      stringBuffer.append(pre).append(obj == null ? "NULL" : String.valueOf(obj));
      pre = ",";
    }
    stringBuffer.append("]");
    return stringBuffer.toString();
  }

  public static String toStringBuilder(Collection collection) {
    if (collection == null) {
      return "";
    }
    StringBuffer stringBuffer = new StringBuffer("{");
    String pre = "";
    Iterator it = collection.iterator();
    while (it.hasNext()) {
      Object obj = (Object) it.next();
      stringBuffer.append(pre).append(obj == null ? "NULL" : String.valueOf(obj));
      pre = ",";
    }
    stringBuffer.append("}");
    return stringBuffer.toString();
  }

  /**
   * 比较两个字符串
   * 
   * @param s1
   * @param s2
   * @return
   * @author SvenAugustus(蔡政滦) e-mail: SvenAugustus@outlook.com
   * @version 2017年8月1日
   */
  public static int compare(String s1, String s2) {
    boolean b1 = isEmpty(s1);
    boolean b2 = isEmpty(s2);
    if (b1 && b2)
      return 0;
    if (b1)
      return -1;
    if (b2)
      return 1;
    return s1.compareTo(s2);
  }


  /**
   * 转义正则特殊字符 （$()*+.[]?\^{},|）
   * 
   * @param keyword
   * @return
   */
  public static String escapeExprSpecialWord(String keyword) {
    if (!StringUtils.isEmpty(keyword)) {
      String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
      for (String key : fbsArr) {
        if (keyword.contains(key)) {
          keyword = keyword.replace(key, "\\" + key);
        }
      }
    }
    return keyword;
  }

  public static void main(String[] args) {

  }
}
