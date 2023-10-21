package io.onhigh.os.flygateway.http.impl;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import io.onhigh.os.flygateway.http.api.HttpGateway;
import io.onhigh.os.flygateway.http.HttpGatewayRegistrar;

import java.lang.annotation.Annotation;
import java.util.List;

public class DefaultHttpGatewayRegistrar implements HttpGatewayRegistrar {
    @Override
    public Class<? extends Annotation> getAnnotation() {
        return HttpGateway.class;
    }

//    @Override
//    public Set<String> getBasePackages() {
//        return null;
//    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public List<RequestInterceptor> getRequestInterceptors() {
        return null;
    }

    @Override
    public ErrorDecoder getErrorDecoder() {
        return null;
    }
}
