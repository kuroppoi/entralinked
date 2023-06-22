package entralinked.serialization;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;

/**
 * Generator base for lazy implementations that simply write everything as strings.
 * Keeps subclasses clean.
 */
public abstract class SimpleGeneratorBase extends GeneratorBase {
    
    protected SimpleGeneratorBase(int features, ObjectCodec codec) {
        super(features, codec);
    }
    
    @Override
    protected void _releaseBuffers() {}

    @Override
    protected void _verifyValueWrite(String typeMsg) throws IOException {}
    
    @Override
    public void writeStartArray() throws IOException {
        throw new UnsupportedOperationException("this format does not support arrays");
    }

    @Override
    public void writeEndArray() throws IOException {
        throw new UnsupportedOperationException("this format does not support arrays");
    }

    @Override
    public void writeStartObject() throws IOException {
        if(!_writeContext.inRoot()) {
            throw new UnsupportedOperationException("this format does not support nested objects");
        }
        
        // Quirk
        _writeContext = _writeContext.createChildObjectContext();
    }

    @Override
    public void writeString(char[] buffer, int offset, int length) throws IOException {
        writeString(new String(buffer, offset, length));
    }

    @Override
    public void writeRawUTF8String(byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUTF8String(byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRaw(String text) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRaw(char[] text, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeRaw(char c) throws IOException {
        writeString(String.valueOf(c));
    }

    @Override
    public void writeBinary(Base64Variant variant, byte[] data, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeNumber(int value) throws IOException {
        writeString(String.valueOf(value));     
    }

    @Override
    public void writeNumber(long value) throws IOException {
        writeString(String.valueOf(value));     
    }

    @Override
    public void writeNumber(BigInteger value) throws IOException {
        writeString(value.toString());
    }

    @Override
    public void writeNumber(double value) throws IOException {
        writeString(String.valueOf(value));
    }

    @Override
    public void writeNumber(float value) throws IOException {
        writeString(String.valueOf(value));
    }

    @Override
    public void writeNumber(BigDecimal value) throws IOException {
        writeString(value.toPlainString()); 
    }
    
    @Override
    public void writeNumber(String encodedValue) throws IOException {
        writeString(encodedValue);      
    }
    
    @Override
    public void writeBoolean(boolean state) throws IOException {
        writeString(String.valueOf(state));
    }
    
    @Override
    public void writeNull() throws IOException {
        writeString("null");
    }
}
