package io.onhigh.os.flygateway.starter;


import io.onhigh.os.flygateway.GatewayConfigurerException;
import io.onhigh.os.flygateway.GatewayType;
import io.onhigh.os.flygateway.InjectGateway;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Marat Kadzhaev
 * @since 21 февр. 2022
 */
@Component
public class InjectGatewayBeanPostProcessor implements BeanPostProcessor {

    private static final String UNSUPPORTED_GATEWAY_TYPE_MSG = "Unsupported GatewayType '%s'";
    private static final String IMPL_NO_FOUND_MSG = "'%s' implementation not found for %s";

    private final ApplicationContext applicationContext;

    public InjectGatewayBeanPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        List<Field> injectGatewayFields = FieldUtils.getFieldsListWithAnnotation(bean.getClass(), InjectGateway.class);
        for (Field field : injectGatewayFields) {
            try {
                processAnnotatedField(field, bean);
            } catch (IllegalAccessException e) {
                throw new GatewayConfigurerException(e.getMessage());
            }
        }

        return bean;
    }

    private void processAnnotatedField(Field field, Object bean) throws IllegalAccessException {
        InjectGateway injectGateway = field.getAnnotation(InjectGateway.class);
        if (injectGateway == null) {
            return;
        }

        boolean accessible = field.canAccess(bean);
        field.setAccessible(true);

        GatewayType gatewayType = injectGateway.type();
        switch (gatewayType) {
            case HTTP:
                injectHttpGateway(field, bean);
                break;
            case MQ:
            default:
                throw new GatewayConfigurerException(String.format(UNSUPPORTED_GATEWAY_TYPE_MSG, gatewayType));
        }

        field.setAccessible(accessible);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private void injectHttpGateway(Field field, Object bean) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Object httpImpl = applicationContext.getBeansOfType(fieldType)
                .values().stream()
                .filter(candidate -> AnnotationUtils.findAnnotation(candidate.getClass(), HttpGateway.class) != null)
                .findFirst()
                .orElseThrow(() -> new GatewayConfigurerException(String.format(IMPL_NO_FOUND_MSG, GatewayType.HTTP, fieldType)));
        field.set(bean, httpImpl);
    }
}
