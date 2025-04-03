package entralinked.gui.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamWorldArea(
        @JsonProperty(required = true) List<Encounter> encounters,
        @JsonProperty(required = true) List<Integer> items) {}
