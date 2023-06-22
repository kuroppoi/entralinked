package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PlayerStatus {
    
    /**
     * No Pokémon is currently tucked in.
     */
    @JsonEnumDefaultValue
    AWAKE,
    
    /**
     * The tucked in Pokémon is asleep, but is not dreaming yet.
     */
    SLEEPING,
    
    /**
     * The tucked in Pokémon is dreaming - waking it up is not allowed.
     */
    DREAMING,
    
    /**
     * The dreamer is ready to be woken up.
     */
    WAKE_READY,
}
