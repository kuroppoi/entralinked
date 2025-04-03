package entralinked.gui;

import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class InputVerifierCellEditor extends DefaultCellEditor {

    private final InputVerifier verifier;
    
    public InputVerifierCellEditor(InputVerifier verifier) {
        this(verifier, new JTextField());
    }
    
    public InputVerifierCellEditor(InputVerifier verifier, JTextField textField) {
        super(textField);
        this.verifier = verifier;
    }
    
    @Override
    public boolean stopCellEditing() {
        return verifier.verify(editorComponent) && super.stopCellEditing();
    }
}
