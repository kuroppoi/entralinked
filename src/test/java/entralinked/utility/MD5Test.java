package entralinked.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MD5Test {
    
    @Test
    @DisplayName("Test if output MD5 hashes are correct")
    void testMD5Hashes() {
        assertEquals("ed076287532e86365e841e92bfc50d8c", MD5.digest("Hello World!"));
        assertEquals("8cfd799409ac5461004bca394a92b0af", MD5.digest("Some random string."));
        assertEquals("c74efaf9dd2782003ba4b27f15ef1049", MD5.digest("What is the meaning of life?"));
    }
}
