package entralinked.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.logging.log4j.Level;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import entralinked.Entralinked;
import entralinked.utility.ConsumerAppender;

/**
 * Simple Swing user interface.
 */
public class MainView {
    
    public static final Color TEXT_COLOR = Color.WHITE.darker();
    public static final Color TEXT_COLOR_WARN = Color.YELLOW.darker();
    public static final Color TEXT_COLOR_ERROR = Color.RED.darker();
    private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final AttributeSet fontAttribute = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontFamily, "Consolas");
    private final JButton dashboardButton;
    private final JLabel statusLabel;
    
    public MainView(Entralinked entralinked) {
        // Set look and feel
        FlatOneDarkIJTheme.setup();
        UIManager.getDefaults().put("Component.focusedBorderColor", UIManager.get("Component.borderColor"));
        
        // Create dashboard button
        dashboardButton = new JButton("Open User Dashboard");
        dashboardButton.setEnabled(false);
        dashboardButton.setFocusable(false);
        dashboardButton.addActionListener(event -> {
            openUrl("http://127.0.0.1/dashboard/profile.html");
        });
        
        // Create status label
        statusLabel = new JLabel("Entralinked is starting...", JLabel.CENTER);
        
        // Create footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(statusLabel, BorderLayout.CENTER);
        footerPanel.add(dashboardButton, BorderLayout.LINE_END);
        
        // Create console output
        JTextPane consoleOutputPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width <= getParent().getSize().width;
            }
            
            @Override
            public Dimension getPreferredSize() {
                return getUI().getPreferredSize(this);
            };
        };
        consoleOutputPane.setFont(new Font("Consola", Font.PLAIN, 12));
        consoleOutputPane.setEditable(false);
        ((DefaultCaret)consoleOutputPane.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        // Create console output appender
        ConsumerAppender.addConsumer("GuiOutput", message -> {
            Document document = consoleOutputPane.getDocument();
            Level level = message.level();
            Color color = level == Level.ERROR ? TEXT_COLOR_ERROR : level == Level.WARN ? TEXT_COLOR_WARN : TEXT_COLOR;
            AttributeSet colorAttribute = styleContext.addAttribute(fontAttribute, StyleConstants.Foreground, color);
            
            try {
                consoleOutputPane.getDocument().insertString(document.getLength(), message.formattedMessage(), colorAttribute);
            } catch(BadLocationException e) {}
        });
        
        // Create console output scroll pane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(consoleOutputPane);
        
        // Create main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.PAGE_END);
        
        // Create window
        JFrame frame = new JFrame("Entralinked");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                // Update status
                dashboardButton.setEnabled(false);
                statusLabel.setText("Entralinked is shutting down ...");
                
                // Run asynchronously so it doesn't just awkwardly freeze
                // Still scuffed but better than nothing I guess
                CompletableFuture.runAsync(() -> {
                    entralinked.stopServers();
                    System.exit(0);
                });
            }
        });
        frame.setIconImages(List.of(
                new ImageIcon(getClass().getResource("/icon-64x.png")).getImage(),
                new ImageIcon(getClass().getResource("/icon-32x.png")).getImage(),
                new ImageIcon(getClass().getResource("/icon-16x.png")).getImage()));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(512, 288));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void setDashboardButtonEnabled(boolean enabled) {
        dashboardButton.setEnabled(enabled);
    }
    
    public void setStatusLabelText(String text) {
        statusLabel.setText(text);
    }
    
    private void openUrl(String url) {
        Desktop desktop = Desktop.getDesktop();
        
        if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URL(url).toURI());
            } catch(IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
