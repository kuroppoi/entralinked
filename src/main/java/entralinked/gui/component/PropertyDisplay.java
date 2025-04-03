package entralinked.gui.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class PropertyDisplay extends JPanel {
    
    private final Map<Integer, String> keys = new HashMap<>();
    private final Map<String, JLabel> labels = new HashMap<>();
    private final Map<String, JTextField> fields = new HashMap<>();
    
    public PropertyDisplay() {
        setLayout(new MigLayout("insets 0, fill"));
    }
    
    public void addProperty(int x, int y, String key, String label) {
        addProperty(x, y, key, label, false);
    }
    
    public void addProperty(int x, int y, String key, String label, boolean hidden) {
        int coordHash = Objects.hash(x, y);
        String existingKey = keys.get(coordHash);
        
        if(existingKey != null) {
            removeProperty(existingKey);
        }
        
        removeProperty(key);
        String labelConstraints = "cell %s %s, sg proplabel";
        
        if(x > 0) {
            labelConstraints += ", gapx 4";
        }
        
        JLabel labelComponent = new JLabel(label);
        labels.put(key, labelComponent);
        add(labelComponent, labelConstraints.formatted(x * 2, y));
        add(createValueField(key, hidden), "cell %s %s, sg propfield, width 96".formatted(x * 2 + 1, y));
        keys.put(coordHash, key);
    }
    
    public boolean removeProperty(String key) {
        if(!labels.containsKey(key)) {
            return false;
        }
        
        remove(labels.remove(key));
        remove(fields.remove(key));
        keys.values().remove(key);
        return true;
    }
    
    public void setValue(String key, Object value) {
        JTextField textField = fields.get(key);
        
        if(textField != null) {
            textField.setText(String.valueOf(value));
        }
    }
    
    private JTextField createValueField(String key, boolean hidden) {
        JTextField textField = null;
        
        if(!hidden) {
            textField = new JTextField();
        } else {
            textField = new JPasswordField();
            textField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; showCapsLock: false");
        }
        
        textField.setEditable(false);
        fields.put(key, textField);
        return textField;
    }
    
    public JTextField getValueField(String key) {
        return fields.get(key);
    }
}
