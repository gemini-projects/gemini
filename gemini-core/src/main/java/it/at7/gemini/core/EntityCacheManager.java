package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;

public interface EntityCacheManager {

    /**
     * Notify that the target entity data was updated
     *
     * @param entity      target entity
     * @param transaction transaction responsible of the update
     */
    void notifyUpdate(Entity entity, Transaction transaction) throws GeminiException;

    /**
     * Notify that the target entities data were updated
     *
     * @param entities    target entities
     * @param transaction transaction responsible of the update
     */
    void notifyUpdate(Collection<Entity> entities, Transaction transaction) throws GeminiException;
}
