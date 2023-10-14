package io.onhigh.os.flygateway.mq;

import io.onhigh.os.flygateway.GatewayAnnotationProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Marat Kadzhaev
 * @since 18 февр. 2022
 */
public class MQGatewayAnnotationProcessor implements GatewayAnnotationProcessor {
    @Override
    public void process(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        throw new UnsupportedOperationException();
    }
}
