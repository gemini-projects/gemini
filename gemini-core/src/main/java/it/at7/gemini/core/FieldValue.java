package it.at7.gemini.core;

import it.at7.gemini.schema.Field;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * A simple container to hold a Field and its Value
 */
public class FieldValue {
    private final Field field;
    private final Object value;

    FieldValue(Field field, @Nullable Object value) {
        this.field = field;
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        FieldValue that = (FieldValue) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldValue{");
        sb.append("field=").append(field);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

    public static FieldValue create(Field field, Object value) {
        return new FieldValue(field, value);
    }
}
