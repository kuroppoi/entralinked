package entralinked.gui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import entralinked.gui.data.DataManager;

public class ImageLoader {
    
    private static final Map<String, BufferedImage> cache = new HashMap<>();
    
    public static BufferedImage getImage(String path) {
        return cache.computeIfAbsent(path, ImageLoader::loadImage);
    }
    
    private static BufferedImage loadImage(String path) {        
        try {
            return ImageIO.read(DataManager.class.getResource(path));
        } catch(Exception e) {
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.drawString("IMAGE LOAD ERROR", 0, 16);
            graphics.drawString("Please report this!", 0, 124);
            drawWrappedString(graphics, path, 0, 32, image.getWidth());
            drawWrappedString(graphics, e.getMessage(), 0, 80, image.getWidth());
            graphics.dispose();
            return image;
        }
    }
    
    private static void drawWrappedString(Graphics2D graphics, String string, int x, int y, int width) {
        int length = string.length();
        FontMetrics metrics = graphics.getFontMetrics();
        String line = "";
        int currentY = y;
        
        for(int i = 0; i < length; i++) {
            char next = string.charAt(i);
            
            if((!line.isEmpty() && x + metrics.stringWidth(line + next) >= width)) {
                graphics.drawString(line, x, currentY);
                line = "";
                currentY += metrics.getHeight();
            }
            
            line += next;
            
            if(i + 1 == length) {
                graphics.drawString(line, x, currentY);
            }
        }
    }
}
