package entralinked.network.gamespy.message;

import com.fasterxml.jackson.annotation.JsonProperty;

@GameSpyMessage(name = "error")
public record GameSpyErrorMessage(
        @JsonProperty("err")    int errorCode,
        @JsonProperty("errmsg") String errorMessage,
        @JsonProperty("fatal")  int fatal,
        @JsonProperty("id")     int sequenceId) {}
