package entralinked.gui;

import java.awt.GridBagLayout;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.formdev.flatlaf.extras.components.FlatTextField;

import entralinked.Entralinked;
import entralinked.model.user.User;
import entralinked.utility.SwingUtility;

public class PidToolDialog {
    
    public static final Pattern WFC_ID_PATTERN = Pattern.compile("[0-9]{16}");
    public static final Pattern FRIEND_CODE_PATTERN = Pattern.compile("[0-9]{12}");
    
    public PidToolDialog(Entralinked entralinked, JFrame frame) {
        // Create dialog
        JDialog dialog = new JDialog(frame, "PID Tool");
        
        // Create input fields
        FlatTextField wfcIdField = new FlatTextField();
        wfcIdField.setPlaceholderText("XXXX-XXXX-XXXX-XXXX");
        FlatTextField friendCodeField = new FlatTextField();
        friendCodeField.setPlaceholderText("XXXX-XXXX-XXXX");
        
        // Create logic
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(event -> {
            if(!entralinked.isInitialized()) {
                JOptionPane.showMessageDialog(dialog, "Please wait for Entralinked to finish starting.", "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String userId = wfcIdField.getText().replace("-", "");
            String friendCode = friendCodeField.getText().replace("-", "");
            
            // Make sure WFC ID is valid
            if(!WFC_ID_PATTERN.matcher(userId).matches()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid Wi-Fi Connection ID.", "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Make sure Friend Code is valid
            if(!FRIEND_CODE_PATTERN.matcher(friendCode).matches()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid Friend Code.", "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            User user = entralinked.getUserManager().getUser(userId.substring(0, 13));
            int profileId = (int)(Long.parseLong(friendCode) & 0x7FFFFFFF);
            
            // Make sure user exists
            if(user == null) {
                JOptionPane.showMessageDialog(dialog, "This Wi-Fi Connection ID does not exist.", "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            user.setProfileIdOverride(profileId);
            JOptionPane.showMessageDialog(dialog,
                    "All done! Please restart your game and use Game Sync.\nGame profile data will be updated and saved once you do so.");
        });
        
        // Create content panel
        JPanel panel = new JPanel(new GridBagLayout());
        String infoLabel = """
                <html>
                Enter the Wi-Fi Connection ID found in the internet settings of your DS<br>
                as well as your Friend Code which you can view in-game using the Pal Pad.<br>
                Confirm that the input data is correct and press 'Update'<br>
                </html>
                """;
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel(infoLabel), SwingUtility.createConstraints(0, 0, 2, 1));
        panel.add(new JLabel("Wi-Fi Connection ID"), SwingUtility.createConstraints(0, 1, 1, 1, 0, 1));
        panel.add(wfcIdField, SwingUtility.createConstraints(1, 1));
        panel.add(new JLabel("Friend Code"), SwingUtility.createConstraints(0, 2, 1, 1, 0, 1));
        panel.add(friendCodeField, SwingUtility.createConstraints(1, 2));
        panel.add(updateButton, SwingUtility.createConstraints(0, 3, 2, 1));
        
        // Set dialog properties
        dialog.setResizable(false);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}
