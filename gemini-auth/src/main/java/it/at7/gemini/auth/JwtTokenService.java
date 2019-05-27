package it.at7.gemini.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;

import java.util.Map;

public interface JwtTokenService {
    String USER_CLAIM = "username";

    String create(String username);

    Map<String, Claim> verify(String token) throws JWTVerificationException;
}
