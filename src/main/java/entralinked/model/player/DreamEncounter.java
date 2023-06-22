package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamEncounter(
        @JsonProperty(required = true) int species,
        @JsonProperty(required = true) int move,
        @JsonProperty(required = true) int form,
        @JsonProperty(required = true) DreamAnimation animation) {}
