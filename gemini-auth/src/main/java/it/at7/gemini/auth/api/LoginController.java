package it.at7.gemini.auth.api;

import it.at7.gemini.auth.AccessToken;
import it.at7.gemini.auth.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static it.at7.gemini.api.RestAPIController.API_URL;

@RestController
public class LoginController {
    public static final String LOGIN_PATH = API_URL + "/login";

    private final UserAuthenticationService authenticationService;

    @Autowired
    public LoginController(UserAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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
            throw  new BadCredentialsException("Must provide both username and password");
        }
        return authenticationService.login(loginRequest.username, loginRequest.password);
    }

    static class LoginRequest {
        public String username;
        public String password;
    }
}
