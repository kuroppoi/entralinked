package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamItem(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) int quantity) {}
