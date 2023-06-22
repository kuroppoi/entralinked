package entralinked.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameProfileDto(
        @JsonProperty(required = true) int id,
        String firstName,
        String lastName,
        String aimName,
        String zipCode) {
    
    public GameProfileDto(GameProfile profile) {
        this(profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getAimName(), profile.getZipCode());
    }
    
    public GameProfile toProfile() {
        return new GameProfile(id, firstName, lastName, aimName, zipCode);
    }
}
