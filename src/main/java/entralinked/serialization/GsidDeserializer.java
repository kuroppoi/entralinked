package entralinked.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import entralinked.utility.GsidUtility;

/**
 * Deserializer that stringifies integers using {@link GsidUtility}
 */
public class GsidDeserializer extends StdDeserializer<String> {
    
    private static final long serialVersionUID = -2973925169701434892L;

    public GsidDeserializer() {
        this(String.class);
    }
    
    protected GsidDeserializer(Class<?> type) {
        super(type);
    }
    
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return GsidUtility.stringifyGameSyncId(parser.getValueAsInt(-1));
    }
}
