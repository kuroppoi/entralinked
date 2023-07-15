package entralinked.model.player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Manager class for managing {@link Player} information (Global Link users)
 */
public class PlayerManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, Player> playerMap = new ConcurrentHashMap<>();
    private final File dataDirectory = new File("players");
    
    public PlayerManager() {
        logger.info("Loading player data ...");
        
        // Check if player directory exists
        if(!dataDirectory.exists()) {
            return;
        }
        
        // Migrate player data if necessary
        migrateLegacyData();
        
        // Load player data
        for(File file : dataDirectory.listFiles()) {
            if(file.isDirectory()) {
                loadPlayer(new File(file, "data.json"));
            }
        }
        
        logger.info("Loaded {} player(s)", playerMap.size());
    }
    
    /**
     * Migrates player data from the old storage format.
     */
    private void migrateLegacyData() {
        // Migrate player configuration files
        for(File file : dataDirectory.listFiles()) {
            if(!file.isDirectory()) {
                try {
                    PlayerDto player = mapper.readValue(file, PlayerDto.class);
                    File outputDirectory = new File(dataDirectory, player.gameSyncId());
                    outputDirectory.mkdirs();
                    Files.copy(file.toPath(), new File(outputDirectory, "data.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    file.delete();
                } catch(IOException e) {
                    logger.error("Could not migrate player data {}", file.getName());
                }
            }
        }
        
        // Migrate game save data files
        File gameSaveDirectory = new File(dataDirectory, "savedata");
        
        if(gameSaveDirectory.exists()) {
            for(File file : gameSaveDirectory.listFiles()) {
                if(!file.isDirectory()) {
                    String name = file.getName();
                    
                    try {
                        if(!name.matches("^SAV-[A-HJ-NP-Z2-9]{10}.bin$")) {
                            logger.warn("Game save data {} will not be migrated because the file name is invalid", name);
                            file.delete();
                            continue;
                        }
                        
                        String gameSyncId = name.split("-")[1].replace(".bin", "");
                        File outputDirectory = new File(dataDirectory, gameSyncId);
                        
                        if(!outputDirectory.isDirectory()) {
                            logger.warn("Game save data {} will not be migrated because no Game Sync data exists for it", name);
                            file.delete();
                            continue;
                        }
                        
                        Files.copy(file.toPath(), new File(outputDirectory, "save.bin").toPath(), StandardCopyOption.REPLACE_EXISTING);
                        file.delete();
                    } catch(IOException e) {
                        logger.error("Could not migrate game save data {}", name, e);
                    }
                }
            }
            
            gameSaveDirectory.delete();
        }
    }
    
    /**
     * Loads a {@link Player} from the specified input file.
     * The loaded player instance is automatically mapped, unless it has an already-mapped Game Sync ID.
     */
    private void loadPlayer(File inputFile) {
        try {
            // Deserialize player data
            Player player = mapper.readValue(inputFile, PlayerDto.class).toPlayer();
            String gameSyncId = player.getGameSyncId();
            
            // Check for duplicate Game Sync ID
            if(doesPlayerExist(gameSyncId)) {
                throw new IOException("Duplicate Game Sync ID %s".formatted(gameSyncId));
            }
            
            player.setDataDirectory(inputFile.getParentFile());
            playerMap.put(gameSyncId, player);
        } catch(IOException e) {
            logger.error("Could not load player data at {}", inputFile.getAbsolutePath(), e);
        }
    }
    
    /**
     * Saves all player data.
     */
    public void savePlayers() {
        playerMap.values().forEach(this::savePlayer);
    }
    
    /**
     * Saves the data of the specified player to disk, and returns {@code true} if it succeeds.
     */
    public boolean savePlayer(Player player) {
        return savePlayer(player, player.getDataFile());
    }
    
    /**
     * Saves the data of the specified player to the specified output file.
     * 
     * @return {@code true} if the data was saved successfully, {@code false} otherwise.
     */
    private boolean savePlayer(Player player, File outputFile) {
        try {
            // Create parent directories
            File parentFile = outputFile.getParentFile();
            
            if(parentFile != null) {
                parentFile.mkdirs();
            }
            
            // Serialize the entire player object first to minimize risk of corrupted files
            byte[] bytes = mapper.writeValueAsBytes(new PlayerDto(player));
            
            // Write serialized data to output file
            Files.write(outputFile.toPath(), bytes);
        } catch(IOException e) {
            logger.error("Could not save player data for {}", player.getGameSyncId(), e);
            return false;
        }
        
        return true;
    }
    
    /**
     * Attempts to register a new {@link Player} with the given data.
     * 
     * @return The newly created {@link Player} object if the registration succeeded.
     * That is, the specified Game Sync ID wasn't already registered and the player data
     * was saved without any errors.
     */
    public Player registerPlayer(String gameSyncId) {
        // Check for duplicate Game Sync ID
        if(playerMap.containsKey(gameSyncId)) {
            logger.warn("Attempted to register duplicate player {}", gameSyncId);
            return null;
        }
        
        // Create data directory
        File playerDataDirectory = new File(dataDirectory, gameSyncId);
        
        if(playerDataDirectory.exists()) {
            logger.warn("Can't register player {} because the data directory already exists!", gameSyncId);
            return null;
        }
        
        // Construct player object
        Player player = new Player(gameSyncId);
        player.setStatus(PlayerStatus.AWAKE);
        player.setDataDirectory(playerDataDirectory);
        
        // Try to save player data
        if(!savePlayer(player)) {
            return null;
        }
        
        // Map player object & return it
        playerMap.put(gameSyncId, player);
        return player;
    }
    
    /**
     * Stores a game save file read from the specified input stream to a file.
     * 
     * @return {@code true} if the game save file was written successfully, otherwise {@code false}.
     */
    public boolean storePlayerGameSaveFile(Player player, InputStream inputStream) {
        try(FileOutputStream outputStream = new FileOutputStream(player.getSaveFile())) {
            inputStream.transferTo(outputStream);
            return true;
        } catch(IOException e) {
            logger.error("Could not write game save data for player {}", player.getGameSyncId());
            return false;
        }
    }
    
    /**
     * @return {@code true} if a player with the specified Game Sync ID exists, {@code false} otherwise.
     */
    public boolean doesPlayerExist(String gameSyncId) {
        return playerMap.containsKey(gameSyncId);
    }
    
    /**
     * @return The {@link Player} object to which this Game Sync ID belongs, or {@code null} if no such player exists.
     */
    public Player getPlayer(String gameSyncId) {
        return playerMap.get(gameSyncId);
    }
    
    /**
     * @return An immutable {@link Collection} containing all players.
     */
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(playerMap.values());
    }
}
