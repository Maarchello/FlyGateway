package io.onhigh.os.flygateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import io.onhigh.os.flygateway.FlyErrorDecoder;
import io.onhigh.os.flygateway.GatewayFactory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public interface HttpGatewayFactory extends GatewayFactory<HttpEnvironment, HttpGatewayFactory.HttpCreateGatewayContext> {

    @Getter
    @Builder
    class HttpCreateGatewayContext implements CreateGatewayContext {

        public static final HttpCreateGatewayContext EMPTY = HttpCreateGatewayContext.builder().build();

        private String url;
        private FlyErrorDecoder<Exception> errorDecoder;
        private ObjectMapper objectMapper;
        private List<RequestInterceptor> requestInterceptors;
        private Map<String, String> properties;

        private Long connectTimeout;
        private Long clientReadTimeout;
        private Long clientWriteTimeout;

        public HttpCreateGatewayContextBuilder asBuilder() {
            return new HttpCreateGatewayContextBuilder()
                    .objectMapper(this.objectMapper)
                    .errorDecoder(this.errorDecoder)
                    .url(this.url)
                    .properties(this.properties)
                    .requestInterceptors(this.requestInterceptors)
                    .connectTimeout(this.connectTimeout)
                    .clientWriteTimeout(this.clientWriteTimeout)
                    .clientReadTimeout(this.clientReadTimeout);
        }

    }

}
