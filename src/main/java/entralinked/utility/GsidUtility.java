package entralinked.utility;

import java.util.regex.Pattern;

public class GsidUtility {
    
    public static final String GSID_CHARTABLE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final Pattern GSID_PATTERN = Pattern.compile("[A-HJ-NP-Z2-9]{10}");
    
    /**
     * Stringifies the specified numerical Game Sync ID
     * 
     * Black 2 - {@code sub_21B480C} (overlay #199)
     */
    public static String stringifyGameSyncId(int gsid) {
        char[] output = new char[10];
        int index = 0;
        
        // v12 = gsid
        // v5 = sub_204405C(gsid, 4u)
        // v8 = v5 + __CFSHR__(v12, 31) + (v12 >> 31)
        
        // uses unsigned ints for bitshift operations
        long ugsid = gsid;
        long checksum = Crc16.calc(gsid); // + __CFSHR__(v12, 31) + (v12 >> 31); ??
        
        // do while v4 < 10
        for(int i = 0; i < output.length; i++) {
            index = (int)((ugsid & 0x1F) & 0x1FFFF); // chartable string is unicode, so normally multiplies by 2
            ugsid = (ugsid >> 5) | (checksum << 27);
            checksum >>= 5;
            output[i] = GSID_CHARTABLE.charAt(index); // sub_2048734(v4, chartable + index)
        }
        
        return new String(output);
    }
    
    public static boolean isValidGameSyncId(String gsid) {
        return GSID_PATTERN.matcher(gsid).matches();
    }
}
