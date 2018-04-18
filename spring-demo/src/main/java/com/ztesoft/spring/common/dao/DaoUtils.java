package com.ztesoft.spring.common.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.github.miemiedev.mybatis.paginator.domain.Paginator;
import com.ztesoft.spring.common.Const;

@SuppressWarnings({"unchecked","rawtypes"})
public class DaoUtils {

  private static Logger logger = LoggerFactory.getLogger(DaoUtils.class);

  public static SqlSessionTemplate getSqlTpl() {
    return getSqlTpl(null);
  }

  public static JdbcTemplate getJdbcTpl() {
    return getJdbcTpl(null);
  }

  public static SqlSessionTemplate getSqlTpl(String jndi_name) {
    if (Const.LOG_JNDI_NAME.equals(jndi_name)) {
      return SpringUtils.getBean("logSqlSessionTemplate");
    }
    return SpringUtils.getBean("sqlSessionTemplate");
  }

  public static JdbcTemplate getJdbcTpl(String jndi_name) {
    if (Const.LOG_JNDI_NAME.equals(jndi_name)) {
      return SpringUtils.getBean("logJdbcTemplate");
    }
    return SpringUtils.getBean("jdbcTemplate");
  }

  /**
   * 分页查询(查询总数)
   * 
   * @param statement
   * @param parameter
   * @param pageNumber
   * @param pageSize
   * @return
   */
  public static <T> PageModel<T> selectPageModel(String statement, Object parameter,
      int pageNumber, int pageSize) {
    return selectPageModel(null, statement, parameter, pageNumber, pageSize, true);
  }

  /**
   * 分页查询(指定数据源名称，查询总数)
   * 
   * @param jndi_name
   * @param statement
   * @param parameter
   * @param pageNumber
   * @param pageSize
   * @return
   */
  public static <T> PageModel<T> selectPageModel(String jndi_name, String statement,
      Object parameter, int pageNumber, int pageSize) {
    return selectPageModel(jndi_name, statement, parameter, pageNumber, pageSize, true);
  }

  /**
   * 分页查询(不查询总数)
   * 
   * @param statement
   * @param parameter
   * @param pageNumber
   * @param pageSize
   * @return
   */
  public static <T> PageModel<T> selectPageModelNoCount(String statement, Object parameter,
      int pageNumber, int pageSize) {
    return selectPageModel(null, statement, parameter, pageNumber, pageSize, false);
  }

  /**
   * 分页查询(指定数据源名称，不查询总数)
   * 
   * @param jndi_name
   * @param statement
   * @param parameter
   * @param pageNumber
   * @param pageSize
   * @return
   */
  public static <T> PageModel<T> selectPageModelNoCount(String jndi_name, String statement,
      Object parameter, int pageNumber, int pageSize) {
    return selectPageModel(jndi_name, statement, parameter, pageNumber, pageSize, false);
  }

  /**
   * 分页查询
   * 
   * @param jndi_name
   * @param statement
   * @param parameter
   * @param pageNumber
   * @param pageSize
   * @param containsTotalCount
   * @return
   */
  public static <T> PageModel<T> selectPageModel(String jndi_name, String statement,
      Object parameter, int pageNumber, int pageSize, boolean containsTotalCount) {
    PageBounds pageBounds = new PageBounds();
    pageBounds.setPage(pageNumber);
    pageBounds.setLimit(pageSize);
    pageBounds.setContainsTotalCount(containsTotalCount);

    PageModel<T> pageModel = new PageModel<T>();
    List<T> list = getSqlTpl(jndi_name).selectList(statement, parameter, pageBounds);
    if (list instanceof PageList) {
      PageList<T> pageList = (PageList<T>) list;
      for (T element : pageList) {
        pageModel.addRow(element);
      }

      Paginator paginator = pageList.getPaginator();
      if (paginator != null) {
        pageModel.setPageNumber(paginator.getPage());
        pageModel.setPageSize(paginator.getLimit());
        pageModel.setPageCount(paginator.getTotalPages());
        pageModel.setTotal(paginator.getTotalCount());
      }
    } else {
      pageModel.setRows(list);
    }

    return pageModel;
  }

  public static class LowerCaseVORowMapper<T> implements RowMapper<T> {

    protected Class classType;

    public LowerCaseVORowMapper(Class classType) {
      super();
      this.classType = classType;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
      T t = null;
      try {
        t = (T) classType.newInstance();
      } catch (InstantiationException e1) {
        logger.error("can not create instance:" + classType, e1);
      } catch (IllegalAccessException e2) {
        logger.error("can not create instance:" + classType, e2);
      }
      if (t != null) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<Object>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
          String key = JdbcUtils.lookupColumnName(rsmd, i).toLowerCase();
          Object obj = JdbcUtils.getResultSetValue(rs, i);
          mapOfColValues.put(key, obj);
        }
        try {
          BeanUtils.populate(t, mapOfColValues);
        } catch (IllegalAccessException e) {
          // TODO Auto-generated catch block
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
        }
      }
      return t;
    }
  }
}
