package entralinked.network.http.dashboard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.Entralinked;
import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.dlc.Dlc;
import entralinked.model.dlc.DlcList;
import entralinked.model.pkmn.PkmnGender;
import entralinked.model.pkmn.PkmnInfo;
import entralinked.model.player.DreamEncounter;
import entralinked.model.player.DreamItem;
import entralinked.model.player.Player;
import entralinked.model.player.PlayerManager;
import entralinked.model.player.PlayerStatus;
import entralinked.network.http.HttpHandler;
import entralinked.utility.Crc16;
import entralinked.utility.GsidUtility;
import entralinked.utility.LEOutputStream;
import entralinked.utility.TiledImageUtility;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

/**
 * HTTP handler for requests made to the user dashboard.
 * 
 * @deprecated
 */
public class DashboardHandler implements HttpHandler {
    
    private static final Logger logger = LogManager.getLogger();
    private final Map<Object, BufferedImage> skinPreviewCache = new HashMap<>();
    private final DlcList dlcList;
    private final PlayerManager playerManager;
    
    public DashboardHandler(Entralinked entralinked) {
        this.dlcList = entralinked.getDlcList();
        this.playerManager = entralinked.getPlayerManager();
        
        // Load & cache skin previews
        logger.info("Loading C-Gear and Pokédex skin previews ...");
        List<Dlc> skins = dlcList.getDlcList(dlc -> dlc.type().startsWith("CGEAR") || dlc.type().equals("ZUKAN"));
        
        // Cache default skins
        for(Dlc skin : skins) {
            try(FileInputStream inputStream = new FileInputStream(skin.path())) {
                BufferedImage image = skin.type().equals("ZUKAN") ? TiledImageUtility.readDexSkin(inputStream, true)
                        : skin.type().equals("CGEAR") ? TiledImageUtility.readCGearSkin(inputStream, true)
                        : TiledImageUtility.readCGearSkin(inputStream, false); // CGEAR2
                skinPreviewCache.put(skin, image);
            } catch(IOException | IndexOutOfBoundsException e) {
                logger.error("Could not load image for skin {} of type {}", skin.name(), skin.type(), e);
            }
        }
        
        // Cache custom skins for each player
        for(Player player : playerManager.getPlayers()) {
            File cgearSkinFile = player.getCGearSkinFile();
            File dexSkinFile = player.getDexSkinFile();
            
            // Cache custom C-Gear skin preview if it exists
            if(cgearSkinFile.exists()) {
                try(FileInputStream inputStream = new FileInputStream(cgearSkinFile)) {
                    boolean version2 = player.getGameVersion().isVersion2();
                    
                    // Read the C-Gear skin image
                    BufferedImage image = TiledImageUtility.readCGearSkin(inputStream, !version2);
                    
                    // Cache the result
                    skinPreviewCache.put("%s/%s".formatted(player.getGameSyncId(), version2 ? "CGEAR2" : "CGEAR"), image);
                } catch(IOException | IndexOutOfBoundsException | NullPointerException e) {
                    logger.error("Could not load custom C-Gear skin preview for player {}", player.getGameSyncId(), e);
                }
            }
            
            // Cache custom Pokédex skin preview if it exists
            if(dexSkinFile.exists()) {
                try(FileInputStream inputStream = new FileInputStream(dexSkinFile)) {
                    // Read the Pokédex skin image
                    BufferedImage image = TiledImageUtility.readDexSkin(inputStream, true);
                    
                    // Cache the result
                    skinPreviewCache.put("%s/ZUKAN".formatted(player.getGameSyncId()), image);
                } catch(IOException | IndexOutOfBoundsException e) {
                    logger.error("Could not load custom Pokédex skin preview for player {}", player.getGameSyncId(), e);
                }
            }
        }
        
        logger.info("Cached {} skin previews", skinPreviewCache.size());
    }
    
    @Override
    public void addHandlers(Javalin javalin) {
        javalin.get("/dashboard/previewskin", this::handlePreviewSkin);
        javalin.get("/dashboard/dlc", this::handleRetrieveDlcList);
        javalin.get("/dashboard/profile", this::handleRetrieveProfile);
        javalin.post("/dashboard/uploadskin", this::handleUploadSkin);
        javalin.post("/dashboard/profile", this::handleUpdateProfile);
        javalin.post("/dashboard/login", this::handleLogin);
        javalin.post("/dashboard/logout", this::handleLogout);
    }
    
    @Override
    public void configureJavalin(JavalinConfig config) {
        // Configure JSON mapper
        config.jsonMapper(new JavalinJackson(new ObjectMapper()));
        
        // Add dashboard pages
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.location = Location.CLASSPATH;
            staticFileConfig.directory = "/dashboard";
            staticFileConfig.hostedPath = "/dashboard";
        });
        
        // Add sprites
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.location = Location.CLASSPATH;
            staticFileConfig.directory = "/sprites";
            staticFileConfig.hostedPath = "/sprites";
        });
    }
    
    /**
     * GET request handler for {@code /dashboard/previewskin}
     */
    private void handlePreviewSkin(Context ctx) throws IOException {
        String type = ctx.queryParam("type");
        String name = ctx.queryParam("name");
        
        // Make sure query parameters are present
        if(type == null || name == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        
        // Handle custom skin preview
        if(name.equals("custom")) {
            Player player = ctx.sessionAttribute("player");
            
            // Check if player exists
            if(player == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                return;
            }
            
            BufferedImage previewImage = skinPreviewCache.get("%s/%s".formatted(player.getGameSyncId(), type));
            
            // Check if preview image exists
            if(previewImage == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                return;
            }
            
            ImageIO.write(previewImage, "png", ctx.outputStream());
            return;
        }
        
        // Handle DLC skin preview
        Dlc dlc = dlcList.getDlc("IRAO", type, name);
        
        // Check if DLC exists
        if(dlc == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        
        // Write cached image data
        ImageIO.write(skinPreviewCache.get(dlc), "png", ctx.outputStream());
    }
    
    /**
     * GET request handler for {@code /dashboard/dlc}
     */
    private void handleRetrieveDlcList(Context ctx) {
        // Make sure that the DLC type is present
        String type = ctx.queryParam("type");
        
        if(type == null) {
            ctx.json(Collections.EMPTY_LIST);
            return;
        }
        
        // Send result
        ctx.json(dlcList.getDlcList("IRAO", type).stream().map(Dlc::name).sorted().collect(Collectors.toList()));
    }
    
    /**
     * GET request handler for {@code /dashboard/profile}
     */
    private void handleRetrieveProfile(Context ctx) {
        // Validate session
        Player player = ctx.sessionAttribute("player");
        
        if(player == null || player.getStatus() == PlayerStatus.AWAKE) {
            ctx.json(new DashboardStatusMessage("Unauthorized", true));
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Send profile data
        ctx.json(new DashboardProfileMessage(getSpritePath(player.getDreamerInfo()), player));
    }
    
    /**
     * GET request handler for {@code /dashboard/login}
     */
    private void handleLogin(Context ctx) {
        String gsid = ctx.formParam("gsid");
        
        // Check if the Game Sync ID is valid
        if(gsid == null || !GsidUtility.isValidGameSyncId(gsid)) {
            ctx.json(new DashboardStatusMessage("Please enter a valid Game Sync ID.", true));
            return;
        }
        
        Player player = playerManager.getPlayer(gsid);
        
        // Check if the Game Sync ID exists
        if(player == null) {
            ctx.json(new DashboardStatusMessage("This Game Sync ID does not exist.", true));
            return;
        }
        
        // Check if there is stuff to play around with
        if(player.getStatus() == PlayerStatus.AWAKE) {
            ctx.json(new DashboardStatusMessage("Please use Game Sync to tuck in a Pokémon before proceeding.", true));
            return;
        }
        
        // Store session attribute and send response
        ctx.sessionAttribute("player", player);
        ctx.json(Collections.EMPTY_MAP);
    }
    
    /**
     * POST request handler for {@code /dashboard/uploadskin}
     */
    private void handleUploadSkin(Context ctx) throws IOException {
        Player player = ctx.sessionAttribute("player");
        
        // Check if player exists
        if(player == null) {
            ctx.json(new DashboardStatusMessage("Unauthorized", true));
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }
        
        String type = ctx.queryParam("type");
        String fileName = ctx.queryParam("filename");
        
        // Check if type & file name are present and are valid
        if(type == null || fileName == null ||
                (!type.equals("CGEAR") && !type.equals("CGEAR2") && !type.equals("ZUKAN"))) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        
        try {
            BufferedImage image = ImageIO.read(ctx.bodyInputStream());
            BufferedImage previewImage = null;
            
            // Make sure file is a valid image
            if(image == null) {
                ctx.json(new DashboardStatusMessage("File is either not an image or uses an unsupported format.", true));
                return;
            }
            
            // Make sure image has the correct dimensions
            if(image.getWidth() != 256 || image.getHeight() != 192) {
                ctx.json(new DashboardStatusMessage("Image must be 256 x 192 pixels.", true));
                return;
            }
            
            File outputFile = type.equals("ZUKAN") ? player.getDexSkinFile() : player.getCGearSkinFile();
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            byte[] skinBytes = null;
            
            // Write skin data to buffer and generate a preview image
            switch(type) {
                case "CGEAR":
                case "CGEAR2":
                    boolean offsetIndices = type.equals("CGEAR");
                    TiledImageUtility.writeCGearSkin(byteOutputStream, image, offsetIndices);
                    skinBytes = byteOutputStream.toByteArray();
                    previewImage = TiledImageUtility.readCGearSkin(new ByteArrayInputStream(skinBytes), offsetIndices);
                    break;
                case "ZUKAN":
                    // Process skin data
                    TiledImageUtility.writeDexSkin(byteOutputStream, image, TiledImageUtility.generateBackgroundColors(image));
                    skinBytes = byteOutputStream.toByteArray();
                    previewImage = TiledImageUtility.readDexSkin(new ByteArrayInputStream(skinBytes), true);
                    break;
            }
            
            // Write custom skin to output file
            try(LEOutputStream outputStream = new LEOutputStream(new FileOutputStream(outputFile))) {
                outputStream.write(skinBytes);
                outputStream.writeShort(Crc16.calc(skinBytes));
            }
            
            // Cache preview image
            skinPreviewCache.put("%s/%s".formatted(player.getGameSyncId(), type), previewImage);
        } catch(IOException e) {
            logger.error("An error occured while processing custom skin data", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        } catch(IllegalArgumentException e) {
            // Silently discard exception, only send feedback to the client
            ctx.json(new DashboardStatusMessage(e.getMessage(), true));
            return;
        }
        
        // Update player information
        if(type.equals("ZUKAN")) {
            player.setDexSkin("custom");
            player.setCustomDexSkin(fileName);
        } else {
            player.setCGearSkin("custom");
            player.setCustomCGearSkin(fileName);
        }
        
        // Try to save player data
        if(!playerManager.savePlayer(player)) {
            ctx.json(new DashboardStatusMessage("Profile data could not be saved due to an error.", true));
            return;
        }
        
        ctx.json(Collections.EMPTY_MAP);
    }
    
    /**
     * POST request handler for {@code /dashboard/logout}
     */
    private void handleLogout(Context ctx) {
        // Who cares if the session actually exists? I sure don't.
        ctx.consumeSessionAttribute("player");
        ctx.json(Collections.EMPTY_MAP);
    }
    
    /**
     * POST request handler for {@code /dashboard/profile}
     */
    private void handleUpdateProfile(Context ctx) {
        // Check if session exists
        Player player = ctx.sessionAttribute("player");
        
        if(player == null || player.getStatus() == PlayerStatus.AWAKE) {
            ctx.json(new DashboardStatusMessage("Unauthorized", true));
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Validate request data
        DashboardProfileUpdateRequest request = ctx.bodyAsClass(DashboardProfileUpdateRequest.class);
        String error = validateProfileUpdateRequest(player, request);
        
        if(error != null) {
            ctx.json(new DashboardStatusMessage("Profile data was NOT saved: " + error, true));
            return;
        }
        
        // Update profile
        player.setStatus(PlayerStatus.WAKE_READY);
        player.setEncounters(request.encounters());
        player.setItems(request.items());
        player.setAvenueVisitors(request.avenueVisitors());
        player.setCGearSkin(request.cgearSkin().equals("none") ? null : request.cgearSkin());
        player.setDexSkin(request.dexSkin().equals("none") ? null : request.dexSkin());
        player.setMusical(request.musical().equals("none") ? null : request.musical());
        player.setLevelsGained(request.gainedLevels());
        
        // Try to save profile data
        if(!playerManager.savePlayer(player)) {
            ctx.json(new DashboardStatusMessage("Profile data could not be saved because of an error.", true));
            return;
        }
        
        // Send response if all succeeded
        ctx.json(new DashboardStatusMessage("Your changes have been saved. Use Game Sync to wake up your Pokémon and download your selected content."));
    }
    
    /**
     * Validates a {@link DashboardProfileUpdateRequest} and returns an error string if the data is invalid.
     * If the data is valid, {@code null} is returned instead.
     */
    private String validateProfileUpdateRequest(Player player, DashboardProfileUpdateRequest request) {
        // Validate encounters
        if(request.encounters().size() > 10) {
            return "Encounter list size exceeds the limit.";
        }
        
        for(DreamEncounter encounter : request.encounters()) {
            if(encounter.species() < 1 || encounter.species() > 649) {
                return "Species is out of range.";
            } else if(encounter.move() < 0 || encounter.move() > 559) {
                return "Move ID is out of range.";
            } else if(encounter.gender() == null) {
                return "Gender is undefined.";
            } else if(encounter.animation() == null) {
                return "Animation is undefined.";
            }
            
            // TODO validate form maybe idk
        }
        
        // Validate items       
        if(request.items().size() > 20) {
            return "Item list size exceeds the limit.";
        }
        
        for(DreamItem item : request.items()) {
            if(item.id() < 0 || item.id() > 638) {
                return "Item ID is out of range";
            } else if(item.quantity() < 0 || item.quantity() > 20) {
                return "Item quantity is out of range.";
            }
        }
        
        // Validate Join Avenue visitors
        Set<String> avenueVisitorNames = new HashSet<>(); // For duplicate checking
        
        if(request.avenueVisitors().size() > 12) {
            return "Join Avenue visitor list size exceeds the limit.";
        }
        
        for(AvenueVisitor visitor : request.avenueVisitors()) {
            if(visitor.type() == null) {
                return "Join Avenue visitor type is undefined.";
            } else if(visitor.shopType() == null) {
                return "Join Avenue visitor shop type is undefined.";
            } else if(visitor.name().isBlank() || visitor.name().length() > 7) {
                return "Join Avenue visitor name must be between 1 and 7 characters in length.";
            } else if(avenueVisitorNames.contains(visitor.name())) {
                return "Join Avenue visitors cannot have the same name!";
            } else if(visitor.gameVersion() == null) {
                return "Join Avenue visitor game version is undefined.";
            } else if(visitor.dreamerSpecies() < 1 || visitor.dreamerSpecies() > 649) {
                return "Tucked-in Pokémon species of the Join Avenue visitor is out of range.";
            }
            
            avenueVisitorNames.add(visitor.name());
        }
        
        // Validate gained levels
        if(request.gainedLevels() < 0 || request.gainedLevels() > 99) {
            return "Gained levels is out of range.";
        }
        
        return null;
    }
    
    private String getSpritePath(PkmnInfo info) {
        String basePath = "/sprites/pokemon/%s".formatted(info.isShiny() ? "shiny" : "normal");
        String path = null;
        
        if(info.form() > 0) {
            path = "%s/%s-%s.png".formatted(basePath, info.species(), info.form());
        } else if(info.gender() == PkmnGender.FEMALE) {
            path = "%s/female/%s.png".formatted(basePath, info.species());
        }
        
        if(path == null || getClass().getResource(path) == null) {
            return "%s/%s.png".formatted(basePath, info.species());
        }
        
        return path;
    }
}
