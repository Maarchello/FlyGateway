package io.onhigh.os.flygateway.http.api;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Request(method = RequestMethod.POST)
public @interface PostRequest {

    String value() default "";

    String[] consumes() default {};

    String[] produces() default {};

    String[] headers() default {};

}
