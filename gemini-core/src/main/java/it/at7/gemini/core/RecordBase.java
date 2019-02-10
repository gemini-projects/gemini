package it.at7.gemini.core;

import it.at7.gemini.exceptions.FieldException;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import it.at7.gemini.schema.Field;
import org.springframework.lang.Nullable;

import java.util.*;

public interface RecordBase {

    Map<String, Object> getStore();

    default boolean set(String fieldName, Object value) {
        return put(fieldName, value);
    }

    boolean put(String fieldName, Object value);

    @SuppressWarnings("unchecked")
    @Nullable
    default <T> T get(String field) throws InvalidTypeForObject {
        Object o = getStore().get(field.toLowerCase());
        if (o == null) {
            return null;
        }
        try {
            return (T) o;
        } catch (ClassCastException e) {
            throw new InvalidTypeForObject();
        }
    }

    default <T> T getRequiredField(String field) throws InvalidTypeForObject, FieldException {
        T value = get(field);
        if (value == null) {
            throw FieldException.FIELD_NOT_FOUND(field);
        }
        return value;
    }

    @Nullable
    default <T> T get(Field field, Class<T> fieldType) {
        return get(field.getName(), fieldType);
    }

    @Nullable
    default <T> T get(String field, Class<T> fieldType) {
        try {
            Object value = get(field);
            return value == null ? null : fieldType.cast(value);
        } catch (InvalidTypeForObject invalidTypeForObject) {
            // mai qua
            return null;
        }
    }

    @Nullable
    default <T> T get(Field field) {
        return get(field.getName());
    }

    default <T> T getFieldOrDefault(String field, T def) {
        return getFieldOrDefault(field, def, false);
    }


    default <T> T getFieldOrSetDefault(String field, T def) {
        return getFieldOrDefault(field, def, true);
    }

    default <T> T getFieldOrDefault(String field, T def, boolean set) {
        T value = get(field);
        if (set && value == null) {
            put(field, def);
        }
        return value == null ? def : value;
    }
}
