package entralinked.serialization;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.IOContext;

public class GameSpyMessageFactory extends JsonFactory {
    
    private static final long serialVersionUID = -8313019676052328025L;
    
    @Override
    protected GameSpyMessageGenerator _createGenerator(Writer writer, IOContext context) throws IOException {
        return new GameSpyMessageGenerator(_generatorFeatures, _objectCodec, writer);
    }
    
    @Override
    protected GameSpyMessageGenerator _createUTF8Generator(OutputStream outputStream, IOContext context) throws IOException {
        return _createGenerator(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), context);
    }
    
    @Override
    protected GameSpyMessageParser _createParser(InputStream inputStream, IOContext context) throws IOException {
        return new GameSpyMessageParser(_parserFeatures, new InputStreamReader(inputStream));
    }

    @Override
    protected GameSpyMessageParser _createParser(byte[] data, int offset, int length, IOContext context) throws IOException, JsonParseException {
        return new GameSpyMessageParser(_parserFeatures, new InputStreamReader(new ByteArrayInputStream(data, offset, length)));
    }
    
    @Override
    protected GameSpyMessageParser _createParser(char[] data, int offset, int length, IOContext context, boolean recyclable) throws IOException {
        return new GameSpyMessageParser(_parserFeatures, new CharArrayReader(data, offset, length));
    }
    
    @Override
    protected GameSpyMessageParser _createParser(DataInput input, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }
}