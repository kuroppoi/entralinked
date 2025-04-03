package entralinked.gui.data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.GameVersion;
import entralinked.gui.ImageLoader;
import entralinked.model.pkmn.PkmnGender;

/**
 * TODO name is too generic & model management is a bit confusing.
 */
public class DataManager {
    
    private static final Map<Integer, String> abilities = new HashMap<>();
    private static final Map<Integer, String> items = new HashMap<>();
    private static final Map<Integer, String> moves = new HashMap<>();
    private static final Map<Integer, PkmnSpecies> species = new HashMap<>();
    private static final Map<Integer, Country> countries = new HashMap<>();
    private static final Map<String, DreamWorldArea> dreamWorldAreas = new HashMap<>();
    private static final Map<GameVersion, List<Encounter>> encounterCache = new HashMap<>();
    
    public static void clearData() {
        abilities.clear();
        items.clear();
        moves.clear();
        species.clear();
        countries.clear();
        dreamWorldAreas.clear();
        encounterCache.clear();
    }
    
    public static void loadData() throws IOException {
        clearData();
        ObjectMapper mapper = new ObjectMapper();
        abilities.putAll(mapper.readValue(DataManager.class.getResource("/data/abilities.json"), new TypeReference<Map<Integer, String>>(){}));
        items.putAll(mapper.readValue(DataManager.class.getResource("/data/items.json"), new TypeReference<Map<Integer, String>>(){}));
        moves.putAll(mapper.readValue(DataManager.class.getResource("/data/moves.json"), new TypeReference<Map<Integer, String>>(){}));
        species.putAll(mapper.readValue(DataManager.class.getResource("/data/species.json"), new TypeReference<Map<Integer, PkmnSpecies>>(){}));
        countries.putAll(mapper.readValue(DataManager.class.getResource("/data/countries.json"), new TypeReference<Map<Integer, Country>>(){}));
        dreamWorldAreas.putAll(mapper.readValue(DataManager.class.getResource("/data/legality.json"), new TypeReference<Map<String, DreamWorldArea>>(){}));
    }
    
    public static BufferedImage getPokemonSprite(int species) {
        return getPokemonSprite(0, 0, false, false);
    }
    
    public static BufferedImage getPokemonSprite(PkmnSpecies species, int form, PkmnGender gender, boolean shiny) {
        return getPokemonSprite(species, form, gender == PkmnGender.FEMALE, shiny);
    }
    
    public static BufferedImage getPokemonSprite(int species, int form, PkmnGender gender, boolean shiny) {
        return getPokemonSprite(species, form, gender == PkmnGender.FEMALE, shiny);
    }
    
    public static BufferedImage getPokemonSprite(int species, int form, boolean female, boolean shiny) {
        return getPokemonSprite(getSpecies(species), form, female, shiny);
    }
    
    public static BufferedImage getPokemonSprite(PkmnSpecies species, int form, boolean female, boolean shiny) {
        String path = "/sprites/pokemon/normal/0.png";
        
        if(species != null) {
            path = "/sprites/pokemon/%s/%s%s%s.png".formatted(
                    shiny ? "shiny" : "normal",
                    species.hasFemaleSprite() && female ? "female/" : "",
                    species.id(), form == 0 ? "" : "-" + form);
        }
        
        return ImageLoader.getImage(path);
    }
    
    public static BufferedImage getItemSprite(int item) {
        return ImageLoader.getImage("/sprites/items/%s.png".formatted(item));
    }
    
    public static String getAbilityName(int id) {
        return abilities.getOrDefault(id, "Unknown (#%s)".formatted(id));
    }
    
    public static Set<Integer> getAbilityIds() {
        return Collections.unmodifiableSet(abilities.keySet());
    }
    
    public static String getItemName(int id) {
        return items.getOrDefault(id, "Unknown (#%s)".formatted(id));
    }
    
    public static Set<Integer> getItemIds() {
        return Collections.unmodifiableSet(items.keySet());
    }
    
    public static String getMoveName(int id) {
        return moves.getOrDefault(id, "Unknown (#%s)".formatted(id));
    }
    
    public static Set<Integer> getMoveIds() {
        return Collections.unmodifiableSet(moves.keySet());
    }
    
    public static PkmnSpecies getSpecies(int id) {
        return species.get(id);
    }
    
    public static Set<Integer> getSpeciesIds() {
        return Collections.unmodifiableSet(species.keySet());
    }
    
    public static Collection<PkmnSpecies> getSpecies() {
        return Collections.unmodifiableCollection(species.values());
    }
    
    public static Country getCountry(int id) {
        return countries.get(id);
    }
    
    public static Collection<Country> getCountries() {
        return Collections.unmodifiableCollection(countries.values());
    }
    
    // TODO functions might be computationally expensive
    
    private static List<Encounter> getEncounters(GameVersion gameVersion) {
        return encounterCache.computeIfAbsent(gameVersion, version -> dreamWorldAreas.values().stream()
                .map(DreamWorldArea::encounters)
                .flatMap(List::stream)
                .filter(x -> x.versionMask() == 0 || version.checkMask(x.versionMask()))
                .toList());
    }
    
    public static List<PkmnSpecies> getDownloadableSpecies(GameVersion gameVersion) {
        return species.values().stream()
                .filter(x -> x.downloadable() && (gameVersion.isVersion2() || x.id() <= 493))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<PkmnSpecies> getSpeciesOptions(GameVersion gameVersion) {
        return getEncounters(gameVersion).stream()
                .map(x -> species.get(x.species()))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<PkmnGender> getGenderOptions(GameVersion gameVersion, PkmnSpecies species) {
        return getEncounters(gameVersion).stream()
                .filter(x -> x.species() == species.id())
                .map(x -> x.isGenderLocked() ? List.of(x.gender()) : species.getGenders())
                .flatMap(List::stream)
                .distinct()
                .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<Integer> getMoveOptions(GameVersion gameVersion, PkmnSpecies species, PkmnGender gender) {
        return getEncounters(gameVersion).stream()
                .filter(x -> x.species() == species.id() && (!x.isGenderLocked() || species.gender() == gender))
                .map(Encounter::moves)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<Integer> getDownloadableItems(GameVersion gameVersion) {
        return items.keySet().stream().filter(x -> gameVersion.isVersion2() || x <= 626).toList();
    }
    
    public static List<Integer> getItemOptions() {
        return dreamWorldAreas.values().stream()
                .map(DreamWorldArea::items)
                .flatMap(List::stream)
                .distinct()
                .toList();
    }
}
