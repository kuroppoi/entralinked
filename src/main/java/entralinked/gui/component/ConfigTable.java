package entralinked.gui.component;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import entralinked.gui.ModelListCellRenderer;
import entralinked.gui.ModelTableCellRenderer;

/**
 * TODO look into custom table models because this current implementation is absolutely ass.
 */
@SuppressWarnings("serial")
public class ConfigTable extends JTable {
    
    private final Map<Integer, TableCellRenderer> tableCellRenderers = new HashMap<>();
    private final Map<Integer, ListCellRenderer<Object>> listCellRenderers = new HashMap<>();
    private final Map<Integer, ObjectToStringConverter> converters = new HashMap<>();
    private TableCellEditor[][] cellEditors;
    private boolean[][] disabledCells;
    
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        resizeArrays();
        return cellEditors[row][column];
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return tableCellRenderers.getOrDefault(column, super.getCellRenderer(row, column));
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        resizeArrays();
        return !disabledCells[row][column] && cellEditors[row][column] != null;
    }
    
    public void setCellEditable(int row, int column, boolean editable) {
        resizeArrays();
        disabledCells[row][column] = !editable;
    }
    
    public void setCellEditor(int row, int column, TableCellEditor editor) {
        resizeArrays();
        cellEditors[row][column] = editor;
    }
    
    public <T> void setCellRenderers(int column, Class<T> type, Function<T, String> supplier) {
        setCellRenderers(column, type, supplier, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> void setCellRenderers(int column, Class<T> type, Function<T, String> supplier, String nullValue) {
        tableCellRenderers.put(column, new ModelTableCellRenderer<T>(type, supplier, nullValue));
        listCellRenderers.put(column, new ModelListCellRenderer<T>(type, supplier, nullValue));
        converters.put(column, new ObjectToStringConverter() {
            @Override
            public String getPreferredStringForItem(Object item) {
                return (item == null || !type.isAssignableFrom(item.getClass())) ? null : supplier.apply((T)item);
            }
        });
    }
    
    public void enableOption(int row, int column) {
        if(isCellEditable(row, column)) {
            return;
        }
        
        Object initialValue = "N/A";
        CellEditor editor = getCellEditor(row, column);
        
        if(editor instanceof DefaultCellEditor) {
            Component component = ((DefaultCellEditor)editor).getComponent();
            
            if(component instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>)component;
                
                if(comboBox.getItemCount() > 0) {
                    initialValue = comboBox.getItemAt(0);
                }
            } else if(component instanceof JTextField) {
                initialValue = "";
            }
        }
        
        enableOption(row, column, initialValue);
    }
    
    public void enableOption(int row, int column, Object initialValue) {
        if(isCellEditable(row, column)) {
            return;
        }
        
        getModel().setValueAt(initialValue, row, column);
        setCellEditable(row, column, true);
    }
    
    public void disableOption(int row, int column) {
        setCellEditable(row, column, false);
        getModel().setValueAt("", row, column);
    }
    
    public void setOptionsAt(int row, int column, Collection<?> options) {
        setOptionsAt(row, column, options, false);
    }
    
    public void setOptionsAt(int row, int column, Collection<?> options, boolean includeNullOption) {
        if(options.isEmpty()) {
            setCellEditor(row, column, null);
            getModel().setValueAt("N/A", row, column);
            return;
        }
        
        Vector<?> vector = new Vector<>(options);
        
        if(includeNullOption) {
            vector.add(0, null);
        }
        
        Object currentValue = getValueAt(row, column);
        
        if(currentValue == null || !vector.contains(currentValue)) {
            currentValue = vector.firstElement();
            getModel().setValueAt(currentValue, row, column);
        }
        
        JComboBox<?> comboBox = new JComboBox<>(vector);
        AutoCompleteDecorator.decorate(comboBox, converters.getOrDefault(column, ObjectToStringConverter.DEFAULT_IMPLEMENTATION));
        
        if(listCellRenderers.containsKey(column)) {
            comboBox.setRenderer(listCellRenderers.get(column));
        }
        
        setCellEditor(row, column, new ComboBoxCellEditor(comboBox));
    }
    
    public void randomizeSelection(int row, int column) {
        randomizeSelection(row, column, true);
    }
    
    public void randomizeSelection(int row, int column, boolean allowNull) {
        CellEditor editor = getCellEditor(row, column);
        
        if(!(editor instanceof DefaultCellEditor)) {
            return;
        }
        
        Component component = ((DefaultCellEditor)editor).getComponent();
        
        if(component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>)component;
            int count = comboBox.getItemCount();
            
            if(count <= 1) {
                return;
            }
            
            int index = (int)(Math.random() * count);
            Object item = comboBox.getItemAt(index);
            
            if(!allowNull) {
                // TODO potential infinite loop
                while(item == null) {
                    item = comboBox.getItemAt((int)(Math.random() * count));
                }
            }
            
            getModel().setValueAt(item, row, column);
        }
    }
    
    public <T> T getValueAt(int row, int column, Class<T> type) {
        return getValueAt(row, column, type, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValueAt(int row, int column, Class<T> type, T def) {
        Object value = getValueAt(row, column);
        
        if(value != null && type.isAssignableFrom(value.getClass())) {
            return (T)value;
        }
        
        return def;
    }
    
    private void resizeArrays() {
        int rows = getRowCount();
        int columns = getColumnCount();
        
        if(cellEditors == null) {
            cellEditors = new TableCellEditor[rows][columns];
            disabledCells = new boolean[rows][columns];
            return;
        }
                
        // Check row size
        if(rows != cellEditors.length) {
            cellEditors = Arrays.copyOf(cellEditors, rows);
            disabledCells = Arrays.copyOf(disabledCells, rows);
        }
        
        // Check column size
        if(rows > 0 && cellEditors[0].length != columns) {
            for(int i = 0; i < rows; i++) {
                cellEditors[i] = Arrays.copyOf(cellEditors[i], columns);
                disabledCells[i] = Arrays.copyOf(disabledCells[i], columns);
            }
        }
    }
}
