package entralinked.utility;

import java.util.regex.Pattern;

/**
 * Game Sync ID generation process:
 *
 * Let's take example PID "1231499195".
 * Start by storing both the PID and its checksum (35497) in working variable "ugsid".
 * We do this by simply shifting the checksum 32 bits to the left: ugsid = pid | (checksum << 32) = 0x8AA949672FBB
 * 
 * Calculating each character is pretty straightforward.
 * We just use the 5 least significant bits of ugsid as the index for the character table.
 * Character 1: 0x8AA949672FBB & 0x1F = 27 = '5'
 * 
 * After each character, we shift ugsid 5 bits to the right. Since ugsid contains 48 bits of data,
 * taking the 5 least significant bits each time gives us enough indexes for (if we round up) exactly 10 characters.
 * If we take a look at the full value of ugsid (0x8AA949672FBB) in binary and split it into sections of 5 bits,
 * we'll actually already be able to see the entire Game Sync ID in reverse:
 * 
 * Character:        'E'   'L'   'X'   'F'   'E'   'Y'   'Q'   'M'   '7'   '5'
 * Chartable index:   4    10    21     5     4    22    14    11    29    27
 * Binary:          XX100 01010 10101 00101 00100 10110 01110 01011 11101 11011
 *                                           
 * Adding all of the characters together gets us the Game Sync ID "57MQYEFXLE".
 * 
 * Reversing this process to retrieve the PID and checksum is very straightforward.
 * Simply go through each character, find the index of it in the character table & left shift the total value 5 bits each time.
 * If we do this with the Game Sync ID we just created, then it should give us back the value of ugsid: 0x8AA949672FBB
 * To then retrieve the PID, simply do: ugsid & 0xFFFFFFFF = 1231499195
 * To retrieve the checksum, simply do: (ugsid >> 32) & 0xFFFF = 35497
 * We can then validate the Game Sync ID if we want to by comparing the checksums.
 */
public class GsidUtility {
    
    public static final String GSID_CHARTABLE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final Pattern GSID_PATTERN = Pattern.compile("[A-HJ-NP-Z2-9]{10}");
    
    /**
     * Stringifies the specified numerical Game Sync ID.
     * 
     * Black 2 - {@code sub_21B480C} (overlay #199)
     */
    public static String stringifyGameSyncId(int gsid) {
        char[] output = new char[10];
        long checksum = Crc16.calc(gsid);
        long ugsid = gsid | (checksum << 32);
        
        for(int i = 0; i < output.length; i++) {
            int index = (int)((ugsid >> (5 * i)) & 0x1F);
            output[i] = GSID_CHARTABLE.charAt(index);
        }
        
        return new String(output);
    }
    
    /**
     * Determines if a Game Sync ID is valid by checking its length, characters & checksum.
     * 
     * @return {@code true} if the Game Sync ID is valid, otherwise {@code false}.
     */
    public static boolean isValidGameSyncId(String gsid) {
        if(gsid == null) {
            return false;
        }
        
        int length = gsid.length();
        long ugsid = 0;
        
        if(length != 10) {
            return false;
        }
                
        for(int i = 0; i < length; i++) {
            int index = GSID_CHARTABLE.indexOf(gsid.charAt(i));
            
            if(index == -1) {
                return false;
            }
            
            ugsid |= (long)index << (5 * i);
        }
        
        int output = (int)(ugsid & 0xFFFFFFFF);
        int checksum = (int)((ugsid >> 32) & 0xFFFF);
        return output >= 0 && Crc16.calc(output) == checksum;
    }
}
