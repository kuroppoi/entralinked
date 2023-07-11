package entralinked.model.pkmn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record containing information about a Pokémon
 */
public record PkmnInfo(
        @JsonProperty(required = true)  String nickname,
        @JsonProperty(required = true)  String trainerName,
        @JsonProperty(required = true)  PkmnNature nature,
        @JsonProperty(required = true)  PkmnGender gender,
        @JsonProperty(required = true)  int species,
        @JsonProperty(required = true)  int personality,
        @JsonProperty(required = true)  int trainerId,
        @JsonProperty(required = true)  int trainerSecretId,
        @JsonProperty(required = true)  int level,
        @JsonProperty(required = false) int form,
        @JsonProperty(required = false) int ability,
        @JsonProperty(required = false) int heldItem) {
    
    @JsonIgnore
    public boolean isShiny() {
        int p1 = (personality >> 16) & 0xFFFF;
        int p2 = personality & 0xFFFF;
        return (trainerId ^ trainerSecretId ^ p1 ^ p2) < 8;
    }
}
