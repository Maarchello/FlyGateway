package io.onhigh.os.flygateway.http;

import io.onhigh.os.flygateway.AbstractGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.GatewayConfigurerException;
import io.onhigh.os.flygateway.GatewayType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marat Kadzhaev
 * @since 18 февр. 2022
 */
@Slf4j
public class HttpGatewayAnnotationProcessor extends AbstractGatewayAnnotationProcessor {

    private static final String URL_PROPERTY_TEMPLATE = "${application.services.%s.url}";

    public HttpGatewayAnnotationProcessor(Environment environment) {
        super(environment);
    }

    @Override
    public void process(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(HttpGatewayFactoryBean.class);

        Class<? extends HttpGatewayRegistrar> registrar = (Class<? extends HttpGatewayRegistrar>) attributes.get("registrar");
        if (registrar != null) {
            processRegistrar(attributes, definition, registrar);
            processHttpClientSettings(annotationMetadata.getAnnotationAttributes(HttpGateway.class.getName()), definition);
        } else {
            definition.addPropertyValue("url", getUrl(attributes, annotationMetadata.getClassName()));
            processHttpClientSettings(attributes, definition);
        }

        String className = annotationMetadata.getClassName();
        definition.addPropertyValue("type", className);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        String alias = className + GatewayType.HTTP.name() + "Gateway";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

        beanDefinition.setPrimary(GatewayType.HTTP == getDefaultGatewayType());

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private void processHttpClientSettings(Map<String, Object> attributes, BeanDefinitionBuilder definition) {
        if (null == attributes) return;

        definition.addPropertyValue("okHttpClientConnectTimeout", attributes.get("okHttpClientConnectTimeout"));
        definition.addPropertyValue("okHttpClientReadTimeout", attributes.get("okHttpClientReadTimeout"));
        definition.addPropertyValue("okHttpClientWriteTimeout", attributes.get("okHttpClientWriteTimeout"));
    }

    private void processRegistrar(Map<String, Object> attributes, BeanDefinitionBuilder definition, Class<? extends HttpGatewayRegistrar> registrar) {
        HttpGatewayRegistrar httpGatewayRegistrar = createHttpGatewayRegistrar(registrar);

        if (httpGatewayRegistrar.isEnabled()) {
            String url = Optional.ofNullable(httpGatewayRegistrar.getUrl())
                    .orElseGet(() -> (String) attributes.get("url"));

            if (url == null) {
                throw new GatewayConfigurerException("You must provide url attribute or implement method getUrl in your HttpGatewayRegistrar");
            }

            definition.addPropertyValue("url", url);
            definition.addPropertyValue("errorDecoder", httpGatewayRegistrar.getErrorDecoder());
            definition.addPropertyValue("objectMapper", httpGatewayRegistrar.getObjectMapper());
            definition.addPropertyValue("requestInterceptors", httpGatewayRegistrar.getRequestInterceptors());
        }
    }

    private HttpGatewayRegistrar createHttpGatewayRegistrar(Class<? extends HttpGatewayRegistrar> registrar) {
        Constructor<?> declaredConstructor = registrar.getDeclaredConstructors()[0];
        HttpGatewayRegistrar httpGatewayRegistrar;
        try {
            if (declaredConstructor.getParameterCount() > 0) {
                httpGatewayRegistrar = registrar.getDeclaredConstructor(Environment.class).newInstance(environment);
            } else {
                httpGatewayRegistrar = registrar.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new GatewayConfigurerException(e.getMessage());
        }

        return httpGatewayRegistrar;
    }

    private String getUrl(Map<String, Object> attributes, String className) {
        String url = (String) attributes.get("url");
        if (!StringUtils.isEmpty(url)) {
            return getUrl(resolve(url));
        }

        String urlProperty = (String) attributes.get("urlProperty");
        if (!StringUtils.isEmpty(urlProperty)) {
            return getUrl(resolve("${" + urlProperty + "}"));
        }

        String serviceName = (String) attributes.get("serviceName");
        if (!StringUtils.isEmpty(serviceName)) {
            String property = String.format(URL_PROPERTY_TEMPLATE, serviceName);
            return getUrl(resolve(property));
        }

        throw new GatewayConfigurerException("At least one attribute required (urlProperty, url or serviceName) for " + className);
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    private String getUrl(String url) {
        if (StringUtils.hasText(url) && !(url.startsWith("#{") && url.contains("}"))) {
            if (!url.contains("://")) {
                url = "http://" + url;
            }
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(url + " is malformed", e);
            }
        }
        return url;
    }

}
