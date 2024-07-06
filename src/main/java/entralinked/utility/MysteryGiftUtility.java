package entralinked.utility;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.Arrays;

public class MysteryGiftUtility {
    
    public static byte[] createUniversalGiftData4(byte[] bytes) {
        // Check data size
        if(bytes.length > 936) {
            throw new IllegalArgumentException("Data too large: %s".formatted(bytes.length));
        }
        
        // TODO gift title & wonder card
        byte[] result = new byte[936];
        
        if(bytes.length <= 856) {
            System.arraycopy(bytes, 0, result, 0x50, bytes.length);
        } else {
            System.arraycopy(bytes, 0, result, 0, bytes.length);
        }
        
        // Clear game version
        result[0x48] = 0;
        result[0x49] = 0;
        return result;
    }
    
    public static byte[] createUniversalGiftData5(byte[] bytes) {
        // Check data size
        if(bytes.length > 720) {
            throw new IllegalArgumentException("Data too large: %s".formatted(bytes.length));
        }
        
        byte[] result = new byte[720];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[0xCE] = 0; // Version flag
        result[0x2CB] = 0; // Language code
        
        // Create standard gift description if there is none
        if(bytes.length == 204) {
            Arrays.fill(result, 0xD0, 0x2CA, (byte)0xFF);
            String description = "No description is available for this gift.";
            byte[] descriptionBytes = description.replace('\n', '\uFFFE').getBytes(StandardCharsets.UTF_16LE);
            System.arraycopy(descriptionBytes, 0, result, 0xD0, descriptionBytes.length);
        }
        
        // Recalculate checksum
        int checksum = Crc16.calc(result, 0, 0x2CE);
        result[0x2CE] = (byte)(checksum & 0xFF);
        result[0x2CF] = (byte)((checksum >> 8) & 0xFF);
        return result;
    }
}
