package io.onhigh.os.flygateway.mq.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Marat Kadzhaev
 * @since 17 февр. 2022
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MQGateway {

    Queue queue();

}
