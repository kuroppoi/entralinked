package entralinked.serialization;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonReadContext;

/**
 * Parser base for lazy implementations that simply parse everything as {@code VALUE_STRING}.
 * Keeps subclasses clean.
 */
public abstract class SimpleParserBase extends ParserMinimalBase {
    
    protected JsonReadContext context;
    protected ObjectCodec codec;
    
    public SimpleParserBase(int features) {
        super(features);
        DupDetector detector = isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION) ? DupDetector.rootDetector(this) : null;
        this.context = JsonReadContext.createRootContext(detector);
    }
    
    @Override
    protected void _handleEOF() throws JsonParseException {}
    
    @Override
    public Number getNumberValue() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public NumberType getNumberType() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getIntValue() throws IOException {
        return Integer.parseInt(getStringValue());
    }
    
    @Override
    public long getLongValue() throws IOException {
        return Long.parseLong(getStringValue());
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        return new BigInteger(getStringValue());
    }
    
    @Override
    public float getFloatValue() throws IOException {
        return Float.parseFloat(getStringValue());
    }
    
    @Override
    public double getDoubleValue() throws IOException {
        return Double.parseDouble(getStringValue());
    }
    
    @Override
    public BigDecimal getDecimalValue() throws IOException {
        return new BigDecimal(getStringValue());
    }
    
    @Override
    public byte[] getBinaryValue(Base64Variant variant) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public JsonStreamContext getParsingContext() {
        return context;
    }
    
    @Override
    public void overrideCurrentName(String name) {
        try {
            context.setCurrentName(name);
        } catch(JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public String getCurrentName() throws IOException {
        return context.getCurrentName();
    }
    
    @Override
    public String getText() throws IOException {
        switch(_currToken) {
            case FIELD_NAME: return context.getCurrentName();
            case VALUE_STRING: return context.getCurrentValue().toString();
            default: throw new IllegalStateException(); // Should not happen
        }
    }
    
    @Override
    public char[] getTextCharacters() throws IOException {
        return getText().toCharArray();
    }
    
    @Override
    public boolean hasTextCharacters() {
        return true;
    }
    
    @Override
    public int getTextLength() throws IOException {
        return getText().length();
    }
    
    @Override
    public int getTextOffset() throws IOException {
        return 0;
    }
    
    @Override
    public void setCodec(ObjectCodec codec) {
        this.codec = codec;
    }
    
    @Override
    public ObjectCodec getCodec() {
        return codec;
    }
    
    @Override
    public JsonLocation getCurrentLocation() {
        return JsonLocation.NA;
    }
    
    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }
    
    @Override
    public Version version() {
        return null;
    }
    
    public String getStringValue() {
        return getCurrentValue() == null ? "null" : getCurrentValue().toString();
    }
}
