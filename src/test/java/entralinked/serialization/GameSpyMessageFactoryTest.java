package entralinked.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
public class GameSpyMessageFactoryTest {
    
    protected ObjectMapper mapper;
    
    @BeforeAll
    void before() {
        mapper = new ObjectMapper(new GameSpyMessageFactory());
    }
    
    @Test
    @DisplayName("Test if generator writes objects correctly")
    void testGeneratorWriteObject() throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", "value");
        data.put("emptyValue", "");
        data.put("hello", "world");
        data.put("numberTest", 123);
        assertEquals("\\key\\value\\emptyValue\\\\hello\\world\\numberTest\\123", mapper.writeValueAsString(data));
    }
    
    @Test
    @DisplayName("Test if generator throws exception when writing a nested object")
    void testGeneratorThrowsExceptionWhenWritingNestedObject() {
        Map<String, Object> data = Map.of("someKey", "someValue", "nestedObjectKey", Map.of("key", "value"));
        JsonMappingException exception = assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(data));
        assertEquals("this format does not support nested objects", exception.getOriginalMessage());
    }
    
    @Test
    @DisplayName("Test if generator throws exception when writing an array")
    void testGeneratorThrowsExceptionWhenWritingArray() {
        Map<String, Object> data = Map.of("arrayKey", List.of(1, 2, 3, 4));
        JsonMappingException exception = assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(data));
        assertEquals("this format does not support arrays", exception.getOriginalMessage());
    }
    
    @Test
    @DisplayName("Test if parser creates object from input string correctly")
    void testParserReadObject() throws IOException {
        String inputString = "\\key\\value\\emptyValue\\\\hello\\world\\numberTest\\123";
        Map<String, Object> data = mapper.readValue(inputString, new TypeReference<Map<String, Object>>(){});
        assertEquals("value", data.get("key"));
        assertEquals("", data.get("emptyValue"));
        assertEquals("world", data.get("hello"));
        assertEquals("123", data.get("numberTest"));
    }
    
    @Test
    @DisplayName("Test if parser throws exception when reading an empty key")
    void testParserThrowsExceptionWhenReadingEmptyKey() {
        String inputString = "\\some\\value\\\\emptyKey";
        JsonParseException exception = assertThrows(JsonParseException.class, () -> mapper.readValue(inputString, Map.class));
        assertEquals("Unexpected character ('\\' (code 92)): expected field name", exception.getOriginalMessage());
    }
    
    @Test
    @DisplayName("Test if parser throws exception when input doesn't start with backslash")
    void testParserThrowsExceptionWhenFirstCharacterNotBackslash() {
        String inputString = "key\\value";
        JsonParseException exception = assertThrows(JsonParseException.class, () -> mapper.readValue(inputString, Map.class));
        assertEquals("Unexpected character ('k' (code 107)): expected '\\' to open field name", exception.getOriginalMessage());
    }
    
    @Test
    @DisplayName("Test if parser throws exception when field name isn't closed")
    void testParserThrowsExceptionWhenFieldNameNotClosed() {
        String inputString = "\\hello\\world\\key";
        JsonParseException exception = assertThrows(JsonParseException.class, () -> mapper.readValue(inputString, Map.class));
        assertEquals("Unexpected end-of-input in FIELD_NAME", exception.getOriginalMessage());
    }
}
