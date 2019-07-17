package it.at7.gemini.core;

import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityRecord implements RecordBase {
    private Map<String, Object> store;
    private Set<EntityField> fields;
    private Entity entity;
    private UUID uuid;

    public EntityRecord(Entity entity) {
        Assert.notNull(entity, "Entity required for Entity DynamicRecord");
        this.store = new HashMap<>();
        this.fields = new HashSet<>();
        this.entity = entity;
    }

    public Set<EntityField> getModifiedFields() {
        return Collections.unmodifiableSet(fields);
    }

    @Override
    public Map<String, Object> getStore() {
        return store;
    }

    public boolean set(EntityField field, Object value) throws EntityFieldException {
        return put(field, value);
    }

    public boolean put(String fieldName, Object value) {
        try {
            EntityField field = getEntityFieldFrom(fieldName);
            return put(field, value);
        } catch (EntityFieldException e) {
            return false;
        }
    }

    public boolean setMeta(String fieldName, Object value) {
        return putMeta(fieldName, value);
    }

    public boolean putMeta(String fieldName, Object value) {
        try {
            EntityField field = getMetaFieldFrom(fieldName);
            return put(field, value);
        } catch (EntityFieldException e) {
            return false;
        }
    }

    public boolean put(EntityField field, Object value) throws EntityFieldException {
        EntityField.Scope scope = field.getScope();
        switch (scope) {
            case META:
                if (!this.entity.getMetaEntityFields().contains(field)) {
                    throw EntityFieldException.ENTITYMETAFIELD_NOT_FOUND(field);
                }
                break;
            case DATA:
                if (!(this.entity.getDataEntityFields().contains(field) || this.entity.getIdEntityField().equals(field))) {
                    throw EntityFieldException.ENTITYFIELD_NOT_FOUND(field);
                }
                break;
        }

        Object convertedValue = FieldConverters.getConvertedFieldValue(field, value);
        fields.add(field);
        store.put(field.getName().toLowerCase(), convertedValue);
        return true;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    public Set<EntityFieldValue> getLogicalKeyValue() {
        Set<EntityField> logicalKey = entity.getLogicalKey().getLogicalKeySet();
        return getEntityFieldValue(logicalKey);
    }

    /**
     * Get values and fields for all the fields available in the Entity Schema. This means
     * that if a new EntityRecord is created with only a subset of fields the remaining fields
     * are extracted with a default value. Returns only DATA fields.
     */
    public Set<EntityFieldValue> getDataEntityFieldValues() {
        return getEntityFieldValue(entity.getDataEntityFields());
    }

    public Set<EntityFieldValue> getMetaEntityFieldValues() {
        return getEntityFieldValue(entity.getMetaEntityFields());
    }

    /**
     * Get DATA and META Entity Fields
     *
     * @return
     */
    public Set<EntityFieldValue> getALLEntityFieldValues() {
        Set<EntityField> fields = Stream.concat(entity.getDataEntityFields().stream(), entity.getMetaEntityFields().stream())
                .collect(Collectors.toSet());
        return getEntityFieldValue(fields);
    }

    /**
     * Get modified fields.. both for DATA and META
     */
    public Set<EntityFieldValue> getOnlyModifiedEntityFieldValue() {
        return getEntityFieldValue(fields);
    }

    public EntityFieldValue getEntityFieldValue(EntityField field) {
        Object value = get(field);
        EntityFieldValue fieldValue = EntityFieldValue.create(field, value);
        return fieldValue;
    }

    /**
     * Get a subset of Entity Fields
     *
     * @param fields filter fields
     * @return
     */
    public Set<EntityFieldValue> getEntityFieldValue(Set<EntityField> fields) {
        Set<EntityFieldValue> fieldValues = new HashSet<>();
        for (EntityField field : fields) {
            fieldValues.add(getEntityFieldValue(field));
        }
        return fieldValues;
    }

    private EntityField getEntityFieldFrom(String fieldName) throws EntityFieldException {
        return this.entity.getField(fieldName);
    }

    private EntityField getMetaFieldFrom(String fieldName) throws EntityFieldException {
        return this.entity.getMetaField(fieldName);
    }

    public void update(EntityRecord rec) {
        Assert.isTrue(entity == rec.entity, "Records mus belong to the same Entity");
        for (EntityFieldValue fieldValue : rec.getOnlyModifiedEntityFieldValue()) {
            EntityField field = fieldValue.getEntityField();
            Object value = rec.get(field);
            try {
                put(field, value);
            } catch (EntityFieldException e) {
                // no exception here
                throw new RuntimeException("Critical bug here");
            }
        }
    }

    @Nullable
    public Object getID() {
        return get(getEntity().getIdEntityField());
    }

    public boolean hasID() {
        return getStore().containsKey(getEntity().getIdEntityField().getName()) && getID() != null;
    }

    public EntityFieldValue getIDEntityFieldValueType() {
        Object value = get(getEntity().getIdEntityField());
        return EntityFieldValue.create(getEntity().getIdEntityField(), value);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean hasUUID() {
        return this.uuid != null;
    }

    @Nullable
    public UUID getUUID() {
        return this.uuid;
    }
}
