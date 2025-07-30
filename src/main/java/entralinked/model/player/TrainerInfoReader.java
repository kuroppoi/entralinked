package entralinked.model.player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TrainerInfoReader {
    private static final int BASE_ADVENTURE_START_TIME = 946684800; // Dates are based on 2000/01/01 00:00:00

    private static final byte ZERO_BYTE = (byte) 0x00;
    private static final byte FF_BYTE = (byte) 0xFF;


    private static final Logger logger = LogManager.getLogger();
    private static final ByteBufAllocator bufferAllocator = PooledByteBufAllocator.DEFAULT;

    public static TrainerInfo readTrainerInfo(InputStream inputStream) throws IOException {
        ByteBuf buffer = bufferAllocator.buffer(Offsets.TRAINER_INFO_SIZE);
        buffer.writeBytes(inputStream, Offsets.TRAINER_INFO_SIZE);

        String trainerName = readTrainerName(buffer);
        int trainerId = buffer.getUnsignedShortLE(Offsets.TRAINER_ID_SUB_OFFSET);
        int secretId = buffer.getUnsignedShortLE(Offsets.SECRET_ID_SUB_OFFSET);
        int country = buffer.getUnsignedByte(Offsets.COUNTRY_SUB_OFFSET);
        int region = buffer.getUnsignedByte(Offsets.REGION_SUB_OFFSET);
        TrainerGender gender = TrainerGender.valueOf(buffer.getUnsignedByte(Offsets.GENDER_OFFSET));
        Playtime playtime = readPlaytime(buffer);

        // Try release buffer
        if(!buffer.release()) {
            logger.warn("Buffer was not deallocated!");
        }

        return new TrainerInfo(trainerName, trainerId, secretId, country, region, gender, playtime);
    }

    private static String readTrainerName(ByteBuf buffer) {
        StringBuilder playerName = new StringBuilder();
        for(int i = 0; i < Offsets.TRAINER_NAME_SIZE; i++) {
            byte currByte = buffer.getByte(Offsets.TRAINER_NAME_SUB_OFFSET + i);

            if (currByte == FF_BYTE) {
                break;
            } else if (currByte != ZERO_BYTE) {
                playerName.append((char) currByte);
            }
        }
        return playerName.toString();
    }

    private static Playtime readPlaytime(ByteBuf buffer) {
        return new Playtime(
            buffer.getUnsignedShortLE(Offsets.PLAYTIME_OFFSET),
            buffer.getUnsignedByte(Offsets.PLAYTIME_OFFSET + 2),
            buffer.getUnsignedByte(Offsets.PLAYTIME_OFFSET + 3)
        );
    }

    public static long readAdventureStartTime(InputStream inputStream) throws IOException {
        return readLong(inputStream) + BASE_ADVENTURE_START_TIME;
    }

    public static long readLong(InputStream inputStream) throws IOException {
        ByteBuf buffer = bufferAllocator.buffer(4);
        buffer.writeBytes(inputStream, 4);

        long read = buffer.getUnsignedIntLE(0);

        // Try release buffer
        if(!buffer.release()) {
            logger.warn("Buffer was not deallocated!");
        }

        return read;
    }

    public static List<GymBadge> readGymBadges(InputStream inputStream) throws IOException {
        ByteBuf buffer = bufferAllocator.buffer(1);
        buffer.writeBytes(inputStream, 1);

        List<GymBadge> gymBadges = new ArrayList<>();
        int badgeFlags = buffer.getUnsignedByte(0);
        for(GymBadge badge : GymBadge.values()) {
            if ((badgeFlags & badge.mask) == badge.mask) {
                gymBadges.add(badge);
            }
        }

        // Try release buffer
        if(!buffer.release()) {
            logger.warn("Buffer was not deallocated!");
        }

        return gymBadges;
    }
}
