package entralinked.model.pkmn;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PkmnNature {
    
    @JsonEnumDefaultValue
    HARDY("Hardy"),
    LONELY("Lonely"),
    BRAVE("Brave"),
    ADAMANT("Adamant"),
    NAUGHTY("Naughty"),
    BOLD("Bold"),
    DOCILE("Docile"),
    RELAXED("Relaxed"),
    IMPISH("Impish"),
    LAX("Lax"),
    TIMID("Timid"),
    HASTY("Hasty"),
    SERIOUS("Serious"),
    JOLLY("Jolly"),
    NAIVE("Naive"),
    MODEST("Modest"),
    MILD("Mild"),
    QUIET("Quiet"),
    BASHFUL("Bashful"),
    RASH("Rash"),
    CALM("Calm"),
    GENTLE("Gentle"),
    SASSY("Sassy"),
    CAREFUL("Careful"),
    QUIRKY("Quirky");
    
    private final String displayName;
    
    private PkmnNature(String displayName) {
        this.displayName = displayName;
    }
    
    public static PkmnNature valueOf(int index) {
        return index >= 0 && index < values().length ? values()[index] : null;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
}
