package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyProfileUpdateRequest(
        @JsonProperty(value = "sesskey", required = true) int sessionKey,
        @JsonProperty("partnerid") int partnerId,
        
        // Lots of possible values here, but these seem to be the only ones sent by generation 5 games.
        @JsonProperty("firstname") String firstName,
        @JsonProperty("lastname")  String lastName,
        @JsonProperty("aim")       String aimName,
        @JsonProperty("zipcode")   String zipCode
) implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {
        if(handler.validateSessionKey(sessionKey)) {
            handler.handleUpdateProfileRequest(this);
        }
    }
    
    @Override
    public String toString() {
        return "GameSpyProfileUpdateRequest[partnerId=%s]".formatted(partnerId);
    }
}
