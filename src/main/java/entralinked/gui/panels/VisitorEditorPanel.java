package entralinked.gui.panels;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

import entralinked.GameVersion;
import entralinked.gui.ImageLoader;
import entralinked.gui.InputVerifierCellEditor;
import entralinked.gui.data.Country;
import entralinked.gui.data.DataManager;
import entralinked.gui.data.PkmnSpecies;
import entralinked.gui.data.Region;
import entralinked.model.avenue.AvenueShopType;
import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.avenue.AvenueVisitorType;
import entralinked.model.player.Player;

@SuppressWarnings("serial")
public class VisitorEditorPanel extends TableEditorPanel {
    
    public static final int VISITOR_COUNT = 12;
    public static final int TYPE_COLUMN = 1;
    public static final int NAME_COLUMN = 2;
    public static final int SHOP_COLUMN = 3;
    public static final int GAME_COLUMN = 4;
    public static final int COUNTRY_COLUMN = 5;
    public static final int REGION_COLUMN = 6;
    public static final int PHRASE_COLUMN = 7;
    public static final int DREAMER_COLUMN = 8;
    private static final String[] COLUMN_NAMES = { "Type", "Name", "Shop", "Game", "Country", "Region", "Phrase", "Pokémon" };
    private static final Class<?>[] COLUMN_TYPES = { AvenueVisitorType.class, String.class, AvenueShopType.class, GameVersion.class, Country.class, Region.class, Integer.class, PkmnSpecies.class };
    
    // Name tables for randomization
    // TODO more languages would be neat but isn't really necessary
    private static final String[] ENG_NAMES_M = { "Aleron", "Alpi", "Amando", "Anselmi", "Anton", "Arhippa", "Arpo", "Artos", "Assar", "Atilio", "Axel", "Azzo", "Jasper", "Jaylen", "Jephew", "Jimbo", "Joakim", "Joelle", "Jonas", "Jule", "Julian", "Julio", "Justan" };
    private static final String[] ENG_NAMES_F = { "Agata", "Ainikki", "Alena", "Alibena", "Alwyn", "Anelma", "Anneli", "Annetta", "Antonie", "Armina", "Assunta", "Asta", "Jaclyn", "Jane", "Janette", "Jannis", "Jeanne", "Jenna", "Jess", "Joan", "Jocelyn", "Josie", "Judith", "Julie" };
    private static final String[] JAP_NAMES_M = { "アートス", "アーポ", "アクセル", "アッサール", "アッツォ", "アッラン", "アティリオ", "アマンド", "アルピ", "アルヒッパ", "アンセルミ", "アントン", "ジェヒュー", "ジェリー", "ジェローム", "ジャコブ", "ジャスパー", "ジャレッド", "ジャン", "ジュール", "ジョエル", "ジョシュア", "ジョナス" };
    private static final String[] JAP_NAMES_F = { "アーム", "アイニッキ", "アガタ", "アスタ", "アッスンタ", "アネルマ", "アラベッラ", "アリビーナ", "アレーナ", "アントニナ", "アンネッタ", "アンネリ", "ジェーン", "ジェシー", "ジェナ", "ジャサント", "ジャニス", "ジャンヌ", "ジュディス", "ジュリー", "ジョアン", "ジョイス", "ジョスリン", "ジョゼ" };
    
    public VisitorEditorPanel() {
        super(VISITOR_COUNT, false, COLUMN_NAMES, COLUMN_TYPES);
        setTitle("Join Avenue");
        setDescription("""
                <html>
                Use the table below to configure Join Avenue visitors.<br/>
                Please be aware that new visitors will not appear if you have<br/>
                already reached the max amount of concurrent visitors.
                </html>
                """);
        table.setCellRenderers(TYPE_COLUMN, AvenueVisitorType.class, AvenueVisitorType::getDisplayName, "— — — — —");
        table.setCellRenderers(SHOP_COLUMN, AvenueShopType.class, AvenueShopType::getDisplayName);
        table.setCellRenderers(GAME_COLUMN, GameVersion.class, GameVersion::getDisplayName);
        table.setCellRenderers(COUNTRY_COLUMN, Country.class, Country::name);
        table.setCellRenderers(REGION_COLUMN, Region.class, Region::name);
        table.setCellRenderers(DREAMER_COLUMN, PkmnSpecies.class, PkmnSpecies::name);
        initializeOptions();
    }
    
    @Override
    public void dataChanged(int row, int column, Object oldValue, Object newValue) {
        if(column == TYPE_COLUMN) {
            if(newValue != null) {
                if(oldValue != null) {
                    return; // Do nothing if new and old values are both non-null
                }
                
                table.enableOption(row, SHOP_COLUMN);
                table.enableOption(row, GAME_COLUMN, GameVersion.BLACK_2_ENGLISH); // Updates country -> region
                table.enableOption(row, PHRASE_COLUMN);
                table.enableOption(row, DREAMER_COLUMN);
                table.enableOption(row, NAME_COLUMN, findRandomName(row));
                model.setValueAt(findRandomName(row), row, NAME_COLUMN);
                return;
            }
            
            disableSecondaryOptions(row);
        } else if(column == GAME_COLUMN) {
            if(newValue == null) {
                table.disableOption(row, COUNTRY_COLUMN);
                return;
            }
            
            GameVersion newVersion = (GameVersion)newValue;
            boolean shouldUpdate = oldValue == null; // Update by default if previous value was null
            
            // Check if language code changed from or to Japanese
            if(oldValue != null) {
                GameVersion oldVersion = (GameVersion)oldValue;
                shouldUpdate |= (oldVersion.getLanguageCode() == 1 && newVersion.getLanguageCode() != 1) 
                             || (oldVersion.getLanguageCode() != 1 && newVersion.getLanguageCode() == 1);
            }
            
            if(shouldUpdate) {
                // Japanese language visitors seem to only be able to appear if their set country is Japan
                setOptions(row, COUNTRY_COLUMN, newVersion.getLanguageCode() == 1 ? Arrays.asList(DataManager.getCountry(105)) : DataManager.getCountries());
                table.enableOption(row, COUNTRY_COLUMN);
            }
        } else if(column == COUNTRY_COLUMN) {
            if(newValue == null) {
                table.disableOption(row, REGION_COLUMN);
                return;
            }
            
            Country country = (Country)newValue;
            setOptions(row, REGION_COLUMN, country.hasRegions() ? country.regions() : Collections.emptyList(), (a, b) -> a.name().compareTo(b.name()));
            table.enableOption(row, REGION_COLUMN);
        }
    }
    
    @Override
    public void initializeOptions(int row) {
        setOptions(row, TYPE_COLUMN, Arrays.asList(AvenueVisitorType.values()), true);
        setOptions(row, SHOP_COLUMN, Arrays.asList(AvenueShopType.values()));
        setOptions(row, GAME_COLUMN, Arrays.asList(GameVersion.values()), (a, b) -> Integer.compare(a.getLanguageCode(), b.getLanguageCode()));
        setOptions(row, PHRASE_COLUMN, IntStream.rangeClosed(0, 7).boxed().toList());
        setOptions(row, DREAMER_COLUMN, DataManager.getSpecies(), (a, b) -> a.name().compareTo(b.name()));
        InputVerifier nameVerifier = new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextField)input).getText();
                String error = checkName(text, row);
                
                if(error != null) {
                    // TODO show hint
                    Toolkit.getDefaultToolkit().beep();
                    return false;
                }
                
                return true;
            }
        };
        table.setCellEditor(row, NAME_COLUMN, new InputVerifierCellEditor(nameVerifier));
        disableSecondaryOptions(row);
    }

    @Override
    public void randomizeSelections(int row) {
        table.randomizeSelection(row, TYPE_COLUMN, false);
        table.randomizeSelection(row, SHOP_COLUMN);
        table.randomizeSelection(row, GAME_COLUMN);
        table.randomizeSelection(row, COUNTRY_COLUMN);
        table.randomizeSelection(row, REGION_COLUMN);
        table.randomizeSelection(row, PHRASE_COLUMN);
        table.randomizeSelection(row, DREAMER_COLUMN);
        model.setValueAt(findRandomName(row), row, NAME_COLUMN);
    }

    @Override
    public void clearSelections(int row) {
        model.setValueAt(null, row, TYPE_COLUMN);
    }

    @Override
    public Image getSelectionIcon(int row) {
        String name = row == -1 || getType(row) == null ? "none" : getType(row).name().toLowerCase();
        return ImageLoader.getImage("/sprites/trainers/%s.png".formatted(name));
    }
    
    public void loadProfile(Player player) {
        table.clearSelection();
        clearSelections();
        int row = 0;
        
        for(AvenueVisitor visitor : player.getAvenueVisitors()) {
            model.setValueAt(visitor.type(), row, TYPE_COLUMN);
            model.setValueAt(visitor.name(), row, NAME_COLUMN);
            model.setValueAt(visitor.shopType(), row, SHOP_COLUMN);
            model.setValueAt(visitor.gameVersion(), row, GAME_COLUMN);
            Country country = DataManager.getCountry(visitor.countryCode());
            model.setValueAt(country, row, COUNTRY_COLUMN);
            
            if(country.hasRegions()) {
                model.setValueAt(country.regions().get(visitor.stateProvinceCode() - 1), row, REGION_COLUMN);
            }
            
            model.setValueAt(visitor.personality(), row, PHRASE_COLUMN);
            model.setValueAt(DataManager.getSpecies(visitor.dreamerSpecies()), row, DREAMER_COLUMN);
            row++;
        }
    }
    
    public void saveProfile(Player player) {
        player.setAvenueVisitors(computeSelectionList(i -> new AvenueVisitor(
                getName(i), getType(i), getShop(i), getGameVersion(i), getCountry(i).id(), getRegion(i), getPhrase(i), getDreamer(i).id()),
                i -> getType(i) != null));
    }
    
    private String findRandomName(int row) {
        boolean japanese = getGameVersion(row).getLanguageCode() == 1;
        String[] table = null;
        
        if(getType(row).isFemale()) {
            table = japanese ? JAP_NAMES_F : ENG_NAMES_F;
        } else {
            table = japanese ? JAP_NAMES_M : ENG_NAMES_M;
        }
        
        List<String> existingNames = computeSelectionList(this::getName, i -> getType(row) != null);
        List<String> options = Stream.of(table).filter(x -> !existingNames.contains(x)).toList();
        return options.get((int)(Math.random() * options.size())); // Just make sure there are enough name options in the tables...
    }
    
    private String checkName(String name, int ignoreRow) {
        if(name.isBlank()) {
            return "Visitor name can't be blank.";
        }
        
        if(name.length() > 7) {
            return "Visitor name can't exceed 7 characters.";
        }
        
        for(int i = 0; i < VISITOR_COUNT; i++) {
            if(i != ignoreRow && name.equals(getName(i))) {
                return "Visitors can't have the same name.";
            }
        }
        
        return null;
    }
    
    private void disableSecondaryOptions(int row) {
        table.disableOption(row, NAME_COLUMN);
        table.disableOption(row, SHOP_COLUMN);
        table.disableOption(row, GAME_COLUMN);
        // table.disableOption(row, COUNTRY_COLUMN);
        // table.disableOption(row, REGION_COLUMN);
        table.disableOption(row, PHRASE_COLUMN);
        table.disableOption(row, DREAMER_COLUMN);
    }
    
    private AvenueVisitorType getType(int row) {
        return table.getValueAt(row, TYPE_COLUMN, AvenueVisitorType.class, null);
    }
    
    private String getName(int row) {
        return table.getValueAt(row, NAME_COLUMN, String.class, "");
    }
    
    private AvenueShopType getShop(int row) {
        return table.getValueAt(row, SHOP_COLUMN, AvenueShopType.class, null);
    }
    
    private GameVersion getGameVersion(int row) {
        return table.getValueAt(row, GAME_COLUMN, GameVersion.class, null);
    }
    
    private Country getCountry(int row) {
        return table.getValueAt(row, COUNTRY_COLUMN, Country.class, null);
    }
    
    private int getRegion(int row) {
        Region region = table.getValueAt(row, REGION_COLUMN, Region.class, null);
        return region == null ? 0 : region.id();
    }
    
    private int getPhrase(int row) {
        return table.getValueAt(row, PHRASE_COLUMN, Integer.class, 0);
    }
    
    private PkmnSpecies getDreamer(int row) {
        return table.getValueAt(row, DREAMER_COLUMN, PkmnSpecies.class, null);
    }
}
