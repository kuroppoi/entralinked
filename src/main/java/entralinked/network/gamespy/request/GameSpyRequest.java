package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import entralinked.network.gamespy.GameSpyHandler;

@JsonSubTypes({
    @Type(name = "ka", value = GameSpyKeepAliveRequest.class),
    @Type(name = "login", value = GameSpyLoginRequest.class),
    @Type(name = "logout", value = GameSpyLogoutRequest.class),
    @Type(name = "status", value = GameSpyStatusRequest.class),
    @Type(name = "getprofile", value = GameSpyProfileRequest.class),
    @Type(name = "updatepro", value = GameSpyProfileUpdateRequest.class),
})
public interface GameSpyRequest {
    
    public void process(GameSpyHandler handler);
}
