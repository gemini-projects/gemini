package it.at7.gemini.core;

import it.at7.gemini.boot.SuiteTestCoreJunitINIT;
import it.at7.gemini.core.entitymanager.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TransactionManagerAbstTest.class,
        SchemaManagerTest.class,
        PersistenceEntityManagerTest.class,
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
public class SuiteTestCore extends SuiteTestCoreJunitINIT {
}
