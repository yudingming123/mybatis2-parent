package com.jimei.mybatis2;

import java.io.Serializable;
import java.util.List;

/**
 * @Author yudm
 * @Date 2020/12/20 15:25
 * @Desc
 */
public class Metadata implements Serializable {
    private String sql;
    private List<Object> params;

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
