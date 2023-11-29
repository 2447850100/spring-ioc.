package org.springframework.stereotype;

import java.lang.annotation.*;

/**
 * @Version 1.0
 * @Author huqiang
 * @Description Service
 * @Date 2023/11/29 11:14
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {

    String value() default "";
}
