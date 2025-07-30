package entralinked.model.player;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import entralinked.GameVersion;
import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.pkmn.PkmnInfo;

/**
 * Serialization DTO for Global Link user information.
 */
public record PlayerDto(
        @JsonProperty(required = true) String gameSyncId,
        @JsonProperty(required = true) GameVersion gameVersion,
        PlayerStatus status,
        TrainerInfo trainerInfo,
        PkmnInfo dreamerInfo,
        String cgearSkin,
        String dexSkin,
        String musical,
        String customCGearSkin,
        String customDexSkin,
        String customMusical,
        int levelsGained,
        @JsonDeserialize(contentAs = DreamEncounter.class) Collection<DreamEncounter> encounters,
        @JsonDeserialize(contentAs = DreamItem.class)      Collection<DreamItem> items,
        @JsonDeserialize(contentAs = AvenueVisitor.class)  Collection<AvenueVisitor> avenueVisitors,
        @JsonDeserialize(contentAs = DreamDecor.class)     Collection<DreamDecor> decor) {
    
    public PlayerDto(Player player) {
        this(player.getGameSyncId(), player.getGameVersion(), player.getStatus(), player.getTrainerInfo(),
                player.getDreamerInfo(), player.getCGearSkin(), player.getDexSkin(), player.getMusical(),
                player.getCustomCGearSkin(), player.getCustomDexSkin(), player.getCustomMusical(),
                player.getLevelsGained(), player.getEncounters(), player.getItems(), player.getAvenueVisitors(),
                player.getDecor());
    }
    
    /**
     * Constructs a new {@link Player} object using the data in this DTO.
     */
    public Player toPlayer() {
        Player player = new Player(gameSyncId);
        player.setStatus(status);
        player.setGameVersion(gameVersion);
        player.setTrainerInfo(trainerInfo);
        player.setDreamerInfo(dreamerInfo);
        player.setCGearSkin(cgearSkin);
        player.setDexSkin(dexSkin);
        player.setMusical(musical);
        player.setCustomCGearSkin(customCGearSkin);
        player.setCustomDexSkin(customDexSkin);
        player.setCustomMusical(customMusical);
        player.setLevelsGained(levelsGained);
        player.setEncounters(encounters == null ? Collections.emptyList() : encounters);
        player.setItems(items == null ? Collections.emptyList() : items);
        player.setAvenueVisitors(avenueVisitors == null ? Collections.emptyList() : avenueVisitors);
        player.setDecor(decor == null ? DreamDecor.DEFAULT_DECOR : decor);
        return player;
    }
}
