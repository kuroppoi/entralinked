package entralinked;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Configuration(
        String hostName,
        boolean clearPlayerDreamInfoOnWake,
        boolean allowOverwritingPlayerDreamInfo,
        boolean allowPlayerGameVersionMismatch,
        boolean allowWfcRegistrationThroughLogin,
        boolean logSensitiveInfo) {
    
    public static final Configuration DEFAULT = new Configuration("local", true, false, false, true, false);
}
