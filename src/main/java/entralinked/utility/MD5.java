package entralinked.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.util.internal.StringUtil;

/**
 * Utility class for generating hex-formatted MD5 hashes.
 */
public class MD5 {
    
    private static final Logger logger = LogManager.getLogger();
    private static MessageDigest digest;
    
    /**
     * @return A hex-formatted MD5 hash of the specified input.
     */
    public static String digest(String string) {
        if(digest == null) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch(NoSuchAlgorithmException e) {
                logger.error("Could not get MD5 MessageDigest instance", e);
            }
        }
        
        return StringUtil.toHexStringPadded(digest.digest(string.getBytes(StandardCharsets.ISO_8859_1)));
    }
}
