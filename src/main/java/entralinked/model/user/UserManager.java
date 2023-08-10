package entralinked.model.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import entralinked.utility.CredentialGenerator;
import entralinked.utility.MD5;

/**
 * Manager class for managing {@link User} information (Wi-Fi Connection users)
 * 
 * TODO session management is a bit scuffed
 */
public class UserManager {
    
    public static final Pattern USER_ID_PATTERN = Pattern.compile("[0-9]{13}");
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, ServiceSession> serviceSessions = new ConcurrentHashMap<>();
    private final File dataDirectory = new File("users");
    
    public UserManager() {      
        logger.info("Loading user and profile data ...");
        
        // Check if directory exists
        if(!dataDirectory.exists()) {
            return;
        }
        
        // Load user data
        for(File file : dataDirectory.listFiles()) {
            if(!file.isDirectory()) {
                loadUser(file);
            }
        }
        
        logger.info("Loaded {} user(s)", users.size());
    }
    
    /**
     * @return {@code true} if this is a valid user ID.
     * That is, it has a length of 13 and only contains digits.
     */
    public static boolean isValidUserId(String id) {
        return USER_ID_PATTERN.matcher(id).matches();
    }
    
    /**
     * Loads a {@link User} from the specified input file.
     * The user is indexed automatically, unless it has a duplicate ID or any duplicate profile ID.
     */
    private void loadUser(File inputFile) {
        try {
            // Deserialize user data
            User user = mapper.readValue(inputFile, UserDto.class).toUser();
            String id = user.getId();
            
            // Check for duplicate user ID
            if(users.containsKey(id)) {
                throw new IOException("Duplicate user ID %s".formatted(id));
            }
            
            // Index user
            users.put(id, user);
        } catch(IOException e) {
            logger.error("Could not load user data at {}", inputFile.getAbsolutePath(), e);
        }
    }
    
    /**
     * Saves the data of all users.
     */
    public void saveUsers() {
        users.values().forEach(this::saveUser);
    }
    
    /**
     * Saves the data of the specified user to disk, and returns {@code true} if it succeeds.
     * The output file is generated as follows:
     * 
     * {@code new File(dataDirectory, "WFC-%s".formatted(formattedId))}
     * where {@code formattedId} is the user ID formatted to {@code ####-####-####-#000}
     */
    public boolean saveUser(User user) {
        return saveUser(user, new File(dataDirectory, "WFC-%s.json".formatted(user.getFormattedId())));
    }
    
    /**
     * Saves the data of the specified user to the specified output file.
     * 
     * @return {@code true} if the data was saved successfully, {@code false} otherwise.
     */
    private boolean saveUser(User user, File outputFile) {
        try {
            // Create parent directories
            File parentFile = outputFile.getParentFile();
            
            if(parentFile != null) {
                parentFile.mkdirs();
            }
            
            // Serialize the entire user object first to minimize risk of corrupted files
            byte[] bytes = mapper.writeValueAsBytes(new UserDto(user));
            
            // Finally, write the data.
            Files.write(outputFile.toPath(), bytes);
        } catch(IOException e) {
            logger.error("Could not save user data for user {}", user.getId(), e);
            return false;
        }
        
        return true;
    }
    
    /**
     * Generates credentials for the client to use when logging into a separate service.
     * The information to be used by the service to verify the client will be cached and may be retrieved using {@link #getServiceSession(token, service)}.
     * 
     * @return A {@link ServiceCredentials} record containing the auth token and (optional) challenge to send to the client.
     */
    public ServiceCredentials createServiceSession(User user, String service, String branchCode) {
        if(service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        
        // Create token
        String authToken = "NDS%s".formatted(CredentialGenerator.generateAuthToken(96));
        
        if(serviceSessions.containsKey(authToken)) {
            return createServiceSession(user, service, branchCode); // Top 5 things that never happen
        }
        
        // Create challenge
        String challenge = CredentialGenerator.generateChallenge(8);
        
        // Create session object
        ServiceSession session = new ServiceSession(user, service, branchCode, MD5.digest(challenge), 30, ChronoUnit.MINUTES);
        serviceSessions.put(authToken, session);
        return new ServiceCredentials(authToken, challenge);
    }
    
    /**
     * @return A {@link ServiceSession} matching the specified auth token and service,
     * or {@code null} if there is none or if the existing session expired.
     */
    public ServiceSession getServiceSession(String authToken, String service) {
        ServiceSession session = serviceSessions.get(authToken);
        
        // Check if session exists
        if(session == null) {
            return null;
        }
        
        // Check if session has expired
        if(session.hasExpired()) {
            serviceSessions.remove(authToken);
            return null;
        }
        
        return session.service().equals(service) ? session : null;
    }
    
    /**
     * Registers a user with the given ID and password.

     * @return {@code true} if the registration was successful.
     * Otherwise, if this user ID has already been registered, or if the user data could not be saved, {@code false} is returned instead.
     */
    public boolean registerUser(String userId, String plainPassword) {
        // Check if user id already exists
        if(users.containsKey(userId)) {
            logger.warn("Attempted to register user with duplicate ID: {}", userId);
            return false;
        }
        
        // Create user
        User user = new User(userId, plainPassword); // TODO hash
        
        // Save user data and return null if it fails
        if(!saveUser(user)) {
            return false;
        }
        
        users.put(userId, user);
        return true;
    }
    
    /**
     * Simple method that returns a {@link User} object whose credentials match the given ones.
     * If no user exists with matching credentials, {@code null} is returned instead.
     */
    public User authenticateUser(String userId, String password) {
        User user = users.get(userId);
        return user == null || !user.getPassword().equals(password) ? null : user;
    }
    
    /**
     * Creates a new profile for the specified user.
     * 
     * @return The newly created profile, or {@code null} if profile creation failed.
     */
    public GameProfile createProfileForUser(User user, String branchCode) {
        // Check for duplicate profile
        if(user.getProfile(branchCode) != null) {
            logger.warn("Attempted to create duplicate profile {} in user {}", branchCode, user.getId());
            return null;
        }
        
        int profileId = (int)(Math.random() * Integer.MAX_VALUE);
        GameProfile profile = new GameProfile(profileId);
        user.addProfile(branchCode, profile);
        
        // Try to save user data and return null if it fails
        if(!saveUser(user)) {
            user.removeProfile(branchCode);
            return null;
        }
        
        return profile;
    }
    
    /**
     * This will forcibly set the profile id of all profiles of this user to the specified one.
     * Potentially a destructive operation; use with caution.
     * 
     * @return {@code true} if the operation was successful, otherwise {@code false}.
     */
    public boolean updateProfileIdForUser(User user, int profileId) {
        // Set the id of all profiles
        for(GameProfile profile : user.getProfiles()) {
            profile.setId(profileId);
        }
        
        // Try to save user
        if(!saveUser(user)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * @return {@code true} if a user with the specified ID exists, otherwise {@code false}.
     */
    public boolean doesUserExist(String id) {
        return users.containsKey(id);
    }
    
    /**
     * @return The {@link User} object to which this ID belongs, or {@code null} if it doesn't exist.
     */
    public User getUser(String id) {
        return users.get(id);
    }
    
    /**
     * @return An immutable {@link Collection} containing all users.
     */
    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}
