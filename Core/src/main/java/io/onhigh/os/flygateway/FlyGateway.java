package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.http.HttpGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import io.onhigh.os.flygateway.mq.MQGateway;
import io.onhigh.os.flygateway.mq.MQGatewayAnnotationProcessor;

public final class FlyGateway {

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
