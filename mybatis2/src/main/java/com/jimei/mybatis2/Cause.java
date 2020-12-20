package com.jimei.mybatis2;

import java.util.List;

/**
 * @Author yudm
 * @Date 2020/12/19 14:40
 * @Desc
 */
public class Cause {
    //许可证
    String license;
    //集群对应的标签
    String label;
    //SQL语句
    String sql;
    //参数
    List<Object> params;

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
