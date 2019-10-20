package it.at7.gemini.core;

import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.Objects;

public class EntityFieldValue extends FieldValue {
    public EntityFieldValue(EntityField field, Object value) {
        super(field, value);
    }

    public EntityField getEntityField() {
        return (EntityField) getField();
    }

    public static EntityFieldValue create(EntityField field, Object value) {
        return new EntityFieldValue(field, value);
    }

    public static EntityFieldValue create(Entity entity, FieldValue value) throws EntityFieldException {
        EntityField field = entity.getField(value.getField().getName().toLowerCase());
        return new EntityFieldValue(field, value.getValue());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public boolean fieldValueEquals(Object value) {
        EntityField entityField = getEntityField();
        Object convertedRecordValue = FieldConverters.getConvertedFieldValue(entityField, getValue());
        Object convertedPersistenceValue = FieldConverters.getConvertedFieldValue(entityField, value);

        if (convertedRecordValue instanceof EntityRecord && convertedPersistenceValue instanceof EntityRecord) {
            EntityRecord convertedPersistenceValueER = (EntityRecord) convertedPersistenceValue;
            return !((EntityRecord) convertedRecordValue).someRealUpdatedNeeded(convertedPersistenceValueER);
        }
        // converted values stores the right target type object.. equals should be consistent both for primitives
        // and for complex object... if the framework evolves in othter direction this code should be checked
        return Objects.equals(convertedRecordValue, convertedPersistenceValue);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
