package entralinked.model.player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import entralinked.GameVersion;
import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.pkmn.PkmnInfo;

public class Player {
    
    private final String gameSyncId;
    private final List<DreamEncounter> encounters = new ArrayList<>();
    private final List<DreamItem> items = new ArrayList<>();
    private final List<AvenueVisitor> avenueVisitors = new ArrayList<>();
    private final List<DreamDecor> decor = new ArrayList<>();
    private PlayerStatus status;
    private GameVersion gameVersion;
    private TrainerInfo trainerInfo;
    private PkmnInfo dreamerInfo;
    private int levelsGained;
    private String cgearSkin;
    private String dexSkin;
    private String musical;
    private String customCGearSkin;
    private String customDexSkin;
    private String customMusical;
    private File dataDirectory;
    
    public Player(String gameSyncId) {
        this.gameSyncId = gameSyncId;
    }
    
    public void resetDreamInfo() {
        status = PlayerStatus.AWAKE;
        dreamerInfo = null;
        encounters.clear();
        items.clear();
        avenueVisitors.clear();
        decor.clear();
        decor.addAll(DreamDecor.DEFAULT_DECOR);
        levelsGained = 0;
        cgearSkin = null;
        dexSkin = null;
        musical = null;
    }
    
    public String getGameSyncId() {
        return gameSyncId;
    }
    
    public void setEncounters(Collection<DreamEncounter> encounters) {
        setList(encounters, this.encounters, 10);
    }
    
    public List<DreamEncounter> getEncounters() {
        return Collections.unmodifiableList(encounters);
    }
    
    public void setItems(Collection<DreamItem> items) {
        setList(items, this.items, 20);
    }
    
    public List<DreamItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public void setAvenueVisitors(Collection<AvenueVisitor> avenueVisitors) {
        setList(avenueVisitors, this.avenueVisitors, 12);
    }
    
    public List<AvenueVisitor> getAvenueVisitors() {
        return Collections.unmodifiableList(avenueVisitors);
    }
    
    public void setDecor(Collection<DreamDecor> decor) {
        setList(decor, this.decor, 5);
    }

    public List<DreamDecor> getDecor() {
        return Collections.unmodifiableList(decor);
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public PlayerStatus getStatus() {
        return status;
    }

    public void setTrainerInfo(TrainerInfo trainerInfo) {
        this.trainerInfo = trainerInfo;
    }

    public TrainerInfo getTrainerInfo() {
        return trainerInfo;
    }

    public void setGameVersion(GameVersion gameVersion) {
        this.gameVersion = gameVersion;
    }
    
    public GameVersion getGameVersion() {
        return gameVersion;
    }
    
    public void setDreamerInfo(PkmnInfo dreamerInfo) {
        this.dreamerInfo = dreamerInfo;
    }
    
    public PkmnInfo getDreamerInfo() {
        return dreamerInfo;
    }
    
    public void setLevelsGained(int levelsGained) {
        this.levelsGained = levelsGained;
    }
    
    public int getLevelsGained() {
        return levelsGained;
    }
    
    public void setCGearSkin(String cgearSkin) {
        this.cgearSkin = cgearSkin;
    }
    
    public String getCGearSkin() {
        return cgearSkin;
    }
    
    public void setDexSkin(String dexSkin) {
        this.dexSkin = dexSkin;
    }
    
    public String getDexSkin() {
        return dexSkin;
    }
    
    public void setMusical(String musical) {
        this.musical = musical;
    }
    
    public String getMusical() {
        return musical;
    }
    
    public void setCustomCGearSkin(String customCGearSkin) {
        this.customCGearSkin = customCGearSkin;
    }
    
    public String getCustomCGearSkin() {
        return customCGearSkin;
    }
    
    public void setCustomDexSkin(String customDexSkin) {
        this.customDexSkin = customDexSkin;
    }
    
    public String getCustomDexSkin() {
        return customDexSkin;
    }
    
    public void setCustomMusical(String customMusical) {
        this.customMusical = customMusical;
    }

    public String getCustomMusical() {
        return customMusical;
    }

    // IO stuff
    
    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
    
    public File getDataDirectory() {
        return dataDirectory;
    }
    
    public File getDataFile() {
        return new File(dataDirectory, "data.json");
    }
    
    public File getSaveFile() {
        return new File(dataDirectory, "save.bin");
    }
    
    public File getCGearSkinFile() {
        return new File(dataDirectory, "cgear.bin");
    }
    
    public File getDexSkinFile() {
        return new File(dataDirectory, "zukan.bin");
    }

    public File getMusicalFile() {
        return new File(dataDirectory, "musical.bin");
    }

    private static <T> void setList(Collection<T> source, List<T> target, int maxSize) {
        if(source.size() <= maxSize) {
            target.clear();
            target.addAll(source);
        }
    }
}
