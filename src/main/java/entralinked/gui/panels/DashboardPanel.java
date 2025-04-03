package entralinked.gui.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTabbedPane.TabAreaAlignment;
import com.formdev.flatlaf.extras.components.FlatTextField;

import entralinked.Entralinked;
import entralinked.GameVersion;
import entralinked.gui.data.DataManager;
import entralinked.model.player.Player;
import entralinked.model.player.PlayerStatus;
import entralinked.utility.GsidUtility;
import entralinked.utility.SwingUtility;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {
    
    public static final String LOGIN_CARD = "login";
    public static final String MAIN_CARD = "main";
    private final Entralinked entralinked;
    private final JLabel sessionLabel;
    private final FlatTabbedPane tabbedPane;
    private CardLayout layout;
    private SummaryPanel summaryPanel;
    private EncounterEditorPanel encounterPanel;
    private ItemEditorPanel itemPanel;
    private VisitorEditorPanel visitorPanel;
    private MiscPanel miscPanel;
    private Player player;
    private boolean initialized;
    
    public DashboardPanel(Entralinked entralinked) {
        this.entralinked = entralinked;
        
        // Create login labels
        JLabel loginTitle = new JLabel("Log in");
        loginTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        JLabel loginDescription = new JLabel("<html>Tuck in a Pokémon and enter your Game Sync ID to continue.<br/>"
                + "Your Game Sync ID can be found in 'Game Sync Settings' in the game's main menu.</html>");
        loginDescription.putClientProperty(FlatClientProperties.STYLE, "[dark]foreground:darken(@foreground,20%)");
        
        // Create GSID field
        FlatTextField gsidTextField = new FlatTextField();
        gsidTextField.setPlaceholderText("XXXXXXXXXX");
        
        // Create login button
        JButton loginButton = new JButton("Log in");
        loginButton.addActionListener(event -> login(gsidTextField.getText()));
        
        // Create browser dashboard button
        JLabel browserButton = SwingUtility.createButtonLabel("Browser dashboard (Legacy)", () -> {
            try {
                Desktop.getDesktop().browse(new URL("http://127.0.0.1/dashboard/profile.html").toURI());
            } catch(IOException | URISyntaxException e) {
                SwingUtility.showExceptionInfo(getRootPane(), "Failed to open URL.", e);
            }
        });
        
        // Create login panel
        JPanel loginPanel = new JPanel(new MigLayout("wrap, align 50% 50%"));
        loginPanel.add(loginTitle);
        loginPanel.add(loginDescription);
        loginPanel.add(new JLabel("Game Sync ID"), "gapy 8");
        loginPanel.add(gsidTextField, "growx");
        loginPanel.add(loginButton, "growx");
        loginPanel.add(browserButton, "gapy 8, align 100%");
        
        // Create save button
        JButton saveButton = new JButton("Save profile");
        saveButton.addActionListener(event -> {
            // Try to save data
            // TODO this is probably not thread-safe!!!
            try {
                encounterPanel.saveProfile(player);
                itemPanel.saveProfile(player);
                visitorPanel.saveProfile(player);
                miscPanel.saveProfile(player);
                player.setStatus(PlayerStatus.WAKE_READY);
                
                if(!entralinked.getPlayerManager().savePlayer(player)) {
                    JOptionPane.showMessageDialog(getRootPane(), "Failed to write player data to disk.", "Attention", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                JOptionPane.showMessageDialog(getRootPane(), "Profile data has been saved successfully!\n"
                        + "Use Game Sync to wake up your Pokémon and download your selected content.");
            } catch(Exception e) {
                SwingUtility.showExceptionInfo(getRootPane(), "Failed to save player data.", e);
            }
        });
        
        // Create log out button
        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(event -> {
            gsidTextField.setText("");
            layout.show(this, LOGIN_CARD);
        });
        
        // Create main footer button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(saveButton);
        buttonPanel.add(logoutButton);
        
        // Create main footer panel
        sessionLabel = new JLabel("", JLabel.CENTER);
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(sessionLabel);
        footerPanel.add(buttonPanel, BorderLayout.LINE_END);
        
        // Create main panel
        tabbedPane = new FlatTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.setTabAreaAlignment(TabAreaAlignment.center);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane);
        mainPanel.add(footerPanel, BorderLayout.PAGE_END);
        
        // Create layout
        layout = new CardLayout();
        setLayout(layout);
        add(LOGIN_CARD, loginPanel);
        add(MAIN_CARD, mainPanel);
    }
    
    private void login(String gameSyncId) {
        // Check if GSID is valid
        if(!GsidUtility.isValidGameSyncId(gameSyncId)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Game Sync ID.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Player player = entralinked.getPlayerManager().getPlayer(gameSyncId);
        
        // Check if player exists
        if(player == null) {
            JOptionPane.showMessageDialog(this, "This Game Sync ID does not exist.\n"
                    + "If you haven't already, please tuck in a Pokémon first.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        GameVersion version = player.getGameVersion();
        
        // Check if necessary data is present
        if(player.getDreamerInfo() == null || version == null) {
            JOptionPane.showMessageDialog(this, "Please use Game Sync to tuck in a Pokémon first.", "Attention", JOptionPane.INFORMATION_MESSAGE);
            player = null;
            return;
        }
        
        // Try to initialize dashboard
        if(!initialized && !initializeDashboard()) {
            return;
        }
        
        // Load player data
        sessionLabel.setText("Game Version: %s, Game Sync ID: %s".formatted(version.getDisplayName(), gameSyncId));
        tabbedPane.setEnabledAt(3, version.isVersion2());
        
        try {
            summaryPanel.loadProfile(player);
            encounterPanel.loadProfile(player);
            itemPanel.loadProfile(player);
            visitorPanel.loadProfile(player);
            miscPanel.loadProfile(player);
        } catch(Exception e) {
            SwingUtility.showExceptionInfo(getRootPane(), "Failed to load player data.", e);
            return;
        }
        
        this.player = player;
        tabbedPane.setSelectedIndex(0);
        layout.show(this, MAIN_CARD);
    }
    
    private boolean initializeDashboard() {
        try {
            DataManager.loadData();
            summaryPanel = new SummaryPanel();
            encounterPanel = new EncounterEditorPanel();
            itemPanel = new ItemEditorPanel();
            visitorPanel = new VisitorEditorPanel();
            miscPanel = new MiscPanel(entralinked);
        } catch(Exception e) {
            SwingUtility.showExceptionInfo(getRootPane(), "Failed to initialize dashboard.", e);
            return false;
        }
        
        tabbedPane.add("Summary", summaryPanel);
        tabbedPane.add("Entree Forest", encounterPanel);
        tabbedPane.add("Dream Remnants", itemPanel);
        tabbedPane.add("Join Avenue", visitorPanel);
        tabbedPane.add("Miscellaneous", miscPanel);
        initialized = true;
        return true;
    }
}
