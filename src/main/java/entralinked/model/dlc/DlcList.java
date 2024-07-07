package entralinked.model.dlc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import entralinked.utility.Crc16;
import entralinked.utility.MD5;

@Deprecated
public class DlcList {
    
    private static final Logger logger = LogManager.getLogger();
    private final List<Dlc> dlcList = new ArrayList<>();
    private final File dataDirectory = new File("dlc");
    
    public DlcList() {
        logger.info("Loading DLC ...");
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/dlc.paths")))) {
            reader.lines().forEach(line -> {
                String[] segments = line.split("\t");
                
                if(segments.length != 2) {
                    return;
                }
                
                String path = segments[0];
                String hash = segments[1];
                File outputFile = new File("./%s".formatted(path));
                
                if(outputFile.getParentFile() != null) {
                    outputFile.getParentFile().mkdirs();
                }
                
                try(InputStream inputStream = getClass().getResourceAsStream(path)){
                    if(!outputFile.exists() || !hash.equals(MD5.digest(outputFile))) {
                        Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch(IOException e) {
                    logger.error("Couldn't process resource '{}'", path, e);
                }
            });
        } catch(IOException e) {
            logger.error("Couldn't extract DLC data", e);
        }
        
        // Just to be sure...
        if(!dataDirectory.isDirectory()) {
            return;
        }
        
        // Game Serial level
        for(File file : dataDirectory.listFiles()) {
            // Make sure that file is a directory, log warning and skip otherwise
            if(!file.isDirectory()) {
                logger.warn("Non-directory '{}' in DLC root folder", file.getName());
                continue;
            }
            
            // DLC Type level
            for(File subFile : file.listFiles()) {
                // Check if file is directory
                if(!subFile.isDirectory()) {
                    logger.warn("Non-directory '{}' in DLC subfolder '{}'", subFile.getName(), file.getName());
                    continue;
                }
                
                int index = 1;
                
                // DLC Content level
                for(File dlcFile : subFile.listFiles()) {
                    String name = dlcFile.getName();
                    
                    // Check if DLC name is reserved as an internal identifier
                    if(name.equals("none") || name.equals("custom")) {
                        logger.warn("DLC '{}/{}/{}' could not be loaded because it uses a reserved name.",
                                file.getName(), subFile.getName(), name);
                        continue;
                    }
                    
                    // Load DLC data
                    Dlc dlc = loadDlcFile(file.getName(), subFile.getName(), index, dlcFile);
                    
                    if(dlc != null) {
                        dlcList.add(dlc);
                        index++;
                    }
                }
            }
        }
        
        logger.info("Loaded {} DLC file(s)", dlcList.size());
    }
    
    private Dlc loadDlcFile(String gameCode, String type, int index, File dlcFile) {
        String name = dlcFile.getName();
        
        if(dlcFile.isDirectory()) {
            logger.warn("Directory '{}' in {} DLC folder", name, gameCode);
            return null;
        }
        
        // Check if there is a valid CRC-16 checksum appended at the end of the file.
        // If not, it will be marked in the DLC record object and the server will automatically append the checksum
        // when the DLC content is requested.
        // Makes it easier to just throw stuff into the DLC folder.
        int projectedSize = 0;
        int checksum = 0;
        boolean checksumEmbedded = true;
        
        try {
            byte[] bytes = Files.readAllBytes(dlcFile.toPath());
            projectedSize = bytes.length;
            checksum = Crc16.calc(bytes, 0, bytes.length - 2);
            int checksumInFile = (bytes[bytes.length - 2] & 0xFF) | ((bytes[bytes.length - 1] & 0xFF) << 8);
            
            if(checksum != checksumInFile) {
                logger.warn("Checksum mismatch in DLC '{}'", name);
                projectedSize += 2;
                checksum = Crc16.calc(bytes, 0, bytes.length);
                checksumEmbedded = false;
            }
        } catch(IOException e) {
            logger.error("Could not read checksum data for {}", dlcFile.getAbsolutePath(), e);
            return null;
        }
        
        return new Dlc(dlcFile.getAbsolutePath(), name, gameCode, type, index, projectedSize, checksum, checksumEmbedded);
    }
    
    public List<Dlc> getDlcList(Predicate<Dlc> filter) {
        return getDlc().stream().filter(filter).collect(Collectors.toList());
    }
    
    public List<Dlc> getDlcList(String gameCode, String type, int index) {
        return getDlcList(dlc -> 
            dlc.gameCode().equals(gameCode) &&
            dlc.type().equals(type) &&
            dlc.index() == index
        );
    }
    
    public List<Dlc> getDlcList(String gameCode, String type) {
        return getDlcList(dlc ->
            dlc.gameCode().equals(gameCode) &&
            dlc.type().equals(type)
        );
    }
    
    public List<Dlc> getDlcList(String gameCode) {
        return getDlcList(dlc -> dlc.gameCode().equals(gameCode));
    }
    
    public String getDlcListString(Collection<Dlc> dlcList) {
        StringBuilder builder = new StringBuilder();
        dlcList.forEach(dlc -> {
            builder.append("%s\t\t%s\t%s\t\t%s\r\n".formatted(dlc.name(), dlc.type(), dlc.index(), dlc.projectedSize()));
        });
        
        return builder.toString();
    }
    
    public Dlc getDlc(String gameCode, String type, String name) {
        List<Dlc> dlcList = getDlcList(gameCode, type).stream()
                .filter(dlc -> dlc.name().equals(name)).collect(Collectors.toList());
        return dlcList.isEmpty() ? null : dlcList.get(0);
    }
    
    public int getDlcIndex(String gameCode, String type, String name) {
        Dlc dlc = getDlc(gameCode, type, name);
        return dlc == null ? 0 : dlc.index();
    }
    
    public Collection<Dlc> getDlc() {
        return Collections.unmodifiableCollection(dlcList);
    }
}
