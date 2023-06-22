package entralinked.network.http.nas;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NasServiceLocationResponse(   
        @JsonProperty("statusdata")   char serviceStatus,  // If 0x59 ('Y') sets flag to true. Game doesn't care when connecting to GL server, though.
        @JsonProperty("svchost")      String serviceHost,
        @JsonProperty("servicetoken") String serviceToken
) implements NasResponse {
    
    public NasServiceLocationResponse(boolean serviceAvailable, String serviceHost, String serviceToken) {
        this(serviceAvailable ? 'Y' : 'N', serviceHost, serviceToken);
    }
}
