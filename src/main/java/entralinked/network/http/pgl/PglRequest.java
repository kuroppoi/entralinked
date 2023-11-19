package entralinked.network.http.pgl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import entralinked.GameVersion;
import entralinked.serialization.GsidDeserializer;

public record PglRequest(
        @JsonProperty(value = "gsid") @JsonDeserialize(using = GsidDeserializer.class) String gameSyncId,
        @JsonProperty(value = "p", required = true)   String type,
        @JsonProperty(value = "tok", required = true) String token,
        @JsonProperty(value = "rom")      int romCode,
        @JsonProperty(value = "langcode") int languageCode,
        @JsonProperty(value = "dreamw")   int dreamWorld) { // Always 1, but what is it for?
    
    public GameVersion gameVersion() {
        return GameVersion.lookup(romCode(), languageCode());
    }
    
    @Override
    public String toString() {
        // Exlude token
        return "PglRequest[gameSyncId=%s, type=%s, romCode=%s, languageCode=%s, dreamWorld=%s]"
                .formatted(gameSyncId, type, romCode, languageCode, dreamWorld);
    }
}
