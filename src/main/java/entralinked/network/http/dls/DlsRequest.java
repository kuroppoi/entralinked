package entralinked.network.http.dls;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DlsRequest(
        // Credentials
        @JsonProperty(value = "userid", required = true) String userId,
        @JsonProperty(value = "passwd", required = true) String password,
        @JsonProperty(value = "macadr", required = true) String macAddress,
        @JsonProperty(value = "token", required = true)  String serviceToken,
        
        // Game info
        @JsonProperty("rhgamecd") String gameCode,

        // Device info
        @JsonProperty("apinfo") String accessPointInfo,
        
        // Request-specific info
        @JsonProperty(value = "action", required = true) String action,
        @JsonProperty("gamecd")   String dlcGameCode,
        @JsonProperty("contents") String dlcName, // action=contents
        @JsonProperty("attr1")    String attr1, // action=list
        @JsonProperty("attr2")    String attr2, // action=list
        @JsonProperty("offset")   int offset, // Start offset in the list
        @JsonProperty("num")      int num) { // Number of entries
    
    @Override
    public String toString() {
        return ("DlsRequest[gameCode=%s, action=%s, dlcGameCode=%s, dlcName=%s, attr1=%s, attr2=%s, offset=%s, num=%s]")
                .formatted(gameCode, action, dlcGameCode, dlcName, attr1, attr2, offset, num);
    }
}
