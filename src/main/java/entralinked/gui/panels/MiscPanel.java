package entralinked.gui.panels;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import entralinked.Entralinked;
import entralinked.GameVersion;
import entralinked.gui.FileChooser;
import entralinked.gui.ModelListCellRenderer;
import entralinked.model.player.Player;
import entralinked.utility.Crc16;
import entralinked.utility.LEOutputStream;
import entralinked.utility.SwingUtility;
import entralinked.utility.TiledImageUtility;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class MiscPanel extends JPanel {
    
    @FunctionalInterface
    private static interface SkinWriter {
        
        public void writeSkin(OutputStream outputStream, BufferedImage image) throws IOException;
    }
    
    /**
     * Internal model for combo boxes.
     */
    private static record DlcOption(String type, String name, String path, boolean custom) {
        
        public DlcOption(String type, String name, String path) {
            this(type, name, path, false);
        }
    }
    
    private static final FileFilter IMAGE_FILE_FILTER = new FileNameExtensionFilter("Image Files (*.png)", "png");
    private static final FileFilter CGEAR_FILE_FILTER = new FileNameExtensionFilter("C-Gear Skin Files (*.bin, *.cgb, *.psk)", "bin", "cgb", "psk");
    private static final FileFilter ZUKAN_FILE_FILTER = new FileNameExtensionFilter("Pokédex Skin Files (*.bin, *.pds)", "bin", "pds");
    private static final byte[] NARC_HEADER = { 0x4E, 0x41, 0x52, 0x43, (byte)0xFE, (byte)0xFF, 0x00, 0x01 };
    private static final BufferedImage EMPTY_IMAGE = new BufferedImage(TiledImageUtility.SCREEN_WIDTH, TiledImageUtility.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private static final Map<String, Image> skinCache = new HashMap<>();
    private final Entralinked entralinked;
    private final JComboBox<DlcOption> cgearComboBox;
    private final JComboBox<DlcOption> zukanComboBox;
    private final JComboBox<DlcOption> musicalComboBox;
    private final JPanel optionPanel;
    private final JSpinner levelSpinner;
    private Player player;
    private GameVersion gameVersion;
    private DlcOption customCGearSkin;
    private DlcOption customDexSkin;
    private DlcOption customMusical;

    public MiscPanel(Entralinked entralinked) {
        this.entralinked = entralinked;
        setLayout(new MigLayout("align 50% 50%"));
        
        // Create preview labels
        JLabel cgearPreviewLabel = new JLabel("", JLabel.CENTER);
        cgearPreviewLabel.setBorder(BorderFactory.createTitledBorder("C-Gear Skin Preview"));
        JLabel dexPreviewLabel = new JLabel("", JLabel.CENTER);
        dexPreviewLabel.setBorder(BorderFactory.createTitledBorder("Pokédex Skin Preview"));
        
        // Create preview image panel
        JPanel previewPanel = new JPanel();
        previewPanel.add(cgearPreviewLabel);
        previewPanel.add(dexPreviewLabel);
        add(previewPanel, "spanx, align 50%, wrap");
        
        // Create combo boxes
        ModelListCellRenderer<DlcOption> renderer = new ModelListCellRenderer<>(DlcOption.class, DlcOption::name, "Do not change");
        cgearComboBox = new JComboBox<>();
        cgearComboBox.setMinimumSize(cgearComboBox.getPreferredSize());
        cgearComboBox.setRenderer(renderer);
        cgearComboBox.addActionListener(event -> {
            cgearPreviewLabel.setIcon(new ImageIcon(getSkinImage((DlcOption)cgearComboBox.getSelectedItem())));
        });
        zukanComboBox = new JComboBox<>();
        zukanComboBox.setMinimumSize(zukanComboBox.getPreferredSize());
        zukanComboBox.setRenderer(renderer);
        zukanComboBox.addActionListener(event -> {
            dexPreviewLabel.setIcon(new ImageIcon(getSkinImage((DlcOption)zukanComboBox.getSelectedItem())));
        });
        musicalComboBox = new JComboBox<>();
        musicalComboBox.setMinimumSize(musicalComboBox.getPreferredSize());
        musicalComboBox.setRenderer(renderer);
        
        // Create option panel
        optionPanel = new JPanel(new MigLayout());
        
        // Create C-Gear skin selector
        createDlcOption("C-Gear Skin", cgearComboBox, () -> {
            FileChooser.showFileOpenDialog(getRootPane(), Arrays.asList(IMAGE_FILE_FILTER, CGEAR_FILE_FILTER), selection -> {
                File dst = player.getCGearSkinFile();
                File file = selection.file();
                FileFilter filter = selection.filter();
                
                if(filter == IMAGE_FILE_FILTER) {
                    if(!importSkinImage(file, dst, (stream, image) -> TiledImageUtility.writeCGearSkin(stream, image, !gameVersion.isVersion2()))) {
                        return;
                    }
                } else if(filter == CGEAR_FILE_FILTER) {
                    if(!importSkinFile(file, dst, 9730)) {
                        return;
                    }
                } else {
                    return;
                }
                
                DlcOption option = new DlcOption(gameVersion.isVersion2() ? "CGEAR2" : "CGEAR", file.getName(), dst.getAbsolutePath(), true);
                updateCustomOption(cgearComboBox, customCGearSkin, option);
                customCGearSkin = option;
                player.setCustomCGearSkin(customCGearSkin.name());
            });
        });
        
        // Create Pokédex skin selector
        createDlcOption("Pokédex Skin", zukanComboBox, () -> {
            FileChooser.showFileOpenDialog(getRootPane(), Arrays.asList(IMAGE_FILE_FILTER, ZUKAN_FILE_FILTER), selection -> {
                File dst = player.getDexSkinFile();
                File file = selection.file();
                FileFilter filter = selection.filter();
                
                if(filter == IMAGE_FILE_FILTER) {
                    if(!importSkinImage(file, dst, (stream, image) -> TiledImageUtility.writeDexSkin(stream, image, TiledImageUtility.generateBackgroundColors(image)))) {
                        return;
                    }
                } else if(filter == ZUKAN_FILE_FILTER) {
                    if(!importSkinFile(file, dst, 25090)) {
                        return;
                    }
                } else {
                    return;
                }
                
                DlcOption option = new DlcOption("ZUKAN", file.getName(), dst.getAbsolutePath(), true);
                updateCustomOption(zukanComboBox, customDexSkin, option);
                customDexSkin = option;
                player.setCustomDexSkin(customDexSkin.name());
            });
        });
        
        // Create musical show selector
        createDlcOption("Musical Show", musicalComboBox, () -> {
            SwingUtility.showIgnorableHint(getRootPane(), "Please exercise caution when importing custom musicals.\n"
                    + "Downloading invalid data might cause game crashes or other issues.", "Attention", JOptionPane.WARNING_MESSAGE);
            FileChooser.showFileOpenDialog(getRootPane(), selection -> {
                File dst = player.getMusicalFile();
                File file = selection.file();
                
                if(!importNarcFile(file, dst)) {
                    return;
                }
                
                DlcOption option = new DlcOption("MUSICAL", file.getName(), dst.getAbsolutePath(), true);
                updateCustomOption(musicalComboBox, customMusical, option);
                customMusical = option;
                player.setCustomMusical(customMusical.name());
            });
        });
        
        // Create level spinner
        levelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1)) ;
        optionPanel.add(new JLabel("Level Gain"), "sizegroup label");
        optionPanel.add(levelSpinner, "sizegroup option");
        add(optionPanel, "spanx, align 50%");
    }
    
    public void loadProfile(Player player) {
        this.player = player;
        gameVersion = player.getGameVersion();
        String cgearType = player.getGameVersion().isVersion2() ? "CGEAR2" : "CGEAR";
        customCGearSkin = player.getCustomCGearSkin() == null ? null : new DlcOption(cgearType, player.getCustomCGearSkin(), player.getCGearSkinFile().getAbsolutePath(), true);
        customDexSkin = player.getCustomDexSkin() == null ? null : new DlcOption("ZUKAN", player.getCustomDexSkin(), player.getDexSkinFile().getAbsolutePath(), true);
        customMusical = player.getCustomMusical() == null ? null : new DlcOption("MUSICAL", player.getCustomMusical(), player.getMusicalFile().getAbsolutePath(), true);
        updateDlcOptions(cgearComboBox, cgearType, player.getCGearSkin(), customCGearSkin);
        updateDlcOptions(zukanComboBox, "ZUKAN", player.getDexSkin(), customDexSkin);
        updateDlcOptions(musicalComboBox, "MUSICAL", player.getMusical(), customMusical);
        levelSpinner.setValue(player.getLevelsGained());
    }
    
    public void saveProfile(Player player) {
        DlcOption cgearSkin = (DlcOption)cgearComboBox.getSelectedItem();
        DlcOption dexSkin = (DlcOption)zukanComboBox.getSelectedItem();
        DlcOption musical = (DlcOption)musicalComboBox.getSelectedItem();
        player.setCGearSkin(cgearSkin == null ? null : cgearSkin.custom() ? "custom" : cgearSkin.name());
        player.setDexSkin(dexSkin == null ? null : dexSkin.custom() ? "custom" : dexSkin.name());
        player.setMusical(musical == null ? null : musical.custom() ? "custom" : musical.name());
        player.setLevelsGained((int)levelSpinner.getValue());
    }
    
    private void createDlcOption(String label, JComboBox<DlcOption> comboBox, Runnable importListener) {
        optionPanel.add(new JLabel(label), "sizegroup label");
        optionPanel.add(comboBox, "sizegroup option");
        JButton importButton = new JButton("Import");
        importButton.addActionListener(event -> importListener.run());
        optionPanel.add(importButton, "wrap");
    }
    
    private void updateDlcOptions(JComboBox<DlcOption> comboBox, String type, String selectedOption, DlcOption customOption) {
        Vector<DlcOption> options = new Vector<>();
        
        if(customOption != null) {
            options.add(customOption);
        }
        
        entralinked.getDlcList().getDlcList("IRAO", type).forEach(dlc -> options.add(new DlcOption(type, dlc.name(), dlc.path())));
        DlcOption selection = selectedOption == null ? null : selectedOption.equals("custom") ? customOption : options.stream().filter(x -> selectedOption.equals(x.name())).findFirst().orElse(null);
        options.add(0, null); // "Do not change" option
        comboBox.setModel(new DefaultComboBoxModel<DlcOption>(options));
        comboBox.setSelectedItem(selection);
    }
    
    private void updateCustomOption(JComboBox<DlcOption> comboBox, DlcOption oldValue, DlcOption newValue) {
        DefaultComboBoxModel<DlcOption> model = (DefaultComboBoxModel<DlcOption>)comboBox.getModel();
        
        if(oldValue != null) {
            model.removeElement(oldValue);
            skinCache.remove(oldValue.path());
        }
        
        model.insertElementAt(newValue, 1);
        model.setSelectedItem(newValue);
    }
    
    private static Image getSkinImage(DlcOption option) {
        return option == null ? EMPTY_IMAGE : skinCache.computeIfAbsent(option.path(), path -> {
            try(FileInputStream inputStream = new FileInputStream(path)) {
                return switch(option.type()) {
                case "CGEAR" -> TiledImageUtility.readCGearSkin(inputStream, true);
                case "CGEAR2" -> TiledImageUtility.readCGearSkin(inputStream, false);
                case "ZUKAN" -> TiledImageUtility.readDexSkin(inputStream, true);
                default -> throw new IllegalArgumentException("Invalid type: " + option.type());
                };
            } catch(Exception e) {
                return EMPTY_IMAGE; // TODO show feedback
            }
        });
    }
    
    private boolean importSkinFile(File src, File dst, int expectedSize) {
        int sizeWithoutChecksum = expectedSize - 2;
        int length = (int)src.length();
        
        // Check content length
        if(length != expectedSize && length != sizeWithoutChecksum) {
            JOptionPane.showMessageDialog(getRootPane(), "Invalid content length, expected either %s or %s bytes."
                    .formatted(sizeWithoutChecksum, expectedSize), "Attention", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            byte[] bytes = Files.readAllBytes(src.toPath());            
            boolean writeChecksum = true;
            
            // Validate checksum
            if(length == expectedSize) {
                int checksum = Crc16.calc(bytes, 0, sizeWithoutChecksum);
                int checksumInFile = (bytes[bytes.length - 2] & 0xFF) | ((bytes[bytes.length - 1] & 0xFF) << 8);
                
                if(checksum != checksumInFile) {
                    JOptionPane.showMessageDialog(getRootPane(), "File checksum doesn't match.", "Attention", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                
                writeChecksum = false;
            }
            
            // Write to destination & append checksum if necessary
            try(LEOutputStream outputStream = new LEOutputStream(new FileOutputStream(dst))) {
                outputStream.write(bytes);
                
                if(writeChecksum) {
                    outputStream.writeShort(Crc16.calc(bytes));
                }
            }
            
            return true;
        } catch(Exception e) {
            e.printStackTrace(); // TODO show feedback
        }
        
        return false;
    }
    
    private boolean importSkinImage(File src, File dst, SkinWriter writer) {
        try {
            BufferedImage image = ImageIO.read(src);
            int width = TiledImageUtility.SCREEN_WIDTH;
            int height = TiledImageUtility.SCREEN_HEIGHT;
            
            if(image.getWidth() != width || image.getHeight() != height) {
                JOptionPane.showMessageDialog(getRootPane(), "Image size must be %sx%s pixels.".formatted(width, height), "Attention", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            writer.writeSkin(byteStream, image);
            byte[] bytes = byteStream.toByteArray();
            
            try(LEOutputStream outputStream = new LEOutputStream(new FileOutputStream(dst))) {
                outputStream.write(bytes);
                outputStream.writeShort(Crc16.calc(bytes));
            }
            
            return true;
        } catch(IllegalArgumentException e) {
            JOptionPane.showMessageDialog(getRootPane(), e.getMessage(), "Attention", JOptionPane.WARNING_MESSAGE);
        } catch(Exception e) {
            e.printStackTrace(); // TODO show feedback
        }
        
        return false;
    }
    
    private boolean importNarcFile(File src, File dst) {
        try {
            byte[] bytes = Files.readAllBytes(src.toPath());
            int offset = 0;
            
            // Check narc header
            if(!Arrays.equals(bytes, 0, NARC_HEADER.length, NARC_HEADER, 0, NARC_HEADER.length)) {
                if(bytes.length < 16 || !Arrays.equals(bytes, 16, 16 + NARC_HEADER.length, NARC_HEADER, 0, NARC_HEADER.length)) {
                    JOptionPane.showMessageDialog(getRootPane(), "Invalid or unsupported file.", "Attention", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                
                offset = 16;
            }
            
            // TODO utility function
            int length = ((bytes[offset + NARC_HEADER.length + 3] & 0xFF) << 24)
                    | ((bytes[offset + NARC_HEADER.length + 2] & 0xFF) << 16)
                    | ((bytes[offset + NARC_HEADER.length + 1] & 0xFF) << 8)
                    | (bytes[offset + NARC_HEADER.length] & 0xFF);
            
            if(offset + length >= bytes.length) {
                JOptionPane.showMessageDialog(getRootPane(), "File data is malformed or corrupt.", "Attention", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Write to destination
            try(LEOutputStream outputStream = new LEOutputStream(new FileOutputStream(dst))) {
                outputStream.write(bytes, offset, length);
                outputStream.writeShort(Crc16.calc(bytes, offset, length));
            }
            
            return true;
        } catch(Exception e) {
            e.printStackTrace(); // TODO show feedback
        }
        
        return false;
    }
}
