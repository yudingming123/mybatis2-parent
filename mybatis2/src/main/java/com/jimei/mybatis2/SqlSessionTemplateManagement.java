package com.jimei.mybatis2;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author yudm
 * @Date 2020/12/8 22:50
 * @Desc 管理SqlSessionTemplate并对其做负载均衡
 */
public class SqlSessionTemplateManagement {
    private final String NO_LABEL = "noLabel";
    //每个集群的SqlSessionTemplate池，一个key代表一个集群
    private Map<String, List<SqlSessionTemplate>> sstListMap;
    //每个集群中SqlSessionTemplate的数量
    private Map<String, Integer> sstCountMap;
    //每个集群中SqlSessionTemplate正被使用的序号
    private Map<String, Integer> sstDutyMap;

    /**
     * @Author yudm
     * @Date 2020/12/19 13:14
     * @Param [cluster]
     * @Desc 针对写操作获取对应集群所有的数据库的连接
     */
    public List<Connection> getConnections(String clusterLabel) {
        if (null == clusterLabel || clusterLabel.isEmpty()) {
            clusterLabel = NO_LABEL;
        }
        List<SqlSessionTemplate> sstList = sstListMap.get(clusterLabel);
        if (null == sstList || sstList.size() <= 0) {
            throw new RuntimeException("找不到" + clusterLabel + "对应的SqlSessionTemplate，请检查数据源配置");
        }
        List<Connection> cns = new ArrayList<>();
        for (SqlSessionTemplate sst : sstList) {
            cns.add(getConnection(sst));
        }
        return cns;
    }

    /**
     * @Author yudm
     * @Date 2020/12/19 13:15
     * @Param [cluster]
     * @Desc 针对读操作，负载均衡对应集群中所有数据库的连接
     */
    public Connection getConnection(String clusterLabel) {
        if (null == clusterLabel || clusterLabel.isEmpty()) {
            clusterLabel = NO_LABEL;
        }
        List<SqlSessionTemplate> sstList = sstListMap.get(clusterLabel);
        if (null == sstList || sstList.size() <= 0) {
            throw new RuntimeException("找不到" + clusterLabel + "对应的SqlSessionTemplate，请检查数据源配置");
        }
        //通过轮询算法对SqlSessionTemplate进行负载均衡
        int index = sstDutyMap.get(clusterLabel);
        int count = sstCountMap.get(clusterLabel);
        index = ++index % count;
        sstDutyMap.put(clusterLabel, index);
        return getConnection(sstList.get(index));
    }

    /**
     * @Author yudm
     * @Date 2020/9/25 15:49
     * @Param []
     * @Desc 获取数据库连接, 通过SqlSessionTemplate从连接池中拿。
     */
    private Connection getConnection(SqlSessionTemplate sst) {
        return SqlSessionUtils.getSqlSession(sst.getSqlSessionFactory(), sst.getExecutorType(), sst.getPersistenceExceptionTranslator()).getConnection();
    }

    public Map<String, List<SqlSessionTemplate>> getSstListMap() {
        return sstListMap;
    }

    public void setSstListMap(Map<String, List<SqlSessionTemplate>> sstListMap) {
        this.sstListMap = sstListMap;
    }

    public Map<String, Integer> getSstCountMap() {
        return sstCountMap;
    }

    public void setSstCountMap(Map<String, Integer> sstCountMap) {
        this.sstCountMap = sstCountMap;
    }

    public Map<String, Integer> getSstDutyMap() {
        return sstDutyMap;
    }

    public void setSstDutyMap(Map<String, Integer> sstDutyMap) {
        this.sstDutyMap = sstDutyMap;
    }

}
