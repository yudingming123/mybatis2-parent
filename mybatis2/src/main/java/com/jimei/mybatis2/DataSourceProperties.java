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
    private List<com.youpin.user.common.base.Properties> propertiesList;

    public List<com.youpin.user.common.base.Properties> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<com.youpin.user.common.base.Properties> propertiesList) {
        this.propertiesList = propertiesList;
    }
}
