package entralinked;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Configuration(
        @JsonProperty(required = true) String hostName,
        @JsonProperty(required = true) boolean clearPlayerDreamInfoOnWake,
        @JsonProperty(required = true) boolean allowOverwritingPlayerDreamInfo,
        @JsonProperty(required = true) boolean allowWfcRegistrationThroughLogin) {
    
    public static final Configuration DEFAULT = new Configuration("local", true, false, true);
}
