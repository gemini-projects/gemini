package it.at7.gemini.auth.core;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationProvider implements AuthenticationProvider {

    private final UserAuthenticationService userAuthenticationService;

    public TokenAuthenticationProvider(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object token = authentication.getCredentials();
        String authenticatedUser = userAuthenticationService.authenticateByToken(String.valueOf(token));
        return new UsernamePasswordAuthenticationToken(authenticatedUser, token, authentication.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class
                .isAssignableFrom(authentication));
    }
}
