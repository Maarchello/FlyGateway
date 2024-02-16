package io.onhigh.os.flygateway.mq.impl;

import io.onhigh.os.flygateway.mq.MQGatewayFactory;

import java.lang.reflect.Proxy;

public class DefaultMQGatewayFactory implements MQGatewayFactory {

    private final Class<?> type;

    public DefaultMQGatewayFactory(Class<?> type) {
        this.type = type;
    }


    @Override
    public Class<?> getGatewayType() {
        return type;
    }

    @Override
    public Object createGateway(CreateGatewayContext context) {

        return null;
    }

}
