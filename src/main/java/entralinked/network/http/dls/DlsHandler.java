package entralinked.network.http.dls;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.Entralinked;
import entralinked.model.dlc.Dlc;
import entralinked.model.dlc.DlcList;
import entralinked.model.user.ServiceSession;
import entralinked.model.user.User;
import entralinked.model.user.UserManager;
import entralinked.network.http.HttpHandler;
import entralinked.network.http.HttpRequestHandler;
import entralinked.serialization.UrlEncodedFormFactory;
import entralinked.utility.LEOutputStream;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * HTTP handler for requests made to {@code dls1.nintendowifi.net}
 */
public class DlsHandler implements HttpHandler {
    
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper(new UrlEncodedFormFactory());
    private final DlcList dlcList;
    private final UserManager userManager;
    
    public DlsHandler(Entralinked entralinked) {
        this.dlcList = entralinked.getDlcList();
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
        
        // If an overriding DLC is present, send the data for that instead.
        if(user.hasDlcOverride(type)) {
            ctx.result(dlcList.getDlcListString(List.of(user.getDlcOverride(type))));
            return;
        }
        
        // TODO NOTE: I assume that in a conventional implementation, certain DLC attributes may be omitted from the request.
        ctx.result(dlcList.getDlcListString(dlcList.getDlcList(gameCode, type, request.dlcIndex())));
    }
    
    /**
     * POST handler for {@code /download action=contents}
     */
    private void handleRetrieveDlcContent(DlsRequest request, Context ctx) throws IOException {
        User user = ctx.attribute("user");
        String gameCode = getDlcGameCode(request.dlcGameCode());
        String type = getRegionlessDlcType(request.dlcType());
        Dlc dlc = user.hasDlcOverride(type) ? user.getDlcOverride(type) : dlcList.getDlc(gameCode, type, request.dlcName());
        
        // Check if the requested DLC exists
        if(dlc == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        
        // Write DLC data
        try(FileInputStream inputStream = new FileInputStream(dlc.path())) {
            LEOutputStream outputStream = new LEOutputStream(ctx.outputStream());
            inputStream.transferTo(outputStream);
            
            // If checksum is not part of the file, manually append it
            if(!dlc.checksumEmbedded()) {
                outputStream.writeShort(dlc.checksum());
            }
        }
    }
    
    /**
     * @return The game serial that should be used for downloading DLC based on the provided input.
     */
    private String getDlcGameCode(String gameCode) {
        return switch(gameCode) {
            case "IRAJ" -> "IRAO";
            default -> gameCode;
        };
    }
    
    /**
     * @return The DLC type without the region identifier, or the input if it is an unknown type.
     */
    private String getRegionlessDlcType(String dlcType) {
        return switch(dlcType) {
            case "CGEAR_E", "CGEAR_F", "CGEAR_I", "CGEAR_G", "CGEAR_S", "CGEAR_J", "CGEAR_K" -> "CGEAR";
            case "CGEAR2_E", "CGEAR2_F", "CGEAR2_I", "CGEAR2_G", "CGEAR2_S", "CGEAR2_J", "CGEAR2_K" -> "CGEAR2";
            case "ZUKAN_E", "ZUKAN_F", "ZUKAN_I", "ZUKAN_G", "ZUKAN_S", "ZUKAN_J", "ZUKAN_K" -> "ZUKAN";
            case "MUSICAL_E", "MUSICAL_F", "MUSICAL_I", "MUSICAL_G", "MUSICAL_S", "MUSICAL_J", "MUSICAL_K" -> "MUSICAL";
            default -> dlcType;
        };
    }
}
