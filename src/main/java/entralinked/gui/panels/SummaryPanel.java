package entralinked.gui.panels;

import java.awt.Desktop;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatClientProperties;

import entralinked.gui.component.PropertyDisplay;
import entralinked.gui.component.ShadowedSprite;
import entralinked.gui.data.DataManager;
import entralinked.model.pkmn.PkmnInfo;
import entralinked.model.player.Player;
import entralinked.utility.SwingUtility;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class SummaryPanel extends JPanel {
    
    public static final String KEY_NAME = "name";
    public static final String KEY_OT = "trainer";
    public static final String KEY_ID = "tid";
    public static final String KEY_SID = "sid";
    public static final String KEY_PID = "pid";
    public static final String KEY_ABILITY = "ability";
    public static final String KEY_NATURE = "nature";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_ITEM = "item";
    private static final String[] FLAVOR_TEXT = {
        "*pokemon* is trying to grow berries in a nearby garden.",
        "*pokemon* is itching to earn some Dream Points.",
        "*pokemon* is looking at the Tree of Dreams from afar.",
        "*pokemon* is playing a fun minigame (and you're not allowed to join, ha!)",
        "*pokemon* is checking out the Friend Board.",
        "*pokemon* is dreaming about when they first met *trainer*.",
        "*pokemon* left a berry at the Tree of Dreams.",
        "*pokemon* is trying to come up with more of these."
    }; // TODO I think it would be fun to grab various bits of data from the save file and use them here
    private final ShadowedSprite icon;
    private final JLabel flavorTextLabel;
    private final PropertyDisplay infoDisplay;
    private Player player;
    
    public SummaryPanel() {
        setLayout(new MigLayout("align 50% 50%, gapy 0, insets 0"));
        
        // Create info labels
        JLabel titleLabel = new JLabel("Summary");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        flavorTextLabel = new JLabel();
        flavorTextLabel.putClientProperty(FlatClientProperties.STYLE, "[dark]foreground:darken(@foreground,20%)");
        JLabel subLabel = new JLabel("Tucked-in Pokémon info:");
        subLabel.putClientProperty(FlatClientProperties.STYLE, "[dark]foreground:darken(@foreground,20%)");
        
        // Create header panel
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 0"));
        headerPanel.add(titleLabel, "wrap");
        headerPanel.add(flavorTextLabel, "wrap");
        headerPanel.add(subLabel, "wrap");
        add(headerPanel, "spanx");

        // Create icon label
        icon = new ShadowedSprite();
        add(icon, "gapright 4");
        
        // Create property display
        infoDisplay = new PropertyDisplay();
        infoDisplay.addProperty(0, 0, KEY_NAME, "Name");
        infoDisplay.addProperty(0, 1, KEY_OT, "OT");
        infoDisplay.addProperty(0, 2, KEY_ID, "ID No.");
        infoDisplay.addProperty(0, 3, KEY_SID, "SID No.", true);
        infoDisplay.addProperty(0, 4, KEY_PID, "PID", true);
        infoDisplay.addProperty(1, 0, KEY_ABILITY, "Ability");
        infoDisplay.addProperty(1, 1, KEY_NATURE, "Nature");
        infoDisplay.addProperty(1, 2, KEY_GENDER, "Gender");
        infoDisplay.addProperty(1, 3, KEY_LEVEL, "Level");
        infoDisplay.addProperty(1, 4, KEY_ITEM, "Held Item");
        add(infoDisplay, "wrap");
        
        // Create save file location button
        JLabel openSaveButton = SwingUtility.createButtonLabel("Open data directory", () -> {
            SwingUtility.showIgnorableHint(getRootPane(), "The game sends incomplete/corrupt save files to the server.\n"
                    + "Please do not rely on Game Sync to back up your save files.", "Attention", JOptionPane.WARNING_MESSAGE);
            
            try {
                Desktop.getDesktop().open(player.getDataDirectory());
            } catch(IOException e) {
                SwingUtility.showExceptionInfo(getRootPane(), "Couldn't open data directory.", e);
            }
        });
        add(openSaveButton, "spanx, align 100%, gapy 8");
    }
    
    public void loadProfile(Player player) {
        this.player = player;
        PkmnInfo info = player.getDreamerInfo();
        icon.setImage(DataManager.getPokemonSprite(info.species(), info.form(), info.gender(), info.isShiny()));
        infoDisplay.setValue(KEY_NAME, info.nickname());
        infoDisplay.setValue(KEY_OT, info.trainerName());
        infoDisplay.setValue(KEY_ID, "%05d".formatted(info.trainerId()));
        infoDisplay.setValue(KEY_SID, "%05d".formatted(info.trainerSecretId()));
        infoDisplay.setValue(KEY_PID, "%08X".formatted(info.personality()));
        infoDisplay.setValue(KEY_ABILITY, DataManager.getAbilityName(info.ability()));
        infoDisplay.setValue(KEY_NATURE, info.nature().getDisplayName());
        infoDisplay.setValue(KEY_GENDER, info.gender().getDisplayName());
        infoDisplay.setValue(KEY_LEVEL, info.level());
        infoDisplay.setValue(KEY_ITEM, info.heldItem() == 0 ? "None" : DataManager.getItemName(info.heldItem()));
        SwingUtility.setTextFieldToggle(infoDisplay.getValueField(KEY_SID), false);
        SwingUtility.setTextFieldToggle(infoDisplay.getValueField(KEY_PID), false);
        String flavorText = FLAVOR_TEXT[(int)(Math.random() * FLAVOR_TEXT.length)]
                .replace("*pokemon*", info.nickname())
                .replace("*trainer*", info.trainerName());
        flavorTextLabel.setText(flavorText);
    }
}
