package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum DreamAnimation {
    
    /**
     * Look around, but stay in the same position.
     */
    @JsonEnumDefaultValue
    LOOK_AROUND,
    
    /**
     * Walk around, but never change direction without moving a step in that direction.
     */
    WALK_AROUND,
    
    /**
     * Walk around and occasionally change direction without moving.
     */
    WALK_LOOK_AROUND,
    
    /**
     * Only walk up and down.
     */
    WALK_VERTICALLY,
    
    /**
     * Only walk left and right.
     */
    WALK_HORIZONTALLY,
    
    /**
     * Only walk left and right, and occasionally change direction without moving.
     */
    WALK_LOOK_HORIZONTALLY,
    
    /**
     * Continuously spin right.
     */
    SPIN_RIGHT,
    
    /**
     * Continuously spin left.
     */
    SPIN_LEFT;
    
    public static DreamAnimation valueOf(int index) {
        return index >= 0 && index < values().length ? values()[index] : null;
    }
}
