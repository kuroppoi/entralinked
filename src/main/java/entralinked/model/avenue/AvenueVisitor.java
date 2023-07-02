package entralinked.model.avenue;

import com.fasterxml.jackson.annotation.JsonProperty;

import entralinked.GameVersion;

public record AvenueVisitor(
        @JsonProperty(required = true) String name,
        @JsonProperty(required = true) AvenueVisitorType type,
        @JsonProperty(required = true) AvenueShopType shopType,
        @JsonProperty(required = true) GameVersion gameVersion,
        @JsonProperty(required = true) int countryCode,
        @JsonProperty(required = true) int stateProvinceCode,
        @JsonProperty(required = true) int personality,
        @JsonProperty(required = true) int dreamerSpecies) {}
