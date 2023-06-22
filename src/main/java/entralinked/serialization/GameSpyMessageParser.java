package entralinked.serialization;

import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonToken;

public class GameSpyMessageParser extends SimpleParserBase {
    
    private final Reader reader;
    private String parsedString;
    private boolean endOfInput;
    private boolean closed;
    
    public GameSpyMessageParser(int features, Reader reader) {
        super(features);
        this.reader = reader;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        if(endOfInput) {
            return _currToken = null; // Return null if there is no content left to read
        }
        
        if(_currToken == null) {
            // TODO
            skipBackSlash();
        }
        
        int i = parseString();
        
        if(_currToken == JsonToken.VALUE_STRING || _currToken == null) {
            // Parse field name
            _currToken = JsonToken.FIELD_NAME;
            
            if(parsedString.isEmpty()) {
                _reportUnexpectedChar(i, "expected field name");
            }
            
            context.setCurrentName(parsedString);
            
            if(i != '\\') {
                _reportUnexpectedChar(i, "expected '\\' to close field name");
            }
            
        } else if(_currToken == JsonToken.FIELD_NAME) {
            // Parse value string
            _currToken = JsonToken.VALUE_STRING;
            context.setCurrentValue(parsedString);
            
            if(i != '\\') {
                if(i != -1) {
                    _reportUnexpectedChar(i, "expected '\\' to open field name");
                }
                
                endOfInput = true; // We have reached the end, so let it be known!
            }
        }
        
        return _currToken;
    }
    
    private int skipBackSlash() throws IOException {
        int i = reader.read();
        
        if(i != '\\') {
            _reportUnexpectedChar(i, "expected '\\' to open field name");
        }
        
        return i;
    }
    
    private int parseString() throws IOException {
        StringBuilder builder = new StringBuilder();
        int i = -1;
        
        while((i = reader.read()) != -1) {
            if(i == '\\') {
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
}
