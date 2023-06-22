package entralinked.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GsidUtilityTest {
    
    @Test
    @DisplayName("Test if output stringified Game Sync IDs are correct")
    void testGameSyncIdStringifier() {
        assertEquals("G5T5MB69TA", GsidUtility.stringifyGameSyncId(45991782));
        assertEquals("S6MJNM63AC", GsidUtility.stringifyGameSyncId(381955984));
        assertEquals("RMLLERWPSA", GsidUtility.stringifyGameSyncId(507849071));
        assertEquals("J89BGT23UD", GsidUtility.stringifyGameSyncId(576782280));
        assertEquals("K3D29LTGSB", GsidUtility.stringifyGameSyncId(1442582313));
        assertEquals("8YJN6SKKGF", GsidUtility.stringifyGameSyncId(1640375006));
    }
    
    @Test
    @DisplayName("Test if invalid Game Sync IDs are seen as invalid")
    void testInvalidGameSyncIds() {     
        // Illegal characters (I, O, 0, 1)
        assertFalse(GsidUtility.isValidGameSyncId("0000000000"));
        assertFalse(GsidUtility.isValidGameSyncId("ABCDEFGHIJ"));
        assertFalse(GsidUtility.isValidGameSyncId("1OEKLRO493"));
        
        // Illegal length (should be 10)
        assertFalse(GsidUtility.isValidGameSyncId("Y67UEN38K"));
        assertFalse(GsidUtility.isValidGameSyncId("3ER5K8MBN4C"));
    }
    
    @Test
    @DisplayName("Test if valid Game Sync IDs are seen as valid")
    void testValidGameSyncIds() {
        assertTrue(GsidUtility.isValidGameSyncId("VFWM2QAXNF"));
        assertTrue(GsidUtility.isValidGameSyncId("44DAWDJKJ8"));
        assertTrue(GsidUtility.isValidGameSyncId("J6F55UB2X9"));
        assertTrue(GsidUtility.isValidGameSyncId("8FAB4Z3EN9"));
        assertTrue(GsidUtility.isValidGameSyncId("HWLNS7BTNB"));
    }
}
