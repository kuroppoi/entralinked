package entralinked.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import entralinked.Entralinked;
import entralinked.utility.ConsumerAppender;

/**
 * Simple Swing user interface.
 */
public class MainView {
    
    private static Logger logger = LogManager.getLogger();
    private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final AttributeSet fontAttribute = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontFamily, "Consolas");
    
    public MainView(Entralinked entralinked) {
        // Try set Look and Feel
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            logger.error("Could not set Look and Feel", e);
        }
        
        // Create dashboard button
        JButton dashboardButton = new JButton("Open User Dashboard");
        dashboardButton.setFocusable(false);
        dashboardButton.addActionListener(event -> {
            openUrl("http://127.0.0.1/dashboard/profile.html");
        });
        
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
            try {
                consoleOutputPane.getDocument().insertString(document.getLength(), message, fontAttribute);
            } catch(BadLocationException e) {}
        });
        
        // Create console output scroll pane
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(consoleOutputPane);
        
        // Create main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(dashboardButton, BorderLayout.PAGE_END);
        
        // Create window
        JFrame frame = new JFrame("Entralinked");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                // Run asynchronously so it doesn't just awkwardly freeze
                // Still scuffed but better than nothing I guess
                CompletableFuture.runAsync(() -> {
                    entralinked.stopServers();
                    System.exit(0);
                });
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(512, 288));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
