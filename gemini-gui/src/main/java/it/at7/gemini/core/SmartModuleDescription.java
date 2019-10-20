package it.at7.gemini.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SmartModuleDescription {
    String name();

    /**
     * Schema Prefix to be appended during the generation of Smart Schema Entities
     *
     * @return the prefix
     */
    String schemaPrefix();

    String[] dependencies() default {};

    /**
     * @return Order of evaluation
     */
    int order() default 0;

    /**
     * Dynamic modules can be modified without a Gemini restart. They can be used to define runtime and dynamic schemas
     * and entities
     */
    boolean dynamic() default false;
}
