package io.onhigh.os.flygateway.mq.api;

/**
 * @author Marat Kadzhaev
 * @since 18 февр. 2022
 */
public @interface Queue {

    String value() default "";
}
