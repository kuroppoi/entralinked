package entralinked.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ColorUtilityTest {
    
    @Test
    @DisplayName("Test if multiplied colors are correct")
    void testColorMultiplying() {
        assertEquals(0x7F7F7F, ColorUtility.multiplyColor(0xFFFFFF, 0.5));
        assertEquals(0xFEFEFE, ColorUtility.multiplyColor(0x7F7F7F, 2.0));
        
        // Test clamping
        assertEquals(0xFFFFFF, ColorUtility.multiplyColor(0xFEFEFE, 2.0));
        assertEquals(0x000000, ColorUtility.multiplyColor(0x010101, 0.5));
    }
    
    @Test
    @DisplayName("Test if colors converted from RGB888 to BGR555 are correct")
    void testRGB88ToBGR555Converter() {
        // Black, white, red, green and blue
        assertEquals(0x0000, ColorUtility.convertRGB888ToBGR555(0x000000));
        assertEquals(0x7FFF, ColorUtility.convertRGB888ToBGR555(0xFFFFFF));
        assertEquals(0x001F, ColorUtility.convertRGB888ToBGR555(0xFF0000));
        assertEquals(0x03E0, ColorUtility.convertRGB888ToBGR555(0x00FF00));
        assertEquals(0x7C00, ColorUtility.convertRGB888ToBGR555(0x0000FF));
        
        // Random colors
        assertEquals(0x07C7, ColorUtility.convertRGB888ToBGR555(0x39F20C));
        assertEquals(0x68B3, ColorUtility.convertRGB888ToBGR555(0x9E2BD3));
        assertEquals(0x6DFE, ColorUtility.convertRGB888ToBGR555(0xF07CDE));
        assertEquals(0x22A4, ColorUtility.convertRGB888ToBGR555(0x26AC44));
    }
    
    @Test
    @DisplayName("Test if colors converted from BGR555 to RGB888 are correct")
    void testBGR555ToRGB888Converter() {
        // Black, white, red, green and blue
        assertEquals(0x000000, ColorUtility.convertBGR555ToRGB888(0x0000));
        assertEquals(0xFFFFFF, ColorUtility.convertBGR555ToRGB888(0xFFFF));
        assertEquals(0xFF0000, ColorUtility.convertBGR555ToRGB888(0x001F));
        assertEquals(0x00FF00, ColorUtility.convertBGR555ToRGB888(0x03E0));
        assertEquals(0x0000FF, ColorUtility.convertBGR555ToRGB888(0x7C00));
        
        // Random colors
        assertEquals(0x7BA59C, ColorUtility.convertBGR555ToRGB888(0x4E8F));
        assertEquals(0x948CFF, ColorUtility.convertBGR555ToRGB888(0xFE32));
        assertEquals(0xBD2121, ColorUtility.convertBGR555ToRGB888(0x1097));
        assertEquals(0x5A185A, ColorUtility.convertBGR555ToRGB888(0xAC6B));
    }
}
