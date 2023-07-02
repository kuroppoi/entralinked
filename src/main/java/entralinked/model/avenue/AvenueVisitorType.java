package entralinked.model.avenue;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvenueVisitorType {
    
    // 0
    @JsonEnumDefaultValue
    YOUNGSTER(0),
    LASS(0, true),
    
    // 1
    ACE_TRAINER_MALE(1),
    ACE_TRAINER_FEMALE(1, true),
    
    // 2
    RANGER_MALE(2),
    RANGER_FEMALE(2, true),
    
    // 3
    BREEDER_MALE(3),
    BREEDER_FEMALE(3, true),
    
    // 4
    SCIENTIST_MALE(4),
    SCIENTIST_FEMALE(4, true),
    
    // 5
    HIKER(5),
    PARASOL_LADY(5, true),
    
    // 6
    ROUGHNECK(6),
    NURSE(6, true),
    
    // 7
    PRESCHOOLER_MALE(7),
    PRESCHOOLER_FEMALE(7, true);
    
    private final int clientId;
    private final boolean female;
    
    private AvenueVisitorType(int clientId, boolean female) {
        this.clientId = clientId;
        this.female = female;
    }
    
    private AvenueVisitorType(int clientId) {
        this(clientId, false);
    }
    
    public int getClientId() {
        return clientId;
    }
    
    public boolean isFemale() {
        return female;
    }
}
