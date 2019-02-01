package it.at7.gemini.dsl.entities;

import java.util.HashSet;
import java.util.Set;

public class RawSchemaBuilder {
    private Set<RawEntity> rawEntities = new HashSet<>();
    private Set<RawEntity> rawInterface = new HashSet<>();


    public void addEntity(RawEntity rawEntity) {
        rawEntities.add(rawEntity);
    }

    public void addInterface(RawEntity rawEntityInterface) {
        rawInterface.add(rawEntityInterface);
    }

    public RawSchema build() {
        return new RawSchema(rawEntities, rawInterface);
    }
}
