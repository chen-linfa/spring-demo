package com.ztesoft.spring.service.impl;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ztesoft.spring.common.dao.DaoUtils;
import com.ztesoft.spring.service.ILoginService;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LoginService implements ILoginService {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  @Transactional
  public List queryMenusList(Map params) {
    List list = DaoUtils.getSqlTpl().selectList("Login.queryMenus");
    Map map = DaoUtils.getSqlTpl().selectOne("Login.queryMap");
    log.info(list.toString());
    log.debug(list.toString());

    return list;
  }

}
