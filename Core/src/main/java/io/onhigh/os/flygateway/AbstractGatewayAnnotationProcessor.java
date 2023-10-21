package io.onhigh.os.flygateway;

/**
 * @author Marat Kadzhaev
 * @since 23 февр. 2022
 */
public abstract class AbstractGatewayAnnotationProcessor<A> implements GatewayAnnotationProcessor<A> {

    protected final FlyEnvironment environment;

    protected AbstractGatewayAnnotationProcessor(FlyEnvironment environment) {
        this.environment = environment;
    }

}
