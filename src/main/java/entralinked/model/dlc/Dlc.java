package entralinked.model.dlc;

/**
 * Simple record for DLC data.
 */
public record Dlc(String path, String name, String gameCode, String type, 
        int index, int projectedSize, int checksum, boolean checksumEmbedded) {}
