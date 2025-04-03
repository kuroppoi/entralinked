package entralinked.model.avenue;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvenueVisitorType {
    
    // 0
    @JsonEnumDefaultValue
    YOUNGSTER("Youngster", 0),
    LASS("Lass", 0, true),
    
    // 1
    ACE_TRAINER_MALE("Ace Trainer♂", 1),
    ACE_TRAINER_FEMALE("Ace Trainer♀", 1, true),
    
    // 2
    RANGER_MALE("Pokémon Ranger♂", 2),
    RANGER_FEMALE("Pokémon Ranger♀", 2, true),
    
    // 3
    BREEDER_MALE("Pokémon Breeder♂", 3),
    BREEDER_FEMALE("Pokémon Breeder♀", 3, true),
    
    // 4
    SCIENTIST_MALE("Scientist♂", 4),
    SCIENTIST_FEMALE("Scientist♀", 4, true),
    
    // 5
    HIKER("Hiker♂", 5),
    PARASOL_LADY("Parasol Lady", 5, true),
    
    // 6
    ROUGHNECK("Roughneck", 6),
    NURSE("Nurse", 6, true),
    
    // 7
    PRESCHOOLER_MALE("Preschooler♂", 7),
    PRESCHOOLER_FEMALE("Preschooler♀", 7, true);
    
    private final String displayName;
    private final int clientId;
    private final boolean female;
    
    private AvenueVisitorType(String displayName, int clientId, boolean female) {
        this.displayName = displayName;
        this.clientId = clientId;
        this.female = female;
    }
    
    private AvenueVisitorType(String displayName, int clientId) {
        this(displayName, clientId, false);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getClientId() {
        return clientId;
    }
    
    public boolean isFemale() {
        return female;
    }
}
