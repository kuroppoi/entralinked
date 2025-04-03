package entralinked.gui.view;

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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
import com.formdev.flatlaf.util.ColorFunctions;

import entralinked.Entralinked;
import entralinked.gui.panels.DashboardPanel;
import entralinked.utility.ConsumerAppender;
import entralinked.utility.SwingUtility;

/**
 * Simple Swing user interface.
 */
public class MainView {
    
    public static final Color TEXT_COLOR = Color.WHITE.darker();
    public static final Color TEXT_COLOR_WARN = Color.YELLOW.darker();
    public static final Color TEXT_COLOR_ERROR = Color.RED.darker();
    private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final AttributeSet fontAttribute = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontFamily, "Consolas");
    private final JLabel statusLabel;
    
    public MainView(Entralinked entralinked) {
        // Set look and feel
        FlatOneDarkIJTheme.setup();
        UIManager.getDefaults().put("Component.focusedBorderColor", UIManager.get("Component.borderColor"));
        UIManager.put("Table.alternateRowColor", ColorFunctions.lighten(UIManager.getColor("Table.background"), 0.05F));
        
        // Create status label
        statusLabel = new JLabel("Servers are starting, please wait a bit...", JLabel.CENTER);
        
        // Create footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(statusLabel, BorderLayout.CENTER);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
        
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
        consoleOutputPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
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
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Console", panel);
        tabbedPane.addTab("Dashboard", new DashboardPanel(entralinked));
        
        // Create window
        JFrame frame = new JFrame("Entralinked");
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(SwingUtility.createAction("Update PID (Error 60000)", () -> new PidToolDialog(entralinked, frame)));
        helpMenu.add(SwingUtility.createAction("GitHub", () -> {
            try {
                Desktop.getDesktop().browse(new URL("https://github.com/kuroppoi/entralinked").toURI());
            } catch(IOException | URISyntaxException e) {
                SwingUtility.showExceptionInfo(frame, "Failed to open URL.", e);
            }
        }));
        menuBar.add(helpMenu);
        
        // Set window properties
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                // Update status
                statusLabel.setText("Servers are shutting down, please wait a bit...");
                
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
        frame.setJMenuBar(menuBar);
        frame.add(tabbedPane);
        frame.getContentPane().setPreferredSize(new Dimension(733, 463));
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void setStatusLabelText(String text) {
        statusLabel.setText(text);
    }
}
