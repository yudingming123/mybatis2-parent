package com.jimei.mybatis2;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author yudm
 * @Date 2020/12/12 14:05
 * @Desc
 */
@Configuration
public class Mybatis2Config implements BeanDefinitionRegistryPostProcessor {
    @Resource
    private DataSourceProperties dsp;

    @Bean
    public SqlSessionTemplateManagement sqlSessionTemplateManagement() throws Exception {
        DruidDataSource dds = new DruidDataSource();
        SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
        ssfb.setDataSource(dds);
        SqlSessionFactory ssf = ssfb.getObject();

        DataSourceTransactionManager dstm = new DataSourceTransactionManager(dds);
        SqlSessionTemplate sst = new SqlSessionTemplate(ssf);
        SqlSessionTemplateManagement sstm = new SqlSessionTemplateManagement();

        return null;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //SqlSessionTemplate池
        Map<String, List<SqlSessionTemplate>> sstListMap = new HashMap<>();
        //SqlSessionTemplate数量
        Map<String, Integer> sstCountMap = new HashMap<>();
        //每个集群中SqlSessionTemplate正被使用的序号
        Map<String, Integer> sstDutyMap = new HashMap<>();
        //配置内容
        Map<String, List<Properties>> pListMap = dsp.getPropertiesList().stream().collect(Collectors.groupingBy(Properties::getClusterLabel));
        pListMap.forEach((k, v) -> {
            List<SqlSessionTemplate> sstList = new ArrayList<>();
            for (Properties p : v) {
                //数据源
                DruidDataSource dds = new DruidDataSource();
                dds.setUrl(p.getUrl());
                dds.setUsername(p.getUsername());
                dds.setPassword(p.getPassword());
                dds.setDriverClassName(p.getDriverClassName());
                //SqlSessionFactoryBean
                SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
                ssfb.setDataSource(dds);
                SqlSessionFactory ssf = null;
                try {
                    ssf = ssfb.getObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //SqlSessionTemplate
                SqlSessionTemplate sst = new SqlSessionTemplate(ssf);
                sstList.add(sst);
            }
            sstListMap.put(k, sstList);
            sstCountMap.put(k, sstList.size());
            sstDutyMap.put(k, 0);
        });
        //构造bean定义
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplateManagement.class);
        BeanDefinition bean = beanBuilder.getRawBeanDefinition();
        //添加属性
        bean.setAttribute("sstListMap", sstListMap);
        bean.setAttribute("sstCountMap", sstCountMap);
        bean.setAttribute("sstDutyMap", sstDutyMap);

        //注册bean
        registry.registerBeanDefinition("sqlSessionTemplateManagement", bean);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
        RootBeanDefinition bean = new RootBeanDefinition(beanClass);
        registry.registerBeanDefinition(name, bean);
    }
}
