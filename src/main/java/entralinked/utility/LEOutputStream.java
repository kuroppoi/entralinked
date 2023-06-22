package entralinked.utility;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Like {@link DataOutputStream}, but for little endian.
 * It's comical how this is not in base Java yet.
 */
public class LEOutputStream extends FilterOutputStream {
    
    protected final byte[] buffer = new byte[8];
    
    public LEOutputStream(OutputStream outputStream) {
        super(outputStream);
    }
    
    public void writeBytes(int value, int amount) throws IOException {
        for(int i = 0; i < amount; i++) {
            out.write(value);
        }
    }
    
    public void writeShort(int value) throws IOException {
        buffer[0] = (byte)(value & 0xFF);
        buffer[1] = (byte)((value >> 8) & 0xFF);
        out.write(buffer, 0, 2);
    }
    
    public void writeInt(int value) throws IOException {
        buffer[0] = (byte)(value & 0xFF);
        buffer[1] = (byte)((value >> 8) & 0xFF);
        buffer[2] = (byte)((value >> 16) & 0xFF);
        buffer[3] = (byte)((value >> 24) & 0xFF);
        out.write(buffer, 0, 4);
    }
    
    public void writeFloat(float value) throws IOException {
        writeInt(Float.floatToIntBits(value));
    }
    
    public void writeLong(long value) throws IOException {
        buffer[0] = (byte)(value & 0xFF);
        buffer[1] = (byte)((value >> 8) & 0xFF);
        buffer[2] = (byte)((value >> 16) & 0xFF);
        buffer[3] = (byte)((value >> 24) & 0xFF);
        buffer[4] = (byte)((value >> 32) & 0xFF);
        buffer[5] = (byte)((value >> 40) & 0xFF);
        buffer[6] = (byte)((value >> 48) & 0xFF);
        buffer[7] = (byte)((value >> 56) & 0xFF);
        out.write(buffer, 0, 8);
    }
    
    public void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }
}
