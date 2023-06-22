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

public class UrlEncodedFormFactory extends JsonFactory {
    
    private static final long serialVersionUID = -8313019676052328025L;
    public static final int DEFAULT_FORMAT_GENERATOR_FEATURES = UrlEncodedFormGenerator.Feature.getDefaults();
    public static final int DEFAULT_FORMAT_PARSER_FEATURES = UrlEncodedFormParser.Feature.getDefaults();
    protected int formatGeneratorFeatures = DEFAULT_FORMAT_GENERATOR_FEATURES;
    protected int formatParserFeatures = DEFAULT_FORMAT_PARSER_FEATURES;
    
    @Override
    protected UrlEncodedFormGenerator _createGenerator(Writer writer, IOContext context) throws IOException {
        return new UrlEncodedFormGenerator(_generatorFeatures, formatGeneratorFeatures, _objectCodec, writer);
    }
    
    @Override
    protected UrlEncodedFormGenerator _createUTF8Generator(OutputStream outputStream, IOContext context) throws IOException {
        return _createGenerator(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), context);
    }
    
    @Override
    protected UrlEncodedFormParser _createParser(InputStream inputStream, IOContext context) throws IOException {
        return new UrlEncodedFormParser(_parserFeatures, formatParserFeatures, new InputStreamReader(inputStream));
    }

    @Override
    protected UrlEncodedFormParser _createParser(byte[] data, int offset, int length, IOContext context) throws IOException, JsonParseException {
        return new UrlEncodedFormParser(_parserFeatures, formatParserFeatures, new InputStreamReader(new ByteArrayInputStream(data, offset, length)));
    }
    
    @Override
    protected UrlEncodedFormParser _createParser(char[] data, int offset, int length, IOContext context, boolean recyclable) throws IOException {
        return new UrlEncodedFormParser(_parserFeatures, formatParserFeatures, new CharArrayReader(data, offset, length));
    }
    
    @Override
    protected UrlEncodedFormParser _createParser(DataInput input, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getFormatGeneratorFeatures() {
        return formatGeneratorFeatures;
    }
    
    @Override
    public int getFormatParserFeatures() {
        return formatParserFeatures;
    }
    
    public UrlEncodedFormFactory configure(UrlEncodedFormGenerator.Feature feature, boolean state) {
        return state ? enable(feature) : disable(feature);
    }
    
    public UrlEncodedFormFactory enable(UrlEncodedFormGenerator.Feature feature) {
        formatGeneratorFeatures |= feature.getMask();
        return this;
    }
    
    public UrlEncodedFormFactory disable(UrlEncodedFormGenerator.Feature feature) {
        formatGeneratorFeatures &= ~feature.getMask();
        return this;
    }
    
    public final boolean isEnabled(UrlEncodedFormGenerator.Feature feature) {
        return feature.enabledIn(formatGeneratorFeatures);
    }
    
    public UrlEncodedFormFactory configure(UrlEncodedFormParser.Feature feature, boolean state) {
        return state ? enable(feature) : disable(feature);
    }
    
    public UrlEncodedFormFactory enable(UrlEncodedFormParser.Feature feature) {
        formatParserFeatures |= feature.getMask();
        return this;
    }
    
    public UrlEncodedFormFactory disable(UrlEncodedFormParser.Feature feature) {
        formatParserFeatures &= ~feature.getMask();
        return this;
    }
    
    public final boolean isEnabled(UrlEncodedFormParser.Feature feature) {
        return feature.enabledIn(formatParserFeatures);
    }
}
