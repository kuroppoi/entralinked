package entralinked.network.http.pgl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.Configuration;
import entralinked.Entralinked;
import entralinked.GameVersion;
import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.dlc.Dlc;
import entralinked.model.dlc.DlcList;
import entralinked.model.pkmn.PkmnInfo;
import entralinked.model.pkmn.PkmnInfoReader;
import entralinked.model.player.DreamDecor;
import entralinked.model.player.DreamEncounter;
import entralinked.model.player.DreamItem;
import entralinked.model.player.Player;
import entralinked.model.player.PlayerManager;
import entralinked.model.player.PlayerStatus;
import entralinked.model.player.TrainerInfo;
import entralinked.model.player.Offsets;
import entralinked.model.player.TrainerInfoReader;
import entralinked.model.user.ServiceSession;
import entralinked.model.user.User;
import entralinked.model.user.UserManager;
import entralinked.network.http.HttpHandler;
import entralinked.network.http.HttpRequestHandler;
import entralinked.serialization.UrlEncodedFormFactory;
import entralinked.serialization.UrlEncodedFormParser;
import entralinked.utility.GsidUtility;
import entralinked.utility.PointedInputStream;
import entralinked.utility.LEOutputStream;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.security.BasicAuthCredentials;
import jakarta.servlet.ServletInputStream;

/**
 * HTTP handler for requests made to {@code en.pokemon-gl.com}
 */
public class PglHandler implements HttpHandler {
    
    private static final Logger logger = LogManager.getLogger();
    private static final String username = "pokemon";
    private static final String password = "2Phfv9MY"; // Best security in the world
    private final ObjectMapper mapper = new ObjectMapper(new UrlEncodedFormFactory()
            .disable(UrlEncodedFormParser.Feature.BASE64_DECODE_VALUES));
    private final Set<Integer> sleepyList = new HashSet<>();
    private final Configuration configuration;
    private final DlcList dlcList;
    private final UserManager userManager;
    private final PlayerManager playerManager;
    
    public PglHandler(Entralinked entralinked) {
        this.configuration = entralinked.getConfiguration();
        this.dlcList = entralinked.getDlcList();
        this.userManager = entralinked.getUserManager();
        this.playerManager = entralinked.getPlayerManager();
        
        // Add all species to the sleepy list
        for(int i = 1; i <= 649; i++) {
            sleepyList.add(i);
        }
    }
    
    @Override
    public void addHandlers(Javalin javalin) {
        javalin.before("/dsio/gw", this::authorizePglRequest);
        javalin.get("/dsio/gw", this::handlePglGetRequest);
        javalin.post("/dsio/gw", this::handlePglPostRequest);
    }
    
    /**
     * BEFORE handler for {@code /dsio/gw} that serves to deserialize and authenticate the request.
     * The deserialized request will be stored in a context attribute named {@code request} and may be retrieved
     * by subsequent handlers.
     */
    private void authorizePglRequest(Context ctx) throws IOException {
        // Verify the authorization header credentials
        BasicAuthCredentials credentials = ctx.basicAuthCredentials();
        
        if(credentials == null || 
                !username.equals(credentials.getUsername()) || !password.equals(credentials.getPassword())) {
            logger.debug("Rejecting PGL request because the auth credentials were incorrect");
            ctx.status(HttpStatus.UNAUTHORIZED);
            clearTasks(ctx);
            return;
        }
        
        // Deserialize the request
        PglRequest request = mapper.readValue(ctx.queryString(), PglRequest.class);
        logger.debug("Received {}", request);
        
        // Verify the service session token
        ServiceSession session = userManager.getServiceSession(request.token(), "external");
        
        if(session == null) {
            logger.debug("Rejecting PGL request because the service session has expired");
            ctx.status(HttpStatus.UNAUTHORIZED);
            clearTasks(ctx);
            return;
        }
        
        // Store attributes for subsequent handlers
        ctx.attribute("request", request);
        ctx.attribute("user", session.user());
    }
    
    /**
     * GET base handler for {@code /dsio/gw}
     */
    private void handlePglGetRequest(Context ctx) throws IOException {
        PglRequest request = ctx.attribute("request");
        
        // Determine request handler function based on type
        HttpRequestHandler<PglRequest> handler = switch(request.type()) {
            case "sleepily.bitlist" -> this::handleGetSleepyList;
            case "account.playstatus" -> this::handleGetAccountStatus;
            case "savedata.download" -> this::handleDownloadSaveData;
            case "savedata.getbw" -> this::handleMemoryLink;
            default -> throw new IllegalArgumentException("Invalid GET request type: " + request.type());
        };
        
        // Handle the request
        handler.process(request, ctx);
    }
    
    /**
     * GET handler for {@code /dsio/gw?p=sleepily.bitlist}
     */
    private void handleGetSleepyList(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        
        // Check if player exists
        if(!playerManager.doesPlayerExist(request.gameSyncId())) {
            writeStatusCode(outputStream, 1); // Unauthorized
            return;
        }
        
        // Create bitlist
        byte[] bitlist = new byte[128]; // TODO pool? maybe just cache
        
        for(int sleepy : sleepyList) {
            // 8 Pokémon (bits) in 1 byte
            int byteOffset = sleepy / 8;
            int bitOffset = sleepy % 8;
            
            // Set the bit to 1!
            bitlist[byteOffset] |= 1 << bitOffset;
        }
        
        // Send bitlist
        writeStatusCode(outputStream, 0);
        outputStream.write(bitlist);
    }
    
    /**
     * GET handler for {@code /dsio/gw?p=account.playstatus}
     * 
     * Black 2 - {@code sub_21B74B4} (overlay #199)
     */
    private void handleGetAccountStatus(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        Player player = playerManager.getPlayer(request.gameSyncId());
        
        // Request account creation if one doesn't exist yet
        if(player == null) {
            writeStatusCode(outputStream, 8); // 5 is also handled separately, but doesn't seem to do anything unique
            return;
        }
        
        writeStatusCode(outputStream, 0);
        outputStream.writeShort(player.getStatus().ordinal());
    }
    
    /**
     * GET handler for {@code /dsio/gw?p=savedata.download}
     * 
     * Black 2 - {@code sub_21B6C9C} (overlay #199)
     */
    private void handleDownloadSaveData(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        Player player = playerManager.getPlayer(request.gameSyncId());
        User user = ctx.attribute("user");
        
        // Check if player exists
        if(player == null) {
            writeStatusCode(outputStream, 1); // Unauthorized
            return;
        }
        
        logger.info("Player {} is downloading save data", player.getGameSyncId());
        
        // Write status code
        writeStatusCode(outputStream, 0);
        
        // Allow waking up but don't send any data
        if(player.getStatus() == PlayerStatus.AWAKE) {
            return;
        }
        
        GameVersion version = player.getGameVersion();
        List<DreamEncounter> encounters = player.getEncounters();
        List<DreamItem> items = player.getItems();
        List<DreamDecor> decorList = player.getDecor();
        
        // When waking up a Pokémon, these 4 bytes are written to 0x1D304 in the save file.
        // If the bytes in the game's save file match the new bytes, they will be set to 0x00000000
        // and no content will be downloaded.
        // Looking at some old save files, this was very likely just a total tuck-in/wake-up counter.
        // Additionally, waking up sets a flag at 0x1D4A3 (seems to be a "Pokémon is tucked in" flag or something) to 0x0.
        outputStream.writeInt((int)(Math.random() * Integer.MAX_VALUE));
        
        // Write encounter data (max 10)
        for(DreamEncounter encounter : encounters) {
            outputStream.writeShort(encounter.species());
            outputStream.writeShort(encounter.move());
            outputStream.write(encounter.form());
            outputStream.write(encounter.gender().ordinal()); // Genderless = 2 = random
            outputStream.write(encounter.animation().ordinal());
            outputStream.write(0); // unknown
        }
        
        // Write encounter padding
        outputStream.writeBytes(0, (10 - encounters.size()) * 8);
        
        // Write misc stuff and DLC information
        outputStream.writeShort(player.getLevelsGained());
        outputStream.write(0); // Unknown
        outputStream.write(getDlcIndex(user, player.getMusical(), "MUSICAL", player.getMusicalFile()));
        outputStream.write(getDlcIndex(user, player.getCGearSkin(), version.isVersion2() ? "CGEAR2" : "CGEAR", player.getCGearSkinFile()));
        outputStream.write(getDlcIndex(user, player.getDexSkin(), "ZUKAN", player.getDexSkinFile()));
        outputStream.write(decorList.isEmpty() ? 0 : 1); // Decor flag (?) stored at 0x1D4A4
        outputStream.write(0); // Must be zero?
        
        // Write item IDs
        for(DreamItem item : items) {
            outputStream.writeShort(item.id());
        }
        
        // Write item ID padding
        outputStream.writeBytes(0, (20 - items.size()) * 2);
        
        // Write item quantities
        for(DreamItem item : items) {
            outputStream.write(item.quantity()); // Hard caps at 20?
        }
        
        // Write quantity padding
        outputStream.writeBytes(0, (20 - items.size()));
        
        // Decor data -- copied to 0x1D420 in the save file
        // Need to send 5 entries or nothing will happen
        // After decor is selected in Nacrene City, the *index* of it (default: 0x7F) will be saved to 0x1D4A6 in the save file.
        for(DreamDecor decor : decorList) {
            byte[] nameBytes = decor.name().getBytes(StandardCharsets.UTF_16LE);
            
            // If any ID is 0x7E it will not work. It also appears as the default in the save file.
            outputStream.writeShort(decor.id());
            
            // Name can't have more than 12 characters
            outputStream.write(nameBytes, 0, Math.min(24, nameBytes.length));
            outputStream.writeBytes(-1, 24 - nameBytes.length);
        }
        
        // Write decor padding
        for(int i = 0; i < (5 - decorList.size()); i++) {
            outputStream.writeShort(0x7E); // Just reset to default state
            outputStream.writeBytes(0, 24);
        }

        outputStream.writeShort(0); // ?
        
        // Join Avenue visitor data -- copied in parts to 0x2422C in the save file.
        // Black Version 2 and White Version 2 only.
        if(version.isVersion2()) {
            List<AvenueVisitor> avenueVisitors = player.getAvenueVisitors();
            
            for(AvenueVisitor visitor : avenueVisitors) {
                // Write visitor name + padding. Names cannot be duplicate.
                byte[] nameBytes = visitor.name().getBytes(StandardCharsets.UTF_16LE);
                outputStream.write(nameBytes, 0, Math.min(14, nameBytes.length));
                outputStream.writeBytes(-1, 16 - nameBytes.length);
                
                // Full visitor type consists of a trainer class and what I call a 'personality' index
                // that, along with the trainer class, determines which phrases the visitor uses.
                // The shop type is calculated in such an odd manner because for some reason,
                // the 'starting' index of the shop type used increases by 2 for each visitor type.
                // For example, if the visitor type is '0', then shop type '0' would be a raffle.
                // However, if the visitor type is '2', then shop type '0' results in a dojo instead.
                int visitorType = visitor.type().getClientId() + visitor.personality() * 8;
                outputStream.write(visitorType);
                outputStream.write(visitor.shopType().ordinal() + (7 - visitorType * 2 % 7));
                outputStream.writeShort(0); // Does nothing
                outputStream.writeInt(1); // [20] Ignores if 0
                outputStream.write(visitor.countryCode());
                outputStream.write(visitor.stateProvinceCode());
                outputStream.write(visitor.gameVersion().getLanguageCode()); // 99% sure this is the lang code because 1 seems to be ignored ONLY if country code isn't Japan (plus it's right above the rom code)
                outputStream.write(visitor.gameVersion().getRomCode()); // Affects shop stock
                outputStream.write(visitor.type().isFemale() ? 1 : 0);
                outputStream.write(0); // [29] Does.. something
                outputStream.writeShort(visitor.dreamerSpecies());
            }
            
            // Write visitor padding
            outputStream.writeBytes(0, (12 - avenueVisitors.size()) * 32);
            outputStream.writeInt(0); // 672 is the total -- there shouldn't be anything left after this. Hooray!
        }
    }
    
    /**
     * GET handler for {@code /dsio/gw?p=savedata.getbw}
     */
    private void handleMemoryLink(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());

        // Check if Game Sync ID is valid
        if(!GsidUtility.isValidGameSyncId(request.gameSyncId())) {
            writeStatusCode(outputStream, 8); // Invalid Game Sync ID
            return;
        }

        Player player = playerManager.getPlayer(request.gameSyncId());
        User user = ctx.attribute("user");
        
        // Check if player exists
        if(player == null) {
            writeStatusCode(outputStream, 8); // Invalid Game Sync ID
            return;
        }
        
        // Version null check because this can happen in specific cases
        if(player.getGameVersion() == null) {
            writeStatusCode(outputStream, 5); // No game save data exists for this Game Sync ID
            return;
        }
        
        // Check if the save file belongs to Black or White
        if(player.getGameVersion().isVersion2()) {
            writeStatusCode(outputStream, 10); // Not a Black or White save
            return;
        }
        
        // Check if the game save data exists
        File file = player.getSaveFile();
        
        if(!file.exists()) {
            writeStatusCode(outputStream, 5); // No game save data exists for this Game Sync ID
            return;
        }
        
        logger.info("User {} is Memory Linking with player {}", user.getRedactedId(), player.getGameSyncId());
        
        // Send the save data!
        try(FileInputStream inputStream = new FileInputStream(file)) {
            writeStatusCode(outputStream, 0);
            inputStream.transferTo(outputStream);
        }
    }
    
    /**
     * POST base handler for {@code /dsio/gw}
     */
    private void handlePglPostRequest(Context ctx) throws IOException {
        // Retrieve context attributes
        PglRequest request = ctx.attribute("request");
        
        // Determine handler function based on request type
        HttpRequestHandler<PglRequest> handler = switch(request.type()) {
            case "savedata.upload" -> this::handleUploadSaveData; 
            case "savedata.download.finish" -> this::handleDownloadSaveDataFinish;
            case "account.create.upload" -> this::handleCreateAccount;
            case "account.createdata" -> this::handleCreateData;
            default -> throw new IllegalArgumentException("Invalid POST request type: " + request.type());
        };
        
        handler.process(request, ctx);    
    }
    
    /**
     * POST handler for {@code /dsio/gw?p=savedata.download.finish}
     */
    private void handleDownloadSaveDataFinish(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        Player player = playerManager.getPlayer(request.gameSyncId());
        
        // Check if player exists
        if(player == null) {
            writeStatusCode(outputStream, 1); // Unauthorized
            return;
        }
        
        // Reset player dream information if configured to do so
        if(configuration.clearPlayerDreamInfoOnWake()) {
            player.resetDreamInfo();
            
            // Try to save player data
            if(!playerManager.savePlayer(player)) {
                logger.warn("Save data failure for player {}", player.getGameSyncId());
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        // Write status code
        writeStatusCode(outputStream, 0);
    }
    
    /**
     * POST handler for {@code /dsio/gw?p=savedata.upload}
     */
    private void handleUploadSaveData(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        Player player = playerManager.getPlayer(request.gameSyncId());

        // Check if the player exists, has no Pokémon tucked in already and uses the same game version
        if(player == null
                || (!configuration.allowOverwritingPlayerDreamInfo() && player.getStatus() != PlayerStatus.AWAKE)
                || (!configuration.allowPlayerGameVersionMismatch() && player.getGameVersion() != null 
                        && request.gameVersion() != player.getGameVersion())) {
            // Skip everything
            ServletInputStream inputStream = ctx.req().getInputStream();
            
            while(!inputStream.isFinished()) {
                inputStream.read();
            }
            
            // Write error response
            writeStatusCode(outputStream, 1); // Unauthorized
            return;
        }
        
        logger.info("Player {} is uploading save data", player.getGameSyncId());
        
        // Try to store save data
        if(!playerManager.storePlayerGameSaveFile(player, ctx.bodyInputStream())) {
            writeStatusCode(outputStream, 4); // Game save data IO error
            return;
        }
        
        // Read save data
        TrainerInfo trainerInfo;
        PkmnInfo dreamerInfo;
        
        try(PointedInputStream inputStream = new PointedInputStream(new FileInputStream(player.getSaveFile()))) {

            inputStream.skipTo(Offsets.TRAINER_INFO);
            trainerInfo = TrainerInfoReader.readTrainerInfo(inputStream);

            inputStream.skipTo(Offsets.DREAM_WORLD_INFO);
            inputStream.skip(Offsets.POKEMON_INFO_SUB_OFFSET);
            dreamerInfo = PkmnInfoReader.readPokeInfo(inputStream);

            inputStream.skipTo(Offsets.ADVENTURE_START_TIME_OFFSET);
            inputStream.skip(Offsets.ADVENTURE_START_TIME_SUB_OFFSET);
            trainerInfo.setAdventureStartTime(TrainerInfoReader.readAdventureStartTime(inputStream));

            inputStream.skipTo(Offsets.getMoneyAndBadges(request.gameVersion()));
            trainerInfo.setMoney(TrainerInfoReader.readLong(inputStream));
            trainerInfo.setGymBadges(TrainerInfoReader.readGymBadges(inputStream));
        }

        // Update and save player information
        player.setStatus(PlayerStatus.SLEEPING);
        player.setGameVersion(request.gameVersion());
        player.setTrainerInfo(trainerInfo);
        player.setDreamerInfo(dreamerInfo);
        
        if(!playerManager.savePlayer(player)) {
            logger.warn("Save data failure for player {}", player.getGameSyncId());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Send status code
        writeStatusCode(outputStream, 0);
    }
    
    /**
     * POST handler for {@code /dsio/gw?p=account.create.upload}
     */
    private void handleCreateAccount(PglRequest request, Context ctx) throws IOException {
        // Have to read all the bytes first for some reason
        byte[] bytes = ctx.bodyAsBytes();
        
        // Prepare response
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        
        // Check if Game Sync ID is valid
        if(!GsidUtility.isValidGameSyncId(request.gameSyncId())) {
            writeStatusCode(outputStream, 8); // Invalid Game Sync ID
            return;
        }
        
        // Check if player doesn't exist already
        if(playerManager.doesPlayerExist(request.gameSyncId())) {
            writeStatusCode(outputStream, 2); // Duplicate Game Sync ID
            return;
        }
        
        // Try to register player
        Player player = playerManager.registerPlayer(request.gameSyncId(), request.gameVersion());
        
        if(player == null) {
            writeStatusCode(outputStream, 3); // Registration error
            return;
        }
        
        // Try to store save data
        if(!playerManager.storePlayerGameSaveFile(player, new ByteArrayInputStream(bytes))) {
            writeStatusCode(outputStream, 4); // Game save data IO error
            return;
        }
        
        // Write status code
        writeStatusCode(outputStream, 0);
    }
    
    /**
     * POST handler for {@code /dsio/gw?p=account.createdata}
     * 
     * Seems to be a funny Japanese version quirk
     */
    private void handleCreateData(PglRequest request, Context ctx) throws IOException {
        LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
        String gameSyncId = GsidUtility.stringifyGameSyncId(Integer.parseInt(ctx.body().replace("\u0000", ""))); // So quirky
        
        // Check if Game Sync ID is valid
        if(!GsidUtility.isValidGameSyncId(request.gameSyncId())) {
            writeStatusCode(outputStream, 8); // Invalid Game Sync ID
            return;
        }

        // Check if player doesn't exist already
        if(playerManager.doesPlayerExist(gameSyncId)) {
            writeStatusCode(outputStream, 2); // Duplicate Game Sync ID
            return;
        }
        
        // Try to register player
        // Regrettably, this request does not contain game save & version data.
        if(playerManager.registerPlayer(gameSyncId, null) == null) {
            writeStatusCode(outputStream, 3); // Registration error
            return;
        }
        
        // Write status code
        writeStatusCode(outputStream, 0);
    }
    
    /**
     * Writes the 4-byte status code and 124 empty bytes to the output stream.
     */
    private void writeStatusCode(LEOutputStream outputStream, int status) throws IOException {
        outputStream.writeInt(status);
        outputStream.writeBytes(0, 124);
    }

    /**
     * Gets the index of the player's chosen DLC for the specified type and prepares DLC overriding if necessary.
     */
    private int getDlcIndex(User user, String name, String type, File customFile) {
        if("custom".equals(name)) {
            user.setDlcOverride(type, new Dlc(customFile.getAbsolutePath(), name, "IRAO", type, 1, (int)customFile.length(), 0, true));
            return 1;
        } else {
            user.removeDlcOverride(type);
            return dlcList.getDlcIndex("IRAO", type, name);
        }
    }
}
