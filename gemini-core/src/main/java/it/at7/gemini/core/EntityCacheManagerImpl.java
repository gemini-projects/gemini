package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityRef;
import it.at7.gemini.schema.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class EntityCacheManagerImpl implements EntityCacheManager {

    @Autowired
    private PersistenceEntityManager persistenceEntityManager;

    @Autowired
    private SchemaManager schemaManager;

    @Override
    public void notifyUpdate(Entity entity, Transaction transaction) throws GeminiException {
        updateEntityEra(transaction, entity);
    }

    @Override
    public void notifyUpdate(Collection<Entity> entities, Transaction transaction) throws GeminiException {
        for (Entity entity : entities) {
            updateEntityEra(transaction, entity);
        }
    }

    private void updateEntityEra(Transaction transaction, Entity entity) throws GeminiException {
        Entity cacheEntity = this.schemaManager.getEntity(EntityRef.ERA.NAME);
        EntityRecord entityRecord = new EntityRecord(cacheEntity);
        entityRecord.put(Field.ID_NAME, entity.getIDValue());
        entityRecord.put(EntityRef.ERA.FIELDS.ENTITY,
                EntityReferenceRecord.fromPKValue(this.schemaManager.getEntity(EntityRef.NAME), entity.getIDValue()));
        entityRecord.put(EntityRef.ERA.FIELDS.TIMESTAMP, transaction.getOpenTime());
        persistenceEntityManager.createOrUpdateEntityRecord(entityRecord, transaction);
    }
}
