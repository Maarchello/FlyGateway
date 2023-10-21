package io.onhigh.os.flygateway;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Marat Kadzhaev
 * @since 24 февр. 2022
 */
public interface GatewayRegistrar {

    Class<? extends Annotation> getAnnotation();

//    Set<String> getBasePackages();

}
