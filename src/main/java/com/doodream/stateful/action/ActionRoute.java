package com.doodream.stateful.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionRoute {
    Class<? extends ActionPublisher>[] from() default {};
    Class<? extends RouterComponent>[] to();
}
