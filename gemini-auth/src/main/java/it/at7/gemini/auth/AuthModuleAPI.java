package it.at7.gemini.auth;

import it.at7.gemini.api.openapi.OpenApiService;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateListener;
import it.at7.gemini.core.StateManager;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static it.at7.gemini.conf.State.API_INITIALIZATION;

@Service
@ComponentScan("it.at7.gemini.auth.api")
@ConditionalOnProperty(name = "gemini.auth", havingValue = "true", matchIfMissing = true)
public class AuthModuleAPI implements StateListener {

    private final OpenApiService openApiService;

    @Autowired
    public AuthModuleAPI(Optional<OpenApiService> openApiService, StateManager stateManager) {
        this.openApiService = openApiService.orElse(null);
        stateManager.register(this);
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual == API_INITIALIZATION) {
            if (openApiService != null) {
                Map<String, Object> parameters = Map.of(
                        "tokenUrl", "/oauth/token");
                this.openApiService.addOAuth2PasswordFlow("OAuth2PwdAPI", parameters);
                this.openApiService.secureAllEntities("OAuth2PwdAPI");
            }
        }
    }
}
