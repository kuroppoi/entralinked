package entralinked.utility;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.Arrays;

/**
 * Haphazardly thrown-together utility for generating DLS Mystery Gift data & removing version/language locks.
 */
public class MysteryGiftUtility {
    
    public static final String DPPGS_CHARTABLE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿŒœŞşªºááá$¡¿!?,.…·/‘'“”„«»()♂♀+-*#=&~:;♠♣♥♦★◉○□△◇@♪%ááááááááááá ";
    
    private static byte[] encodeDPPGS(String string) {
        int length = string.length();
        byte[] bytes = new byte[length * 2];
        
        for(int i = 0; i < length; i++) {
            char character = string.charAt(i);
            int encoded = character == '\n' ? 0xE000 : DPPGS_CHARTABLE.indexOf(character) + 0x121;
            
            if(encoded == 0x120) {
                encoded = 0x1DE;
            }
            
            bytes[i * 2] = (byte)(encoded & 0xFF);
            bytes[i * 2 + 1] = (byte)((encoded >> 8) & 0xFF);
        }
        
        return bytes;
    }
    
    public static byte[] createUniversalGiftData4(byte[] bytes) {
        return createUniversalGiftData4(bytes, "Here's your Mystery Gift.\nEnjoy!");
    }
    
    public static byte[] createUniversalGiftData4(byte[] bytes, String title) {
        // Check data size
        if(bytes.length > 936) {
            throw new IllegalArgumentException("Data too large: %s".formatted(bytes.length));
        }
        
        byte[] result = new byte[936];
        
        if(bytes.length <= 856) {
            // Create gift title data
            System.arraycopy(bytes, 0x00, result, 0x50, bytes.length);
            Arrays.fill(result, 0x00, 0x48, (byte)0xFF);
            byte[] titleBytes = encodeDPPGS(title);
            System.arraycopy(titleBytes, 0, result, 0, Math.min(0x48, titleBytes.length));
            
            // Wonder card index (prevents duplicate redemptions)
            int id = Crc16.calc(result, 0x00, 0x3A8); // Let's just use the checksum for now
            result[0x4C] = (byte)(id & 0xFF);
            result[0x4D] = (byte)((id >> 8) & 0xFF);
            
            // Wonder card present flag?
            result[0x4E] = (byte)(bytes.length == 0x358 ? 0x0D : 0x00);
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
            System.arraycopy(descriptionBytes, 0, result, 0xD0, Math.min(0x1FA, descriptionBytes.length));
        }
        
        // Recalculate checksum
        int checksum = Crc16.calc(result, 0, 0x2CE);
        result[0x2CE] = (byte)(checksum & 0xFF);
        result[0x2CF] = (byte)((checksum >> 8) & 0xFF);
        return result;
    }
}
