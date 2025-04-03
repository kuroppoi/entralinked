package entralinked.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import entralinked.utility.SwingUtility;

@SuppressWarnings({"serial", "unchecked"})
public class ModelTableCellRenderer<T> extends DefaultTableCellRenderer {
    
    private final Class<T> type;
    private final Function<T, String> textSupplier;
    private final String nullValue;
    
    public ModelTableCellRenderer(Class<T> type, Function<T, String> textSupplier, String nullValue) {
        this.type = type;
        this.textSupplier = textSupplier;
        this.nullValue = nullValue;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setText(value == null ? String.valueOf(nullValue) : type.isAssignableFrom(value.getClass()) ? textSupplier.apply((T)value) : String.valueOf(value));
        Font font = component.getFont();
        String text = getText();
        
        if(font.canDisplayUpTo(text) != -1) {
            component.setFont(SwingUtility.findSupportingFont(text, font));
        }
        
        return component;
    }
}
