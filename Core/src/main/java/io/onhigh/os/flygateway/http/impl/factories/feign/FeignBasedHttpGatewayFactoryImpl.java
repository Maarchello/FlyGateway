package io.onhigh.os.flygateway.http.impl.factories.feign;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import io.onhigh.os.flygateway.FlyEnvironment;
import io.onhigh.os.flygateway.http.HttpGatewayFactory;
import io.onhigh.os.flygateway.http.codec.JacksonExtendedDecoder;

import java.util.concurrent.TimeUnit;

public class FeignBasedHttpGatewayFactoryImpl implements HttpGatewayFactory {

    private final Class<?> type;
    private final FlyEnvironment flyEnvironment;

    private long connectTimeout = -1L;
    private long socketTimeout = -1L;

    public FeignBasedHttpGatewayFactoryImpl(Class<?> type, FlyEnvironment flyEnvironment) {
        this.type = type;
        this.flyEnvironment = flyEnvironment;
    }

    @Override
    public Class<?> getGatewayType() {
        return type;
    }

    @Override
    public Object getGateway(Context context) {
        return getObject(context);
    }

    private <T> T getObject(Context context) {

        Request.Options requestOptions = new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                socketTimeout, TimeUnit.MILLISECONDS, true);

        return (T) Feign.builder()
                .requestInterceptors(context.getRequestInterceptors())
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder(context.getErrorDecoder())
//                .contract(new FlyGatewayApiContract(flyEnvironment))
                .client(this.flyEnvironment.getHttpClientProvider().getClient(context))
                .encoder(new JacksonEncoder(context.getObjectMapper()))
                .decoder(new JacksonExtendedDecoder(new JacksonDecoder(context.getObjectMapper())))
                .logger(new Slf4jLogger(type))
                .logLevel(Logger.Level.NONE)
                .options(requestOptions)
                .target(type, context.getUrl());
    }

}
