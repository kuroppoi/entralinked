package entralinked.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
     * @return A hex-formatted MD5 hash of the specified input string.
     */
    public static String digest(String string) {
        return StringUtil.toHexStringPadded(digest(string.getBytes(StandardCharsets.ISO_8859_1)));
    }
    
    /**
     * @return A hex-formatted MD5 hash of the specified input file.
     */
    public static String digest(File file) throws IOException {
        return StringUtil.toHexStringPadded(digest(Files.readAllBytes(file.toPath())));
    }
    
    /**
     * @return An MD5 hash of the specified input.
     */
    public static byte[] digest(byte[] bytes) {
        if(digest == null) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch(NoSuchAlgorithmException e) {
                logger.error("Could not get MD5 MessageDigest instance", e);
            }
        }
        
        return digest.digest(bytes);
    }
}
