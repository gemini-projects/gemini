package it.at7.gemini.schema;

import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Services;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Objects;

public class Field {
    public static final String ID_NAME = "id";
    // public static final Field ID = new Field(FieldType.PK, "id", true);

    private final FieldType fieldType;
    private final String fieldName;
    private final String entityRefName;
    //private final String entityCollectionRefField;

    public Field(FieldType fieldType, String fieldName) {
        this(fieldType, fieldName, null);
    }

    /* public Field(FieldType fieldType, String fieldName, String entityRefName) {
        this(fieldType, fieldName, entityRefName, null);
    } */

    public Field(FieldType fieldType, String fieldName, String entityRefName /*  String entityCollectionRefField */) {
        Assert.notNull(fieldType, "FielType required for Field");
        Assert.notNull(fieldName, "Name required for Field");
        if (fieldType == FieldType.ENTITY_REF) {
            Assert.notNull(entityRefName, String.format("Entity Name Required for %s FieldType", FieldType.ENTITY_REF.name()));
        }
        /* if (fieldType == FieldType.ENTITY_COLLECTION_REF) {
            Assert.notNull(entityCollectionRefField, String.format("Entity Collection Field Name Required for %s FieldType", FieldType.ENTITY_COLLECTION_REF.name()));
        } */
        this.entityRefName = entityRefName;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        // this.entityCollectionRefField = entityCollectionRefField;
    }


    public FieldType getType() {
        return fieldType;
    }

    public String getName() {
        return fieldName;
    }

    /**
     * @return Entity reference if the fieldtype is a reference
     */
    @Nullable
    public Entity getEntityRef() {
        if (entityRefName != null && !entityRefName.isEmpty()) {
            SchemaManager schemaManager = Services.getSchemaManager();
            assert schemaManager != null;
            return schemaManager.getEntity(entityRefName);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return fieldType == field.fieldType &&
                Objects.equals(fieldName, field.fieldName) &&
                Objects.equals(entityRefName, field.entityRefName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, fieldName, entityRefName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Field{");
        sb.append("fieldType=").append(fieldType);
        sb.append(", fieldName='").append(fieldName).append('\'');
        sb.append(", entityRefName='").append(entityRefName).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
