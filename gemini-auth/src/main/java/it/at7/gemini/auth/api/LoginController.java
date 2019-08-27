package it.at7.gemini.auth.api;

import it.at7.gemini.api.GeminiWrappers;
import it.at7.gemini.auth.core.AccessToken;
import it.at7.gemini.auth.core.UserAuthenticationService;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static it.at7.gemini.api.RestAPIController.API_URL;

@RestController
public class LoginController {
    public static final String AUTH_URL = "/auth";

    public static final String LOGIN_PATH = API_URL + AUTH_URL + "/login";
    public static final String REFRESH_TOKEN_PATH = API_URL + AUTH_URL + "/refresh_token";
    public static final String USER_ME = API_URL + AUTH_URL + "/me";

    private final UserAuthenticationService authenticationService;
    private final EntityManager entityManager;

    @Autowired
    public LoginController(UserAuthenticationService authenticationService,
                           EntityManager entityManager) {
        this.authenticationService = authenticationService;
        this.entityManager = entityManager;
    }

    @PostMapping(value = LOGIN_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AccessToken login(@RequestParam Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return authenticationService.login(username, password);
    }

    @PostMapping(value = LOGIN_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AccessToken login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.username == null || loginRequest.password == null) {
            throw new BadCredentialsException("Must provide both username and password");
        }
        return authenticationService.login(loginRequest.username, loginRequest.password);
    }

    @PostMapping(value = REFRESH_TOKEN_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AccessToken refreshToken(@RequestBody RefreshTokenRequest rftRequest) {
        if (rftRequest.refresh_token == null) {
            throw new BadCredentialsException("Must provide refresh_token");
        }
        return authenticationService.refreshLogin(rftRequest.refresh_token);
    }

    @GetMapping(value = USER_ME)
    public Object userMe(HttpServletRequest request) throws GeminiException {
        String userName = request.getRemoteUser();
        EntityRecord newEntityRecord = entityManager.getNewEntityRecord((UserRef.NAME));
        newEntityRecord.put(UserRef.FIELDS.USERNAME, userName);
        EntityRecord user = entityManager.get(newEntityRecord);
        return GeminiWrappers.EntityRecordApiType.of(user);
    }

    static class LoginRequest {
        public String username;
        public String password;
    }

    static class RefreshTokenRequest {
        public String refresh_token;
    }
}
