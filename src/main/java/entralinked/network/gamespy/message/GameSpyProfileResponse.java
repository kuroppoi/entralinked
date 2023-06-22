package entralinked.network.gamespy.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import entralinked.model.user.GameProfile;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@GameSpyMessage(name = "pi")
public record GameSpyProfileResponse(
        @JsonProperty("profileid") int profileId,
        @JsonProperty("firstname") String firstName,
        @JsonProperty("lastname")  String lastName,
        @JsonProperty("aim")       String aimName,
        @JsonProperty("zipcode")   String zipCode,
        @JsonProperty("sig")       String signature,
        @JsonProperty("id")        int sequenceId) {
    
    public GameSpyProfileResponse(GameProfile profile, int sequenceId) {
        this(profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getAimName(), profile.getZipCode(), "signature", sequenceId);
    }
}
