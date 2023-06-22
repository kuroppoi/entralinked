package entralinked.serialization;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.json.JsonWriteContext;

/**
 * Generator for URL encoded forms.
 * I don't doubt its imperfection, but it does what it needs to do.
 */
public class UrlEncodedFormGenerator extends SimpleGeneratorBase {
    
    /**
     * Generator format features for form-data
     */
    public enum Feature implements FormatFeature {
        
        /**
         * Whether values should be encoded as base64
         */
        BASE64_ENCODE_VALUES(true);
        
        private final boolean defaultState;
        private final int mask;
        
        private Feature(boolean defaultState) {
            this.defaultState = defaultState;
            this.mask = 1 << ordinal();
        }
        
        public static int getDefaults() {
            int flags = 0;
            
            for(Feature feature : values()) {
                if(feature.enabledByDefault()) {
                    flags |= feature.getMask();
                }
            }
            
            return flags;
        }

        @Override
        public boolean enabledByDefault() {
            return defaultState;
        }

        @Override
        public int getMask() {
            return mask;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & mask) != 0;
        }
        
    }
    
    protected final int formatFeatures;
    protected final Writer writer;
    
    protected UrlEncodedFormGenerator(int features, int formatFeatures, ObjectCodec codec, Writer writer) {
        super(features, codec);
        this.formatFeatures = formatFeatures;
        this.writer = writer;
    }
    
    @Override
    public void writeEndObject() throws IOException {
        _writeContext = _writeContext.getParent();
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        int status = _writeContext.writeFieldName(name);
        
        // Check if an entry separator should be appended before the field name
        if(status == JsonWriteContext.STATUS_OK_AFTER_COMMA) {
            writer.write('&');
        }
        
        writer.write(URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    @Override
    public void writeString(String text) throws IOException {
        int status = _writeContext.writeValue();
        
        // Check if a key/value separator should be appended before the string value (should always be the case)
        if(status == JsonWriteContext.STATUS_OK_AFTER_COLON) {
            writer.write('=');
        }
        
        String value = text;
        
        // Encode value as base64 if feature is enabled
        if(Feature.BASE64_ENCODE_VALUES.enabledIn(formatFeatures)) {
            value = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.ISO_8859_1))
                    .replace('=', '*').replace('+', '.').replace('/', '-');
        }
        
        writer.write(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
    
    @Override
    public int getFormatFeatures() {
        return formatFeatures;
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
