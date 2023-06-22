package entralinked.network.http.nas;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * List of NAS return codes ({@code returncd})
 */
public enum NasReturnCode {
    
    /**
     * (Generic) Action was successful.
     */
    SUCCESS(1),
    
    /**
     * (Generic) An error occured on the server whilst processing a request.
     */
    INTERNAL_SERVER_ERROR(100),
    
    /**
     * (Registration) User account creation was successful.
     * When this is sent, the user ID (WFC ID) will be stored on the client device.
     */
    REGISTRATION_SUCCESS(2),
    
    /**
     * (Generic) Request is missing data.
     */
    BAD_REQUEST(102),
    
    /**
     * (Registration) The client tried to register a user ID that already exists on the server.
     * When this is sent, the client will generate a new user ID and try again.
     */
    USER_ALREADY_EXISTS(104),
    
    /**
     * (Login) The user data has been deleted from the server.
     */
    USER_EXPIRED(108),
    
    /**
     * (Login) The user with the client-specified user ID does not exist
     */
    USER_NOT_FOUND(204);
    
    private final int clientId;
    
    private NasReturnCode(int clientId) {
        this.clientId = clientId;
    }
    
    @JsonValue
    public String getFormattedClientId() {
        return "%03d".formatted(clientId);
    }
    
    public int getClientId() {
        return clientId;
    }
}
