package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface EntityManager {

    Collection<Entity> getAllEntities();

    Entity getEntity(String entity);

    EntityRecord putIfAbsent(EntityRecord rec) throws GeminiException;

    Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> recs) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec, Transaction transaction) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    default EntityRecord update(EntityRecord rec) throws GeminiException {
        return update(rec, rec.getLogicalKeyValue());
    }

    EntityRecord update(EntityRecord rec, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord update(EntityRecord rec, UUID uuid) throws GeminiException;

    default EntityRecord delete(EntityRecord entityRecord) throws GeminiException {
        return delete(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord delete(Entity e, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord delete(Entity e, UUID uuid) throws GeminiException;

    default EntityRecord get(EntityRecord entityRecord) throws GeminiException {
        return get(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord get(Entity e, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord get(Entity e, UUID uuid) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, DynamicRecord searchRecord) throws GeminiException {
        assert searchRecord != null;
        return getRecordsMatching(entity, searchRecord.getFieldValues());
    }

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType, Transaction transaction) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, Transaction transaction) throws GeminiException;
}
