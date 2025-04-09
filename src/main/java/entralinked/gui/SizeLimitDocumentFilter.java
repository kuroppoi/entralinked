package entralinked.gui;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class SizeLimitDocumentFilter extends DocumentFilter {
    
    private final int limit;
    
    public SizeLimitDocumentFilter(int limit) {
        this.limit = limit;
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
        replace(fb, offset, 0, text, attrs);
    }     
    
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if(text == null) {
            return;
        }
        
        if(limit <= 0) {
            super.replace(fb, offset, length, text, attrs);
            return;
        }
        
        int finalLength = Math.min(text.length(), Math.max(0, limit - fb.getDocument().getLength() + length));
        super.replace(fb, offset, length, text.substring(0, finalLength), attrs);
        
        if(finalLength != text.length()) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
