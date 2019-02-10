package it.at7.gemini.core;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.Field;

import java.util.HashSet;
import java.util.Set;

public class EntityReferenceRecord {

    public static EntityReferenceRecord NO_REFERENCE = new EntityReferenceRecord();

    private Set<StoredType> storedTypeSet;
    private Object primaryKey;
    private DynamicRecord logicalKeyValue;
    private Entity entity;

    private EntityReferenceRecord() {
        this.storedTypeSet = new HashSet<>();
        this.logicalKeyValue = new DynamicRecord();
    }

    public EntityReferenceRecord(Entity entity) {
        this();
        this.entity = entity;
    }

    public void addPKValue(Object primaryKey) {
        this.storedTypeSet.add(StoredType.PK);
        this.primaryKey = primaryKey;
    }

    public Entity getEntity() {
        return entity;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public boolean hasPrimaryKey() {
        return storedTypeSet.contains(StoredType.PK);
    }

    public boolean hasLogicalKey() {
        return storedTypeSet.contains(StoredType.LOGICAL_KEY);
    }

    public DynamicRecord getLogicalKeyRecord() {
        return logicalKeyValue;
    }

    public void addLogicalKeyValue(Field field, Object value) {
        this.storedTypeSet.add(StoredType.LOGICAL_KEY);
        logicalKeyValue.put(field, value);
    }

    public void addLogicalKeyValues(Set<EntityRecord.EntityFieldValue> logicalKeyValue) {
        for (EntityRecord.EntityFieldValue entityFieldValue : logicalKeyValue) {
            addLogicalKeyValue(entityFieldValue.getEntityField(), entityFieldValue.getValue());
        }
    }

    public static EntityReferenceRecord fromPKValue(Entity entity, Object primaryKey) {
        EntityReferenceRecord entityReferenceRecord = new EntityReferenceRecord(entity);
        entityReferenceRecord.addPKValue(primaryKey);
        return entityReferenceRecord;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityReferenceRecord that = (EntityReferenceRecord) o;
        if (hasPrimaryKey() && that.hasPrimaryKey()) {
            return primaryKey.equals(that.getPrimaryKey());
        }
        return getLogicalKeyRecord().equals(that.getLogicalKeyRecord());
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EntityReferenceRecord{");
        sb.append("storedTypeSet=").append(storedTypeSet);
        sb.append(", primaryKey=").append(primaryKey);
        sb.append(", logicalKeyValue=").append(logicalKeyValue);
        sb.append('}');
        return sb.toString();
    }

    enum StoredType {
        LOGICAL_KEY,
        PK
    }
}
