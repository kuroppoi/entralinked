package entralinked;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import entralinked.gui.MainView;
import entralinked.model.dlc.DlcList;
import entralinked.model.player.PlayerManager;
import entralinked.model.user.UserManager;
import entralinked.network.dns.DnsServer;
import entralinked.network.gamespy.GameSpyServer;
import entralinked.network.http.HttpServer;
import entralinked.network.http.dashboard.DashboardHandler;
import entralinked.network.http.dls.DlsHandler;
import entralinked.network.http.nas.NasHandler;
import entralinked.network.http.pgl.PglHandler;

public class Entralinked {
    
    public static void main(String[] args) {
        new Entralinked(args);
    }
    
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Configuration configuration;
    private final DlcList dlcList;
    private final UserManager userManager;
    private final PlayerManager playerManager;
    private final DnsServer dnsServer;
    private final GameSpyServer gameSpyServer;
    private final HttpServer httpServer;
    private MainView mainView;
    
    public Entralinked(String[] args) {
        // Read command line arguments
        CommandLineArguments arguments = new CommandLineArguments(args);
        
        // Create GUI if enabled
        if(!arguments.disableGui()) {
            try {
                SwingUtilities.invokeAndWait(() -> mainView = new MainView(this));
            } catch (InvocationTargetException | InterruptedException e) {
                logger.error("An error occured whilst creating main view", e);
            }
        }
        
        // Load config
        configuration = loadConfigFile();
        logger.info("Using configuration {}", configuration);
        
        // Get host address
        InetAddress hostAddress = null;
        String hostName = configuration.hostName();
        
        if(hostName.equals("local") || hostName.equals("localhost")) {
            hostAddress = getLocalHost();
        } else {
            try {
                hostAddress = InetAddress.getByName(hostName);
            } catch(UnknownHostException e) {
                hostAddress = getLocalHost();
                logger.error("Could not resolve host name - falling back to {} ", hostAddress, e);
            }
        }
        
        // Load persistent data
        dlcList = new DlcList();
        userManager = new UserManager();
        playerManager = new PlayerManager();
        
        // Start servers
        boolean started = true;
        
        // Create DNS server
        dnsServer = new DnsServer(hostAddress);
        started &= dnsServer.start();
        
        // Create GameSpy server
        gameSpyServer = new GameSpyServer(this);
        started &= gameSpyServer.start();
        
        // Create HTTP server
        httpServer = new HttpServer(this);
        httpServer.addHandler(new NasHandler(this));
        httpServer.addHandler(new PglHandler(this));
        httpServer.addHandler(new DlsHandler(this));
        httpServer.addHandler(new DashboardHandler(this));
        started &= httpServer.start();
        
        // Handle post-startup GUI stuff
        if(mainView != null) {
            if(!started) {
                SwingUtilities.invokeLater(() -> mainView.setStatusLabelText(
                        "ERROR: One or more servers failed to start! Please check the logs for info."));
                return;
            }
            
            String hostIpAddress = hostAddress.getHostAddress();
            SwingUtilities.invokeLater(() -> {
                mainView.setDashboardButtonEnabled(true);
                mainView.setStatusLabelText("Configure your DS to use the following DNS server: %s".formatted(hostIpAddress));
            });
        }
    }
    
    public void stopServers() {
        if(httpServer != null) {
            httpServer.stop();
        }
        
        if(gameSpyServer != null) {
            gameSpyServer.stop();
        }
        
        if(dnsServer != null) {
            dnsServer.stop();
        }
    }
    
    private Configuration loadConfigFile() {
        logger.info("Loading configuration ...");
        
        try {
            File configFile = new File("config.json");
            
            if(!configFile.exists()) {
                logger.info("No configuration file exists - default configuration will be used");
                mapper.writeValue(configFile, Configuration.DEFAULT);
                return Configuration.DEFAULT;
            } else {
                return mapper.readValue(configFile, Configuration.class);
            }
        } catch(IOException e) {
            logger.error("Could not load configuration - default configuration will be used", e);
            return Configuration.DEFAULT;
        }
    }
    
    private InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch(UnknownHostException e) {
            logger.error("Could not resolve local host", e);
            return null;
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public DlcList getDlcList() {
        return dlcList;
    }
    
    public UserManager getUserManager() {
        return userManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
