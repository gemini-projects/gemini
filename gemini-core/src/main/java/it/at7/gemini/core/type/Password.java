package it.at7.gemini.core.type;

import it.at7.gemini.exceptions.GeminiRuntimeException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class Password {
    public static final String DEFAULT_PASSWORD_ALGO = "PBKDF2WithHmacSHA512";
    public static final int DEFAULT_HASH_ITERATION = 10000;
    public static final int DEFAULT_KEY_LENGHT = 512;

    private final String salt;
    private final String hash;
    private final String originalPassword;
    private final int hashIteration;
    private final int keyLength;
    private final String hashAlgo;

    private Password() {    // for Jackson Deserializer
        originalPassword = null;
        hash = null;
        salt = null;
        hashIteration = 0;
        keyLength = 0;
        hashAlgo = "";
    }

    public Password(String password) {
        this.hashIteration = DEFAULT_HASH_ITERATION;
        this.keyLength = DEFAULT_KEY_LENGHT;
        this.hashAlgo = DEFAULT_PASSWORD_ALGO;
        this.originalPassword = password;
        try {
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[20];
            random.nextBytes(saltBytes);
            Base64.Encoder b64encoder = Base64.getEncoder();
            this.salt = b64encoder.encodeToString(saltBytes);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, this.hashIteration, this.keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(this.hashAlgo);
            SecretKey key = skf.generateSecret(spec);
            byte[] hashBytes = key.getEncoded();
            this.hash = b64encoder.encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new GeminiRuntimeException("Algorithm " + this.hashAlgo + " not found");
        }
    }

    public String getSalt() {
        return salt;
    }

    public String getHash() {
        return hash;
    }

    public int getHashIteration() {
        return hashIteration;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password)) return false;
        Password password = (Password) o;
        // firs of all check if both password has the same original password
        if (this.originalPassword != null && password.originalPassword != null && this.originalPassword.equals(password.originalPassword))
            return true;
        // then we try to check the two password with the target password salt (target not have origial password)
        if (this.originalPassword != null) {
            if (password.isEquals(originalPassword)) {
                return true;
            }
        }
        // the last is the case when both this and target password loose the original password and have only salt and hash
        return Objects.equals(salt, password.salt) &&
                Objects.equals(hash, password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salt, hash);
    }

    public boolean isEquals(String password) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, this.hashIteration, this.keyLength);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(this.hashAlgo);
            SecretKey key = skf.generateSecret(spec);
            byte[] passwordHash = key.getEncoded();
            byte[] hashedBytes = Base64.getDecoder().decode(this.hash);
            return Arrays.equals(passwordHash, hashedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new GeminiRuntimeException("Algorithm " + this.hashAlgo + " not found");
        }
    }

    @Override
    public String toString() {
        return "Password{" +
                "salt='" + salt + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
