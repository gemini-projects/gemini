package it.at7.gemini.schema;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Objects;

public class EntityField extends Field {
    private final Entity entity;
    private final boolean isLogicalKey;
    private final Scope scope;
    private final String interfaceName;
    private final int lkOrder;

    public EntityField(Entity entity, FieldType fieldType, String fieldName, boolean isLogicalKey, int lkOrder, String entityRefName, String interfaceName, Scope scope) {
        super(fieldType, fieldName, entityRefName);
        Assert.notNull(entity, "EntityField must have a not null entity");
        this.isLogicalKey = isLogicalKey;
        this.lkOrder = lkOrder;
        this.entity = entity;
        Assert.notNull(scope, "EntityField must have a not null scope");
        this.scope = scope;
        this.interfaceName = interfaceName;
        Assert.isTrue(!isLogicalKey || lkOrder > 0, "EntityField is a logical key and must have an order");
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
     * @return  Logical key order gives an order when the entity have more than one logical key
     */
    public int getLkOrder() {
        return lkOrder;
    }

    public Scope getScope() {
        return scope;
    }

    @Nullable
    public String getInterfaceName() {
        return interfaceName;
    }

    public EntityRecord toInitializationEntityRecord() throws InvalidLogicalKeyValue, InvalidTypeForObject, EntityFieldException {
        HashMap<String, Object> values = new HashMap<>();
        values.put("name", getName());
        values.put("type", getType().name());
        Entity ENTITY_ENTITY = Services.getSchemaManager().getEntity("ENTITY");
        EntityReferenceRecord entityRefRec = EntityReferenceRecord.fromPKValue(ENTITY_ENTITY, this.entity.getIDValue());
        EntityField ENTIY_NAME_FIELD = ENTITY_ENTITY.getField("name");
        entityRefRec.addLogicalKeyValue(ENTIY_NAME_FIELD, this.entity.getName());
        values.put("entity", entityRefRec);
        Entity entityRef = getEntityRef();
        if (entityRef != null) {
            values.put("refentity", EntityReferenceRecord.fromPKValue(entityRef, entityRef.getIDValue()));
        }
        values.put("islogicalkey", isLogicalKey());
        values.put("lkOrder", getLkOrder());
        values.put("scope", scope.name());
        Entity FIELD_ENTITY = Services.getSchemaManager().getEntity("FIELD");
        assert FIELD_ENTITY != null;
        return RecordConverters.entityRecordFromMap(FIELD_ENTITY, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityField)) return false;
        if (!super.equals(o)) return false;
        EntityField that = (EntityField) o;
        return super.equals(o)
                && Objects.equals(entity, that.entity)
                && Objects.equals(scope, that.scope)
                && Objects.equals(interfaceName, that.interfaceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), entity, scope, interfaceName);
    }

    public enum Scope {
        META,
        DATA
    }
}
