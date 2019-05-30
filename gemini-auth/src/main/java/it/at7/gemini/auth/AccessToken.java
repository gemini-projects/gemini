package it.at7.gemini.auth;


public class AccessToken {
    private final String access_token;
    private final Type token_type;
    private final int  expires_in;

    private AccessToken(String access_token, Type token_type, int expires_in){
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
    }


    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type.name().toLowerCase();
    }

    public int getExpires_in() {
        return expires_in;
    }

    public static AccessToken bearer(String access_token, int expires_in){
        return new AccessToken(access_token, Type.BEARER, expires_in);
    }

    public enum Type {
        BEARER
    }
}
