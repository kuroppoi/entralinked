package entralinked.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import entralinked.GameVersion;
import entralinked.model.pkmn.PkmnInfo;

public class Player {
    
    private final String gameSyncId;
    private final GameVersion gameVersion;
    private final List<DreamEncounter> encounters = new ArrayList<>();
    private final List<DreamItem> items = new ArrayList<>();
    private PlayerStatus status;
    private PkmnInfo dreamerInfo;
    private int levelsGained;
    private String cgearSkin;
    private String dexSkin;
    private String musical;
    
    public Player(String gameSyncId, GameVersion gameVersion) {
        this.gameSyncId = gameSyncId;
        this.gameVersion = gameVersion;
    }
    
    public void resetDreamInfo() {
        status = PlayerStatus.AWAKE;
        dreamerInfo = null;
        encounters.clear();
        items.clear();
        levelsGained = 0;
        cgearSkin = null;
        dexSkin = null;
        musical = null;
    }
    
    public String getGameSyncId() {
        return gameSyncId;
    }
    
    public GameVersion getGameVersion() {
        return gameVersion;
    }
    
    public void setEncounters(Collection<DreamEncounter> encounters) {
        if(encounters.size() <= 10) {
            this.encounters.clear();
            this.encounters.addAll(encounters);
        }
    }
    
    public List<DreamEncounter> getEncounters() {
        return Collections.unmodifiableList(encounters);
    }
    
    public void setItems(Collection<DreamItem> items) {
        if(encounters.size() <= 20) {
            this.items.clear();
            this.items.addAll(items);
        }
    }
    
    public List<DreamItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public PlayerStatus getStatus() {
        return status;
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
}
