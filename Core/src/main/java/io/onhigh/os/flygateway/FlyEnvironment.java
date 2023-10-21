package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.http.HttpClientProvider;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface FlyEnvironment {

//    /**
//     * Method has to return at least default processors
//     * @return custom gateway annotation processors
//     */
//    Map<Class<? extends Annotation>, GatewayAnnotationProcessor> getGatewayAnnotationProcessors();

    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    Map<String, String> getPropertiesMap();

    HttpClientProvider getHttpClientProvider();
}
