package entralinked.network.http.nas;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

public record NasRequest(
        // Credentials
        @JsonProperty(value = "userid", required = true) String userId,
        @JsonProperty(value = "passwd", required = true) String password,
        @JsonProperty(value = "macadr", required = true) String macAddress,
        
        // Game info
        @JsonProperty("gamecd")  String gameCode,
        @JsonProperty("makercd") String makerCode,
        @JsonProperty("unitcd")  String unitCode,
        @JsonProperty("sdkver")  String sdkVersion,
        @JsonProperty("lang")    String language,
        
        // Device info
        @JsonProperty("bssid")   String bssid,
        @JsonProperty("apinfo")  String accessPointInfo,
        @JsonProperty("devname") String deviceName,
        @JsonProperty("birth")   String birthDate, // Hex, apparently
        @JsonProperty("devtime") @JsonFormat(shape = Shape.STRING, pattern = "yyMMddHHmmss") LocalDateTime deviceTime,
        
        // Request-specific info
        @JsonProperty(value = "action", required = true) String action,
        @JsonProperty("gsbrcd") String branchCode, // action=login
        @JsonProperty("svc")    String serviceType) {} // action=SVCLOC
