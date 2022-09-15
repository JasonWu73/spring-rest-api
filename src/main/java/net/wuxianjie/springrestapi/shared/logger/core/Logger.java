package net.wuxianjie.springrestapi.shared.logger.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

  String value() default "接口描述";

  String ignores() default "password,idCard";
}
