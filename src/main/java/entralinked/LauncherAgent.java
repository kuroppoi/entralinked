package entralinked;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.jar.JarFile;

/**
 * Stupid solution to a stupid problem.
 * If this just randomly breaks in the future because of some nonsense security reason I will completely lose it.
 */
public class LauncherAgent {
    
    private static boolean bouncyCastlePresent = true;
    private static Exception cause;
    
    public static void agentmain(String args, Instrumentation instrumentation) {        
        try {
            String[] jarNames = {
                "bcutil-jdk15on-1.70.jar",
                "bcprov-jdk15on-1.70.jar",
                "bcpkix-jdk15on-1.70.jar"
            };
            
            // Try create library directory
            File libraryDirectory = new File("libs");
            libraryDirectory.mkdirs();
            
            // Extract jars if necessary
            for(int i = 0; i < jarNames.length; i++) {
                String jarName = jarNames[i];
                File jarFile = new File(libraryDirectory, jarName);
                
                if(!jarFile.exists()) {
                    Files.copy(LauncherAgent.class.getResourceAsStream("/%s".formatted(jarName)), jarFile.toPath());
                }
                
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            }
        } catch(Exception e) {
            bouncyCastlePresent = false;
            cause = e; // Store exception to log later
        }
    }
    
    public static boolean isBouncyCastlePresent() {
        return bouncyCastlePresent;
    }
    
    public static Exception getCause() {
        return cause;
    }
}
