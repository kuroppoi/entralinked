package entralinked.network.http.nas;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NasLoginResponse(
        @JsonProperty("locator")   String partner,
        @JsonProperty("token")     String partnerToken,
        @JsonProperty("challenge") String partnerChallenge
) implements NasResponse {}
