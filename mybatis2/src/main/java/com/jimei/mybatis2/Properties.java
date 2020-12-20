package com.jimei.mybatis2;

/**
 * @Author yudm
 * @Date 2020/12/12 14:38
 * @Desc
 */
public class Properties {
    private String clusterLabel = "noLabel";
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public String getClusterLabel() {
        return clusterLabel;
    }

    public void setClusterLabel(String clusterLabel) {
        this.clusterLabel = clusterLabel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

}
