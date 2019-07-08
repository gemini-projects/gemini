package it.at7.gemini.auth.api;

import it.at7.gemini.api.openapi.OpenApiService;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateListener;
import it.at7.gemini.core.StateManager;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static it.at7.gemini.auth.api.LoginController.LOGIN_PATH;
import static it.at7.gemini.conf.State.API_INITIALIZATION;

@Service
@ComponentScan("it.at7.gemini.auth.api")
public class AuthModuleAPI implements StateListener {

    private final OpenApiService openApiService;

    @Autowired
    public AuthModuleAPI(OpenApiService openApiService, StateManager stateManager) {
        this.openApiService = openApiService;
        stateManager.register(this);
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual == API_INITIALIZATION) {
            Map<String, Object> parameters = Map.of("tokenUrl", LOGIN_PATH);
            this.openApiService.addOAuth2PasswordFlow("OAuth2PwdAPI", parameters);
            this.openApiService.secureAllEntities("OAuth2PwdAPI");
        }
    }
}
