package it.at7.gemini.auth.api;

import it.at7.gemini.api.ApiUtility;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static it.at7.gemini.api.RestAPIController.API_URL;

@RestController
@RequestMapping(API_URL + "/me")
@ConditionalOnProperty(name = "gemini.auth.mecontroller", havingValue = "true", matchIfMissing = true)
public class GeminiMeController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping
    public Object me(OAuth2Authentication auth,
                     HttpServletRequest request,
                     HttpServletResponse response) throws GeminiException {
        String username = (String) auth.getPrincipal();
        EntityRecord userER = entityManager.get(UserRef.NAME, username);
        return ApiUtility.handleGeminiDataTypeResponse(userER, request, response);
    }
}
