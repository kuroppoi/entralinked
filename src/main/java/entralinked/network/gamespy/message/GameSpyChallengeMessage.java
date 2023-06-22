package entralinked.network.gamespy.message;

import com.fasterxml.jackson.annotation.JsonProperty;

@GameSpyMessage(name = "lc", value = "1")
public record GameSpyChallengeMessage(
        @JsonProperty("challenge") String challenge,
        @JsonProperty("id")        int sequenceId) {}
