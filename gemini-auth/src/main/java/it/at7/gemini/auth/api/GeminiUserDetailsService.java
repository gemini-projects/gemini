package it.at7.gemini.auth.api;

import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GeminiUserDetailsService implements UserDetailsService {


    @Autowired
    private EntityManager entityManager;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            EntityRecord user = entityManager.get("USER", username);
            // Password pwd = user.get("password");

            if (user.getFieldOrDefault(UserRef.FIELDS.FRAMEWORK, false)) {
                throw new UsernameNotFoundException("Framework User");
            }

            return User.builder()
                    .username(username)
                    .password(Objects.requireNonNull(user.get("password", String.class)))
                    .authorities("CUSTOMER_USER")
                    .build();
        } catch (EntityRecordException.LkNotFoundException e) {
            throw new UsernameNotFoundException(username, e);
        } catch (GeminiException e) {
            throw new GeminiRuntimeException(e);
        }
    }
}
