package entralinked.gui.panels;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;

import entralinked.GameVersion;
import entralinked.gui.data.DataManager;
import entralinked.gui.data.PkmnForm;
import entralinked.gui.data.PkmnSpecies;
import entralinked.model.pkmn.PkmnGender;
import entralinked.model.player.DreamAnimation;
import entralinked.model.player.DreamEncounter;
import entralinked.model.player.Player;

@SuppressWarnings("serial")
public class EncounterEditorPanel extends TableEditorPanel {
    
    public static final int ENCOUNTER_COUNT = 10;
    public static final int SPECIES_COLUMN = 1;
    public static final int MOVE_COLUMN = 2;
    public static final int FORM_COLUMN = 3;
    public static final int GENDER_COLUMN = 4;
    public static final int ANIMATION_COLUMN = 5;
    private static final String[] COLUMN_NAMES = { "Species", "Move", "Form", "Gender", "Animation" };
    private static final Class<?>[] COLUMN_TYPES = { PkmnSpecies.class, Integer.class, PkmnForm.class, PkmnGender.class, DreamAnimation.class };
    private GameVersion gameVersion;
    private boolean optionLock; // Used to prevent double option updates
    
    public EncounterEditorPanel() {
        super(ENCOUNTER_COUNT, COLUMN_NAMES, COLUMN_TYPES);
        setTitle("Entree Forest");
        setDescription("""
                <html>
                Use the table below to configure Entree Forest encounters.<br/>
                Up to 10 different encounters can be configured at a time.<br/>
                Please be aware that Unova Pokémon are exclusive to B2W2.
                </html>
                """);
        table.setCellRenderers(SPECIES_COLUMN, PkmnSpecies.class, PkmnSpecies::name, "— — — — —");
        table.setCellRenderers(MOVE_COLUMN, Integer.class, DataManager::getMoveName, "None");
        table.setCellRenderers(FORM_COLUMN, PkmnForm.class, PkmnForm::name);
        table.setCellRenderers(GENDER_COLUMN, PkmnGender.class, PkmnGender::getDisplayName);
        table.setCellRenderers(ANIMATION_COLUMN, DreamAnimation.class, DreamAnimation::getDisplayName);
        initializeOptions();
    }

    @Override
    public void dataChanged(int row, int column, Object oldValue, Object newValue) {
        if(optionLock) {
            return;
        }
        
        if(column == SPECIES_COLUMN) {
            if(newValue == null) {
                disableSecondaryOptions(row);
                return;
            }
            
            optionLock = true;
            table.enableOption(row, GENDER_COLUMN);
            table.enableOption(row, MOVE_COLUMN);
            table.enableOption(row, FORM_COLUMN);
            table.enableOption(row, ANIMATION_COLUMN);
            updateGenderOptions(row);
            updateFormOptions(row);
            updateMoveOptions(row);
            optionLock = false;
        } else if(column == GENDER_COLUMN) {
            if(!isLegalMode()) {
                return; // Gender only affects form options if legal mode is enabled
            }
            
            if(newValue == null) {
                table.disableOption(row, FORM_COLUMN);
                return;
            }
            
            updateFormOptions(row);
            
            if(oldValue == null) {
                table.enableOption(row, FORM_COLUMN);
            }
        } else if(column == FORM_COLUMN) {
            if(!isLegalMode()) {
                return; // Form only affects move options if legal mode is enabled
            }
            
            if(newValue == null) {
                table.disableOption(row, MOVE_COLUMN);
                return;
            }
            
            updateMoveOptions(row);
            
            if(oldValue == null) {
                table.enableOption(row, MOVE_COLUMN);
            }
        }
    }
    
    @Override
    public void legalModeChanged() {
        for(int i = 0; i < ENCOUNTER_COUNT; i++) {
            updateSpeciesOptions(i);
            PkmnSpecies species = getSpecies(i);
            
            if(species != null) {
                optionLock = true;
                updateGenderOptions(i);
                updateFormOptions(i);
                updateMoveOptions(i);
                optionLock = false;
            }
        }
    }
    
    @Override
    public void initializeOptions(int row) {
        setOptions(row, ANIMATION_COLUMN, Arrays.asList(DreamAnimation.values()));
        disableSecondaryOptions(row);
    }

    @Override
    public void randomizeSelections(int row) {
        table.randomizeSelection(row, SPECIES_COLUMN, false);
        table.randomizeSelection(row, GENDER_COLUMN);
        table.randomizeSelection(row, FORM_COLUMN);
        table.randomizeSelection(row, MOVE_COLUMN);
        table.randomizeSelection(row, ANIMATION_COLUMN);
    }

    @Override
    public void clearSelections(int row) {
        model.setValueAt(null, row, SPECIES_COLUMN);
    }

    @Override
    public Image getSelectionIcon(int row) {
        return row == -1 ? DataManager.getPokemonSprite(0) : DataManager.getPokemonSprite(getSpecies(row), getForm(row), getGender(row), false);
    }
    
    @Override
    public boolean shouldUpdateSelectionIcon(int column) {
        return column == SPECIES_COLUMN || column == GENDER_COLUMN || column == FORM_COLUMN;
    }
    
    private void updateSpeciesOptions(int row) {
        setOptions(row, SPECIES_COLUMN,
                isLegalMode() ? DataManager.getSpeciesOptions(gameVersion) : DataManager.getDownloadableSpecies(gameVersion),
                (a, b) -> a.name().compareTo(b.name()), true);
    }
    
    private void updateGenderOptions(int row) {
        PkmnSpecies species = getSpecies(row);
        setOptions(row, GENDER_COLUMN, isLegalMode() ? DataManager.getGenderOptions(gameVersion, species) : species.getGenders());
    }
    
    private void updateFormOptions(int row) {
        PkmnSpecies species = getSpecies(row);
        setOptions(row, FORM_COLUMN, species.hasForms() ? (isLegalMode() ? DataManager.getFormOptions(gameVersion, species, getGender(row)) 
                : Arrays.asList(species.forms())) : Collections.emptyList());
    }
    
    private void updateMoveOptions(int row) {
        setOptions(row, MOVE_COLUMN,
                isLegalMode() ? DataManager.getMoveOptions(gameVersion, getSpecies(row), getGender(row), getForm(row)) : DataManager.getMoveIds(), 
                (a, b) -> DataManager.getMoveName(a).compareTo(DataManager.getMoveName(b)), true);
    }
    
    public void loadProfile(Player player) {
        gameVersion = player.getGameVersion();
        table.clearSelection();
        clearSelections();
        legalModeToggle.setSelected(false);
        legalModeChanged();
        int row = 0;
        
        for(DreamEncounter encounter : player.getEncounters()) {
            PkmnSpecies species = DataManager.getSpecies(encounter.species());
            model.setValueAt(species, row, SPECIES_COLUMN);
            model.setValueAt(encounter.move() == 0 ? null : encounter.move(), row, MOVE_COLUMN);
            model.setValueAt(encounter.gender(), row, GENDER_COLUMN);
            model.setValueAt(encounter.animation(), row, ANIMATION_COLUMN);
            
            if(species.hasForms()) {
                model.setValueAt(species.forms()[encounter.form()], row, FORM_COLUMN);
            }
            
            row++;
        }
    }
    
    public void saveProfile(Player player) {
        player.setEncounters(computeSelectionList(row ->
                new DreamEncounter(getSpecies(row).id(), getMove(row), getForm(row), getGender(row), getAnimation(row)),
                row -> getSpecies(row) != null));
    }
    
    private void disableSecondaryOptions(int row) {
        table.disableOption(row, MOVE_COLUMN);
        table.disableOption(row, FORM_COLUMN);
        table.disableOption(row, GENDER_COLUMN);
        table.disableOption(row, ANIMATION_COLUMN);
    }
    
    private PkmnSpecies getSpecies(int row) {
        return table.getValueAt(row, SPECIES_COLUMN, PkmnSpecies.class);
    }
    
    private int getMove(int row) {
        return table.getValueAt(row, MOVE_COLUMN, Integer.class, 0);
    }
    
    private int getForm(int row) {
        PkmnForm form = table.getValueAt(row, FORM_COLUMN, PkmnForm.class);
        return form == null ? 0 : form.id();
    }
    
    private PkmnGender getGender(int row) {
        return table.getValueAt(row, GENDER_COLUMN, PkmnGender.class);
    }
    
    private DreamAnimation getAnimation(int row) {
        return table.getValueAt(row, ANIMATION_COLUMN, DreamAnimation.class);
    }
}
