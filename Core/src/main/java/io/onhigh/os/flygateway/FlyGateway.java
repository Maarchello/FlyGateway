package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.http.HttpClientProvider;
import io.onhigh.os.flygateway.http.HttpGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.http.OkHttpClientProvider;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import io.onhigh.os.flygateway.mq.MQGateway;
import io.onhigh.os.flygateway.mq.MQGatewayAnnotationProcessor;

import java.util.Map;

public final class FlyGateway {

    public static void main(String[] args) {
        TestGateway testGateway = get(TestGateway.class, new FlyEnvironment() {
            @Override
            public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
                return null;
            }

            @Override
            public Map<String, String> getPropertiesMap() {
                return null;
            }

            @Override
            public HttpClientProvider getHttpClientProvider() {
                return new OkHttpClientProvider();
            }
        });

        System.out.println(testGateway);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> gatewayClass, FlyEnvironment environment) {
        HttpGateway httpGateway = gatewayClass.getAnnotation(HttpGateway.class);
        MQGateway mqGateway = gatewayClass.getAnnotation(MQGateway.class);

        if (httpGateway == null && mqGateway == null) {
            throw new GatewayConfigurerException("The @HttpGateway or @MQGateway must be present");
        }

        if (httpGateway != null && mqGateway != null) {
            throw new GatewayConfigurerException("The @HttpGateway and @MQGateway annotations cannot be used together.");
        }

        boolean isHttpGateway = httpGateway != null;
        if (isHttpGateway) {
            return (T) new HttpGatewayAnnotationProcessor(environment)
                    .process(httpGateway, gatewayClass);
        } else {
            return (T) new MQGatewayAnnotationProcessor()
                    .process(mqGateway, gatewayClass);
        }
    }

}
