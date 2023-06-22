package entralinked.model.pkmn;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PkmnNature {
    
    @JsonEnumDefaultValue
    HARDY,
    LONELY,
    BRAVE,
    ADAMANT,
    NAUGHTY,
    BOLD,
    DOCILE,
    RELAXED,
    IMPISH,
    LAX,
    TIMID,
    HASTY,
    SERIOUS,
    JOLLY,
    NAIVE,
    MODEST,
    MILD,
    QUIET,
    BASHFUL,
    RASH,
    CALM,
    GENTLE,
    SASSY,
    CAREFUL,
    QUIRKY;
    
    public static PkmnNature valueOf(int index) {
        return index >= 0 && index < values().length ? values()[index] : null;
    }
}
