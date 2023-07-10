package entralinked.utility;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for reading tiled images (C-Gear & Pokédex skin data) into a usable {@link BufferedImage}.
 */
public class TiledImageReader {
    
    public static final int TILE_WIDTH = 8;
    public static final int TILE_HEIGHT = 8;
    public static final int TILE_SIZE = TILE_WIDTH * TILE_HEIGHT;
    public static final int COLOR_PALETTE_SIZE = 16;
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;
    public static final int SCREEN_SIZE = SCREEN_WIDTH * SCREEN_HEIGHT;
    public static final int SCREEN_TILE_COUNT = SCREEN_SIZE / TILE_SIZE;
    private static final byte[] dexBackgroundColorIndices = new byte[SCREEN_SIZE];
    private static final Logger logger = LogManager.getLogger();
    
    static {
        // Load Pokédex skin background color indices
        try(InputStream inputStream = TiledImageReader.class.getResourceAsStream("/zukan.bin")) {
            int currentIndex = 0;
            int valueAmount = -1;
            int value = -1;
            
            // Read until end of stream
            while((valueAmount = inputStream.read()) != -1 && (value = inputStream.read()) != -1) {
                for(int i = 0; i < valueAmount; i++) {
                    dexBackgroundColorIndices[currentIndex++] = (byte)(value & 63);
                }
            }
        } catch(IOException e) {
            logger.error("Could not load Pokédex background data", e);
        }
    }
    
    /**
     * Reads a C-Gear skin from the specified {@link InputStream}.
     * 
     * @param normalizeIndices Should be {@code true} if the provided C-Gear skin data is from the original Black & White.
     * @return A {@link BufferedImage} representing the read C-Gear skin data.
     */
    public static BufferedImage readCGearSkin(InputStream inputStream, boolean normalizeIndices) throws IOException {
        return readTiledImage(inputStream, 255, 0, null, normalizeIndices);
    }
    
    /**
     * Reads a Pokédex skin from the specified {@link InputStream}.
     * If the skin in question is an overlay, the Pokédex background will be applied to the resulting image automatically.
     * 
     * @return A {@link BufferedImage} representing the read Pokédex skin data.
     */
    public static BufferedImage readDexSkin(InputStream inputStream) throws IOException {
        return readTiledImage(inputStream, 768, 64, dexBackgroundColorIndices, false);
    }
    
    /**
     * Reads tiled image data from the provided {@link InputStream} and returns a {@link BufferedImage} representing the read image data.
     * 
     * @param tileCount The number of tiles to be read. This should be equal to the maximum number of tiles for this image.
     * @param backgroundColorCount The number of background colors this image has.
     * @param backgroundColorIndices The background color indices of the background image. If {@code null}, no background will be used.
     * @param normalizedIndices Indicates that tile indices are not linear (Black & White C-Gear skins) and should be normalized.
     * @return A {@link BufferedImage} representing the read image data.
     */
    public static BufferedImage readTiledImage(InputStream inputStream, 
            int tileCount, int backgroundColorCount, byte[] backgroundColorIndices, boolean normalizeIndices) throws IOException {
        BufferedImage image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB); // Result image
        int[] tileData = new int[tileCount * TILE_SIZE];
        int[] tileIndices = new int[tileCount]; // Tile index lookup table
        int[] colorPalette = new int[COLOR_PALETTE_SIZE];
        int[] backgroundColorPalette = new int[backgroundColorCount];
        boolean[] transparencyMarkers = new boolean[COLOR_PALETTE_SIZE]; // Keeps track of which colors should become background colors
        
        // Read tile data.
        for(int i = 0; i < tileCount; i++) {
            // TODO This next loop is a bottleneck
            for(int j = 0; j < TILE_SIZE / 2; j++) {
                int paletteIndices = inputStream.read(); // Contains color palette indices for 2 adjacent pixels.
                tileData[i * TILE_SIZE + j * 2] = paletteIndices & (COLOR_PALETTE_SIZE - 1);
                tileData[i * TILE_SIZE + j * 2 + 1] = (paletteIndices >> 4) & (COLOR_PALETTE_SIZE - 1);
            }
            
            // Store the index of the tile as it would be in memory so we can look it up later when we're mapping the tiles.
            tileIndices[i] = normalizeIndices ? i + i / 17 * 15 + 0xA0A0 : i;
        }
        
        // Read foreground color data.
        for(int i = 0; i < COLOR_PALETTE_SIZE; i++) {
            int leftBits = inputStream.read();
            int rightBits = inputStream.read();
            
            // So far, the least significant bits of colors that are replaced by background colors are always 0x2A.
            // TODO Needs more thorough testing -- is this check reliable? What do the most significant bits signify here?
            if(backgroundColorIndices != null && rightBits == 0x2A) {
                transparencyMarkers[i] = true;
                continue;
            }
            
            colorPalette[i] = convertColor(leftBits | rightBits << 8);
        }
        
        // Read background color data.
        // Pokédex skins contain room for 240 extra colors, 64 of which are defined and appear to be used as the 
        // 'background' colors in cases where the skin is only an overlay that is displayed on top of the 'true' Pokédex.
        for(int i = 0; i < backgroundColorCount; i++) {
            backgroundColorPalette[i] = convertColor(inputStream.read() | inputStream.read() << 8);
        }
        
        // Map tiles to the resulting image.
        // In cases where the tile count is 768 or greater, which is exactly enough tiles to fill the entire screen,
        // the tiles will be applied in the order they are provided and no additional mapping data will be read.
        // This is always the case for Pokédex skins, and never the case for C-Gear skins.
        if(tileCount < SCREEN_TILE_COUNT) {
            // Not enough tiles -- read additional mapping data to figure out their placement.
            for(int i = 0; i < SCREEN_TILE_COUNT; i++) {
                int x = i * TILE_WIDTH % SCREEN_WIDTH;
                int y = i * TILE_WIDTH / SCREEN_WIDTH * TILE_HEIGHT;
                int leftBits = inputStream.read();
                int rightBits = inputStream.read();
                int memoryIndex = leftBits | (rightBits & ~12) << 8;
                int flipBits = rightBits & 12;
                
                // The normalized tile index is the index of the in-memory tile index in the lookup table.
                int tileIndex = 0;
                
                for(int k = 0; k < tileIndices.length; k++) {
                    if(memoryIndex == tileIndices[k]) {
                        tileIndex = k;
                        break;
                    }
                }
                
                // Apply the pixels of this tile to the resulting image.
                for(int j = 0; j < TILE_SIZE; j++) {
                    // Get the color index for this pixel based on how the tile is flipped.
                    int tilePixelIndex = switch(flipBits) {
                        case 4 -> (TILE_WIDTH * (j / TILE_WIDTH) + TILE_WIDTH) - j % TILE_WIDTH - 1; // Flip horizontally
                        case 8 -> TILE_SIZE - (TILE_WIDTH * (j / TILE_WIDTH) + TILE_WIDTH) + j % TILE_WIDTH; // Flip vertically
                        case 12 -> TILE_SIZE - j - 1; // Flip horizontally & vertically
                        default -> j; // Don't flip
                    };
                    
                    int pixelX = x + j % TILE_WIDTH;
                    int pixelY = y + j / TILE_WIDTH;
                    int paletteIndex = tileData[tileIndex * TILE_SIZE + tilePixelIndex];
                    
                    // If this color has been marked as a 'transparent' color, use the color from the background instead.
                    int color = !transparencyMarkers[paletteIndex] ? colorPalette[paletteIndex]
                            : backgroundColorPalette[backgroundColorIndices[pixelY * SCREEN_WIDTH + pixelX]];
                    
                    // Finally, set the pixel!
                    image.setRGB(pixelX, pixelY, color);
                }
            }
        } else {
            // There are enough tiles to fill up the entire screen, so let's just place them in order.
            for(int i = 0; i < SCREEN_TILE_COUNT; i++) {
                int x = i * TILE_WIDTH % SCREEN_WIDTH;
                int y = i * TILE_WIDTH / SCREEN_WIDTH * TILE_WIDTH;
                
                for(int j = 0; j < TILE_SIZE; j++) {
                    int pixelX = x + j % TILE_WIDTH;
                    int pixelY = y + j / TILE_WIDTH;
                    int paletteIndex = tileData[i * TILE_SIZE + j];
                    int color = !transparencyMarkers[paletteIndex] ? colorPalette[paletteIndex]
                            : backgroundColorPalette[backgroundColorIndices[pixelY * SCREEN_WIDTH + pixelX]];
                    image.setRGB(pixelX, pixelY, color);
                }
            }
        }
        
        return image;
    }
    
    /**
     * Converts the input BGR555 color value to an RGB888 color value.
     * 
     * @return The RGB888 color value.
     */
    private static int convertColor(int color) {
        int red = (color & 0x1F) << 3;
        int green = ((color & 0x3E0) >> 5) << 3;
        int blue = ((color & 0x7C00) >> 10) << 3;
        return (red << 16) | (green << 8) | blue;
    }
}
