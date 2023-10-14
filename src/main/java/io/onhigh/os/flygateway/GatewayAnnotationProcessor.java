package io.onhigh.os.flygateway;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Marat Kadzhaev
 * @since 17 февр. 2022
 */
public interface GatewayAnnotationProcessor {

    void process(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes);

}
