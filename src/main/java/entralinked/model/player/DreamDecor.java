package entralinked.model.player;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamDecor(
        @JsonProperty(required = true) int id,
        @JsonProperty(required = true) String name) {
    
    // TODO names probably differed per language
    public static final List<DreamDecor> DEFAULT_DECOR = List.of(
        new DreamDecor(1, "Design Table"),
        new DreamDecor(2, "Design Stool"),
        new DreamDecor(3, "Flower Vase"),
        new DreamDecor(4, "Cuddle Rug"),
        new DreamDecor(6, "Wall Poster")
    );
}
