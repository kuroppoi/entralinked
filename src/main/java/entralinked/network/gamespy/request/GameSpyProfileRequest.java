package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyProfileRequest(
        @JsonProperty(value = "sesskey", required = true) int sessionKey,
        @JsonProperty(value = "id", required = true)      int sequenceId,
        @JsonProperty("profileid") int profileId
) implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {
        if(handler.validateSessionKey(sessionKey, sequenceId)) {
            handler.handleProfileRequest(this);
        }
    }
    
    @Override
    public String toString() {
        // Exlude session key
        return "GameSpyProfileRequest[sequenceId=%s, profileId=%s]".formatted(sequenceId, profileId);
    }
}
