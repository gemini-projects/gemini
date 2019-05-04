package it.at7.gemini.core.events;

import java.lang.annotation.*;

@Repeatable(BeforeInsertField.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeInsertField {
    String field();


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        BeforeInsertField[] value();
    }
}
