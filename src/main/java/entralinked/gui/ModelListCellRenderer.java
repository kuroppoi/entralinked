package entralinked.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import entralinked.utility.SwingUtility;

@SuppressWarnings({"serial", "unchecked"})
public class ModelListCellRenderer<T> extends DefaultListCellRenderer {
    
    private final Class<T> type;
    private final Function<T, String> textSupplier;
    private final String nullValue;
    
    public ModelListCellRenderer(Class<T> type, Function<T, String> textSupplier, String nullValue) {
        this.type = type;
        this.textSupplier = textSupplier;
        this.nullValue = nullValue;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(value == null ? String.valueOf(nullValue) : type.isAssignableFrom(value.getClass()) ? textSupplier.apply((T)value) : String.valueOf(value));
        Font font = component.getFont();
        String text = getText();
        
        if(font.canDisplayUpTo(text) != -1) {
            component.setFont(SwingUtility.findSupportingFont(text, font));
        }
        
        return component;
    }
}
