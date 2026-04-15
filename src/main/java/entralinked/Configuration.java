package entralinked;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Configuration(
        String hostName,
        boolean clearPlayerDreamInfoOnWake,
        boolean allowWfcRegistrationThroughLogin) {
    
    public static final Configuration DEFAULT = new Configuration("local", true, true);
}
