package entralinked.gui.panels;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import entralinked.gui.component.ConfigTable;
import entralinked.gui.component.ShadowedSprite;
import entralinked.utility.SwingUtility;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public abstract class TableEditorPanel extends JPanel {
    
    protected final Map<Integer, Object> oldValues = new HashMap<>();
    protected final int rowCount;
    protected final ConfigTable table;
    protected final TableModel model;
    protected final JLabel titleLabel;
    protected final JLabel descriptionLabel;
    protected final ShadowedSprite selectionIcon;
    protected JButton randomizeButton;
    protected JButton clearAllButton;
    protected JCheckBox legalModeToggle;
    
    public TableEditorPanel(int rowCount, String[] columnNames, Class<?>[] columnTypes) {
        this(rowCount, true, columnNames, columnTypes);
    }
    
    public TableEditorPanel(int rowCount, boolean incluceLegalMode, String[] columnNames, Class<?>[] columnTypes) {
        if(columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Length mismatch between column names and column types");
        }
        
        this.rowCount = rowCount;
        String[] columns = new String[columnNames.length + 1];
        columns[0] = "No.";
        System.arraycopy(columnNames, 0, columns, 1, columnNames.length);
        
        // Create labels
        titleLabel = SwingUtility.createTitleLabel();
        descriptionLabel = SwingUtility.createDescriptionLabel();
        selectionIcon = new ShadowedSprite();
        
        // Create buttons & toggles
        randomizeButton = new JButton("Random");
        randomizeButton.addActionListener(event -> randomizeSelections());
        clearAllButton = new JButton("Clear all");
        clearAllButton.addActionListener(event -> clearSelections());
        legalModeToggle = new JCheckBox("Legal mode");
        legalModeToggle.addActionListener(event -> {
            boolean selected = legalModeToggle.isSelected();
            
            // Since we're clearing illegal settings, ask for confirmation first.
            if(selected && !SwingUtility.showIgnorableConfirmDialog(getRootPane(), "Enable legal mode? Illegal selections will be cleared.", "Attention")) {
                legalModeToggle.setSelected(false);
                return;
            }
            
            legalModeChanged();
        });
        
        // Create layout
        setLayout(new MigLayout("align 50% 50%, gapy 0, insets 0 8 0 8"));
        
        // Create header panels
        JPanel labelPanel = new JPanel(new MigLayout("insets 0, fill"));
        labelPanel.add(titleLabel, "wrap");
        labelPanel.add(descriptionLabel, "wrap");
        
        JPanel headerPanel = new JPanel(new MigLayout("insets n n 0 n", "[][grow]"));
        headerPanel.add(labelPanel);
        headerPanel.add(selectionIcon, "align 100%");
        add(headerPanel, "wrap, grow");
        
        // Create table
        model = new DefaultTableModel(columns, rowCount);
        model.addTableModelListener(event -> {
            int column = event.getColumn();
            
            if(column == 0) {
                return; // Ignore number column
            }
            
            if(shouldUpdateSelectionIcon(column)) {
                updateSelectionIcon();
            }
            
            for(int i = event.getFirstRow(); i <= event.getLastRow(); i++) {
                int index = i * rowCount + column;
                Object oldValue = oldValues.get(index);
                Object newValue = model.getValueAt(i, column);
                Class<?> type = columnTypes[column - 1];
                
                // Set to null if types don't match
                if(oldValue != null && !type.isAssignableFrom(oldValue.getClass())) {
                    oldValue = null;
                }
                
                if(newValue != null && !type.isAssignableFrom(newValue.getClass())) {
                    newValue = null;
                }
                
                // Fire data changed if new value is different from old value
                if(newValue != oldValue) {
                    dataChanged(i, column, oldValue, newValue);
                    oldValues.put(index, model.getValueAt(i, column)); // We use getValueAt again because dataChanged might have updated the value
                }
            }
        });
        table = new ConfigTable();
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setModel(model);  
        table.getSelectionModel().addListSelectionListener(event -> updateSelectionIcon());
        int width = Math.max(table.getPreferredSize().width, table.getPreferredScrollableViewportSize().width);
        table.setPreferredScrollableViewportSize(new Dimension(width, table.getRowHeight() * rowCount));
        table.getColumnModel().getColumn(0).setResizable(false);
        table.getColumnModel().getColumn(0).setMinWidth(32);
        table.getColumnModel().getColumn(0).setMaxWidth(32);
        add(new JScrollPane(table), "spanx, grow");
        updateSelectionIcon();
        
        // Initialize rows
        for(int i = 0; i < rowCount; i++) {
            model.setValueAt(i + 1, i, 0);
        }
        
        // Create footer panel
        JPanel footerPanel = new JPanel(new MigLayout("insets 0", "0[]"));
        footerPanel.add(randomizeButton);
        footerPanel.add(clearAllButton);
        
        if(incluceLegalMode) {
            footerPanel.add(legalModeToggle);
        }
        
        add(footerPanel, "spanx, gapy 4");
    }
    
    public abstract void initializeOptions(int row);
    public abstract void randomizeSelections(int row);
    public abstract void clearSelections(int row);
    public abstract Image getSelectionIcon(int row);
    
    public void dataChanged(int row, int column, Object oldValue, Object newValue) {
        // Override
    }
    
    public void legalModeChanged() {
        // Override
    }
    
    public boolean shouldUpdateSelectionIcon(int column) {
        return true; // Override
    }
    
    public int getSelectionIconScale() {
        return 1;
    }
    
    public void initializeOptions() {
        for(int i = 0; i < rowCount; i++) {
            initializeOptions(i);
        }
    }
    
    public void randomizeSelections() {
        for(int i = 0; i < rowCount; i++) {
            randomizeSelections(i);
        }
    }
    
    public void clearSelections() {
        for(int i = 0; i < rowCount; i++) {
            clearSelections(i);
        }
    }
    
    public void setTitle(String text) {
        titleLabel.setText(text);
    }
    
    public void setDescription(String text) {
        descriptionLabel.setText(text);
    }
    
    public boolean isLegalMode() {
        return legalModeToggle.isSelected();
    }
    
    private void updateSelectionIcon() {
        BufferedImage image = (BufferedImage)getSelectionIcon(table.getSelectedRow());
        selectionIcon.setImage(image, getSelectionIconScale(), 96, 96);
    }
    
    protected <T> void setOptions(int row, int column, Collection<T> options) {
        setOptions(row, column, options, null);
    }
    
    protected <T> void setOptions(int row, int column, Collection<T> options, Comparator<T> sorter) {
        setOptions(row, column, options, sorter, false);
    }
    
    protected <T> void setOptions(int row, int column, Collection<T> options, boolean includeNullOption) {
        setOptions(row, column, options, null, includeNullOption);
    }
    
    protected <T> void setOptions(int row, int column, Collection<T> options, Comparator<T> sorter, boolean includeNullOption) {
        List<T> list = new ArrayList<>(options);
        
        if(sorter != null) {
            list.sort(sorter);
        }
        
        table.setOptionsAt(row, column, list, includeNullOption);
    }
    
    protected <T> List<T> computeSelectionList(Function<Integer, T> supplier, Function<Integer, Boolean> rowFilter) {
        List<T> list = new ArrayList<>();
        
        for(int i = 0; i < rowCount; i++) {
            if(rowFilter.apply(i)) {
                list.add(supplier.apply(i));
            }
        }
        
        return list;
    }
}
