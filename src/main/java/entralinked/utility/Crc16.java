package entralinked.utility;

/**
 * Utility class for calculating CRC-16 checksums.
 */
public class Crc16 {
    
    public static int calc(byte[] input, int offset, int length) {
        int crc = 0xFFFF;
        
        for(int i = offset; i < offset + length; i++) {
            crc ^= (input[i] << 8);
            
            for(int j = 0; j < 8; j++) {
                if((crc & 0x8000) != 0) {
                    crc = crc << 1 ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        
        return crc & 0xFFFF;
    }
    
    public static int calc(byte[] input) {
        return calc(input, 0, input.length);
    }
    
    public static int calc(int input) {
        return calc(new byte[] {
            (byte)(input & 0xFF),
            (byte)((input >> 8) & 0xFF),
            (byte)((input >> 16) & 0xFF),
            (byte)((input >> 24) & 0xFF)
        });
    }
}
