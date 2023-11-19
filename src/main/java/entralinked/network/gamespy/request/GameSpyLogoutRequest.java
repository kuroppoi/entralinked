package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyLogoutRequest(
        @JsonProperty("sesskey") int sessionKey
) implements GameSpyRequest {
    
    @Override
    public void process(GameSpyHandler handler) {
        if(handler.validateSessionKey(sessionKey)) {
            handler.handleLogout();
        }
    }
    
    @Override
    public String toString() {
        // Exlude session key
        return "GameSpyLogoutRequest[]";
    }
}
