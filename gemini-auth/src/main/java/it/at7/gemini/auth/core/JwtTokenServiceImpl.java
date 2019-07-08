package it.at7.gemini.auth.core;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final Algorithm algorithm;
    private final int expiration;

    @Autowired
    public JwtTokenServiceImpl(
            @Value("${gemini.jwt.secret}") String secret,
            @Value("${gemini.jwt.defaultExp:3600}") int expiration) {
        this.algorithm = Algorithm.HMAC512(secret);
        this.expiration = expiration;
    }

    @Override
    public AccessToken createBearer(String username) {
        Instant issuedAt = Instant.now();
        String access_token = JWT.create()
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(issuedAt.plusSeconds(expiration)))
                .withClaim(USER_CLAIM, username)
                .sign(algorithm);

        String refresh_token = JWT.create()
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(issuedAt.plusSeconds(expiration * 10)))
                .withClaim(USER_CLAIM, username)
                .sign(algorithm);
        return AccessToken.bearer(access_token, refresh_token, expiration);
    }

    @Override
    public Map<String, Claim> verify(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaims();
    }
}
