package me.project.rpc.annotation;

import java.lang.annotation.*;

/**
 * 负载均衡注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoadBalanceStrategy {
    String value() default "";
}
