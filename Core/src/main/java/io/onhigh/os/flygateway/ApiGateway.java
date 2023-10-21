package io.onhigh.os.flygateway;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiGateway {

    String type();

    Class<? extends GatewayRegistrar> registrar();

}
