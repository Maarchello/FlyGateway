package io.onhigh.os.flygateway.starter;

import org.springframework.context.annotation.Import;

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
@Import({ EnableFlyGatewaysRegistrar.class})
public @interface EnableFlyGateway {

    String[] basePackages() default {};

    Class<?>[] include() default {};

}
