package entralinked.gui.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Country(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name,
        List<Region> regions) {
    
    @JsonProperty
    public boolean hasRegions() {
        return regions != null && !regions.isEmpty();
    }
}
