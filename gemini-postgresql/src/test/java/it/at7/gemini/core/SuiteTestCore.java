package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.*;
import it.at7.gemini.core.schemamanager.SchemaManagerInitAbstTest;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.context.ConfigurableApplicationContext;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DynamicSchemaEntityManagerAbstTest.class,
        TransactionManagerAbstTest.class,
        SchemaManagerAbstTest.class,
        SchemaManagerInitAbstTest.class,
        PersistenceEntityManagerAbstTest.class,
        BasicTypesEntityManagerAbstTest.class,
        ClosedDomainEntityManagerAbstTest.class,
        EmbedableTypeEntityManagerAbstTest.class,
        EntityRefArrayEntityManagerAbstTest.class,
        EntityRefEntityManagerAbstTest.class,
        ExternalEntityFieldsEntityManagerAbstTest.class,
        FilterEntityManagerAbstTest.class,
        GenericEntityRefEntityManagerAbstTest.class,
        OneRecordEntityManagerAbstTest.class,
        PasswordEntityManagerAbstTest.class
})
public class SuiteTestCore {

    static ConfigurableApplicationContext applictionContext;

    @BeforeClass
    public static void setup() throws GeminiException {
        applictionContext = IntegrationTestMain.initializeGemini();
    }

    @AfterClass
    public static void clean() {
        applictionContext.close();

    }
}
