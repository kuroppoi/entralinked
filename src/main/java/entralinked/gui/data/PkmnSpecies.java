package entralinked.gui.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.model.pkmn.PkmnGender;

public record PkmnSpecies(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name,
        boolean downloadable, boolean hasFemaleSprite, PkmnGender gender, PkmnForm[] forms) {
    
    @JsonIgnore
    public boolean isSingleGender() {
        return gender != null;
    }
    
    @JsonIgnore
    public boolean hasForms() {
        return forms != null && forms.length > 0;
    }
    
    @JsonIgnore
    public List<PkmnGender> getGenders() {
        return isSingleGender() ? List.of(gender) : List.of(PkmnGender.MALE, PkmnGender.FEMALE);
    }
}
