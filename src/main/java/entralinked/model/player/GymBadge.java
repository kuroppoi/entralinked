package entralinked.model.player;

public enum GymBadge {
    BADGE_ONE(0b00000001),
    BADGE_TWO(0b00000010),
    BADGE_THREE(0b00000100),
    BADGE_FOUR(0b00001000),
    BADGE_FIVE(0b00010000),
    BADGE_SIX(0b00100000),
    BADGE_SEVEN(0b01000000),
    BADGE_EIGHT(0b10000000);

    public final int mask;

    GymBadge(int mask) {
        this.mask = mask;
    }
}
