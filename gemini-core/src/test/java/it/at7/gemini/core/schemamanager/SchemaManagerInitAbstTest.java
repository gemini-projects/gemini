package it.at7.gemini.core.schemamanager;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.Test;

import java.util.List;

public class SchemaManagerInitAbstTest {

    @Test(expected = IllegalArgumentException.class)
    public void testMetaAndDataNotUnique() throws GeminiException {
        TransactionManager transactionManager = Services.getTransactionManager();
        SchemaManagerInit schemaManager = (SchemaManagerInit) Services.getSchemaManager();

        try (Transaction transaction = transactionManager.openRawTransaction()) {
            schemaManager.loadGeminiModulesSchemas(List.of(new CoreModule(), new MetaNotUnique()));
            schemaManager.initializeSchemaStorage(transaction);
        }
    }


    @ModuleDescription(
            name = "MetaFieldsNotUnique",
            dependencies = {},
            order = -700)
    public class MetaNotUnique implements GeminiModule {
    }
}
