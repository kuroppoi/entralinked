package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Playtime (
        @JsonProperty(required = true) int hours,
        @JsonProperty(required = true) int minutes,
        @JsonProperty(required = true) int seconds
) {}
