package it.at7.gemini.core;

import org.springframework.util.Assert;

import java.util.List;

public class EntityResolutionContextBuilder {
    private EntityResolutionContext.Strategy collection;
    private List<String> collectionProvided;


    public EntityResolutionContextBuilder() {
        this.collectionProvided = List.of();
    }

    public EntityResolutionContextBuilder collection(EntityResolutionContext.Strategy type) {
        Assert.isTrue(type != EntityResolutionContext.Strategy.PROVIDED, "For PROVIDED you need to use the collection String API");
        this.collection = type;
        return this;
    }

    public EntityResolutionContext build() {
        return new EntityResolutionContext(collection, collectionProvided);
    }

    public EntityResolutionContextBuilder collection(List<String> collectionProvided) {
        this.collection = EntityResolutionContext.Strategy.PROVIDED;
        this.collectionProvided = collectionProvided;
        return this;
    }
}
