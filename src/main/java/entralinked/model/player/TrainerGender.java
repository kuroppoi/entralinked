package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum TrainerGender {
    @JsonEnumDefaultValue
    MALE,
    FEMALE;

    public static TrainerGender valueOf(int gender) {
        return switch (gender) {
            case 0 -> MALE;
            case 1 -> FEMALE;
            default -> MALE;
        };
    }
}
