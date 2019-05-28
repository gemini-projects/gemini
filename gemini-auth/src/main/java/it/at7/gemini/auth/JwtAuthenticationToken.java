package it.at7.gemini.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String jwtToken;

    public JwtAuthenticationToken(String jwtToken) {
        super(AuthorityUtils.createAuthorityList("user"));
        this.jwtToken = jwtToken;
    }

    @Override
    public String getCredentials() {
        return jwtToken;
    }

    @Override
    public String getPrincipal() {
        return jwtToken;
    }
}
