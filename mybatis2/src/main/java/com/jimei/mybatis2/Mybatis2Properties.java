package com.jimei.mybatis2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author yudm
 * @Date 2020/12/6 21:22
 * @Desc
 */
@Component
@ConfigurationProperties(prefix = "mybatis2")
public class Mybatis2Properties {
    private List<String> dbList;

}
