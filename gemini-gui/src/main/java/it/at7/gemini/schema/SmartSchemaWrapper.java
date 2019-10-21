package it.at7.gemini.schema;

import it.at7.gemini.schema.smart.SmartSchema;

public class SmartSchemaWrapper {
    private final SmartSchema smartSchema;
    private final String smartSchemaAsString;

    public SmartSchemaWrapper(SmartSchema smartSchema, String smartSchemaAsString) {
        this.smartSchema = smartSchema;
        this.smartSchemaAsString = smartSchemaAsString;
    }

    public SmartSchema getSmartSchema() {
        return smartSchema;
    }

    public String getSmartSchemaAsString() {
        return smartSchemaAsString;
    }

    public static SmartSchemaWrapper of(SmartSchema smartSchema, String smartSchemaAsString) {
        return new SmartSchemaWrapper(smartSchema, smartSchemaAsString);
    }
}
