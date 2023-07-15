package entralinked.utility;

/**
 * Utility class for various color-related shenanigans.
 */
public class ColorUtility {
    
    /**
     * Multiplies the red, green and blue components of the specified RGB888 color value by the given factor.
     * 
     * @param color The color to multiply.
     * @param factor The factor to use.
     * @return The multiplied color.
     */
    public static int multiplyColor(int color, double factor) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        red = Math.max(0, Math.min(255, (int)(red * factor)));
        green = Math.max(0, Math.min(255, (int)(green * factor)));
        blue = Math.max(0, Math.min(255, (int)(blue * factor)));
        return (red << 16) | (green << 8) | blue;
    }
    
    /**
     * Converts the input BGR555 color value to an RGB888 color value.
     * 
     * @return The RGB888 color value.
     */
    public static int convertBGR555ToRGB888(int color) {
        int red = (color & 0x1F) << 3;
        int green = ((color & 0x3E0) >> 5) << 3;
        int blue = ((color & 0x7C00) >> 10) << 3;
        return (red << 16) | (green << 8) | blue;
    }
    
    /**
     * Converts the input RGB888 color value to a BGR555 color value.
     * 
     * @return The BGR555 color value.
     */
    public static int convertRGB888ToBGR555(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        return (red >> 3) | (green >> 3 << 5) | (blue >> 3 << 10);
    }
}
