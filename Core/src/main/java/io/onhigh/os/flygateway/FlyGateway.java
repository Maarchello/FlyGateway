package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.http.HttpEnvironment;
import io.onhigh.os.flygateway.http.HttpGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import io.onhigh.os.flygateway.mq.MQEnvironment;
import io.onhigh.os.flygateway.mq.api.MQGateway;
import io.onhigh.os.flygateway.mq.MQGatewayAnnotationProcessor;

public final class FlyGateway {

    public static <T> T get(Class<T> gatewayClass, MQEnvironment environment) {
        HttpGateway httpGateway = gatewayClass.getAnnotation(HttpGateway.class);
        MQGateway mqGateway = gatewayClass.getAnnotation(MQGateway.class);

        validate(httpGateway, mqGateway);

        return (T) new MQGatewayAnnotationProcessor()
                .process(mqGateway, gatewayClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> gatewayClass, HttpEnvironment environment) {
        HttpGateway httpGateway = gatewayClass.getAnnotation(HttpGateway.class);
        MQGateway mqGateway = gatewayClass.getAnnotation(MQGateway.class);

        validate(httpGateway, mqGateway);

        return (T) new HttpGatewayAnnotationProcessor(environment)
                .process(httpGateway, gatewayClass);
    }

    private static void validate(HttpGateway httpGateway, MQGateway mqGateway) {
        if (httpGateway == null && mqGateway == null) {
            throw new GatewayConfigurerException("The @HttpGateway or @MQGateway must be present");
        }

        if (httpGateway != null && mqGateway != null) {
            throw new GatewayConfigurerException("The @HttpGateway and @MQGateway annotations cannot be used together.");
        }
    }

}
