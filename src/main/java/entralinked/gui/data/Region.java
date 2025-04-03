package entralinked.gui.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Region(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name) {}
