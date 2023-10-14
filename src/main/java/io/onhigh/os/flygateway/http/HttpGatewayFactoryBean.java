package io.onhigh.os.flygateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.*;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.onhigh.os.flygateway.http.codec.JacksonExtendedDecoder;
import io.onhigh.os.flygateway.http.contract.SpringMvcContract;
import lombok.Setter;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Marat Kadzhaev
 * @since 30 окт. 2021 г.
 */
@Setter
public class HttpGatewayFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware, EnvironmentAware {

    private Environment environment;
    private String url;
    private Class<?> type;
    private ErrorDecoder errorDecoder;
    private ObjectMapper objectMapper;
    private Collection<RequestInterceptor> requestInterceptors;

    private ApplicationContext applicationContext;

    /**
     * Client connect timeout in milliseconds. A value of 0 means no timeout.
     */
    private long okHttpClientConnectTimeout = -1L;

    /**
     * Client read timeout in milliseconds. A value of 0 means no timeout.
     */
    private long okHttpClientReadTimeout = -1L;

    /**
     * Client write timeout in milliseconds. A value of 0 means no timeout.
     */
    private long okHttpClientWriteTimeout = -1L;

    @Override
    public void afterPropertiesSet() {

    }

    @Override
    public Object getObject() {
        return getTarget();
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    private <T> T getTarget() {
        ObjectMapper objectMapper = Optional.ofNullable(this.objectMapper)
                .orElseGet(() -> applicationContext.getBean(ObjectMapper.class));

        Collection<RequestInterceptor> interceptors = Optional.ofNullable(this.requestInterceptors)
                .orElseGet(() -> applicationContext.getBeansOfType(RequestInterceptor.class).values());

        ErrorDecoder errorDecoder = Optional.ofNullable(this.errorDecoder).orElseGet(ErrorDecoder.Default::new);

        Request.Options requestOptions = new Request.Options(
                okHttpClientConnectTimeout, TimeUnit.MILLISECONDS,
                okHttpClientReadTimeout, TimeUnit.MILLISECONDS, true);

        return (T) Feign.builder()
                .requestInterceptors(interceptors)
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder(errorDecoder)
                .contract(new SpringMvcContract())
                .client(buildHttpClient())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonExtendedDecoder(new JacksonDecoder(objectMapper)))
                .logger(new Slf4jLogger(type))
                .logLevel(Logger.Level.NONE)
                .options(requestOptions)
                .target(type, url);
    }

    private OkHttpClient buildHttpClient() {

        HttpLoggingInterceptor.Level logLevel = environment.getProperty(
                "application.feign.okhttp.log.level", HttpLoggingInterceptor.Level.class,
                HttpLoggingInterceptor.Level.BASIC);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(logLevel);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(okHttpClientConnectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(okHttpClientReadTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(okHttpClientWriteTimeout, TimeUnit.MILLISECONDS)
                .build();

        return new OkHttpClient(client);
    }
}
