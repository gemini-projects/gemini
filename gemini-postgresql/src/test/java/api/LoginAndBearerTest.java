package api;

import it.at7.gemini.api.LoginAndBearerAbstTest;
import it.at7.gemini.auth.AuthModule;
import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

public class LoginAndBearerTest extends LoginAndBearerAbstTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() throws GeminiException {
        return IntegrationTestMain.initializeFullIntegrationWebApp(Set.of(AuthModule.Core.class), Set.of(AuthModule.API.class));
    }
}
