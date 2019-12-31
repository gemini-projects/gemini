package it.at7.gemini.auth.api;

import it.at7.gemini.auth.core.OAuthClientRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GeminClientDetailsService implements ClientDetailsService {

    @Autowired
    private EntityManager entityManager;


    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        // get the client application

        try {
            EntityRecord entityRecord = this.entityManager.get(OAuthClientRef.NAME, clientId);
            String client_secret = entityRecord.get(OAuthClientRef.FIELDS.CLIENT_SECRET);

            BaseClientDetails baseClientDetails = new BaseClientDetails();
            baseClientDetails.setClientId(clientId);
            baseClientDetails.setClientSecret(client_secret);
            ArrayList<String> credentials = new ArrayList<>();
            credentials.add("password");
            credentials.add("refresh_token");
            baseClientDetails.setAuthorizedGrantTypes(credentials);
            ArrayList<String> scope = new ArrayList<>();
            scope.add("DEFAULT");
            baseClientDetails.setScope(scope);
            baseClientDetails.setAccessTokenValiditySeconds(86400);
            return baseClientDetails;

        } catch (EntityRecordException.LkNotFoundException e) {
            throw new ClientRegistrationException(String.format("Client id %s not found", clientId));
        } catch (GeminiException e) {
            throw new ClientRegistrationException(e.getMessage(), e);
        }
    }
}
