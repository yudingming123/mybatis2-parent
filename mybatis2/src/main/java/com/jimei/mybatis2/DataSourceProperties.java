package com.jimei.mybatis2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author yudm
 * @Date 2020/12/12 11:00
 * @Desc
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    private List<Properties> propertiesList;

    /*
    url: jdbc:mysql://localhost:3306/book?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
    password: ***
    username: ***
    password: com.mysql.jdbc.Driver
    platform: mysql
    # 下面为连接池的补充设置，应用到上面所有数据源中
    type: com.alibaba.druid.pool.DruidDataSource
    * */

    static class Properties {
        private String label;
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private String platform;
        private String type;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public List<Properties> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<Properties> propertiesList) {
        this.propertiesList = propertiesList;
    }
}
