package entralinked.model.player;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import entralinked.GameVersion;
import entralinked.model.pkmn.PkmnInfo;

/**
 * Serialization DTO for Global Link user information.
 */
public record PlayerDto(
        @JsonProperty(required = true) String gameSyncId,
        @JsonProperty(required = true) GameVersion gameVersion,
        PlayerStatus status,
        PkmnInfo dreamerInfo,
        String cgearSkin,
        String dexSkin,
        String musical,
        int levelsGained,
        @JsonDeserialize(contentAs = DreamEncounter.class) Collection<DreamEncounter> encounters,
        @JsonDeserialize(contentAs = DreamItem.class)      Collection<DreamItem> items) {
    
    public PlayerDto(Player player) {
        this(player.getGameSyncId(), player.getGameVersion(), player.getStatus(), player.getDreamerInfo(), player.getCGearSkin(), 
                player.getDexSkin(), player.getMusical(), player.getLevelsGained(), player.getEncounters(), player.getItems());
    }
    
    /**
     * Constructs a new {@link Player} object using the data in this DTO.
     */
    public Player toPlayer() {
        Player player = new Player(gameSyncId);
        player.setStatus(status);
        player.setGameVersion(gameVersion);
        player.setDreamerInfo(dreamerInfo);
        player.setCGearSkin(cgearSkin);
        player.setDexSkin(dexSkin);
        player.setMusical(musical);
        player.setLevelsGained(levelsGained);
        player.setEncounters(encounters == null ? Collections.emptyList() : encounters);
        player.setItems(items == null ? Collections.emptyList() : items);
        return player;
    }
}
