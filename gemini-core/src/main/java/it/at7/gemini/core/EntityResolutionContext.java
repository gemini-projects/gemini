package it.at7.gemini.core;

import java.util.Collections;
import java.util.List;

public class EntityResolutionContext {
    public static final EntityResolutionContext DEFAULT = of()
            .collection(Strategy.NONE)
            .build();

    private final EntityResolutionContext.Strategy collection;
    private final List<String> providedCollections;

    public EntityResolutionContext(Strategy collection, List<String> providedCollections) {
        this.collection = collection;
        this.providedCollections = Collections.unmodifiableList(providedCollections);
    }

    public Strategy getCollection() {
        return collection;
    }

    public List<String> getProvidedCollections() {
        return providedCollections;
    }

    public static EntityResolutionContextBuilder of() {
        return new EntityResolutionContextBuilder();
    }

    public enum Strategy {
        ALL,
        PROVIDED,
        NONE
    }
}
