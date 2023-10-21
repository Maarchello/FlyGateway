package io.onhigh.os.flygateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public interface GatewayFactory {

    Class<?> getGatewayType();

    Object getGateway(Context context);

    @Getter
    @Builder
    class Context {

        public static final Context EMPTY = Context.builder().build();

        private String url;
        private ErrorDecoder errorDecoder;
        private ObjectMapper objectMapper;
        private List<RequestInterceptor> requestInterceptors;
        private Map<String, String> properties;
        /**
         * OkHttpClient connect timeout for new connections (in milliseconds).
         * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
         * @return Timeout in milliseconds
         */
        private Long connectTimeout = 10000L;

        /**
         * OkHttpClient read timeout for new connections (in milliseconds).
         * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
         * @return Timeout in milliseconds
         */
        private Long clientReadTimeout = 60000L;

        /**
         * OkHttpClient write timeout for new connections (in milliseconds).
         * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
         * @return Timeout in milliseconds
         */
        private Long clientWriteTimeout = 10000L;

        public ContextBuilder asBuilder() {
            return new ContextBuilder()
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
