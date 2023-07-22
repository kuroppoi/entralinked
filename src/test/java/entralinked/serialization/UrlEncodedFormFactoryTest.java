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
public class UrlEncodedFormFactoryTest {
    
    protected ObjectMapper mapper;
    protected ObjectMapper mapperNoBase64;

    @BeforeAll
    void before() {
        mapper = new ObjectMapper(new UrlEncodedFormFactory());
        mapperNoBase64 = new ObjectMapper(new UrlEncodedFormFactory()
                .disable(UrlEncodedFormParser.Feature.BASE64_DECODE_VALUES)
                .disable(UrlEncodedFormGenerator.Feature.BASE64_ENCODE_VALUES));
    }
    
    @Test
    @DisplayName("Test if generator writes objects correctly")
    void testGeneratorWriteObject() throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("hello", "world");
        data.put("test", "space test");
        data.put("emptyValue", "");
        data.put("someNumber", 1234567890);
        assertEquals("hello=d29ybGQ*&test=c3BhY2UgdGVzdA**&emptyValue=&someNumber=MTIzNDU2Nzg5MA**", mapper.writeValueAsString(data));
        assertEquals("hello=world&test=space+test&emptyValue=&someNumber=1234567890", mapperNoBase64.writeValueAsString(data));
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
        String inputString = "hello=world&test=space+test&emptyValue=&someNumber=1234567890";
        Map<String, Object> data = mapperNoBase64.readValue(inputString, new TypeReference<Map<String, Object>>(){});
        assertEquals("world", data.get("hello"));
        assertEquals("space test", data.get("test"));
        assertEquals("", data.get("emptyValue"));
        assertEquals("1234567890", data.get("someNumber"));
        
        // Test Base64
        inputString = "hello=d29ybGQ*&test=c3BhY2UgdGVzdA**&emptyValue=&someNumber=MTIzNDU2Nzg5MA**";
        assertEquals(data, mapper.readValue(inputString, Map.class));
    }
    
    @Test
    @DisplayName("Test if parser throws exception when reading an empty key")
    void testParserThrowsExceptionWhenReadingEmptyKey() {
        String inputString = "someKey=someValue&=emptyKey";
        JsonParseException exception = assertThrows(JsonParseException.class, () -> mapperNoBase64.readValue(inputString, Map.class));
        assertEquals("Unexpected character ('=' (code 61)): expected field name", exception.getOriginalMessage());
    }
    
    @Test
    @DisplayName("Test if parser throws exception when field name isn't closed")
    void testParserThrowsExceptionWhenFieldNameNotClosed() {
        String inputString = "someKey=someValue&notClosed";
        JsonParseException exception = assertThrows(JsonParseException.class, () -> mapperNoBase64.readValue(inputString, Map.class));
        assertEquals("Unexpected end-of-input in FIELD_NAME", exception.getOriginalMessage());
    }
}
