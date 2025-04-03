package entralinked.gui.panels;

import java.awt.Image;
import java.util.stream.IntStream;

import entralinked.GameVersion;
import entralinked.gui.data.DataManager;
import entralinked.model.player.DreamItem;
import entralinked.model.player.Player;

@SuppressWarnings("serial")
public class ItemEditorPanel extends TableEditorPanel {
    
    public static final int ITEM_COUNT = 20;
    public static final int MAX_ITEM_QUANTITY = 20;
    public static final int ITEM_COLUMN = 1;
    public static final int QUANTITY_COLUMN = 2;
    private static final String[] COLUMN_NAMES = { "Item", "Quantity" };
    private static final Class<?>[] COLUMN_TYPES = { Integer.class, Integer.class };
    private GameVersion gameVersion;
    
    public ItemEditorPanel() {
        super(ITEM_COUNT, COLUMN_NAMES, COLUMN_TYPES);
        setTitle("Dream Remnants");
        setDescription("""
                <html>
                Use the table below to select the items you want to receive.<br/>
                After waking up your Pokémon, talk to the boy near the<br/>
                Entree Forest entrance in the Entralink to receive your items.
                </html>
                """);
        table.setCellRenderers(ITEM_COLUMN, Integer.class, DataManager::getItemName, "— — — — —");
        initializeOptions();
    }
    
    @Override
    public void dataChanged(int row, int column, Object oldValue, Object newValue) {
        if(column != ITEM_COLUMN) {
            return;
        }
        
        if(newValue != null) {
            if(oldValue != null) {
                return; // Do nothing if new and old values are both non-null
            }
            
            table.enableOption(row, QUANTITY_COLUMN);
            return;
        }
        
        table.disableOption(row, QUANTITY_COLUMN);
    }
    
    @Override
    public void legalModeChanged() {
        for(int i = 0; i < ITEM_COUNT; i++) {
            updateItemOptions(i);
        }
    }
    
    @Override
    public void initializeOptions(int row) {
        setOptions(row, QUANTITY_COLUMN, IntStream.rangeClosed(1, ITEM_COUNT).boxed().toList());
        table.disableOption(row, QUANTITY_COLUMN);
    }

    @Override
    public void randomizeSelections(int row) {
        table.randomizeSelection(row, ITEM_COLUMN, false);
        int quantity = 1;
        
        while(quantity < MAX_ITEM_QUANTITY && Math.random() < 0.5) {
            quantity++;
        }
        
        model.setValueAt(quantity, row, QUANTITY_COLUMN);
    }

    @Override
    public void clearSelections(int row) {
        model.setValueAt(null, row, ITEM_COLUMN);
    }
    
    @Override
    public Image getSelectionIcon(int row) {
        int item = row == -1 ? 0 : getItem(row);
        return DataManager.getItemSprite(item);
    }
    
    @Override
    public boolean shouldUpdateSelectionIcon(int column) {
        return column == ITEM_COLUMN;
    }
    
    @Override
    public int getSelectionIconScale() {
        return 2;
    }
    
    public void loadProfile(Player player) {
        gameVersion = player.getGameVersion();
        table.clearSelection();
        clearSelections();
        legalModeToggle.setSelected(false);
        legalModeChanged();
        int row = 0;
        
        for(DreamItem item : player.getItems()) {
            model.setValueAt(item.id(), row, ITEM_COLUMN);
            model.setValueAt(item.quantity(), row, QUANTITY_COLUMN);
            row++;
        }
    }
    
    public void saveProfile(Player player) {
        player.setItems(computeSelectionList(row -> new DreamItem(getItem(row), getQuantity(row)), row -> getItem(row) != 0));
    }
    
    private void updateItemOptions(int row) {
        setOptions(row, ITEM_COLUMN,
                isLegalMode() ? DataManager.getItemOptions() : DataManager.getDownloadableItems(gameVersion),
                (a, b) -> DataManager.getItemName(a).compareTo(DataManager.getItemName(b)), true);
    }
    
    private int getItem(int row) {
        return table.getValueAt(row, ITEM_COLUMN, Integer.class, 0);
    }
    
    private int getQuantity(int row) {
        return table.getValueAt(row, QUANTITY_COLUMN, Integer.class, 0);
    }
}
