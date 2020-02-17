package it.at7.gemini.schema;

public interface EntityRef {
    String NAME = "ENTITY";

    interface FIELDS {
        String NAME = "name";
        String MODULE = "module";
        String CLOSED_DOMAIN = "closedDomain";
    }

    interface ERA {
        String NAME = "ENTITYERA";

        interface FIELDS {
            String ENTITY = "entity";
            String TIMESTAMP = "timestamp";
        }
    }
}
