package entralinked.network.gamespy.message;

import com.fasterxml.jackson.annotation.JsonProperty;

@GameSpyMessage(name = "lc", value = "2")
public record GameSpyLoginResponse(
        @JsonProperty("userid")    String userId,
        @JsonProperty("profileid") int profileId,
        @JsonProperty("proof")     String proof,
        @JsonProperty("sesskey")   int sessionKey,
        @JsonProperty("id")        int sequenceId) {}
