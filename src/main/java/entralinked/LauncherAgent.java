package entralinked;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stupid solution to a stupid problem.
 * If this just randomly breaks in the future because of some nonsense security reason I will completely lose it.
 */
public class LauncherAgent {
    
    private static final Logger logger = LogManager.getLogger();
    private static boolean bouncyCastlePresent = true;
    
    public static void agentmain(String args, Instrumentation instrumentation) {        
        try {
            String[] jarNames = {
                "bcutil-jdk15on-1.70.jar",
                "bcprov-jdk15on-1.70.jar",
                "bcpkix-jdk15on-1.70.jar"
            };
            
            for(int i = 0; i < jarNames.length; i++) {
                String jarName = jarNames[i];
                Path jarPath = Files.createTempFile(jarNames[i], null);
                File jarFile = jarPath.toFile();
                jarFile.deleteOnExit(); // Doesn't actually do anything on terminal exit because Java.
                Files.copy(LauncherAgent.class.getResourceAsStream("/%s".formatted(jarName)), jarPath, StandardCopyOption.REPLACE_EXISTING);
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            }
        } catch(Exception e) {
            logger.error("Could not add BouncyCastle to SystemClassLoader search", e);
            bouncyCastlePresent = false;
        }
    }
    
    public static boolean isBouncyCastlePresent() {
        return bouncyCastlePresent;
    }
}
