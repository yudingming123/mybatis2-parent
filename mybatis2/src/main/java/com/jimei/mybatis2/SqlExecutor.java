package com.jimei.mybatis2;

import com.google.common.base.CaseFormat;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yudm
 * @Date 2020/12/7 15:54
 * @Desc Sql语句执行者，可分数据源并负载均衡
 */
public class SqlExecutor {
    private static final Map<String, List<String>> labelMap = initLabelMap();
    private static final Map<String, SqlSessionTemplate> sstMap = initSstMap();

    /**
     *@Author yudm
     *@Date 2020/12/7 17:22
     *@Param []
     *@Desc 初始化labelMap，里面存放 标签->[SqlSessionTemplate在容器中的名字]
     */
    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> initLabelMap() {
        try {
            return (Map<String, List<String>>) SpringContextUtil.getBean("labelWithSqlSessionTemplateBeanNamesMap");
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     *@Author yudm
     *@Date 2020/12/7 17:24
     *@Param []
     *@Desc 从IOC容器中获取所有的SqlSessionTemplate对象，用于初始化sstMap
     */
    private static Map<String, SqlSessionTemplate> initSstMap() {
        return SpringContextUtil.getBeans(SqlSessionTemplate.class);
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param [sst]
     * @Desc 获取数据库连接, 从连接池中拿。
     */
    private static Connection getConnection(SqlSessionTemplate sst) {
        return SqlSessionUtils.getSqlSession(sst.getSqlSessionFactory(), sst.getExecutorType(), sst.getPersistenceExceptionTranslator()).getConnection();
    }

    /**
     * @Author yudm
     * @Date 2020/12/7 17:12
     * @Param [label 用于区分数据源]
     * @Desc 根据label获取对应的Connection，同一个label可能对应多个数据源。
     */
    public static List<Connection> getConnections(String label) {
        List<Connection> list = new ArrayList<>();
        if (null == labelMap || StringUtils.isEmpty(label)) {
            for (SqlSessionTemplate sst : sstMap.values()) {
                if (null != sst) {
                    list.add(getConnection(sst));
                }
            }
        } else {
            for (String beanName : labelMap.get(label)) {
                if (null == beanName) {
                    continue;
                }
                SqlSessionTemplate sst = sstMap.get(beanName);
                if (null != sst) {
                    list.add(getConnection(sst));
                }
            }
        }
        return list;
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:48
     * @Param [statement, values]
     * @Desc 向sql的占位符中填充值
     */
    public static void fillPlaceholder(PreparedStatement pst, List<Object> values) throws SQLException {
        if (null == values || values.size() < 1) {
            return;
        }
        for (int i = 0; i < values.size(); ++i) {
            pst.setObject(i + 1, values.get(i));
        }
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:34
     * @Param [sql, values]
     * @Desc 执行update操作
     */
    public static int executeUpdate(String sql, List<Object> values) {
        Connection cn = getConnection();
        PreparedStatement pst = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            fillPlaceholder(pst, values);
            return pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(pst, null);
        }
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:34
     * @Param [sql, value]
     * @Desc 执行update操作
     */
    public static int executeUpdate(String sql, Object value) {
        Connection cn = getConnection();
        PreparedStatement pst = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            pst.setObject(1, value);
            return pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(pst, null);
        }
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:34
     * @Param [clazz, sql, values]
     * @Desc 执行query操作
     */
    public static <T> List<T> executeQuery(Class<T> clazz, String sql, List<Object> values) {
        Connection cn = getConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            if (null != values && values.size() > 0) {
                fillPlaceholder(pst, values);
            }
            rs = pst.executeQuery();
            return parsResultSet(clazz, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(pst, rs);
        }
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:33
     * @Param [clazz, resultSet]
     * @Desc 将ResultSet转化成对应的实体类集合
     */
    public static <T> List<T> parsResultSet(Class<T> clazz, ResultSet rs) {
        if (null == rs) {
            return null;
        }
        List<T> list = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> fMap = new HashMap<>();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fMap.put(field.getName(), field);
            }
        }
        try {
            T t = clazz.newInstance();
            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            //跳过表头
            while (rs.next()) {
                for (int i = 1; i <= count; ++i) {
                    //获取表中字段的名字并转为小写
                    String colName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, md.getColumnName(i));
                    Field field = fMap.get(colName);
                    if (null != field) {
                        boolean flag = field.isAccessible();
                        field.setAccessible(true);
                        field.set(t, rs.getObject(i));
                        field.setAccessible(flag);
                    }
                }
                list.add(t);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:47
     * @Param [connection, statement]
     * @Desc 关闭资源, 当Connection是从连接池中来的时候，必须要关闭，传null。
     */
    public static void close(PreparedStatement st, ResultSet rs) {
        try {
            if (null != st) {
                st.close();
            }
            if (null != rs) {
                rs.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
