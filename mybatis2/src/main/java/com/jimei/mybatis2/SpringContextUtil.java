package com.jimei.mybatis2;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext appContext = null;

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext appContext)
            throws BeansException {
        SpringContextUtil.appContext = appContext;
    }

    public static ApplicationContext getAppContext() throws BeansException {
        return SpringContextUtil.appContext;
    }

    public static Object getBean(String name) throws BeansException {
        return appContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType)
            throws BeansException {
        return appContext.getBean(name, requiredType);
    }

    public static boolean containsBean(String name) {
        return appContext.containsBean(name);
    }

    public static boolean isSingleton(String name)
            throws NoSuchBeanDefinitionException {
        return appContext.isSingleton(name);
    }

    public static Class<?> getType(String name)
            throws NoSuchBeanDefinitionException {
        return appContext.getType(name);
    }

    public static String[] getAliases(String name)
            throws NoSuchBeanDefinitionException {
        return appContext.getAliases(name);
    }

    public static <T> T getBean(final Class<T> clazz) {
        Map<String, T> beans = appContext.getBeansOfType(clazz);
        if (beans.size() == 0) {
            return null;
        }
        if (beans.size() > 1) {
            throw new RuntimeException(clazz + "对应的bean不只一个");
        }
        return beans.values().iterator().next();
    }
    public static <T> Map<String,T> getBeans(final Class<T> clazz) {
        return appContext.getBeansOfType(clazz);
    }
}
