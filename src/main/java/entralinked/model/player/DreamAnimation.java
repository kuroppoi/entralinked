package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum DreamAnimation {
    
    /**
     * Look around, but stay in the same position.
     */
    @JsonEnumDefaultValue
    LOOK_AROUND("Look around"),
    
    /**
     * Walk around, but never change direction without moving a step in that direction.
     */
    WALK_AROUND("Walk around"),
    
    /**
     * Walk around and occasionally change direction without moving.
     */
    WALK_LOOK_AROUND("Walk and look around"),
    
    /**
     * Only walk up and down.
     */
    WALK_VERTICALLY("Walk up and down"),
    
    /**
     * Only walk left and right.
     */
    WALK_HORIZONTALLY("Walk left and right"),
    
    /**
     * Only walk left and right, and occasionally change direction without moving.
     */
    WALK_LOOK_HORIZONTALLY("Walk left and right and look around"),
    
    /**
     * Continuously spin right.
     */
    SPIN_RIGHT("Spin right"),
    
    /**
     * Continuously spin left.
     */
    SPIN_LEFT("Spin left");
    
    private final String displayName;
    
    private DreamAnimation(String displayName) {
        this.displayName = displayName;
    }
    
    public static DreamAnimation valueOf(int index) {
        return index >= 0 && index < values().length ? values()[index] : null;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
