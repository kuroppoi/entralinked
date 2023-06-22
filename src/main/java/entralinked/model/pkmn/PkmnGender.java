package entralinked.model.pkmn;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PkmnGender {
    
    @JsonEnumDefaultValue
    MALE,
    FEMALE,
    GENDERLESS;
}
