package io.onhigh.os.flygateway.http.api;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Request(method = RequestMethod.GET)
public @interface GetRequest {

    /**
     * url
     * @return
     */
    String value() default "";

    String[] consumes() default {};

    String[] produces() default {};

    String[] headers() default {};

}
