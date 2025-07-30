package entralinked.model.player;

import entralinked.GameVersion;

public class Offsets {
    public static final int TRAINER_INFO = 0x19400;
    public static final int TRAINER_INFO_SIZE = 0x67;
    public static final int TRAINER_NAME_SUB_OFFSET = 0x4;
    public static final byte TRAINER_NAME_SIZE = 0x10;
    public static final int TRAINER_ID_SUB_OFFSET = 0x14;
    public static final int SECRET_ID_SUB_OFFSET = 0x16;
    public static final int COUNTRY_SUB_OFFSET = 0x1C;
    public static final int REGION_SUB_OFFSET = 0x1D;
    public static final int GENDER_OFFSET = 0x21;
    public static final int PLAYTIME_OFFSET = 0x24;

    public static final int DREAM_WORLD_INFO = 0x1D300;
    public static final int POKEMON_INFO_SUB_OFFSET = 0x8;

    public static final int ADVENTURE_START_TIME_OFFSET = 0x1D900;
    public static final int ADVENTURE_START_TIME_SUB_OFFSET = 0x34;

    public static final int MONEY_AND_BADGES_VERSION_1 = 0x21200;
    public static final int MONEY_AND_BADGES_VERSION_2 = 0x21100;

    public static int getMoneyAndBadges(GameVersion gameVersion) {
        return gameVersion.isVersion2() ? MONEY_AND_BADGES_VERSION_2 : MONEY_AND_BADGES_VERSION_1;
    }
}
