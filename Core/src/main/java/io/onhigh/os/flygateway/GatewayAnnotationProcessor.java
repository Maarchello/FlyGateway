package io.onhigh.os.flygateway;

import java.lang.annotation.Annotation;

/**
 * @author Marat Kadzhaev
 * @since 17 февр. 2022
 */
public interface GatewayAnnotationProcessor<A> {

    Object process(A gatewayAnnotation, Class<?> candidateComponent);

}
