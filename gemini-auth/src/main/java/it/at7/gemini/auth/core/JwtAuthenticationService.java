package it.at7.gemini.auth.core;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.type.Password;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtAuthenticationService implements UserAuthenticationService {

    private final EntityManager entityManager;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public JwtAuthenticationService(EntityManager entityManager, JwtTokenService jwtTokenService) {
        this.entityManager = entityManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AccessToken login(String username, String password) throws BadCredentialsException {
        Entity userEntity = this.entityManager.getEntity(UserRef.NAME);
        try {
            List<EntityRecord> recordsMatching = this.entityManager.getRecordsMatching(userEntity, UserRef.FIELDS.USERNAME, username);
            if (recordsMatching.isEmpty()) {
                throw new BadCredentialsException(String.format("User %s not found", username));
            }
            EntityRecord userRecord = recordsMatching.get(0);
            Password pwd = userRecord.get(UserRef.FIELDS.PASSWORD);
            if (pwd != null && pwd.isEquals(password)) {
                return jwtTokenService.createBearer(userRecord.get(UserRef.FIELDS.USERNAME));
            }
            throw new BadCredentialsException(String.format("Invalid password for %s", username));
        } catch (GeminiException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public String authenticateByToken(String token) throws AuthenticationException {
        try {
            Claim username = jwtTokenService.verify(token).get(JwtTokenService.USER_CLAIM);
            return username.asString();
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid JWT token.", e);
        }
    }

    @Override
    public AccessToken refreshLogin(String refresh_token) {
        try {
            Claim username = jwtTokenService.verify(refresh_token).get(JwtTokenService.USER_CLAIM);
            // TODO ??  do some check withRecord the user (and blacklist the RF ? )
            return jwtTokenService.createBearer(username.asString());
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid JWT RefreshToken.", e);
        }
    }

    @Override
    public void logout(String username) {

    }
}
