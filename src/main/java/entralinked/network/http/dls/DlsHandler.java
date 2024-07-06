package entralinked.network.http.dls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.Entralinked;
import entralinked.GameVersion;
import entralinked.model.user.ServiceSession;
import entralinked.model.user.User;
import entralinked.model.user.UserManager;
import entralinked.network.http.HttpHandler;
import entralinked.network.http.HttpRequestHandler;
import entralinked.serialization.UrlEncodedFormFactory;
import entralinked.utility.MysteryGiftUtility;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * HTTP handler for requests made to {@code dls1.nintendowifi.net}
 */
public class DlsHandler implements HttpHandler {
    
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper(new UrlEncodedFormFactory());
    private final File rootDirectory = new File("dlc");
    private final UserManager userManager;
    
    public DlsHandler(Entralinked entralinked) {
        this.userManager = entralinked.getUserManager();
    }
    
    @Override
    public void addHandlers(Javalin javalin) {
        javalin.post("/download", this::handleDownloadRequest);
    }
    
    /**
     * POST base handler for {@code /download}
     */
    private void handleDownloadRequest(Context ctx) throws IOException {
        // Deserialize request body
        DlsRequest request = mapper.readValue(ctx.body().replace("%2A", "*"), DlsRequest.class);
        logger.debug("Received {}", request);
        
        // Check if service session is valid
        ServiceSession session = userManager.getServiceSession(request.serviceToken(), "dls1.nintendowifi.net");
        
        if(session == null) {
            logger.debug("Rejecting DLS request because the service session has expired");
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Determine handler function based on request action
        HttpRequestHandler<DlsRequest> handler = switch(request.action()) {
            case "list" -> this::handleRetrieveDlcList;
            case "contents" -> this::handleRetrieveDlcContent;
            case "count" -> this::handleRetrieveDlcCount;
            default -> throw new IllegalArgumentException("Invalid POST request action: " + request.action());
        };
        
        // Store user attribute for subsequent handlers
        ctx.attribute("user", session.user());
        
        // Handle the request
        handler.process(request, ctx);
    }
    
    /**
     * POST handler for {@code /download action=list}
     */
    private void handleRetrieveDlcList(DlsRequest request, Context ctx) throws IOException {
        User user = ctx.attribute("user");
        String gameCode = getDlcGameCode(request.dlcGameCode());
        String type = getRegionlessDlcType(request.dlcType());
        String attr2 = request.attr2();
        List<File> files = user.hasDlcOverride(type) ? Arrays.asList(user.getDlcOverride(type)) 
                : Arrays.asList(getDlcDirectory(gameCode, type).listFiles());
        
        // Return empty string if no DLC could be found
        if(files == null) {
            ctx.result("");
            return;
        }
        
        // PGL content attr2 hack
        if(attr2 != null) {
            files = Arrays.asList(files.get(Integer.parseInt(attr2) - 1));
        }
        
        StringBuilder builder = new StringBuilder();
        int count = Math.min(files.size(), request.num());
        
        // Create DLC list string
        for(int i = 0; i < count; i++) {
            File file = files.get(i);
            
            if(type == null) {
                // Generation 4 Mystery Gift
                builder.append("%s\t\t\t\t\t%s\r\n".formatted(file.getName(), file.length()));
            } else if(type.equals("MYSTERY")) {
                // Generation 5 Mystery Gift
                String gameFlag = GameVersion.lookup(request.gameCode()).isVersion2() ? "F00000" : "300000";
                builder.append("%s\t\t%s\t%s\t\t%s\r\n".formatted(file.getName(), type, gameFlag, 720));
            } else {
                // PGL content
                builder.append("%s\t\t%s\t%s\t\t%s\r\n".formatted(file.getName(), type, i + 1, file.length()));
            }
        }
        
        // Send result
        ctx.result(builder.toString());
    }
    
    /**
     * POST handler for {@code /download action=contents}
     */
    private void handleRetrieveDlcContent(DlsRequest request, Context ctx) throws IOException {
        User user = ctx.attribute("user");
        String gameCode = getDlcGameCode(request.dlcGameCode());
        String type = getRegionlessDlcType(request.dlcType());
        File file = user.hasDlcOverride(type) ? user.getDlcOverride(type) : type != null 
                ? new File(rootDirectory, "%s/%s/%s".formatted(gameCode, type, request.dlcName()))
                : new File(rootDirectory, "%s/%s".formatted(gameCode, request.dlcName()));
        
        // Check if the requested DLC exists
        if(!file.exists()) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        
        if(type == null) {
            // Generation 4 Mystery Gift
            bytes = MysteryGiftUtility.createUniversalGiftData4(bytes);
        } else if(type.equals("MYSTERY")) {
            // Generation 5 Mystery Gift
            bytes = MysteryGiftUtility.createUniversalGiftData5(bytes);
        }
        
        // Send result
        ctx.result(bytes);
    }
    
    /**
     * POST handler for {@code /download action=count}
     */
    private void handleRetrieveDlcCount(DlsRequest request, Context ctx) throws IOException {
        ctx.result("1"); // TODO
    }
    
    /**
     * @return The game serial that should be used for downloading DLC based on the provided input.
     */
    private String getDlcGameCode(String gameCode) {
        return switch(gameCode.substring(0, 3)) {
            case "IRA" -> "IRAO"; // BW & B2W2
            case "ADA", "CPU", "IPG" -> "ADAE"; // DPPt & HGSS
            default -> gameCode;
        };
    }
    
    /**
     * @return The DLC type without the region identifier, or the input if it is an unknown type.
     */
    private String getRegionlessDlcType(String dlcType) {
        if(dlcType == null) {
            return null;
        }
        
        return switch(dlcType) {
            case "CGEAR_E", "CGEAR_F", "CGEAR_I", "CGEAR_G", "CGEAR_S", "CGEAR_J", "CGEAR_K" -> "CGEAR";
            case "CGEAR2_E", "CGEAR2_F", "CGEAR2_I", "CGEAR2_G", "CGEAR2_S", "CGEAR2_J", "CGEAR2_K" -> "CGEAR2";
            case "ZUKAN_E", "ZUKAN_F", "ZUKAN_I", "ZUKAN_G", "ZUKAN_S", "ZUKAN_J", "ZUKAN_K" -> "ZUKAN";
            case "MUSICAL_E", "MUSICAL_F", "MUSICAL_I", "MUSICAL_G", "MUSICAL_S", "MUSICAL_J", "MUSICAL_K" -> "MUSICAL";
            case "MYSTERY_E", "MYSTERY_F", "MYSTERY_I", "MYSTERY_G", "MYSTERY_S", "MYSTERY_J", "MYSTERY_K" -> "MYSTERY";
            default -> dlcType;
        };
    }
    
    private File getDlcDirectory(String gameCode, String dlcType) {
        return dlcType == null ? new File(rootDirectory, gameCode) : new File(rootDirectory, "%s/%s".formatted(gameCode, dlcType));
    }
}
