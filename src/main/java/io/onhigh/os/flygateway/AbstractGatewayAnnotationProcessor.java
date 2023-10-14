package io.onhigh.os.flygateway;

import org.springframework.core.env.Environment;

/**
 * @author Marat Kadzhaev
 * @since 23 февр. 2022
 */
public abstract class AbstractGatewayAnnotationProcessor implements GatewayAnnotationProcessor {

    protected final Environment environment;

    protected AbstractGatewayAnnotationProcessor(Environment environment) {
        this.environment = environment;
    }

    protected GatewayType getDefaultGatewayType() {
        return environment.getProperty("application.gateways.defaultType", GatewayType.class, GatewayType.HTTP);
    }

}
