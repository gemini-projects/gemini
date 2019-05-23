package it.at7.gemini.core.type;

import it.at7.gemini.exceptions.GeminiRuntimeException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

public class Password {
    public static final String PASSWORD_ALGO = "PBKDF2WithHmacSHA512";
    public static final int HASH_ITERATION = 1000;
    public static final int KEY_LENGHT = 1000;

    private String salt;
    private String hash;

    private Password() { // FOR Jackson deserializer
    }

    public Password(String password) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PASSWORD_ALGO);
            SecureRandom random = new SecureRandom();
            byte saltBytes[] = new byte[20];
            random.nextBytes(saltBytes);
            Base64.Encoder b64encoder = Base64.getEncoder();
            this.salt = b64encoder.encodeToString(saltBytes);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, HASH_ITERATION, KEY_LENGHT);
            SecretKey key = skf.generateSecret(spec);
            byte[] hashBytes = key.getEncoded();
            this.hash = b64encoder.encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new GeminiRuntimeException("Algorithm " + PASSWORD_ALGO + " not found");
        }
    }

    public String getSalt() {
        return salt;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password)) return false;
        Password password = (Password) o;
        return Objects.equals(salt, password.salt) &&
                Objects.equals(hash, password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salt, hash);
    }

    @Override
    public String toString() {
        return "Password{" +
                "salt='" + salt + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
