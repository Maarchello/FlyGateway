package io.onhigh.os.flygateway.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Marat Kadzhaev
 * @since 30 окт. 2021 г.
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpGateway {

    String url() default "";

    String urlProperty() default "";

    String serviceName() default "";

    /**
     * OkHttpClient connect timeout for new connections (in milliseconds).
     * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
     * @return Timeout in milliseconds
     */
    long okHttpClientConnectTimeout() default 10000L;

    /**
     * OkHttpClient read timeout for new connections (in milliseconds).
     * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
     * @return Timeout in milliseconds
     */
    long okHttpClientReadTimeout() default 60000L;

    /**
     * OkHttpClient write timeout for new connections (in milliseconds).
     * A value of 0 means no timeout, otherwise values must be between 1 and {@link Integer#MAX_VALUE}.
     * @return Timeout in milliseconds
     */
    long okHttpClientWriteTimeout() default 10000L;
}
