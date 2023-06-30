package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamDecor(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name) {}
