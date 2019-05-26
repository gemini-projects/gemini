package it.at7.gemini.core.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PasswordTest {

    @Test
    public void testEqualsOriginalPassword() {
        Password password = new Password("test");
        Password password2 = new Password("test");
        Assert.isTrue(password.equals(password2), "Password not equals");
    }

    @Test
    public void testEqualsOriginalPasswordWithIsEquals() throws IOException {
        Password password = new Password("test");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("salt", password.getSalt());
        jsonMap.put("hash", password.getHash());
        jsonMap.put("hashAlgo", Password.DEFAULT_PASSWORD_ALGO);
        jsonMap.put("keyLength", Password.DEFAULT_KEY_LENGHT);
        jsonMap.put("hashIteration", Password.DEFAULT_HASH_ITERATION);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(jsonMap);

        // contains only hash and salt
        Password convertedPWD = objectMapper.readValue(jsonString, Password.class);
        Assert.isTrue(convertedPWD.isEquals("test"), "Password not equals");
    }

    @Test
    public void tesHashAndSaltEquals() throws IOException {
        Password password = new Password("test");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("salt", password.getSalt());
        jsonMap.put("hash", password.getHash());
        jsonMap.put("hashAlgo", Password.DEFAULT_PASSWORD_ALGO);
        jsonMap.put("keyLength", Password.DEFAULT_KEY_LENGHT);
        jsonMap.put("hashIteration", Password.DEFAULT_HASH_ITERATION);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(jsonMap);

        // contains only hash and salt
        Password firstConverted = objectMapper.readValue(jsonString, Password.class);
        Password secondConverted = objectMapper.readValue(jsonString, Password.class);
        Assert.isTrue(firstConverted.equals(secondConverted), "Password not equals");
    }
}