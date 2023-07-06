package entralinked.network.http.dashboard;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
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
import entralinked.utility.GsidUtility;
import entralinked.utility.TiledImageReader;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

/**
 * HTTP handler for requests made to the user dashboard.
 */
public class DashboardHandler implements HttpHandler {
    
    private static final Logger logger = LogManager.getLogger();
    private final Set<Integer> availableBlackAndWhiteSpecies = Set.of(
            505, 507, 510, 511, 513, 515, 519, 523, 525, 527, 529, 531, 533, 535, 538, 539, 542, 545, 546, 548, 
            550, 553, 556, 558, 559, 561, 564, 569, 572, 575, 578, 580, 583, 587, 588, 594, 596, 600, 605, 607, 
            610, 613, 616, 618, 619, 621, 622, 624, 626, 628, 630, 631, 632);
    private final Map<String, BufferedImage> skinPreviewCache = new HashMap<>();
    private final DlcList dlcList;
    private final PlayerManager playerManager;
    
    public DashboardHandler(Entralinked entralinked) {
        this.dlcList = entralinked.getDlcList();
        this.playerManager = entralinked.getPlayerManager();
        
        // Load & cache skin previews
        logger.info("Loading C-Gear and Pokédex skin previews ...");
        List<Dlc> skins = dlcList.getDlcList(dlc -> dlc.type().startsWith("CGEAR") || dlc.type().equals("ZUKAN"));
        
        for(Dlc skin : skins) {
            try(FileInputStream inputStream = new FileInputStream(skin.path())) {
                BufferedImage image = 
                        skin.type().equals("ZUKAN") ? TiledImageReader.readDexSkin(inputStream) :
                        skin.type().equals("CGEAR") ? TiledImageReader.readCGearSkin(inputStream, true) :
                        TiledImageReader.readCGearSkin(inputStream, false); // CGEAR2
                skinPreviewCache.put(skin.name(), image);
            } catch(IOException | IndexOutOfBoundsException e) {
                logger.error("Could not load image for skin {} of type {}", skin.name(), skin.type(), e);
            }
        }
        
        logger.info("Cached {} skin previews", skinPreviewCache.size());
    }
    
    @Override
    public void addHandlers(Javalin javalin) {
        javalin.get("/dashboard/previewskin", this::handlePreviewSkin);
        javalin.get("/dashboard/dlc", this::handleRetrieveDlcList);
        javalin.get("/dashboard/profile", this::handleRetrieveProfile);
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
        // Make sure that the name is present and exists
        String name = ctx.queryParam("name");
        
        if(name == null || !skinPreviewCache.containsKey(name)) {
            ctx.status(404);
            return;
        }
        
        // Write cached image data
        ImageIO.write(skinPreviewCache.get(name), "png", ctx.outputStream());
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
        ctx.json(new DashboardStatusMessage("ok")); // heh
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
            if(encounter.species() < 1) {
                return "Species is out of range.";
            } else if(encounter.species() > 493) {
                if(!player.getGameVersion().isVersion2()) {
                    return "Sorry, Generation V Pokémon are exclusive to Black Version 2 and White Version 2.";
                } else if(!availableBlackAndWhiteSpecies.contains(encounter.species())) {
                    return "You have selected one or more Pokémon species that cannot be downloaded.";
                }
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
            } else if(item.id() > 626 && !player.getGameVersion().isVersion2()) {
                return "You have selected one or more items that are exclusive to Black Version 2 and White Version 2.";
            }
            
            if(item.quantity() < 0 || item.quantity() > 20) {
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
