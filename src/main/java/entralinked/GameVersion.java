package entralinked;

import java.util.HashMap;
import java.util.Map;

public enum GameVersion {
        
    // ==================================
    // Black Version & White Version
    // ==================================
    
    BLACK_JAPANESE(21, 1, "IRBJ", "ブラック"),
    BLACK_ENGLISH(21, 2, "IRBO", "Black Version"),
    BLACK_FRENCH(21, 3, "IRBF", "Version Noire"),
    BLACK_ITALIAN(21, 4, "IRBI", "Versione Nera"),
    BLACK_GERMAN(21, 5, "IRBD", "Schwarze Edition"),
    BLACK_SPANISH(21, 7, "IRBS", "Edicion Negra"),
    BLACK_KOREAN(21, 8, "IRBK", "블랙"),
    
    WHITE_JAPANESE(20, 1, "IRAJ", "ホワイト"),
    WHITE_ENGLISH(20, 2, "IRAO", "White Version"),
    WHITE_FRENCH(20, 3, "IRAF", "Version Blanche"),
    WHITE_ITALIAN(20, 4, "IRAI", "Versione Bianca"),
    WHITE_GERMAN(20, 5, "IRAD", "Weisse Edition"),
    WHITE_SPANISH(20, 7, "IRAS", "Edicion Blanca"),
    WHITE_KOREAN(20, 8, "IRAK", "화이트"),
    
    // ==================================
    // Black Version 2 & White Version 2
    // ==================================
    
    BLACK_2_JAPANESE(23, 1, "IREJ", "ブラック2", true),
    BLACK_2_ENGLISH(23, 2, "IREO", "Black Version 2", true),
    BLACK_2_FRENCH(23, 3, "IREF", "Version Noire 2", true),
    BLACK_2_ITALIAN(23, 4, "IREI", "Versione Nera 2", true),
    BLACK_2_GERMAN(23, 5, "IRED", "Schwarze Edition 2", true),
    BLACK_2_SPANISH(23, 7, "IRES", "Edicion Negra 2", true),
    BLACK_2_KOREAN(23, 8, "IREK", "블랙2", true),
    
    WHITE_2_JAPANESE(22, 1, "IRDJ", "ホワイト2", true),
    WHITE_2_ENGLISH(22, 2, "IRDO", "White Version 2", true),
    WHITE_2_FRENCH(22, 3, "IRDF", "Version Blanche 2", true),
    WHITE_2_ITALIAN(22, 4, "IRDI", "Versione Bianca 2", true),
    WHITE_2_GERMAN(22, 5, "IRDD", "Weisse Edition 2", true),
    WHITE_2_SPANISH(22, 7, "IRDS", "Edicion Blanca 2", true),
    WHITE_2_KOREAN(22, 8, "IRDK", "화이트2", true);
    
    // Lookup maps
    private static final Map<String, GameVersion> mapBySerial = new HashMap<>();
    private static final Map<Integer, GameVersion> mapByCodes = new HashMap<>();
    
    static {
        for(GameVersion version : values()) {
            mapBySerial.put(version.getSerial(), version);
            mapByCodes.put(version.getRomCode() << version.getLanguageCode(), version);
        }
    }
    
    private final int romCode;
    private final int languageCode; // Values are not tested
    private final String serial;
    private final String displayName;
    private final boolean isVersion2;
    
    private GameVersion(int romCode, int languageCode, String serial, String displayName, boolean isVersion2) {
        this.romCode = romCode;
        this.languageCode = languageCode;
        this.serial = serial;
        this.displayName = displayName;
        this.isVersion2 = isVersion2;
    }
    
    private GameVersion(int romCode, int languageCode, String serial, String displayName) {
        this(romCode, languageCode, serial, displayName, false);
    }
    
    public static GameVersion lookup(String serial) {
        return mapBySerial.get(serial);
    }
    
    public static GameVersion lookup(int romCode, int languageCode) {
        return mapByCodes.get(romCode << languageCode);
    }
    
    public int getRomCode() {
        return romCode;
    }
    
    public int getLanguageCode() {
        return languageCode;
    }
    
    public String getSerial() {
        return serial;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isVersion2() {
        return isVersion2;
    }
}
