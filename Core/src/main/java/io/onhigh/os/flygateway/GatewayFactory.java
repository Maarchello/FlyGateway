package io.onhigh.os.flygateway;

public interface GatewayFactory<Env extends FlyEnvironment, Ctx extends GatewayFactory.CreateGatewayContext> {

    Class<?> getGatewayType();

    Object createGateway(Ctx context);

    interface CreateGatewayContext {

        FlyErrorDecoder<?> getErrorDecoder();

    }

}
