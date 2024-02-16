package io.onhigh.os.flygateway.http.impl.factories.feign;

import feign.*;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import io.onhigh.os.flygateway.http.HttpEnvironment;
import io.onhigh.os.flygateway.http.HttpGatewayFactory;
import io.onhigh.os.flygateway.http.codec.JacksonExtendedDecoder;
import io.onhigh.os.flygateway.http.impl.factories.feign.contract.FlyGatewayApiContract;
import io.onhigh.os.flygateway.util.VarArgs;

import java.util.concurrent.TimeUnit;

public class FeignBasedHttpGatewayFactoryImpl implements HttpGatewayFactory {

    private final Class<?> type;
    private final HttpEnvironment flyEnvironment;

    public FeignBasedHttpGatewayFactoryImpl(Class<?> type, HttpEnvironment flyEnvironment) {
        this.type = type;
        this.flyEnvironment = flyEnvironment;
    }

    @Override
    public Class<?> getGatewayType() {
        return type;
    }

    @Override
    public Object createGateway(HttpCreateGatewayContext context) {
        return getObject(context);
    }

    private <T> T getObject(HttpCreateGatewayContext context) {

        Request.Options requestOptions = new Request.Options(
                context.getConnectTimeout(), TimeUnit.MILLISECONDS,
                context.getClientReadTimeout(), TimeUnit.MILLISECONDS, true);

        return (T) Feign.builder()
                .requestInterceptors(context.getRequestInterceptors())
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder((methodKey, response) -> context.getErrorDecoder().decode(new VarArgs(methodKey, response)))
                .contract(new FlyGatewayApiContract(flyEnvironment))
                .client(this.flyEnvironment.getHttpClientProvider().getClient(context))
                .encoder(new JacksonEncoder(context.getObjectMapper()))
                .decoder(new JacksonExtendedDecoder(new JacksonDecoder(context.getObjectMapper())))
                .logger(new Slf4jLogger(type))
                .logLevel(Logger.Level.NONE)
                .options(requestOptions)
                .target(type, context.getUrl());
    }

}
