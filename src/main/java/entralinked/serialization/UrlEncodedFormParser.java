package entralinked.serialization;

import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Parser for URL encoded forms.
 * It's not perfect, but it works well enough for the intended purpose.
 */
public class UrlEncodedFormParser extends SimpleParserBase {
    
    /**
     * Parser format features for form-data
     */
    public enum Feature implements FormatFeature {
        
        /**
         * Whether values are encoded in base64 and should be decoded as such
         */
        BASE64_DECODE_VALUES(true);
        
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
    
    private final Reader reader;
    private final int formatFeatures;
    private String parsedString;
    private boolean closed;
    private boolean endOfInput;
    
    public UrlEncodedFormParser(int features, int formatFeatures, Reader reader) {
        super(features);
        this.formatFeatures = formatFeatures;
        this.reader = reader;
    }
    
    @Override
    public JsonToken nextToken() throws IOException {
        if(endOfInput) {
            return _currToken = null; // Return null if there is no content left to read
        }
        
        int i = parseStringAndSkipSeparator();
        
        if(_currToken == JsonToken.VALUE_STRING || _currToken == null) {
            // Parse field name
            _currToken = JsonToken.FIELD_NAME;
            
            if(parsedString.isEmpty()) {
                _reportUnexpectedChar(i, "expected field name");
            }
            
            context.setCurrentName(URLDecoder.decode(parsedString, StandardCharsets.UTF_8));
            
            if(i != '=') {
                _reportUnexpectedChar(i, "expected '=' to mark end of key and start of value");
            }
            
        } else if(_currToken == JsonToken.FIELD_NAME) {
            // Parse value string
            _currToken = JsonToken.VALUE_STRING;
            
            // Decode base64 if feature is enabled
            // Otherwise, decode using URLDecoder.
            if(Feature.BASE64_DECODE_VALUES.enabledIn(formatFeatures)) {
                context.setCurrentValue(new String(Base64.getDecoder().decode(
                        parsedString.replace('*', '=').replace('.', '+').replace('-', '/')), StandardCharsets.ISO_8859_1));
            } else {
                context.setCurrentValue(URLDecoder.decode(parsedString, StandardCharsets.UTF_8));
            }
            
            if(i != '&') {
                if(i != -1) {
                    _reportUnexpectedChar(i, "expected '&' to mark end of value and start of new key");
                }
                
                endOfInput = true; // We have reached the end, so let it be known!
            }
        }
        
        return _currToken;
    }
    
    /**
     * Parses the next string and stores the output in {@link #parsedString}.
     * The returned value is the first character read after the parsed string, or -1 if end-of-input.
     */
    private int parseStringAndSkipSeparator() throws IOException {
        StringBuilder builder = new StringBuilder();
        int i = -1;
        
        while((i = reader.read()) != -1) {
            if(i == '&' || i == '=') {
                break;
            }
            
            builder.append((char)i);
        }
        
        parsedString = builder.toString();
        return i;
        
    }
    
    @Override
    public void close() throws IOException {
        if(!closed) {
            try {
                reader.close();
            } finally {
                closed = true;
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public int getFormatFeatures() {
        return formatFeatures;
    }
}
