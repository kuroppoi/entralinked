package entralinked.network.http.nas;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

public interface NasResponse {
    
    @JsonProperty("returncd")
    default NasReturnCode returnCode() {
        return NasReturnCode.SUCCESS;
    }
    
    @JsonProperty("datetime")
    @JsonFormat(shape = Shape.STRING, pattern = "yyMMddHHmmss")
    default LocalDateTime dateTime() {
        return LocalDateTime.now();
    }
}
