package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.model.pkmn.PkmnGender;

public record DreamEncounter(
        @JsonProperty(required = true)  int species,
        @JsonProperty(required = true)  int move,
        @JsonProperty(required = true)  int form,
        @JsonProperty(required = false) PkmnGender gender, // Can't require cuz it's new
        @JsonProperty(required = true)  DreamAnimation animation) {
    
    @Override
    public PkmnGender gender() {
        return gender == null ? PkmnGender.GENDERLESS : gender; // Default to genderless (random gender) if not present
    }
}
