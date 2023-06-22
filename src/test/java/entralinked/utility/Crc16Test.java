package entralinked.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Crc16Test {
    
    @Test
    @DisplayName("Test if output CRC-16 checksums are valid")
    void testCrc16Checksums() {
        // Test checksums calculated from byte arrays with varying lengths
        assertEquals(0xEF9D, Crc16.calc(new byte[] {114, 49, -30, -50, 46, -62, 47, 39, -85, 73, -91, 40, 21, -80, -95, -3}));
        assertEquals(0x23DF, Crc16.calc(new byte[] {-32, -95, 74, 56, 2, -57, 90, 78, 81, 81, -126, 29, 8, 1, 65, -7}));
        assertEquals(0x263D, Crc16.calc(new byte[] {-128, 47, -44, 118, 1, 91, 124, 104, 2, -4, -84, -76}));
        assertEquals(0xBF19, Crc16.calc(new byte[] {-9, 108, -105, -33, -110, -8, 33, 44}));
        
        // Test checksums calculated from different sections of a byte array
        byte[] bytes = {81, -81, -69, -18, 70, -94, -61, 73, -63, 56, 56, 113, -75, -87, -30, -31, -76, 76, -120, -14, -79, -43, -117, -22, 23, 9, -81, 77, 64, -93, 48, 1};
        assertEquals(0xC8F5, Crc16.calc(bytes, 0, 4)); // bytes 0-3
        assertEquals(0x6093, Crc16.calc(bytes, 4, 8)); // bytes 4-11
        assertEquals(0xD7C3, Crc16.calc(bytes, 8, 8)); // bytes 8-15
        assertEquals(0xFF5C, Crc16.calc(bytes, 16, 16)); // bytes 16-31
        
        // Test checksums calculated from integers
        assertEquals(0x9EFB, Crc16.calc(12345));
        assertEquals(0x005E, Crc16.calc(847190349));
        assertEquals(0x8C87, Crc16.calc(Integer.MAX_VALUE));
        assertEquals(0x1548, Crc16.calc(Integer.MIN_VALUE));
    }
}
