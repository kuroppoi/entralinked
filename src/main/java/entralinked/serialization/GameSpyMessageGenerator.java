package entralinked.serialization;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.ObjectCodec;

public class GameSpyMessageGenerator extends SimpleGeneratorBase {
    
    protected final Writer writer;
    
    public GameSpyMessageGenerator(int features, ObjectCodec codec, Writer writer) {
        super(features, codec);
        this.writer = writer;
    }

    @Override
    public void writeEndObject() throws IOException {
        _writeContext = _writeContext.getParent();
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        writer.write('\\');
        writer.write(name);
        writer.write('\\');
    }

    @Override
    public void writeString(String text) throws IOException {
        writer.write(text);
    }
    
    @Override
    public void flush() throws IOException {
        writer.flush();
    }
    
    @Override
    public void close() throws IOException {
        try {
            writer.close();
        } finally {
            super.close();
        }
    }
}