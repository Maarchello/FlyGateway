package io.onhigh.os.flygateway.mq;

import io.onhigh.os.flygateway.GatewayAnnotationProcessor;

/**
 * @author Marat Kadzhaev
 * @since 18 февр. 2022
 */
public class MQGatewayAnnotationProcessor implements GatewayAnnotationProcessor<MQGateway> {

    @Override
    public Object process(MQGateway gatewayAnnotation, Class<?> candidateComponent) {
        throw new UnsupportedOperationException();
    }
}
