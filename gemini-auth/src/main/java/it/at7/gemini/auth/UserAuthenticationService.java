package it.at7.gemini.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

public interface UserAuthenticationService {
    String login(String username, String password) throws BadCredentialsException;

    String authenticateByToken(String token) throws AuthenticationException;

    void logout(String username);
}