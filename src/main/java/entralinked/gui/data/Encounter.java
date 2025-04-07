package entralinked.gui.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.model.pkmn.PkmnGender;

public record Encounter(
        @JsonProperty(required = true) int species,
        @JsonProperty(required = true) List<Integer> moves,
        PkmnGender gender, int formMask, int versionMask) {
    
    @JsonIgnore
    public boolean isGenderLocked() {
        return gender != null;
    }
    
    @JsonIgnore
    public boolean isFormLocked() {
        return formMask != 0;
    }
    
    @JsonIgnore
    public boolean hasForm(int form) {
        int bits = 1 << form;
        return !isFormLocked() || (bits & formMask) == bits;
    }
}
