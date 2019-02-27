package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EntityManager {

    Collection<Entity> getAllEntities();

    Entity getEntity(String entity);

    EntityRecord putIfAbsent(EntityRecord rec) throws GeminiException;

    Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> recs) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec) throws GeminiException;

    default EntityRecord update(EntityRecord rec)throws GeminiException{
        return update(rec, rec.getLogicalKeyValue());
    }

    EntityRecord update(EntityRecord rec, Collection<? extends DynamicRecord.FieldValue> logicalKey) throws GeminiException;

    default EntityRecord delete(EntityRecord entityRecord) throws GeminiException {
        return delete(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord delete(Entity e, Collection<? extends DynamicRecord.FieldValue> logicalKey) throws GeminiException;

    default EntityRecord get(EntityRecord entityRecord) throws GeminiException {
        return get(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord get(Entity e, Collection<? extends DynamicRecord.FieldValue> logicalKey) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, DynamicRecord searchRecord) throws GeminiException {
        assert searchRecord != null;
        return getRecordsMatching(entity, searchRecord.getFieldValues());
    }

    List<EntityRecord> getRecordsMatching(Entity entity, Set<DynamicRecord.FieldValue> filterFielValueType) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, Set<DynamicRecord.FieldValue> filterFielValueType, Transaction transaction) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext) throws GeminiException;
}
