package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyLogoutRequest(
        @JsonProperty("sesskey") int sessionKey
) implements GameSpyRequest {
    
    @Override
    public void process(GameSpyHandler handler) {
        handler.destroySessionKey(sessionKey);
    }
}
