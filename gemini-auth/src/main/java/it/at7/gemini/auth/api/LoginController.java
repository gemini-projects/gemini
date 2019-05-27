package it.at7.gemini.auth.api;

import it.at7.gemini.auth.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static it.at7.gemini.api.RestAPIController.API_URL;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
public class LoginController {
    public static final String LOGIN_URL = API_URL + "/login";

    private final UserAuthenticationService authenticationService;

    @Autowired
    public LoginController(UserAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(LOGIN_URL)
    public Object login(@RequestBody LoginRequest loginRequest) {
        try {
            return authenticationService.login(loginRequest.username, loginRequest.password);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(e.getMessage());
        }
    }


    static class LoginRequest {
        public String username;
        public String password;
    }
}
