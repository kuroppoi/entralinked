package entralinked.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LittleEndianStreamTest {
    
    @Test
    @DisplayName("Test if LEOutputStream and LEInputStream produce correct values")
    void testLittleEndianStreams() throws IOException {
        short shortValue = 0x6A84;
        int intValue = 0xF827EC80;
        long longValue = 0x948EC1AB3F2C88L;
        
        // Test writing
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        LEOutputStream outputStream = new LEOutputStream(byteOutputStream);        
        outputStream.writeShort(shortValue);
        outputStream.writeInt(intValue);
        outputStream.writeLong(longValue);
        
        // Test reading
        LEInputStream inputStream = new LEInputStream(new ByteArrayInputStream(byteOutputStream.toByteArray()));
        assertEquals(shortValue, inputStream.readShort());
        assertEquals(intValue, inputStream.readInt());
        assertEquals(longValue, inputStream.readLong());
    }
}
