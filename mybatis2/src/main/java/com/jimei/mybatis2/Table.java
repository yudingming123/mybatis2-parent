package com.jimei.mybatis2;

import com.google.common.base.CaseFormat;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yudm
 * @Date 2020/9/19 17:32
 * @Desc 用于和数据库表进行映射的类，提供一些简单通用的CURD操作，同时也可以执行xml中的自定义SQL语句。
 */
@Component
public class Table {
    private static SqlSessionTemplate st;

    @Autowired
    private void setSqlSessionTemplate(SqlSessionTemplate st) {
        Table.st = st;
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:24
     * @Param [entity 实体类对象，用于关联某张表同时也是入参]
     * @Desc 通用添加，null值也会写入。
     */
    public static <T> int insert(T entity) {
        if (null == entity) {
            return 0;
        }
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = parsColumns(fields);
        List<Object> values = parsValues(fields, entity);
        if (columns.size() < 1 || values.size() < 1) {
            return 0;
        }
        String sql = buildInsertSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), columns);
        return executeUpdate(sql, values);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:25
     * @Param [entity 实体类对象，用于关联某张表同时也是入参]
     * @Desc 通用添加，null值不写入。
     */
    public static <T> int insertSelective(T entity) {
        if (null == entity) {
            return 0;
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        parsToCVSelective(entity, columns, values);
        if (columns.size() < 1 || values.size() < 1) {
            return 0;
        }
        String sql = buildInsertSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getClass().getSimpleName()), columns);
        return executeUpdate(sql, values);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:27
     * @Param [statementId xml中某条SQL语句的id, entity 实体类对象，用于关联某个xml同时也是入参]
     * @Desc 自定义添加，对应xml中某条SQL语句
     */
    public static int insert(String statement, Object obj) {
        if (null == statement || null == obj) {
            return 0;
        }
        return st.insert(statement, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:27
     * @Param [clazz 用于关联某个xml, statement 用于关联某条SQL语句]
     * @Desc 自定义添加没有入参，对应xml中某条SQL语句。
     */
    public static int insert(String statement) {
        if (null == statement) {
            return 0;
        }
        return st.insert(statement);
    }


    /**
     * @Author yudm
     * @Date 2020/9/20 15:36
     * @Param [entity 实体类对象，用于关联某张表同时也是入参]
     * @Desc 通用更新，默认实体类第一个字段为主键，null值也会写入。
     */
    public static <T> int updateByKey(T entity) {
        if (null == entity) {
            return 0;
        }
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = parsColumns(fields);
        List<Object> values = parsValues(fields, entity);
        if (columns.size() < 1 || values.size() < 1) {
            return 0;
        }
        String sql = buildUpdateSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), columns);
        return executeUpdate(sql, values);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:40
     * @Param [entity 实体类对象，用于关联某张表同时也是入参]
     * @Desc 通用更新，默认实体类第一个字段为主键，null值不会写入。
     */
    public static <T> int updateByKeySelective(T entity) {
        if (null == entity) {
            return 0;
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        parsToCVSelective(entity, columns, values);
        if (columns.size() < 1 || values.size() < 1) {
            return 0;
        }
        String sql = buildUpdateSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getClass().getSimpleName()), columns);
        return executeUpdate(sql, values);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:27
     * @Param [statement 对应xml中某条SQL语句, obj 入参]
     * @Desc 自定义更新，对应xml中某条SQL语句
     */
    public static int update(String statement, Object obj) {
        if (null == statement || null == obj) {
            return 0;
        }
        return st.update(statement, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 15:27
     * @Param [clazz 用于关联某个xml, statement 用于关联某条SQL语句]
     * @Desc 自定义更新没有入参，对应xml中某条SQL语句。
     */
    public static int update(String statement) {
        if (null == statement) {
            return 0;
        }
        return st.update(statement);
    }


    /**
     * @Author yudm
     * @Date 2020/9/20 16:25
     * @Param [clazz 用于关联某个表, key 主键的值]
     * @Desc 通用删除，默认clazz第一个字段为主键。
     */
    public static <T> int deleteByKey(Class<T> clazz, Object obj) {
        if (null == clazz || null == obj) {
            return 0;
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = parsColumns(fields);
        if (columns.size() < 1) {
            return 0;
        }
        String sql = buildDeleteSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), columns.get(0));
        return executeUpdate(sql, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 16:44
     * @Param [clazz 用于关联某个xml, statement 用于关联某条SQL语句, obj 删除条件]
     * @Desc 自定义删除，有删除条件，对应xml中某条SQL语句。
     */
    public static int delete(String statement, Object obj) {
        if (null == statement || null == obj) {
            return 0;
        }
        return st.delete(statement, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 16:41
     * @Param [clazz 用于关联某个xml, statement 用于关联某条SQL语句]
     * @Desc 自定义删除，没有删除条件，对应xml中某条SQL语句。
     */
    public static int delete(String statement) {
        if (null == statement) {
            return 0;
        }
        return st.delete(statement);
    }


    /**
     * @Author yudm
     * @Date 2020/9/20 17:08
     * @Param [entity 实体类对象，用于关联某张表同时也是查询条件]
     * @Desc 通用查询单个，有查询条件且都用and连接。
     */
    public static <T> T selectOne(T entity) {
        if (null == entity) {
            return null;
        }
        Class<?> clazz = entity.getClass();
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        parsToCVSelective(entity, columns, values);
        if (columns.size() < 1 || values.size() < 1) {
            return null;
        }
        String sql = buildSelectSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), columns);
        List<T> list = executeQuery((Class<T>) clazz, sql, values);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:19
     * @Param [statement 用于关联某条SQL语句, condition 查询条件]
     * @Desc 自定义查询单个，对应xml中一条SQL语句，有查询条件。
     */
    public static <T> T selectOne(String statement, Object obj) {
        if (null == statement || obj == null) {
            return null;
        }
        return st.selectOne(statement, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:17
     * @Param [statement 用于关联某条SQL语句]
     * @Desc 自定义查询单个，对应xml中一条SQL语句，没有查询条件。
     */
    public static <T> T selectOne(String statement) {
        if (null == statement) {
            return null;
        }
        return st.selectOne(statement);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:07
     * @Param [clazz 用于关联某张表]
     * @Desc 通用查询所有
     */
    public static <T> List<T> selectAll(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        return executeQuery(clazz, "SELECT * FROM " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), null);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:22
     * @Param [entity 实体类对象，用于关联某个xml同时也是查询条件]
     * @Desc 通用查询多个，有查询条件。
     */
    public static <T> List<T> selectList(T entity) {
        if (null == entity) {
            return null;
        }
        Class<T> clazz = (Class<T>) entity.getClass();
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        parsToCVSelective(entity, columns, values);
        if (columns.size() < 1 || values.size() < 1) {
            return null;
        }
        String sql = buildSelectSql(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()), columns);
        return executeQuery(clazz, sql, values);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:26
     * @Param [statement 用于关联某条SQL语句, obj 查询条件]
     * @Desc 自定义查询多个，对应xml中一条SQL语句，有查询条件。
     */
    public static <T> List<T> selectList(String statement, Object obj) {
        if (null == statement || null == obj) {
            return null;
        }
        return st.selectList(statement, obj);
    }

    /**
     * @Author yudm
     * @Date 2020/9/20 17:24
     * @Param [statement 用于关联某条SQL语句]
     * @Desc 自定义查询多个，对应xml中一条SQL语句，没有查询条件。
     */
    public static <T> List<T> selectList(String statement) {
        if (null == statement) {
            return null;
        }
        return st.selectList(statement);
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:36
     * @Param [fields]
     * @Desc 解析出属性名
     */
    private static List<String> parsColumns(Field[] fields) {
        if (fields == null || fields.length < 1) {
            return null;
        }
        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            columns.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
        }
        return columns;
    }

    /**
     * @Author yudm
     * @Date 2020/10/4 12:36
     * @Param [fields, entity]
     * @Desc 解析出对象中属性值
     */
    private static <T> List<Object> parsValues(Field[] fields, T entity) {
        if (fields == null || fields.length < 1) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object obj;
            try {
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                obj = field.get(entity);
                field.setAccessible(flag);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            values.add(obj);
        }
        return values;
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:50
     * @Param [entity, columns, values]
     * @Desc 将实体类中的所有非静态非null的字段名和值解析到columns和values
     */
    private static <T> void parsToCVSelective(T entity, List<String> columns, List<Object> values) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object obj;
            try {
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                obj = field.get(entity);
                field.setAccessible(flag);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (null != obj) {
                columns.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
                values.add(obj);
            }
        }
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param [tableName, columns]
     * @Desc 构建插入sql
     */
    private static String buildInsertSql(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (String column : columns) {
            sql.append(column).append(",");
        }
        //去掉最后一个“,”号
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); ++i) {
            sql.append("?,");
        }
        //去掉最后一个“,”号
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param [tableName, columns]
     * @Desc 构建更新sql
     */
    private static String buildUpdateSql(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        for (String column : columns) {
            sql.append(column).append("=?,");
        }
        //去掉最后一个“,”号
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" where ").append(columns.get(0)).append("=?");
        return sql.toString();
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param [tableName, columns]
     * @Desc 构建删除sql
     */
    private static String buildDeleteSql(String tableName, String keyName) {
        return "DELETE FROM " + tableName + " WHERE " + keyName + "=?";
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param [tableName, columns]
     * @Desc 构建查询sql
     */
    private static String buildSelectSql(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        if (null == columns || columns.size() < 1) {
            return sql.toString();
        }
        sql.append(" WHERE ");
        for (String column : columns) {
            sql.append(column).append("=? AND ");
        }
        //去掉最后一个空格和AND号
        sql.delete(sql.length() - 5, sql.length() - 1);
        return sql.toString();
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param []
     * @Desc 获取数据库连接, 从连接池中拿。
     */
    private static Connection getConnection() {
        return SqlSessionUtils.getSqlSession(st.getSqlSessionFactory(), st.getExecutorType(), st.getPersistenceExceptionTranslator()).getConnection();
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:48
     * @Param [statement, values]
     * @Desc 向sql的占位符中填充值
     */
    private static void setValueToSql(PreparedStatement pst, List<Object> values) throws SQLException {
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
    private static int executeUpdate(String sql, List<Object> values) {
        Connection cn = getConnection();
        PreparedStatement pst = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            setValueToSql(pst, values);
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
    private static int executeUpdate(String sql, Object value) {
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
    private static <T> List<T> executeQuery(Class<T> clazz, String sql, List<Object> values) {
        Connection cn = getConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            if (null != values && values.size() > 0) {
                setValueToSql(pst, values);
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
    private static <T> List<T> parsResultSet(Class<T> clazz, ResultSet rs) {
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
    private static void close(PreparedStatement st, ResultSet rs) {
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
