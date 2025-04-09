package entralinked.gui;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import entralinked.utility.GsidUtility;

public class GsidDocumentFilter extends SizeLimitDocumentFilter {
    
    public GsidDocumentFilter() {
        super(10);
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
        
        StringBuilder builder = new StringBuilder();
        boolean shouldBeep = false;
        
        for(int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            
            if(GsidUtility.GSID_CHARTABLE.indexOf(c) != -1) {
                builder.append(c);
            } else {
                shouldBeep = true;
            }
        }
        
        if(shouldBeep) {
            Toolkit.getDefaultToolkit().beep();
        }
        
        super.replace(fb, offset, length, builder.toString(), attrs);
    }
}
