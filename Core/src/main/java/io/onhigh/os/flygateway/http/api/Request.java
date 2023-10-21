package io.onhigh.os.flygateway.http.api;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Request {

    String value() default "";

    RequestMethod[] method() default {};

    String[] consumes() default {};

    String[] produces() default {};

    String[] headers() default {};


}
