package entralinked.network.http.dashboard;

import java.util.Collection;

import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.pkmn.PkmnInfo;
import entralinked.model.player.DreamEncounter;
import entralinked.model.player.DreamItem;
import entralinked.model.player.Player;

public record DashboardProfileMessage(
        String gameVersion, 
        String dreamerSprite,
        PkmnInfo dreamerInfo,
        String cgearSkin, 
        String dexSkin, 
        String musical, 
        int levelsGained,
        Collection<DreamEncounter> encounters,
        Collection<DreamItem> items,
        Collection<AvenueVisitor> avenueVisitors) {
    
    public DashboardProfileMessage(String dreamerSprite, Player player) {
        this(player.getGameVersion().getDisplayName(), dreamerSprite, player.getDreamerInfo(), 
                player.getCGearSkin(), player.getDexSkin(), player.getMusical(), player.getLevelsGained(), 
                player.getEncounters(), player.getItems(), player.getAvenueVisitors());
    }
}
