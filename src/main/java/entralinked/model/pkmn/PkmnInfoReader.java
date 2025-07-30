package entralinked.model.pkmn;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * Utility class for reading binary Pokémon data.
 */
public class PkmnInfoReader {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ByteBufAllocator bufferAllocator = PooledByteBufAllocator.DEFAULT;
    private static final byte[] blockShuffleTable = {
            0, 1, 2, 3,     0, 1, 3, 2,     0, 2, 1, 3,     0, 3, 1, 2,
            0, 2, 3, 1,     0, 3, 2, 1,     1, 0, 2, 3,     1, 0, 3, 2,
            2, 0, 1, 3,     3, 0, 1, 2,     2, 0, 3, 1,     3, 0, 2, 1,
            1, 2, 0, 3,     1, 3, 0, 2,     2, 1, 0, 3,     3, 1, 0, 2,
            2, 3, 0, 1,     3, 2, 0, 1,     1, 2, 3, 0,     1, 3, 2, 0,
            2, 1, 3, 0,     3, 1, 2, 0,     2, 3, 1, 0,     3, 2, 1, 0
    };
    
    public static PkmnInfo readPokeInfo(InputStream inputStream) throws IOException {
        ByteBuf buffer = bufferAllocator.buffer(236); // Allocate buffer
        buffer.writeBytes(inputStream, 236); // Read data from input stream into buffer
        
        // Read header info
        int personality = buffer.readIntLE();
        buffer.skipBytes(2);
        int checksum = buffer.readShortLE() & 0x0000FFFF;
        
        // Decrypt data
        decryptData(buffer, 8, 128, checksum);
        decryptData(buffer, 136, 100, personality);
        
        // Unshuffle blocks
        ByteBuf shuffleBuffer = bufferAllocator.buffer(128); // Allocate shuffle buffer
        int shift = ((personality & 0x3E000) >> 0xD) % 24;
        
        for(int i = 0; i < 4; i++) {
            int fromIndex = blockShuffleTable[i + shift * 4] * 32;
            int toIndex = i * 32;
            shuffleBuffer.setBytes(toIndex, buffer, 8 + fromIndex, 32);
        }
        
        buffer.setBytes(8, shuffleBuffer, 0, 128);
        
        // Try release shuffle buffer
        if(!shuffleBuffer.release()) {
            logger.warn("Buffer was not deallocated!");
        }
        
        // Read Pokémon data
        int species = buffer.getShortLE(8);
        int heldItem = buffer.getShortLE(10) & 0xFFFF;
        int trainerId = buffer.getShortLE(12) & 0xFFFF;
        int trainerSecretId = buffer.getShortLE(14) & 0xFFFF;
        int level = buffer.getByte(140);
        int ability = buffer.getByte(21) & 0xFF;
        int form = (buffer.getByte(64) >> 3) & 0x1F;
        boolean genderless = ((buffer.getByte(64) >> 2) & 1) == 1;
        boolean female = ((buffer.getByte(64) >> 1) & 1) == 1;
        int natureByte = buffer.getByte(65);
        PkmnGender gender = genderless ? PkmnGender.GENDERLESS : female ? PkmnGender.FEMALE : PkmnGender.MALE;
        PkmnNature nature = PkmnNature.valueOf(natureByte);
        String nickname = getString(buffer, 72, 20);
        String trainerName = getString(buffer, 104, 14);
        
        // Try release buffer
        if(!buffer.release()) {
            logger.warn("Buffer was not deallocated!");
        }

        // Loosely verify data
        if(species < 1 || species > 649) throw new IOException(String.format("Invalid species: %d", species));
        if(heldItem < 0 || heldItem > 638) throw new IOException(String.format("Invalid held item: %d", heldItem));
        if(ability < 1 || ability > 164) throw new IOException(String.format("Invalid ability: %d", ability));
        if(level < 1 || level > 100) throw new IOException(String.format("Level is out of range: %d", level));
        if(nature == null) throw new IOException(String.format("Invalid nature: %d", natureByte));
        
        // Create record
        return new PkmnInfo(nickname, trainerName, nature, gender, species, personality,
                trainerId, trainerSecretId, level, form, ability, heldItem);
    }
    
    private static void decryptData(ByteBuf buffer, int offset, int length, int seed) throws IOException {
        if(length % 2 != 0) {
            throw new IOException("Length must be multiple of 2");
        }
        
        int tempSeed = seed;
        
        for(int i = 0; i < length / 2; i++) {
            int index = offset + i * 2;
            short word = buffer.getShortLE(index);
            tempSeed = 0x41C64E6D * tempSeed + 0x6073;
            buffer.setShortLE(index, (short)(word ^ (tempSeed >> 16)));
        }
    }
    
    private static String getString(ByteBuf buffer, int offset, int length) {
        char[] charBuffer = new char[length];
        int read = 0;
        
        for(int i = 0; i < charBuffer.length; i++) {
            int c = buffer.getShortLE(offset + i * 2) & 0xFFFF;
            
            if(c == 0 || c == 65535) {
                break; // Doubt 65535 is a legitimate character..
            }
            
            charBuffer[i] = (char)c;
            read++;
        }
        
        return new String(charBuffer, 0, read);
    }
}
