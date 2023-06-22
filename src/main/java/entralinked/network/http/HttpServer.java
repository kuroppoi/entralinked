package entralinked.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import entralinked.Entralinked;
import entralinked.LauncherAgent;
import entralinked.utility.CertificateGenerator;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.util.JavalinException;

public class HttpServer {
    
    private static final Logger logger = LogManager.getLogger();
    private static final String keyStorePassword = "password"; // Very secure!
    private final Javalin javalin;
    private boolean started;
    
    public HttpServer(Entralinked entralinked) {        
        // Create certificate keystore
        KeyStore keyStore = null;
        
        if(LauncherAgent.isBouncyCastlePresent()) {
            CertificateGenerator.initialize();
            logger.info("Creating certificate keystore ...");
            keyStore = createKeyStore();
            
            if(keyStore == null) {
                logger.warn("SSL will be disabled because keystore creation failed. You may have to manually sign a certificate.");
            }
        }
        
        KeyStore _keyStore = keyStore; // Java moment
        
        // Create Javalin instance
        javalin = Javalin.create(config -> {
            config.jetty.server(() -> createJettyServer(80, 443, _keyStore));
        });
        
        // Create exception handler
        javalin.exception(Exception.class, (exception, ctx) -> {
            logger.error("Caught exception", exception);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        });
        
        // Conntest
        javalin.get("/", ctx -> {
            ctx.header("X-Organization", "Nintendo"); // Conntest fails with 052210-1 if this is not present
            ctx.result("Test");
        });
    }
    
    public void addHandler(HttpHandler handler) {
        javalin.updateConfig(handler::configureJavalin); // Dirty
        handler.addHandlers(javalin);
    }
    
    public boolean start() {
        if(started) {
            logger.warn("start() was called while HTTP server was already running!");
            return true;
        }
        
        logger.info("Starting HTTP server ...");
        
        try {
            javalin.start();
        } catch(JavalinException e) {
            logger.error("Could not start HTTP server", e);
            return false;
        }
        
        started = true;
        return true;
    }
    
    public boolean stop() {
        if(!started) {
            logger.warn("stop() was called while HTTP server wasn't running!");
            return true;
        }
        
        logger.info("Stopping HTTP server ...");
        
        try {
            javalin.stop();
        } catch(JavalinException e) {
            logger.error("Could not stop HTTP server", e);
            return false;
        }
        
        started = false;
        return true;
    }
    
    private KeyStore createKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            File keyStoreFile = new File("server.p12");
            
            if(keyStoreFile.exists()) {
                // Load keystore from file if it exists
                logger.info("Cached keystore found - loading it!");
                keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
            } else {
                // Otherwise, generate a new one and store it in a file
                logger.info("No keystore found - generating one!");
                keyStore = CertificateGenerator.generateCertificateKeyStore("PKCS12", null);
                keyStore.store(new FileOutputStream(keyStoreFile), keyStorePassword.toCharArray());
            }
            
            return keyStore;
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Could not create keystore", e);
        }
        
        return null;
    }
    
    private Server createJettyServer(int port, int sslPort, KeyStore keyStore) {
         Server server = new Server();
         
         // Regular HTTP connector
         ServerConnector httpConnector = new ServerConnector(server);
         httpConnector.setPort(port);
         server.addConnector(httpConnector);
         
         if(keyStore != null) {
             // Create SSL/HTTPS connector if a keystore is present
             SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
             sslContextFactory.setIncludeProtocols("SSLv3");
             sslContextFactory.setExcludeProtocols("");
             sslContextFactory.setIncludeCipherSuites("SSL_RSA_WITH_RC4_128_SHA", "SSL_RSA_WITH_RC4_128_MD5");
             sslContextFactory.setExcludeCipherSuites("");
             sslContextFactory.setKeyStore(keyStore);
             sslContextFactory.setKeyStorePassword(keyStorePassword);
             sslContextFactory.setSslSessionCacheSize(0);
             sslContextFactory.setSslSessionTimeout(0);
             
             HttpConfiguration httpsConfiguration = new HttpConfiguration();
             httpsConfiguration.addCustomizer(new SecureRequestCustomizer(false));
             httpsConfiguration.setSendServerVersion(true);
             
             ServerConnector httpsConnector = new ServerConnector(server,
                 new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                 new HttpConnectionFactory(httpsConfiguration));
             
             httpsConnector.setPort(sslPort);
             server.addConnector(httpsConnector);
         }
         
         return server;
    }
}
