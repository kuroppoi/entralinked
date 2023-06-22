package entralinked.network.http.pgl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import entralinked.GameVersion;
import entralinked.serialization.GsidDeserializer;

public record PglRequest(
        @JsonProperty(value = "gsid", required = true) @JsonDeserialize(using = GsidDeserializer.class) String gameSyncId,
        @JsonProperty(value = "p", required = true)        String type,
        @JsonProperty(value = "rom", required = true)      int romCode,
        @JsonProperty(value = "langcode", required = true) int languageCode,
        @JsonProperty(value = "dreamw", required = true)   int dreamWorld, // Always 1, but what is it for?
        @JsonProperty(value = "tok", required = true)      String token) {
    
    public GameVersion gameVersion() {
        return GameVersion.lookup(romCode(), languageCode());
    }
}
