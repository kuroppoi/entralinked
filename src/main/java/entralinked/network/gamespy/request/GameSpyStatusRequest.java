package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyStatusRequest(
        @JsonProperty(value = "sesskey", required = true) int sessionKey,
        @JsonProperty("statstring") String statusString,
        @JsonProperty("locstring")  String locationString
) implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {
        handler.validateSessionKey(sessionKey);
    }
    
    @Override
    public String toString() {
        return "GameSpyStatusRequest[]";
    }
}
