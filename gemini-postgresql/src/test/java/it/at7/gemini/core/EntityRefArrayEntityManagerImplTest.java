package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.EntityRefArrayEntityManagerAbsTest;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

public class EntityRefArrayEntityManagerImplTest extends EntityRefArrayEntityManagerAbsTest {
    static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() throws SQLException, GeminiException {
        contex = IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
        insertDomainRecords();
    }

    @AfterClass
    public static void after() {
        if (contex != null)
            contex.close();
    }
}
