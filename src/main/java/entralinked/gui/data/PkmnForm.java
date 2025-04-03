package entralinked.gui.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PkmnForm(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name) {}
