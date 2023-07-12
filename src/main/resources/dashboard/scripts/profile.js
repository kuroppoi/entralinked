// HTML document elements
const ELEMENT_GAME_SUMMARY = document.getElementById("game-summary");

const ELEMENT_DREAMER_SPRITE = document.getElementById("dreamer-sprite");
const ELEMENT_DREAMER_SPECIES = document.getElementById("dreamer-species");
const ELEMENT_DREAMER_ABILITY = document.getElementById("dreamer-ability");
const ELEMENT_DREAMER_NATURE = document.getElementById("dreamer-nature");
const ELEMENT_DREAMER_NAME = document.getElementById("dreamer-name");
const ELEMENT_DREAMER_GENDER = document.getElementById("dreamer-gender");
const ELEMENT_DREAMER_TRAINER = document.getElementById("dreamer-trainer");
const ELEMENT_DREAMER_TRAINER_ID = document.getElementById("dreamer-trainer-id");
const ELEMENT_DREAMER_LEVEL = document.getElementById("dreamer-level");

const ELEMENT_ENCOUNTER_CONFIG = document.getElementById("encounter-config");
const ELEMENT_ENCOUNTER_SPECIES = document.getElementById("encounter-form-species");
const ELEMENT_ENCOUNTER_MOVE = document.getElementById("encounter-form-move");
const ELEMENT_ENCOUNTER_FORM = document.getElementById("encounter-form-form");
const ELEMENT_ENCOUNTER_GENDER = document.getElementById("encounter-form-gender");
const ELEMENT_ENCOUNTER_ANIMATION = document.getElementById("encounter-form-animation");

const ELEMENT_ITEM_CONFIG = document.getElementById("item-config");
const ELEMENT_ITEM_ID = document.getElementById("item-form-id");
const ELEMENT_ITEM_QUANTITY = document.getElementById("item-form-quantity");

const ELEMENT_VISITOR_CONFIG = document.getElementById("visitor-config");
const ELEMENT_VISITOR_NAME = document.getElementById("visitor-form-name");
const ELEMENT_VISITOR_TYPE = document.getElementById("visitor-form-type");
const ELEMENT_VISITOR_SHOP_TYPE = document.getElementById("visitor-form-shop-type");
const ELEMENT_VISITOR_GAME = document.getElementById("visitor-form-game");
const ELEMENT_VISITOR_REGION = document.getElementById("visitor-form-region");
const ELEMENT_VISITOR_SUBREGION = document.getElementById("visitor-form-subregion");
const ELEMENT_VISITOR_PERSONALITY = document.getElementById("visitor-form-personality");
const ELEMENT_VISITOR_DREAMER = document.getElementById("visitor-form-dreamer");

const ELEMENT_CGEAR_SKIN_INPUT = document.getElementById("cgear-skin");
const ELEMENT_DEX_SKIN_INPUT = document.getElementById("dex-skin");
const ELEMENT_MUSICAL_INPUT = document.getElementById("musical");
const ELEMENT_LEVEL_GAIN_INPUT = document.getElementById("level-gain-input");

// Local variables
var encounterTableIndex = -1;
var itemTableIndex = -1;
var visitorTableIndex = -1;
var profile = {
    encounters: [],
    items: [],
    visitors: []
};

(async function() {
    // Create event listeners
    clampOnChange(ELEMENT_ITEM_QUANTITY);
    clampOnChange(ELEMENT_LEVEL_GAIN_INPUT);
    clampOnChange(ELEMENT_VISITOR_PERSONALITY);
    
    // Fetch profile data
    await fetchData("GET", "/dashboard/profile").then((response) => {
        // Update game summary
        profile.gameVersion = response.gameVersion;
        ELEMENT_GAME_SUMMARY.innerHTML = "Game Card in use: " + profile.gameVersion;
        
        // Update dreamer summary
        if(response.dreamerInfo) {
            let dreamerInfo = response.dreamerInfo;
            ELEMENT_DREAMER_SPRITE.innerHTML = "<image src='" + response.dreamerSprite + "'/>";
            ELEMENT_DREAMER_SPECIES.innerHTML = SPECIES_MAP[dreamerInfo.species] ? (SPECIES_MAP[dreamerInfo.species].name) : "N/A";
            ELEMENT_DREAMER_ABILITY.innerHTML = ABILITY_MAP[dreamerInfo.ability] ? ABILITY_MAP[dreamerInfo.ability].name : "N/A";
            ELEMENT_DREAMER_NATURE.innerHTML = stringToWord(dreamerInfo.nature);
            ELEMENT_DREAMER_NAME.innerHTML = dreamerInfo.nickname;
            ELEMENT_DREAMER_GENDER.innerHTML = stringToWord(dreamerInfo.gender);
            ELEMENT_DREAMER_TRAINER.innerHTML = dreamerInfo.trainerName;
            ELEMENT_DREAMER_TRAINER_ID.innerHTML = ("0000" + dreamerInfo.trainerId).slice(-5);
            ELEMENT_DREAMER_LEVEL.innerHTML = dreamerInfo.level;
        }
        
        // Update encounter table
        if(response.encounters){
            profile.encounters = response.encounters;
            updateEncounterTable(0, 10);
        } 
        
        // Update item table
        if(response.items){
            profile.items = response.items;
            updateItemTable(0, 20);
        }   
        
        // Update Join Avenue visitor table
        if(response.avenueVisitors) {
            profile.visitors = response.avenueVisitors;
            updateVisitorTable(0, 12);
        }
        
        // Update selected DLC
        profile.cgearSkin = response.cgearSkin ? response.cgearSkin : "none";
        profile.dexSkin = response.dexSkin ? response.dexSkin : "none";
        profile.musical = response.musical ? response.musical : "none";
        fetchDlcData();
        ELEMENT_LEVEL_GAIN_INPUT.value = response.levelsGained;
        
        // Show Join Avenue visitor table if Black 2 or White 2
        if(isVersion2()) {
            document.getElementById("visitor-table-container").style.display = "block";
        }
        
        // Show div
        document.getElementById("main-container").style.display = "flex";
    });
    
    // Sort data lists alphabetically
    let sortedSpecies = [...SPECIES_LIST].sort((a, b) => a.name.localeCompare(b.name));
    let sortedMoves = [...MOVE_LIST].sort((a, b) => a.name.localeCompare(b.name));
    let sortedItems = [...ITEM_LIST].sort((a, b) => a.name.localeCompare(b.name));
    
    // Add species data
    for(let i in sortedSpecies) {
        let species = sortedSpecies[i];
        ELEMENT_VISITOR_DREAMER.options[ELEMENT_VISITOR_DREAMER.options.length] = new Option(species.name, species.id);
        
        if(species.downloadable && (isVersion2() || species.id <= 493)) {
            ELEMENT_ENCOUNTER_SPECIES.options[ELEMENT_ENCOUNTER_SPECIES.options.length] = new Option(species.name, species.id);
        }
    }
    
    // Add move data
    for(let i in sortedMoves) {
        let move = sortedMoves[i];
        ELEMENT_ENCOUNTER_MOVE.options[ELEMENT_ENCOUNTER_MOVE.options.length] = new Option(move.name, move.id);
    }
    
    // Add item data
    for(let i in sortedItems) {
        let item = sortedItems[i];
        
        if(isVersion2() || item.id <= 626) {
            ELEMENT_ITEM_ID.options[ELEMENT_ITEM_ID.options.length] = new Option(item.name, item.id);
        }
    }
    
    // Add region data (already sorted alphabetically)
    for(let i in REGION_LIST) {
        let region = REGION_LIST[i];
        ELEMENT_VISITOR_REGION.options[ELEMENT_VISITOR_REGION.options.length] = new Option(region.name, region.id);
    }
    
    // Event listener for changing the form selector contents when species changes
    ELEMENT_ENCOUNTER_SPECIES.addEventListener("change", function() {
        updateEncounterFormOptions();
        ELEMENT_ENCOUNTER_FORM.value = 0;
    });
    
    // Same thing, but for Join Avenue visitor region & subregion
    ELEMENT_VISITOR_REGION.addEventListener("change", function() {
        ELEMENT_VISITOR_SUBREGION.value = updateVisitorSubregionOptions();
    });
})();

/**
 * Encounter configuration stuff
 */

function updateEncounterFormOptions() {
    clearSelectOptions(ELEMENT_ENCOUNTER_FORM);
    let species = SPECIES_MAP[ELEMENT_ENCOUNTER_SPECIES.value];
    
    // Update special form options
    if(species.forms) {
        for(let i in species.forms) {
            ELEMENT_ENCOUNTER_FORM.options[ELEMENT_ENCOUNTER_FORM.options.length] = new Option(species.forms[i], i);
        }
    } else {
        ELEMENT_ENCOUNTER_FORM.options[ELEMENT_ENCOUNTER_FORM.options.length] = new Option("N/A", 0);
    }
}

function configureEncounter(index) {
    encounterTableIndex = Math.min(10, Math.min(index, profile.encounters.length));
    let encounter = profile.encounters[encounterTableIndex];
    ELEMENT_ENCOUNTER_SPECIES.value = encounter ? encounter.species : 493;
    updateEncounterFormOptions();
    ELEMENT_ENCOUNTER_MOVE.value = encounter ? encounter.move : 0;
    ELEMENT_ENCOUNTER_FORM.value = encounter ? encounter.form : 0;
    ELEMENT_ENCOUNTER_GENDER.value = encounter ? encounter.gender : "GENDERLESS";
    ELEMENT_ENCOUNTER_ANIMATION.value = encounter ? encounter.animation : "LOOK_AROUND";
    ELEMENT_ENCOUNTER_CONFIG.style.display = "block";
}

function saveEncounter() {
    if(encounterTableIndex < 0) {
        closeEncounterForm();
        return;
    }
    
    profile.encounters[encounterTableIndex] = {
        species: ELEMENT_ENCOUNTER_SPECIES.value,
        move: ELEMENT_ENCOUNTER_MOVE.value,
        form: ELEMENT_ENCOUNTER_FORM.value,
        gender: ELEMENT_ENCOUNTER_GENDER.value,
        animation: ELEMENT_ENCOUNTER_ANIMATION.value
    };
    updateEncounterCell(encounterTableIndex);
    closeEncounterForm();
}

function removeEncounter() {
    if(encounterTableIndex < 0) {
        closeEncounterForm();
        return;
    }
    
    let oldLength = profile.encounters.length;
    profile.encounters.splice(encounterTableIndex, 1);
    updateEncounterTable(encounterTableIndex, oldLength);
    closeEncounterForm();
}

function updateEncounterTable(startIndex, endIndex) {
    for(let i = startIndex; i < endIndex; i++) {
        updateEncounterCell(i);
    }
}

function updateEncounterCell(index) {
    let cell = document.getElementById("encounter" + index);
    let encounterData = profile.encounters[index];
    let spriteBase = "/sprites/pokemon/normal/";
    let spriteImage = spriteBase + "0.png";
    
    if(encounterData) {
        spriteImage = spriteBase + encounterData.species + ".png";
        
        // Use unique form sprite if it exists
        if(encounterData.form > 0) {
            let formSpriteImage = spriteBase + encounterData.species + "-" + encounterData.form + ".png";
            
            if(checkURL(formSpriteImage)){
                spriteImage = formSpriteImage;
            }
        }
    }
    
    cell.innerHTML = "<img src='" + spriteImage + "'/>";
}

function closeEncounterForm() {
    encounterTableIndex = -1;
    ELEMENT_ENCOUNTER_CONFIG.style.display = "none";
}

/**
 * Join Avenue visitor configuration stuff
 */
 
 function updateVisitorSubregionOptions() {
    clearSelectOptions(ELEMENT_VISITOR_SUBREGION);
    let region = REGION_MAP[ELEMENT_VISITOR_REGION.value];
    
    // Update subregion options
    if(region.subregions) {
        let sortedSubregions = [...region.subregions].sort((a, b) => a.name.localeCompare(b.name));
        
        for(let i in sortedSubregions) {
            let subregion = sortedSubregions[i];
            ELEMENT_VISITOR_SUBREGION.options[ELEMENT_VISITOR_SUBREGION.options.length] = new Option(subregion.name, subregion.id);
        }
        
        return 1;
    } else {
        ELEMENT_VISITOR_SUBREGION.options[ELEMENT_VISITOR_SUBREGION.options.length] = new Option("N/A", 0);
    }
    
    return 0;
}

function configureVisitor(index) {
    visitorTableIndex = Math.min(12, Math.min(index, profile.visitors.length));
    let visitor = profile.visitors[visitorTableIndex];
    ELEMENT_VISITOR_NAME.value = visitor ? visitor.name : "";
    ELEMENT_VISITOR_TYPE.value = visitor ? visitor.type : "ACE_TRAINER_MALE";
    ELEMENT_VISITOR_SHOP_TYPE.value = visitor ? visitor.shopType : "RAFFLE";
    ELEMENT_VISITOR_GAME.value = visitor ? visitor.gameVersion : "BLACK_ENGLISH";
    ELEMENT_VISITOR_REGION.value = visitor ? visitor.countryCode : 1;
    updateVisitorSubregionOptions();
    ELEMENT_VISITOR_SUBREGION.value = visitor ? visitor.stateProvinceCode : 0;
    ELEMENT_VISITOR_PERSONALITY.value = visitor ? visitor.personality : 0;
    ELEMENT_VISITOR_DREAMER.value = visitor ? visitor.dreamerSpecies : 1;
    ELEMENT_VISITOR_CONFIG.style.display = "block";
}

function saveVisitor() {
    if(visitorTableIndex < 0) {
        closeVisitorForm();
        return;
    }
    
    // Check if name is empty
    if(ELEMENT_VISITOR_NAME.value == "") {
        alert("Please enter a name for this visitor.");
        return;
    }
    
    // Check if name is duplicate
    for(i in profile.visitors) {
        if(i != visitorTableIndex) {
            let visitor = profile.visitors[i];
            
            if(visitor.name == ELEMENT_VISITOR_NAME.value) {
                alert("A visitor with this name already exists!")
                return;
            }
        }
    }
    
    profile.visitors[visitorTableIndex] = {
        name: ELEMENT_VISITOR_NAME.value,
        type: ELEMENT_VISITOR_TYPE.value,
        shopType: ELEMENT_VISITOR_SHOP_TYPE.value,
        gameVersion: ELEMENT_VISITOR_GAME.value,
        countryCode: ELEMENT_VISITOR_REGION.value,
        stateProvinceCode: ELEMENT_VISITOR_SUBREGION.value,
        personality: ELEMENT_VISITOR_PERSONALITY.value,
        dreamerSpecies: ELEMENT_VISITOR_DREAMER.value
    };
    updateVisitorCell(visitorTableIndex);
    closeVisitorForm();
}

function removeVisitor() {
    if(visitorTableIndex < 0) {
        closeVisitorForm();
        return;
    }
    
    let oldLength = profile.visitors.length;
    profile.visitors.splice(visitorTableIndex, 1);
    updateVisitorTable(visitorTableIndex, oldLength);
    closeVisitorForm();
}

function updateVisitorTable(startIndex, endIndex) {
    for(let i = startIndex; i < endIndex; i++) {
        updateVisitorCell(i);
    }
}

function updateVisitorCell(index) {
    let cell = document.getElementById("visitor" + index);
    let visitor = profile.visitors[index];
    let spriteBase = "/sprites/trainers/";
    let spriteImage = spriteBase + "none.png";
    
    if(visitor) {
        let newSpriteImage = spriteBase + visitor.type.toLowerCase() + ".png";
        
        if(checkURL(newSpriteImage)){
            spriteImage = newSpriteImage;
        }
    }
    
    cell.innerHTML = "<img src='" + spriteImage + "'/>";
}

function closeVisitorForm() {
    visitorTableIndex = -1;
    ELEMENT_VISITOR_CONFIG.style.display = "none";
}

/**
 * Item configuration stuff
 */

function configureItem(index) {
    itemTableIndex = Math.min(20, Math.min(index, profile.items.length));
    let item = profile.items[itemTableIndex];
    ELEMENT_ITEM_ID.value = item ? item.id : 1;
    ELEMENT_ITEM_QUANTITY.value = item ? item.quantity : 1;
    ELEMENT_ITEM_CONFIG.style.display = "block";
}

function saveItem() {
    if(itemTableIndex < 0) {
        closeItemForm();
        return;
    }
    
    profile.items[itemTableIndex] = {
        id: ELEMENT_ITEM_ID.value,
        quantity: ELEMENT_ITEM_QUANTITY.value
    };
    updateItemCell(itemTableIndex);
    closeItemForm();
}

function removeItem() {
    if(itemTableIndex < 0) {
        closeItemForm();
        return;
    }
    
    let oldLength = profile.items.length;
    profile.items.splice(itemTableIndex, 1);
    updateItemTable(itemTableIndex, oldLength);
    closeItemForm();
}

function updateItemTable(startIndex, endIndex) {
    for(let i = startIndex; i < endIndex; i++) {
        updateItemCell(i);
    }
}

function updateItemCell(index) {
    let cell = document.getElementById("item" + index);
    let item = profile.items[index];
    let spriteBase = "/sprites/items/";
    let spriteImage = spriteBase + "0.png";
    let quantityStr = "";
    
    if(item) {
        let newSpriteImage = spriteBase + item.id + ".png";
        quantityStr = "x" + item.quantity;
        
        if(checkURL(newSpriteImage)){
            spriteImage = newSpriteImage;
        }
    }
    
    cell.innerHTML = "<img src='" + spriteImage + "'/><br>" + quantityStr;
}

function closeItemForm() {
    itemTableIndex = -1;
    ELEMENT_ITEM_CONFIG.style.display = "none";
}

/**
 * Miscellaneous stuff
 */

function previewSkin(inputElementId, type) {
    let value = document.getElementById(inputElementId).value;
    
    if(value == "none") {
        window.alert("Please select a skin to preview it.");
        return false;
    }
    
    if(type == "CGEAR" && isVersion2()) {
        type = "CGEAR2";
    }
    
    window.open("/dashboard/previewskin?type=" + type + "&name=" + value);
    return false;
}

function fetchDlcData() {
    // Fetch C-Gear skins
    fetchData("GET", "/dashboard/dlc?type=" + (isVersion2() ? "CGEAR2" : "CGEAR")).then((response) => {
        addDlcNames(ELEMENT_CGEAR_SKIN_INPUT, response);
        ELEMENT_CGEAR_SKIN_INPUT.value = response.includes(profile.cgearSkin) ? profile.cgearSkin : "none";
    });
    
    // Fetch Dex skins
    fetchData("GET", "/dashboard/dlc?type=ZUKAN").then((response) => {
        addDlcNames(ELEMENT_DEX_SKIN_INPUT, response);
        ELEMENT_DEX_SKIN_INPUT.value = response.includes(profile.dexSkin) ? profile.dexSkin : "none";
    });
    
    // Fetch musicals
    fetchData("GET", "/dashboard/dlc?type=MUSICAL").then((response) => {
        addDlcNames(ELEMENT_MUSICAL_INPUT, response);
        ELEMENT_MUSICAL_INPUT.value = response.includes(profile.musical) ? profile.musical : "none";
    });
}

function addDlcNames(selectElement, names) {
    for(let i in names) {
        let name = names[i];
        selectElement.options[selectElement.options.length] = new Option(name.replace(/\.[^/.]+$/, ""), name);
    }
}

function postProfileData() {
    fetchData("POST", "/dashboard/profile", JSON.stringify({
        encounters: profile.encounters,
        items: profile.items,
        avenueVisitors: profile.visitors,
        cgearSkin: ELEMENT_CGEAR_SKIN_INPUT.value,
        dexSkin: ELEMENT_DEX_SKIN_INPUT.value,
        musical: ELEMENT_MUSICAL_INPUT.value,
        gainedLevels: ELEMENT_LEVEL_GAIN_INPUT.value
    })).then((response) => {
        alert(response.message);
    });
}

function postLogout() {
    fetchData("POST", "/dashboard/logout").then((response) => {
        // Assume it succeeded
        window.location.href = "/dashboard/login.html";
    });
}

function isVersion2() {
    return profile.gameVersion.includes("2");
}
