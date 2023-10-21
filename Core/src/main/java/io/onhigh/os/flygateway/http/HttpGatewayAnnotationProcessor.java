package io.onhigh.os.flygateway.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onhigh.os.flygateway.AbstractGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.FlyEnvironment;
import io.onhigh.os.flygateway.GatewayConfigurerException;
import io.onhigh.os.flygateway.GatewayFactory;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import io.onhigh.os.flygateway.http.impl.factories.feign.FeignBasedHttpGatewayFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import static io.onhigh.os.flygateway.util.ReflectionUtils.getAnnotationAttributeValue;

/**
 * @author Marat Kadzhaev
 * @since 18 февр. 2022
 */
@Slf4j
public class HttpGatewayAnnotationProcessor extends AbstractGatewayAnnotationProcessor<HttpGateway> {

    public HttpGatewayAnnotationProcessor(FlyEnvironment environment) {
        super(environment);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object process(HttpGateway gatewayAnnotation, Class<?> candidateComponent) {

        Class<? extends HttpGatewayRegistrar> registrar = (Class<? extends HttpGatewayRegistrar>) getAnnotationAttributeValue("registrar", gatewayAnnotation.getClass())
                .orElse(null);

        GatewayFactory.Context.ContextBuilder gatewayFactoryContext = Optional.ofNullable(registrar)
                .map(r -> processRegistrar(r).asBuilder())
                .orElseGet(buildDefaultContext(gatewayAnnotation));

        processHttpClientSettings(gatewayAnnotation, gatewayFactoryContext);

        HttpGatewayFactory factory = new FeignBasedHttpGatewayFactoryImpl(candidateComponent, environment);
        return factory.getGateway(gatewayFactoryContext.build());
    }

    @NotNull
    private Supplier<GatewayFactory.Context.ContextBuilder> buildDefaultContext(HttpGateway gatewayAnnotation) {
        return () -> GatewayFactory.Context.builder()
                .url(getUrl(gatewayAnnotation))
                .properties(environment.getPropertiesMap())
//                .errorDecoder(null)
                .clientReadTimeout(gatewayAnnotation.clientReadTimeout())
                .connectTimeout(gatewayAnnotation.clientConnectTimeout())
                .clientWriteTimeout(gatewayAnnotation.clientWriteTimeout())
                .objectMapper(new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
                .requestInterceptors(new ArrayList<>());
    }

    private void processHttpClientSettings(HttpGateway gatewayAnnotation, GatewayFactory.Context.ContextBuilder gatewayFactoryContext) {
        if (gatewayFactoryContext == null || gatewayAnnotation == null) return;

        gatewayFactoryContext.connectTimeout(gatewayAnnotation.clientConnectTimeout());
        gatewayFactoryContext.clientReadTimeout(gatewayAnnotation.clientReadTimeout());
        gatewayFactoryContext.clientWriteTimeout(gatewayAnnotation.clientWriteTimeout());
    }

    private GatewayFactory.Context processRegistrar(Class<? extends HttpGatewayRegistrar> registrar) {
        HttpGatewayRegistrar httpGatewayRegistrar = createHttpGatewayRegistrar(registrar);

        if (httpGatewayRegistrar.isEnabled()) {
            return GatewayFactory.Context.EMPTY;
        }

        String url = httpGatewayRegistrar.getUrl();
        if (url == null) {
            throw new GatewayConfigurerException("You must provide url attribute or implement method getUrl in your HttpGatewayRegistrar");
        }

        return GatewayFactory.Context.builder()
                .url(url)
                .errorDecoder(httpGatewayRegistrar.getErrorDecoder())
                .objectMapper(httpGatewayRegistrar.getObjectMapper())
                .requestInterceptors(httpGatewayRegistrar.getRequestInterceptors())
                .build();
    }

    private HttpGatewayRegistrar createHttpGatewayRegistrar(Class<? extends HttpGatewayRegistrar> registrar) {
        Constructor<?> declaredConstructor = registrar.getDeclaredConstructors()[0];
        HttpGatewayRegistrar httpGatewayRegistrar;
        try {
            if (declaredConstructor.getParameterCount() > 0) {
                httpGatewayRegistrar = registrar.getDeclaredConstructor(FlyEnvironment.class).newInstance(environment);
            } else {
                httpGatewayRegistrar = registrar.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new GatewayConfigurerException(e.getMessage());
        }

        return httpGatewayRegistrar;
    }

    private String getUrl(HttpGateway gatewayAnnotation) {
        String url = gatewayAnnotation.url();

        if (url != null && !url.isBlank()) {
            return url;
        }

        String urlProperty = gatewayAnnotation.urlProperty();

        if (urlProperty != null && !urlProperty.isBlank()) {
            return getUrl(environment.getProperty(urlProperty, String.class, urlProperty));
        }

        throw new GatewayConfigurerException("At least one attribute required (urlProperty or url) ");
    }

    private String resolve(String value) {
        if (value != null && !value.isBlank()) {
            return environment.getProperty(value, String.class, value);
        }

        return value;
    }

    private String getUrl(String url) {
        if (!(url.startsWith("#{") && url.contains("}"))) {
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
