package entralinked.model.pkmn;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PkmnGender {
    
    @JsonEnumDefaultValue
    MALE("Male"),
    FEMALE("Female"),
    GENDERLESS("Genderless");
    
    private final String displayName;
    
    private PkmnGender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
