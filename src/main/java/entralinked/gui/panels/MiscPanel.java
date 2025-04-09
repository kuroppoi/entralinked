package entralinked.gui.panels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.PlainDocument;

import entralinked.gui.SizeLimitDocumentFilter;
import entralinked.model.player.DreamDecor;
import entralinked.model.player.Player;
import entralinked.utility.SwingUtility;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class MiscPanel extends JPanel {
    
    /**
     * Container for keeping track of Décor options.
     */
    private static class DecorOption {
        
        private final JCheckBox checkBox;
        private final JSpinner spinner;
        private final JTextField nameField;
        
        public DecorOption(JPanel parent, String label) {
            spinner = new JSpinner(new SpinnerNumberModel(0, 0, 127, 1));
            nameField = new JTextField();
            ((PlainDocument)nameField.getDocument()).setDocumentFilter(new SizeLimitDocumentFilter(12));
            checkBox = new JCheckBox(label);
            checkBox.addActionListener(event -> {
                boolean active = checkBox.isSelected();
                spinner.setEnabled(active);
                nameField.setEnabled(active);
            });
            parent.add(checkBox);
            parent.add(spinner);
            parent.add(nameField, "growx");
            setActive(true);
        }
        
        public void setActive(boolean active) {
            checkBox.setSelected(active);
            spinner.setEnabled(active);
            nameField.setEnabled(active);
        }
        
        public boolean isActive() {
            return checkBox.isSelected();
        }
        
        public void setId(int id) {
            spinner.setValue(id);
        }
        
        public int getId() {
            return (int)spinner.getValue();
        }
        
        public void setName(String name) {
            nameField.setText(name);
        }
        
        public String getName() {
            return nameField.getText();
        }
    }
    
    public static final int DECOR_COUNT = 5;
    private final List<DecorOption> decorOptions = new ArrayList<>();
    private final JSpinner levelSpinner;
    
    public MiscPanel() {
        setLayout(new MigLayout("align 50% 50%, insets 0, wrap", "", "[]0[]"));
        
        // Create decor panel
        JPanel decorPanel = new JPanel(new MigLayout("insets 0, wrap 3, fill", "[][][grow]"));
        String decorDescription = """
                <html>
                Configure Décor options to appear in Loblolly's studio.<br/>
                Due to the closure of the Dream World, this function serves no real purpose<br/>
                and is mostly meant for people who want to test or just play around with it.
                </html>
                """;
        decorPanel.add(SwingUtility.createTitleLabel("Dream Décor"), "spanx, wrap");
        decorPanel.add(SwingUtility.createDescriptionLabel(decorDescription), "spanx, wrap");

        for(int i = 0; i < DECOR_COUNT; i++) {
            decorOptions.add(new DecorOption(decorPanel, "Decor %s".formatted(i + 1)));
        }
        
        // Create decor option buttons
        JButton resetButton = new JButton("Default");
        resetButton.addActionListener(event -> {
            for(int i = 0; i < DECOR_COUNT; i++) {
                DecorOption option = decorOptions.get(i);
                DreamDecor decor = DreamDecor.DEFAULT_DECOR.get(i);
                option.setActive(true);
                option.setId(decor.id());
                option.setName(decor.name());
            }
        });
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> {
            for(DecorOption option : decorOptions) {
                option.setActive(false);
                option.setId(0);
                option.setName("");
            }
        });
        
        // Create decor button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "0[]"));
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);
        decorPanel.add(buttonPanel, "spanx, align right");
        add(decorPanel, "spanx, growx");
        
        // Create level panel
        levelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        JPanel levelPanel = new JPanel(new MigLayout("insets 0, wrap"));
        levelPanel.add(SwingUtility.createTitleLabel("Level Gain"));
        levelPanel.add(SwingUtility.createDescriptionLabel("Amount of levels to gain on waking up."));
        levelPanel.add(levelSpinner, "grow");
        add(levelPanel, "");
    }
    
    public void loadProfile(Player player) {
        levelSpinner.setValue(player.getLevelsGained());
        List<DreamDecor> decorList = player.getDecor();
        
        for(int i = 0; i < DECOR_COUNT; i++) {
            DecorOption option = decorOptions.get(i);
            DreamDecor decor = i < decorList.size() ? decorList.get(i) : null;
            option.setActive(decor != null);
            option.setId(decor == null ? 0 : decor.id());
            option.setName(decor == null ? "" : decor.name());
        }
    }
    
    public void saveProfile(Player player) {
        player.setDecor(decorOptions.stream().filter(DecorOption::isActive).map(x -> new DreamDecor(x.getId(), x.getName())).toList());
        player.setLevelsGained((int)levelSpinner.getValue());
    }
}
