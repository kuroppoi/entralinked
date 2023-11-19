package entralinked.network.gamespy;

import java.nio.channels.ClosedChannelException;
import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import entralinked.Entralinked;
import entralinked.model.user.GameProfile;
import entralinked.model.user.ServiceSession;
import entralinked.model.user.User;
import entralinked.model.user.UserManager;
import entralinked.network.gamespy.message.GameSpyChallengeMessage;
import entralinked.network.gamespy.message.GameSpyErrorMessage;
import entralinked.network.gamespy.message.GameSpyLoginResponse;
import entralinked.network.gamespy.message.GameSpyProfileResponse;
import entralinked.network.gamespy.request.GameSpyLoginRequest;
import entralinked.network.gamespy.request.GameSpyProfileRequest;
import entralinked.network.gamespy.request.GameSpyProfileUpdateRequest;
import entralinked.network.gamespy.request.GameSpyRequest;
import entralinked.utility.CredentialGenerator;
import entralinked.utility.MD5;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * GameSpy request handler.
 */
public class GameSpyHandler extends SimpleChannelInboundHandler<GameSpyRequest> {
    
    private static final Logger logger = LogManager.getLogger();
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserManager userManager;
    private Channel channel;
    private String serverChallenge;
    private int sessionKey = -1; // It's pointless
    private User user;
    private GameProfile profile;
    
    public GameSpyHandler(Entralinked entralinked) {
        this.userManager = entralinked.getUserManager();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();
        logger.debug("Sending GameSpy server challenge");
        
        // Generate random server challenge
        serverChallenge = CredentialGenerator.generateChallenge(10);
        
        // Send challenge message
        sendMessage(new GameSpyChallengeMessage(serverChallenge, 1));
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("User {} disconnected from GameSpy server", user == null ? null : user.getFormattedId());
        
        // Clear data
        serverChallenge = null;
        sessionKey = -1;
        user = null;
        profile = null;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameSpyRequest request) throws Exception {
        logger.debug("Received {}", request);
        request.process(this);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof ClosedChannelException) {
            logger.debug("Silently discarded ClosedChannelException");
            return; // Ignore this stupid exception.
        }
        
        // Handle timeout
        if(cause instanceof ReadTimeoutException) {
            logger.debug("User {} timed out", user == null ? null : user.getFormattedId());
            return;
        }
        
        logger.error("Exception caught in GameSpy handler", cause);
        
        // Send error message and close channel afterwards.
        sendErrorMessage(0x100, "An internal error occured on the server.", true, 0).addListener(future -> close());
    }
    
    public void handleLoginRequest(GameSpyLoginRequest request) {
        String authToken = request.partnerToken();
        String clientChallenge = request.challenge();
        
        // Check if session exists
        ServiceSession session = userManager.getServiceSession(authToken, "gamespy");
        
        if(session == null) {
            logger.debug("Rejecting GameSpy login request because the partner token is invalid");
            sendErrorMessage(0x200, "Invalid partner token.", request.sequenceId());
            return;
        }
        
        // Verify client credential hash
        String partnerChallengeHash = session.challengeHash();
        String expectedResponse = createCredentialHash(partnerChallengeHash, authToken, clientChallenge, serverChallenge);
        
        if(!expectedResponse.equals(request.response())) {
            logger.debug("Rejecting GameSpy login request because the challenge response is invalid");
            sendErrorMessage(0x202, "Invalid response.", request.sequenceId());
            return;
        }
        
        // Fetch profile or create one if it doesn't exist
        user = session.user();
        profile = user.getProfile(session.branchCode());
        
        if(profile == null) {
            profile = userManager.createProfileForUser(user, session.branchCode());
            
            // Check if creation succeeded
            if(profile == null) {
                sendErrorMessage(0x203, "Profile creation failed due to an error.", request.sequenceId());
                return;
            }
        }
        
        // Update profile id if an override is set
        int profileIdOverride = user.getProfileIdOverride();
        
        if(profileIdOverride > 0) {
            profile.setId(profileIdOverride);
            user.setProfileIdOverride(0);
            userManager.saveUser(user); // It's not too big of a deal if this fails for some reason
        }
        
        logger.info("User {} logged in with profile {}", user.getFormattedId(), profile.getId());
        
        // Prepare and send response
        sessionKey = secureRandom.nextInt(Integer.MAX_VALUE);
        String proof = createCredentialHash(partnerChallengeHash, authToken, serverChallenge, clientChallenge);
        sendMessage(new GameSpyLoginResponse(user.getId(), profile.getId(), proof, sessionKey, request.sequenceId()));
    }
    
    public void handleProfileRequest(GameSpyProfileRequest request) {
        sendMessage(new GameSpyProfileResponse(profile, request.sequenceId()));
    }
    
    public void handleUpdateProfileRequest(GameSpyProfileUpdateRequest request) {
        // Update profile info
        boolean profileChanged = setValue(request::firstName, profile::setFirstName, profile::getFirstName);
        profileChanged |= setValue(request::lastName, profile::setLastName, profile::getLastName);
        profileChanged |= setValue(request::aimName, profile::setAimName, profile::getAimName);
        profileChanged |= setValue(request::zipCode, profile::setZipCode, profile::getZipCode);
        
        // Save user data if the profile was changed
        if(profileChanged) {
            userManager.saveUser(user);
        }
    }
    
    /**
     * Sets a value if it isn't {@code null} and returns {@code true} if the value is different from the current value.
     * 
     * @param valueSupplier Supplies the value that needs to be set
     * @param valueConsumer Consumes the value from {@code valueSupplier} (a setter)
     * @param currentValueSupplier Supplies the current value to test against the new value
     */
    private <T> boolean setValue(Supplier<T> valueSupplier, Consumer<T> valueConsumer, Supplier<T> currentValueSupplier) {
        T value = valueSupplier.get();
        
        // Return false if value is null or is equal to the existing value
        if(value == null || value.equals(currentValueSupplier.get())) {
            return false;
        }
        
        valueConsumer.accept(value);
        return true;
    }
    
    protected String createCredentialHash(String passwordHash, String user, String inChallenge, String outChallenge) {
        return MD5.digest("%s%s%s%s%s%s".formatted(
                passwordHash,
                "                                                ",
                user,
                inChallenge,
                outChallenge,
                passwordHash));
    }
    
    public void handleLogout() {
        logger.info("User {} logged out of profile {}", user.getFormattedId(), profile.getId());
        sessionKey = -1; // Is there a point?
    }
    
    public boolean validateSessionKey(int sessionKey) {
        return validateSessionKey(sessionKey, 0);
    }
    
    public boolean validateSessionKey(int sessionKey, int sequenceId) {
        if(sessionKey < 0 || this.sessionKey != sessionKey) {
            sendErrorMessage(0x201, "Invalid session key.", sequenceId);
            return false;
        }
        
        return true;
    }
    
    public ChannelFuture sendMessage(Object message) {
        return channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
    
    public ChannelFuture sendErrorMessage(int errorCode, String errorMessage, boolean fatal, int sequenceId) {
        return sendMessage(new GameSpyErrorMessage(errorCode, errorMessage, fatal ? 1 : 0, sequenceId));
    }
    
    public ChannelFuture sendErrorMessage(int errorCode, String errorMessage, int sequenceId) {
        return sendErrorMessage(errorCode, errorMessage, false, sequenceId);
    }
    
    public ChannelFuture close() {
        return channel.close();
    }
}
