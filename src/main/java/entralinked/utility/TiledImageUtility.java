package entralinked.utility;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for reading tiled images (C-Gear & Pokédex skin data) into a usable {@link BufferedImage}.
 */
public class TiledImageUtility {
    
    public static final int[] DEFAULT_DEX_BACKGROUND_COLORS = {
            0xE8E8E8, 0xE0E0E0, 0xD8D8D8, 0xD0D0D0, 0xD0C8C8, 0xC8C8C8, 0xC0C0C0, 0xB8B8B8, 0xB0B0B0, 0xA8A8A8, 0xA0A0A0,
            0xA0A098, 0x989898, 0x909090, 0x888888, 0x808080, 0x787878, 0x707070, 0x686868, 0x606060, 0x585858, 0x505050,
            0x484850, 0x484848, 0x484840, 0x404040, 0xF800F8, 0xF800F8, 0xF800F8, 0xF800F8, 0xF800F8, 0xF800F8, 0x282828,
            0x202020, 0x181818, 0x101010, 0xF800F8, 0x303030, 0x404040, 0x505050, 0xF800F8, 0xF800F8, 0xF800F8, 0xF800F8,
            0xF800F8, 0xF800F8, 0xF800F8, 0xF800F8, 0xF870A0, 0xF84878, 0xC03860, 0x802848, 0x482030, 0x505858, 0x909090,
            0xC8C8C8, 0xE0E8E8, 0xF800F8, 0x404848, 0x707878, 0xA8B0B0, 0xF800F8, 0xF800F8, 0xF800F8}; // Unsafe
    public static final int TILE_WIDTH = 8;
    public static final int TILE_HEIGHT = 8;
    public static final int TILE_SIZE = TILE_WIDTH * TILE_HEIGHT;
    public static final int COLOR_PALETTE_SIZE = 16;
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 192;
    public static final int SCREEN_SIZE = SCREEN_WIDTH * SCREEN_HEIGHT;
    public static final int SCREEN_TILE_COUNT = SCREEN_SIZE / TILE_SIZE;
    private static final byte[] dexBackgroundColorIndices = new byte[SCREEN_SIZE];
    private static final boolean[] dexBackgroundOverrides = new boolean[SCREEN_SIZE];
    private static final Logger logger = LogManager.getLogger();
    
    static {
        // Load Pokédex skin background data
        try(LEInputStream inputStream = new LEInputStream(TiledImageUtility.class.getResourceAsStream("/zukan.bin"))) {
            int index = 0;
            int pairs = inputStream.readShort();
            
            // Read background color indices
            for(int i = 0; i < pairs; i++) {
                int amount = inputStream.read();
                int value = inputStream.read();
                
                for(int j = 0; j < amount; j++) {
                    dexBackgroundColorIndices[index++] = (byte)(value & 63);
                }
            }
            
            index = 0;
            pairs = inputStream.readShort();
            
            // Read background override bits
            for(int i = 0; i < pairs; i++) {
                int amount = inputStream.readShort();
                int value = inputStream.read();
                
                for(int j = 0; j < amount; j++) {
                    for(int k = 0; k < 8; k++) {
                        dexBackgroundOverrides[index++] = (value & 1 << k) != 0;
                    }
                }
            }
        } catch(IOException e) {
            logger.error("Could not load Pokédex background data", e);
        }
    }
    
    /**
     * Reads a C-Gear skin from the specified {@link InputStream}.
     * 
     * @param inputStream The input stream to read from.
     * @param normalizeIndices Should be {@code true} if the provided C-Gear skin data is from the original Black & White.
     * @return A {@link BufferedImage} representing the read C-Gear skin data.
     */
    public static BufferedImage readCGearSkin(InputStream inputStream, boolean normalizeIndices) throws IOException {
        return readTiledImage(new LEInputStream(inputStream), 255, 0, null, null, normalizeIndices);
    }
    
    /**
     * Reads a Pokédex skin from the specified {@link InputStream}.
     * If the skin in question is an overlay, the Pokédex background will be applied to the resulting image automatically.
     * 
     * @param inputStream The input stream to read from.
     * @param applyBackground Whether to apply the Pokédex background to the result image, or read the data as-is.
     * @return A {@link BufferedImage} representing the read Pokédex skin data.
     */
    public static BufferedImage readDexSkin(InputStream inputStream, boolean applyBackground) throws IOException {
        return readTiledImage(new LEInputStream(inputStream), 768, 64,
                applyBackground ? dexBackgroundColorIndices : null, applyBackground ? dexBackgroundOverrides : null, false);
    }
    
    /**
     * Reads tiled image data from the provided {@link InputStream} and returns a {@link BufferedImage} representing the read image data.
     * 
     * @param inputStream The input stream to read from.
     * @param tileCount The number of tiles to be read. This should be equal to the maximum number of tiles for this image.
     * @param backgroundColorCount The number of background colors this image has.
     * @param backgroundColorIndices The background color indices of the background image. If {@code null}, no background will be used.
     * @param backgroundOverrides An array of booleans representing pixels that forcibly use the background color. Can be {@code null}.
     * @param normalizedIndices Indicates that tile indices are not linear (Black & White C-Gear skins) and should be normalized.
     * @return A {@link BufferedImage} representing the read image data.
     */
    private static BufferedImage readTiledImage(LEInputStream inputStream, int tileCount, int backgroundColorCount,
            byte[] backgroundColorIndices, boolean[] backgroundOverrides, boolean normalizeIndices) throws IOException {
        BufferedImage image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB); // Result image
        int[] tileData = new int[tileCount * TILE_SIZE];
        int[] tileIndices = new int[tileCount]; // Tile index lookup table
        int[] colorPalette = new int[COLOR_PALETTE_SIZE];
        int[] backgroundColorPalette = new int[backgroundColorCount];
        
        // Read tile data.
        for(int i = 0; i < tileCount; i++) {
            byte[] rawTileData = inputStream.readNBytes(TILE_SIZE / 2);
            
            for(int j = 0; j < rawTileData.length; j++) {
                int paletteIndices = rawTileData[j]; // Contains color palette indices for 2 adjacent pixels.
                tileData[i * TILE_SIZE + j * 2] = paletteIndices & (COLOR_PALETTE_SIZE - 1);
                tileData[i * TILE_SIZE + j * 2 + 1] = (paletteIndices >> 4) & (COLOR_PALETTE_SIZE - 1);
            }
            
            // Store the index of the tile as it would be in memory so we can look it up later when we're mapping the tiles.
            tileIndices[i] = normalizeIndices ? i + i / 17 * 15 + 0xA0A0 : i;
        }
        
        // Read foreground color data.
        // In cases where background colors are present, pixels that use the *first* foreground color
        // will be replaced by the background color at that pixel's location.
        for(int i = 0; i < COLOR_PALETTE_SIZE; i++) {
            colorPalette[i] = ColorUtility.convertBGR555ToRGB888(inputStream.readShort());
        }
        
        // Read background color data.
        // Pokédex skins contain room for 240 extra colors, 64 of which are defined and appear to be used as the 
        // 'background' colors in cases where the skin is only an overlay that is displayed on top of the 'true' Pokédex.
        for(int i = 0; i < backgroundColorCount; i++) {
            backgroundColorPalette[i] = ColorUtility.convertBGR555ToRGB888(inputStream.readShort());
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
                    int pixelIndex = pixelY * SCREEN_WIDTH + pixelX;
                    int paletteIndex = tileData[tileIndex * TILE_SIZE + tilePixelIndex];
                    
                    // Determine whether the foreground or background color should be used.
                    int color = backgroundColorIndices == null || 
                            (paletteIndex > 0 && (backgroundOverrides == null || !backgroundOverrides[pixelIndex])) 
                            ? colorPalette[paletteIndex] : backgroundColorPalette[backgroundColorIndices[pixelIndex]];
                    
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
                    int pixelIndex = pixelY * SCREEN_WIDTH + pixelX;
                    int paletteIndex = tileData[i * TILE_SIZE + j];
                    int color = backgroundColorIndices == null || 
                            (paletteIndex > 0 && (backgroundOverrides == null || !backgroundOverrides[pixelIndex])) 
                            ? colorPalette[paletteIndex] : backgroundColorPalette[backgroundColorIndices[pixelIndex]];
                    image.setRGB(pixelX, pixelY, color);
                }
            }
        }
        
        return image;
    }
    
    /**
     * Writes a {@link BufferedImage} to the specified {@link OutputStream} as a C-Gear skin.
     * 
     * @param outputStream The output stream to write to.
     * @param image The image to write.
     * @param offsetIndices Should be {@code true} if the C-Gear skin is intended for use with the original Black & White games.
     */
    public static void writeCGearSkin(OutputStream outputStream, BufferedImage image, boolean offsetIndices) throws IOException {
        writeTiledImage(new LEOutputStream(outputStream), image, 255, null, offsetIndices);
    }
    
    /**
     * Writes a {@link BufferedImage} to the specified {@link OutputStream} as a Pokédex skin.
     * 
     * @param outputStream The output stream to write to.
     * @param image The image to write.
     * @param backgroundColors The background colors this skin will use in-game. Should contain 64 RGB888 color values.
     */
    public static void writeDexSkin(OutputStream outputStream, BufferedImage image, int[] backgroundColors) throws IOException {
        if(backgroundColors == null || backgroundColors.length != 64) {
            throw new IllegalArgumentException("Background color array must contain 64 colors");
        }
        
        writeTiledImage(new LEOutputStream(outputStream), image, 768, backgroundColors, false);
        outputStream.write(new byte[352]); // Extra data that writeTiledImage doesn't write
    }
    
    /**
     * Writes a {@link BufferedImage} to the specified {@link OutputStream} as a tiled image.
     * 
     * @param outputStream The output stream to write to.
     * @param image The image to write.
     * @param tileCount The maximum number of unique tiles that can be used.
     * @param backgroundColors Additional background colors that the game might use. Can be {@code null}.
     * @param offsetIndices Indicates that tile indices are not linear (Black & White C-Gear skins) and should be offset.
     */
    private static void writeTiledImage(LEOutputStream outputStream, BufferedImage image,
            int tileCount, int[] backgroundColors, boolean offsetTileIndices) throws IOException {
        int[] tileData = new int[TILE_SIZE * tileCount]; // Stores colors, NOT indices!
        int[] tileMappingData = new int[SCREEN_TILE_COUNT];
        int[] colorPalette = new int[COLOR_PALETTE_SIZE];
        int currentTileCount = 0;
        int currentColorCount = 0;
        
        // Create tile data
        for(int i = 0; i < SCREEN_TILE_COUNT; i++) {
            // If tile count is limited, check if we can replicate this tile by flipping an existing tile.
            if(tileCount < SCREEN_TILE_COUNT) {
                boolean duplicateTile = false;
                
                for(int j = 0; j < currentTileCount; j++) {
                    boolean equal = true;
                    boolean flipX = true;
                    boolean flipY = true;
                    boolean flipXY = true;
                    
                    for(int k = 0; k < TILE_SIZE; k++) {
                        int x = i * TILE_WIDTH % SCREEN_WIDTH;
                        int y = i * TILE_WIDTH / SCREEN_WIDTH * TILE_WIDTH;
                        int color = image.getRGB(x + k % TILE_WIDTH, y + k / TILE_WIDTH);
                        equal &= color == tileData[j * TILE_SIZE + k];
                        flipX &= color == tileData[j * TILE_SIZE + ((TILE_WIDTH * (k / TILE_WIDTH) + TILE_WIDTH) - k % TILE_WIDTH - 1)];
                        flipY &= color == tileData[j * TILE_SIZE + (TILE_SIZE - (TILE_WIDTH * (k / TILE_WIDTH) + TILE_WIDTH) + k % TILE_WIDTH)];
                        flipXY &= color == tileData[j * TILE_SIZE + (TILE_SIZE - k - 1)];
                    }
                    
                    int flipBits = equal ? 0 : flipX ? 4 : flipY ? 8 : flipXY ? 12 : -1;
                    
                    // If this tile is equal to an existing tile or can be replicated by flipping another tile,
                    // create mapping data for it and skip to the next tile.
                    if(flipBits != -1) {
                        tileMappingData[i] = (offsetTileIndices ? (j + (15 * (j / 17)) + 0xA0A0) : j) | flipBits << 8;
                        duplicateTile = true;
                        break;
                    }
                }
                
                // Skip to next tile if it is duplicate.
                if(duplicateTile) {
                    continue;
                }
            }
            
            // Throw exception if a new tile needs to be added but we have reached the limit.
            if(currentTileCount >= tileCount) {
                throw new IllegalArgumentException(
                        "Image is too complex. Only %s unique tiles (8x8 pixel sections) are permitted.".formatted(tileCount));
            }
            
            int x = i * TILE_WIDTH % SCREEN_WIDTH;
            int y = i * TILE_WIDTH / SCREEN_WIDTH * TILE_WIDTH;
            
            for(int j = 0; j < TILE_SIZE; j++) {
                int pixelX = x + j % TILE_WIDTH;
                int pixelY = y + j / TILE_WIDTH;
                int color = image.getRGB(pixelX, pixelY);
                int paletteIndex = indexOf(colorPalette, color);
                
                 // Add color to the palette if it hasn't been already.
                if(paletteIndex == -1) {
                    // Throw exception if we have already reached the maximum amount of colors.
                    if(currentColorCount >= COLOR_PALETTE_SIZE) {
                        throw new IllegalArgumentException(
                                "Too many unique colors. Only %s unique colors are permitted.".formatted(COLOR_PALETTE_SIZE));
                    }
                    
                    paletteIndex = currentColorCount++;
                    colorPalette[paletteIndex] = color;
                }
                
                tileData[currentTileCount * TILE_SIZE + j] = color;
            }
            
            // Create mapping data for this tile.
            tileMappingData[i] = offsetTileIndices ? (currentTileCount + currentTileCount / 17 * 15 + 0xA0A0) : currentTileCount;
            currentTileCount++;
        }
        
        // Write tile data.
        for(int i = 0; i < tileData.length; i += 2) {
            int paletteIndices = indexOf(colorPalette, tileData[i]) | (indexOf(colorPalette, tileData[i + 1]) << 4);
            outputStream.write(paletteIndices);
        }
        
        // Write foreground color data.
        for(int color : colorPalette) {
            outputStream.writeShort(ColorUtility.convertRGB888ToBGR555(color));
        }
        
        // Write background color data if it is present.
        if(backgroundColors != null) {
            for(int color : backgroundColors) {
                outputStream.writeShort(ColorUtility.convertRGB888ToBGR555(color));
            }
        }
        
        // Write tile mapping data.
        if(tileCount < SCREEN_TILE_COUNT) {
            for(int i = 0; i < SCREEN_TILE_COUNT; i++) {
                outputStream.writeShort(tileMappingData[i]);
            }
        }
    }
    
    /**
     * @return The index of the element in the array, or {@code -1} if no such element exists.
     */
    private static int indexOf(int[] array, int element) {
        for(int i = 0; i < array.length; i++) {
            if(element == array[i]) {
                return i;
            }
        }
        
        return -1;
    }
}
