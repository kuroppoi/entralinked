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

import entralinked.gui.view.MainView;
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
import entralinked.utility.NetworkUtility;

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
	private boolean initialized;

	public Entralinked(String[] args) {
		long beginTime = System.currentTimeMillis();

		// Read command line arguments
		CommandLineArguments arguments = new CommandLineArguments(args);

		// Create GUI if enabled
		if (!arguments.disableGui()) {
			try {
				SwingUtilities.invokeAndWait(() -> mainView = new MainView(this));
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error("An error occured whilst creating main view", e);
			}
		}

		// Log premain errors
		if (!LauncherAgent.isBouncyCastlePresent()) {
			logger.error("Could not add BouncyCastle to SystemClassLoader search", LauncherAgent.getCause());
		}

		// Load config
		configuration = loadConfigFile();
		logger.info("Using configuration {}", configuration);

		InetAddress hostAddress = null;
		// Get host address
		if (configuration.hostAddress() != null) {
			try {
				hostAddress = InetAddress.getByName(configuration.hostAddress());
			} catch (UnknownHostException e) {
				hostAddress = null;
			}
		}
		String hostName = configuration.hostName();

		if (hostAddress == null) {
			if (hostName.equals("local") || hostName.equals("localhost")) {
				hostAddress = NetworkUtility.getLocalHost();
			} else {
				try {
					hostAddress = InetAddress.getByName(hostName);
				} catch (UnknownHostException e) {
					hostAddress = NetworkUtility.getLocalHost();
					logger.error("Could not resolve host name - falling back to {} ", hostAddress, e);
				}
			}
		}

		// Emergency stop if host address manages to be null somehow
		if (hostAddress == null) {
			logger.fatal("ABORTING - hostAddress is null!");
			System.exit(1);
		}

		// Load persistent data
		dlcList = new DlcList();
		userManager = new UserManager();
		playerManager = new PlayerManager();

		// Create DNS server
		dnsServer = new DnsServer(hostAddress);

		// Create GameSpy server
		gameSpyServer = new GameSpyServer(this);

		// Create HTTP server
		httpServer = new HttpServer(this);
		httpServer.addHandler(new NasHandler(this));
		httpServer.addHandler(new PglHandler(this));
		httpServer.addHandler(new DlsHandler(this));
		httpServer.addHandler(new DashboardHandler(this));

		// Start servers
		boolean started = startServers();

		// Post-startup
		if (started) {
			String hostIpAddress = hostAddress.getHostAddress();
			logger.info("Startup complete! Took a total of {} milliseconds", System.currentTimeMillis() - beginTime);
			logger.info("Configure your DS to use the following DNS server: {}", hostIpAddress);

			if (mainView != null) {
				SwingUtilities.invokeLater(() -> {
					mainView.setStatusLabelText(
							"Configure your DS to use the following DNS server: %s".formatted(hostIpAddress));
				});
			}
		} else {
			stopServers();

			if (mainView != null) {
				SwingUtilities.invokeLater(() -> mainView.setStatusLabelText(
						"ERROR: Entralinked failed to start. Please check the logs for info."));
			}
		}

		initialized = true;
	}

	public boolean startServers() {
		logger.info("Starting servers ...");
		return httpServer.start() && gameSpyServer.start() && dnsServer.start();
	}

	public void stopServers() {
		logger.info("Stopping servers ...");

		if (httpServer != null) {
			httpServer.stop();
		}

		if (gameSpyServer != null) {
			gameSpyServer.stop();
		}

		if (dnsServer != null) {
			dnsServer.stop();
		}
	}

	private Configuration loadConfigFile() {
		logger.info("Loading configuration ...");
		Configuration configuration = null;

		try {
			File configFile = new File("config.json");

			if (!configFile.exists()) {
				logger.info("No configuration file exists - default configuration will be used");
				configuration = Configuration.DEFAULT;
			} else {
				configuration = mapper.readValue(configFile, Configuration.class);
			}

			mapper.writeValue(configFile, configuration);
		} catch (IOException e) {
			logger.error("Could not load configuration - default configuration will be used", e);
			configuration = Configuration.DEFAULT;
		}

		return configuration;
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

	public boolean isInitialized() {
		return initialized;
	}
}
