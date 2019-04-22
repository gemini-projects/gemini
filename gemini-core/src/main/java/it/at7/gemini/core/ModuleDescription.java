package it.at7.gemini.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDescription {
    String name();

    String[] dependencies() default {};

    boolean editable() default false;

    int order() default 0;
}
