package com.doodream.stateful.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHandler {
    String name();

    Class<?> param() default Void.class;
}
