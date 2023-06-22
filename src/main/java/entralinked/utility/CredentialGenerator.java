package entralinked.utility;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple utility class for generating client credentials.
 */
public class CredentialGenerator {
    
    public static final String CHALLENGE_CHARTABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * @return A securely-generated server challenge of the specified length.
     */
    public static String generateChallenge(int length) {
        char[] challenge = new char[length];
        
        for(int i = 0; i < challenge.length; i++) {
            challenge[i] = CHALLENGE_CHARTABLE.charAt(secureRandom.nextInt(CHALLENGE_CHARTABLE.length()));
        }
        
        return new String(challenge);
    }
    
    /**
     * @return A base64-encoded, securely-generated auth token of the specified length.
     */
    public static String generateAuthToken(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}
