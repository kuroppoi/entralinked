package entralinked;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Configuration(
        String hostName,
        String hostAddress,
        boolean clearPlayerDreamInfoOnWake,
        boolean allowOverwritingPlayerDreamInfo,
        boolean allowPlayerGameVersionMismatch,
        boolean allowWfcRegistrationThroughLogin) {
    
    public static final Configuration DEFAULT = new Configuration("local", null, true, false, false, true);
}
