package entralinked.utility;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Like {@link LEOutputStream}, but {@link InputStream}.
 */
public class LEInputStream extends FilterInputStream {
    
    protected final byte[] buffer = new byte[8];
    
    public LEInputStream(InputStream inputStream) {
        super(inputStream);
    }
    
    public short readShort() throws IOException {
        in.read(buffer, 0, 2);
        return (short)(buffer[0] & 0xFF
                   | ((buffer[1] & 0xFF) << 8));
    }
    
    public int readInt() throws IOException {
        in.read(buffer, 0, 4);
        return buffer[0] & 0xFF
            | ((buffer[1] & 0xFF) << 8)
            | ((buffer[2] & 0xFF) << 16)
            | ((buffer[3] & 0xFF) << 24);
    }
    
    public double readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    public long readLong() throws IOException {
        in.read(buffer, 0, 8);
        return buffer[0] & 0xFF
            | ((buffer[1] & 0xFF) << 8)
            | ((buffer[2] & 0xFF) << 16)
            | ((long)(buffer[3] & 0xFF) << 24)
            | ((long)(buffer[4] & 0xFF) << 32)
            | ((long)(buffer[5] & 0xFF) << 40)
            | ((long)(buffer[6] & 0xFF) << 48)
            | ((long)(buffer[7] & 0xFF) << 56);
    }
    
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
}
