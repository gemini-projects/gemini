package it.at7.gemini.schema;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Objects;

public class EntityField extends Field {
    private final Entity entity;
    private final boolean isLogicalKey;
    private Object idValue;

    public EntityField(Entity entity, FieldType fieldType, String fieldName, boolean isLogicalKey,
                       String entityRefName) {
        //super(fieldType, fieldName, entityRefName, entityCollectionRefField);
        super(fieldType, fieldName, entityRefName);
        Assert.notNull(entity, "EntityField must have a not null entity");
        /* if (fieldType == FieldType.ENTITY_COLLECTION_REF) {
            Assert.isTrue(!isLogicalKey, String.format("Field Type %s not supported as Entity logiacl key", FieldType.ENTITY_COLLECTION_REF.name()));
        } */
        this.isLogicalKey = isLogicalKey;
        this.entity = entity;
    }

    /**
     * @return The Entity the Field belongs
     */
    public Entity getEntity() {
        return entity;
    }


    public boolean isLogicalKey() {
        return isLogicalKey;
    }

    /**
     * Optional setter, used to set an ID Value to better identity the field. ID should be setted accordingly to the
     * persistence strategy.
     * Useful for example for schema checking.
     *
     * @param idValue
     */
    public void setFieldIDValue(Object idValue) {
        this.idValue = idValue;
    }

    @Nullable
    public Object getIDValue() {
        return idValue;
    }

    public EntityRecord toInitializationEntityRecord() throws InvalidLogicalKeyValue, InvalidTypeForObject {
        HashMap<String, Object> values = new HashMap<>();
        values.put("name", getName());
        values.put("type", getType().name());
        values.put("entity", EntityReferenceRecord.fromPKValue(entity, entity.getIDValue()));
        Entity entityRef = getEntityRef();
        if (entityRef != null) {
            values.put("refentity", EntityReferenceRecord.fromPKValue(entityRef, entityRef.getIDValue()));
        }
        values.put("islogicalkey", isLogicalKey());
        Entity entity = Services.getSchemaManager().getEntity("FIELD");
        assert entity != null;
        return RecordConverters.entityRecordFromMap(entity, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityField)) return false;
        if (!super.equals(o)) return false;
        EntityField that = (EntityField) o;
        return super.equals(o) && Objects.equals(entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), entity);
    }
}
