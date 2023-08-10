package entralinked.utility;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

public class SwingUtility {
    
    @SuppressWarnings("serial")
    public static Action createAction(String name, Icon icon, Runnable handler) {
        AbstractAction action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.run();
            }
        };
        
        if(icon != null) {
            action.putValue(Action.SHORT_DESCRIPTION, name);
        }
        
        return action;
    }
    
    public static Action createAction(String name, Runnable handler) {
        return createAction(name, null, handler);
    }
    
    public static GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
        return createConstraints(x, y, width, height, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY) {
        return createConstraints(x, y, width, height, weightX, weightY, 8, 8);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY, 
            int paddingX, int paddingY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        constraints.ipadx = paddingX;
        constraints.ipady = paddingY;
        return constraints;
    }
}
