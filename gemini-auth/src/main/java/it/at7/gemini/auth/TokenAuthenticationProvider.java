package it.at7.gemini.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final UserAuthenticationService userAuthenticationService;

    public TokenAuthenticationProvider(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object token = authentication.getCredentials();

        String authenticatedUser = userAuthenticationService.authenticateByToken(String.valueOf(token));

        return User.builder().username(authenticatedUser).password(authenticatedUser).authorities("admin").build();
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // No need to additional check
    }
}
