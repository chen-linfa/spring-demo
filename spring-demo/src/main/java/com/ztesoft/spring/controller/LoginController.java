package com.ztesoft.spring.controller;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ReqMethod;
import org.springframework.web.bind.annotation.ReqProxy;
import org.springframework.web.bind.annotation.RestController;
import com.ztesoft.spring.service.ILoginService;

@RestController
@ReqProxy
@SuppressWarnings({"unchecked", "rawtypes"})
public class LoginController {

  private Logger logger = LoggerFactory.getLogger(LoginController.class);
  
  @Autowired
  private ILoginService loginService;

  @ReqMethod
  public List queryMenus(Map<String, String> params) throws Exception {
    List list = null;
    try {
      list = loginService.queryMenusList(params);
    } catch (Exception e) {
      logger.error("系统内部异常", e);
    }
    return list;
  }
}
