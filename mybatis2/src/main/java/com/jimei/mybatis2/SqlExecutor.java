package com.jimei.mybatis2;

import com.google.common.base.CaseFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
@Component
public class SqlExecutor {
    private final Log log = LogFactory.getLog(SqlExecutor.class);
    @Resource
    private com.youpin.user.common.base.SqlSessionTemplateManagement sstm;

    /**
     * @Author yudm
     * @Date 2020/9/25 15:48
     * @Param [statement, values]
     * @Desc 向sql的占位符中填充值
     */
    private static void fillPlaceholder(PreparedStatement pst, List<Object> values) throws SQLException {
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
    @Transactional
    public int executeUpdate(String clusterLabel, String sql, List<Object> values) {
        List<Connection> cns = sstm.getConnections(clusterLabel);
        PreparedStatement pst = null;
        try {
            int res = 0;
            //向每一个数据库都执行写操作
            for (Connection cn : cns) {
                pst = cn.prepareStatement(sql);
                //将属性值设置到sql中的占位符中
                fillPlaceholder(pst, values);
                res = pst.executeUpdate();
            }
            return res;
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
    public List<Map<String, Object>> executeQuery(String clusterLabel, String sql, List<Object> params) {
        Connection cn = sstm.getConnection(clusterLabel);
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = cn.prepareStatement(sql);
            //将属性值设置到sql中的占位符中
            if (null != params && params.size() > 0) {
                fillPlaceholder(pst, params);
            }
            rs = pst.executeQuery();
            return parsResultSet(rs);
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
    public List<Map<String, Object>> parsResultSet(ResultSet rs) {
        if (null == rs) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        try {

            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            //跳过表头
            while (rs.next()) {
                for (int i = 1; i <= count; ++i) {
                    //获取表中字段的名字并转为小写
                    String colName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, md.getColumnName(i));
                    map.put(colName, rs.getObject(i));
                }
                list.add(map);
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
    public void close(PreparedStatement st, ResultSet rs) {
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
