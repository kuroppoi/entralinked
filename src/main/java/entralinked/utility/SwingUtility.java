package entralinked.utility;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class SwingUtility {
    
    private static final Set<String> ignoredMessages = new HashSet<>();
    
    @SuppressWarnings("serial")
    public static Action createAction(String name, Icon icon, Runnable handler) {
        AbstractAction action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.run();
            }
        };
        
        if(icon != null) {
            action.putValue(Action.SHORT_DESCRIPTION, name);
        }
        
        return action;
    }
    
    public static Action createAction(String name, Runnable handler) {
        return createAction(name, null, handler);
    }
    
    public static GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
        return createConstraints(x, y, width, height, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY) {
        return createConstraints(x, y, width, height, weightX, weightY, 8, 8);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY, 
            int paddingX, int paddingY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        constraints.ipadx = paddingX;
        constraints.ipady = paddingY;
        return constraints;
    }
    
    public static void setTextFieldToggle(JTextField textField, boolean selected) {     
        for(Component component : textField.getComponents()) {
            if(component instanceof JToggleButton) {
                JToggleButton button = (JToggleButton)component;
                
                if(button.isSelected() != selected) {
                    button.doClick();
                }
            }
        }
    }
    
    // TODO find a good unicode font maybe?
    public static Font findSupportingFont(String text, Font def) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        for(Font font : graphicsEnvironment.getAllFonts()) {
            if(font.canDisplayUpTo(text) == -1) {
                return new Font(font.getName(), def.getStyle(), def.getSize());
            }
        }
        
        return def;
    }
    
    public static JLabel createTitleLabel() {
        return createTitleLabel("");
    }
    
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        return label;
    }
    
    public static JLabel createDescriptionLabel() {
        return createDescriptionLabel("");
    }
    
    public static JLabel createDescriptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE, "[dark]foreground:darken(@foreground,20%)");
        return label;
    }
    
    public static JLabel createButtonLabel(String text, Runnable actionHandler) {
        JLabel label = new JLabel("<html><u>%s</u></html>".formatted(text));
        label.putClientProperty(FlatClientProperties.STYLE, "font: -1");
        label.addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseClicked(MouseEvent event) {
                if(SwingUtilities.isLeftMouseButton(event)) {
                    actionHandler.run();
                }
            }
        });
        return label;
    }
    
    public static void showIgnorableHint(Component parentComponent, String message, String title, int messageType) {
        synchronized(ignoredMessages) {
            // Do nothing if message has been ignored
            if(ignoredMessages.contains(message)) {
                return;
            }
            
            // Create ignore checkbox
            JCheckBox checkBox = new JCheckBox("Don't show this again");
            JOptionPane.showMessageDialog(parentComponent, createIgnorableDialogPanel(message, checkBox), title, messageType);
            
            // Add to ignore list if checkbox is selected
            if(checkBox.isSelected()) {
                ignoredMessages.add(message);
            }
        }
    }
    
    public static boolean showIgnorableConfirmDialog(Component parentComponent, String message, String title) {
        synchronized(ignoredMessages) {
            // Do nothing if message has been ignored
            if(ignoredMessages.contains(message)) {
                return true;
            }
            
            JCheckBox checkBox = new JCheckBox("Don't ask this again");
            int result = JOptionPane.showConfirmDialog(parentComponent, createIgnorableDialogPanel(message, checkBox), title, JOptionPane.YES_NO_OPTION);
            
            // Add to ignore list if checkbox is selected
            if(checkBox.isSelected()) {
                ignoredMessages.add(message);
            }
            
            return result == JOptionPane.YES_OPTION;
        }
    }
    
    private static JPanel createIgnorableDialogPanel(String message, JCheckBox checkBox) {
        JPanel panel = new JPanel(new MigLayout("insets 0"));
        panel.add(new JLabel("<html>%s</html>".formatted(message.replace("\n", "<br/>"))), "wrap"); // TODO no idea how JOptionPane does line breaks
        panel.add(checkBox, "gapy 8");
        return panel;
    }
    
    public static void showExceptionInfo(Component parentComponent, String message, Throwable throwable) {
        // Create stacktrace string
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        
        // Create text area
        JTextArea area = new JTextArea(writer.toString());
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 0));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        
        // Create scroll pane
        int height = Math.min(200, area.getFontMetrics(area.getFont()).getHeight() * area.getLineCount() + 10);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, height));
        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
        
        // Create dialog
        String label = String.format("<html><b>%s</b><br>Exception details:<br><br></html>", message);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.PAGE_START);
        panel.add(scrollPane);
        JOptionPane.showMessageDialog(parentComponent, panel, "An error has occured", JOptionPane.ERROR_MESSAGE);
    }
}
