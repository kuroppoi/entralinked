package entralinked.gui.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class ShadowedSprite extends JComponent {
    
    private BufferedImage image;
    private BufferedImage shadowImage;
    private int scale;
    
    public ShadowedSprite() {
        setOpaque(false);
    }
    
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        
        if(image == null) {
            return;
        }
        
        Dimension size = getSize();
        int width = image.getWidth() * scale;
        int height = image.getHeight() * scale;
        int x = size.width / 2 - width / 2;
        int y = size.height / 2 - height / 2;
        graphics.drawImage(shadowImage, x + 4, y + 2, width, height, null);
        graphics.drawImage(image, x, y, width, height, null);
    }
    
    private void imageChanged() {
        if(image == null) {
            return;
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        shadowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // TODO shadow can be cut off if pixels touch the border
        for(int i = 0; i < image.getWidth(); i++) {
            for(int j = 0; j < image.getHeight(); j++) {
                shadowImage.setRGB(i, j, image.getRGB(i, j) & 0x7F000000);
            }
        }
    }
    
    private void updateSize(int width, int height) {
        Dimension oldSize = getPreferredSize();
        Dimension size = new Dimension(width, height);
        setMinimumSize(size);
        setPreferredSize(size);
        
        if(oldSize.width != width || oldSize.height != height) {
            revalidate();
        }
    }
    
    public void setImage(BufferedImage image) {
        setImage(image, 1);
    }
    
    public void setImage(BufferedImage image, int width, int height) {
        setImage(image, 1, width, height);
    }
    
    public void setImage(BufferedImage image, int scale) {
        setImage(image, scale, image == null ? 0 : image.getWidth(), image == null ? 0 : image.getHeight());
    }
    
    public void setImage(BufferedImage image, int scale, int width, int height) {
        this.image = image;
        this.scale = scale;
        imageChanged();
        updateSize(width, height);
        repaint();
    }
}
