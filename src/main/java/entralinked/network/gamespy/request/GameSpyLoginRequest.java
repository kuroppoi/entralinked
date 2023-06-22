package entralinked.network.gamespy.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyLoginRequest(
        // Important stuff
        @JsonProperty(value = "response", required = true)  String response,
        @JsonProperty(value = "challenge", required = true) String challenge,
        @JsonProperty(value = "authtoken", required = true) String partnerToken,
        @JsonProperty(value = "id", required = true)        int sequenceId,
        
        // Not-so-important stuff
        @JsonProperty("userid")      String userId,
        @JsonProperty("gamename")    String gameName,
        @JsonProperty("profileid")   int profileId,
        @JsonProperty("namespaceid") int namespaceId,
        @JsonProperty("partnerid")   int partnerId,
        @JsonProperty("productid")   int productId,
        @JsonProperty("sdkrevision") int sdkRevision,
        @JsonProperty("firewall")    int firewall,
        @JsonProperty("port")        int port,
        @JsonProperty("quiet")       int quiet
) implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {
        handler.handleLoginRequest(this);
    }
}
