package io.onhigh.os.flygateway.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import io.onhigh.os.flygateway.GatewayRegistrar;

import java.util.List;

/**
 * @author Marat Kadzhaev
 * @since 29 дек. 2021 г.
 */
public interface HttpGatewayRegistrar extends GatewayRegistrar {

    String getUrl();

    List<RequestInterceptor> getRequestInterceptors();

    ErrorDecoder getErrorDecoder();

    default boolean isEnabled() {
        return false;
    }

    default ObjectMapper getObjectMapper() {
        return JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
}
