const ELEMENT_GAME_SUMMARY = document.getElementById("game-summary");

// Dreamer elements
const ELEMENT_DREAMER_SPRITE = document.getElementById("dreamer-sprite");
const ELEMENT_DREAMER_SPECIES = document.getElementById("dreamer-species");
const ELEMENT_DREAMER_NATURE = document.getElementById("dreamer-nature");
const ELEMENT_DREAMER_NAME = document.getElementById("dreamer-name");
const ELEMENT_DREAMER_GENDER = document.getElementById("dreamer-gender");
const ELEMENT_DREAMER_TRAINER = document.getElementById("dreamer-trainer");
const ELEMENT_DREAMER_TRAINER_ID = document.getElementById("dreamer-trainer-id");
const ELEMENT_DREAMER_LEVEL = document.getElementById("dreamer-level");

// Encounter form elements
const ELEMENT_ENCOUNTER_SPECIES = document.getElementById("encounter-form-species");
const ELEMENT_ENCOUNTER_MOVE = document.getElementById("encounter-form-move");
const ELEMENT_ENCOUNTER_FORM = document.getElementById("encounter-form-form");
const ELEMENT_ENCOUNTER_GENDER = document.getElementById("encounter-form-gender");
const ELEMENT_ENCOUNTER_ANIMATION = document.getElementById("encounter-form-animation");

// Item form elements
const ELEMENT_ITEM_ID = document.getElementById("item-form-id");
const ELEMENT_ITEM_QUANTITY = document.getElementById("item-form-quantity");

// Misc input elements
const ELEMENT_CGEAR_SKIN_INPUT = document.getElementById("cgear-skin");
const ELEMENT_DEX_SKIN_INPUT = document.getElementById("dex-skin");
const ELEMENT_MUSICAL_INPUT = document.getElementById("musical");
const ELEMENT_LEVEL_GAIN_INPUT = document.getElementById("level-gain-input");

// Create event listeners
ELEMENT_ENCOUNTER_SPECIES.addEventListener("change", clampValue);
ELEMENT_ENCOUNTER_MOVE.addEventListener("change", clampValue);
ELEMENT_ITEM_ID.addEventListener("change", clampValue);
ELEMENT_ITEM_QUANTITY.addEventListener("change", clampValue);
ELEMENT_LEVEL_GAIN_INPUT.addEventListener("change", clampValue);

function clampValue() {
    let value = parseInt(this.value);
    
    if(value < this.min) {
        this.value = this.min;
    } else if(value > this.max) {
        console.log(value);
        this.value = this.max;
    }
}

// Other constant stuff
const AVAILABLE_GENERATION_V_POKEMON = new Array(
            505, 507, 510, 511, 513, 515, 519, 523, 525, 527, 529, 531, 533, 535, 538, 539, 542, 545, 546, 548, 
            550, 553, 556, 558, 559, 561, 564, 569, 572, 575, 578, 580, 583, 587, 588, 594, 596, 600, 605, 607, 
            610, 613, 616, 618, 619, 621, 622, 624, 626, 628, 630, 631, 632); // Defining this 3 times is a brilliant idea.


// Local variables
var encounterTableIndex = -1;
var itemTableIndex = -1;
var profile = {
    encounters: [],
    items: []
};

function configureEncounter(index) {
    encounterTableIndex = Math.min(10, Math.min(index, profile.encounters.length));
    
    // Load existing settings
    let encounter = profile.encounters[encounterTableIndex];
    ELEMENT_ENCOUNTER_SPECIES.value = encounter ? encounter.species : 1;
    ELEMENT_ENCOUNTER_MOVE.value = encounter ? encounter.move : 1;
    ELEMENT_ENCOUNTER_FORM.value = encounter ? encounter.form : 0;
    ELEMENT_ENCOUNTER_GENDER.value = encounter ? encounter.gender : "GENDERLESS";
    ELEMENT_ENCOUNTER_ANIMATION.value = encounter ? encounter.animation : "WALK_AROUND";
}

function saveEncounter() {
    if(encounterTableIndex < 0) {
        return;
    }
    
    // Check if this species can be downloaded
    let species = parseInt(ELEMENT_ENCOUNTER_SPECIES.value);
    
    if(species > 493 && !AVAILABLE_GENERATION_V_POKEMON.includes(species)) {
        alert("This Pokémon species cannot be downloaded. Click 'View list' in the encounter form to view a list of available Pokémon.");
        return;
    }
        
    // Create encounter data
    let encounterData = {
        species: species,
        move: ELEMENT_ENCOUNTER_MOVE.value,
        form: ELEMENT_ENCOUNTER_FORM.value,
        gender: ELEMENT_ENCOUNTER_GENDER.value,
        animation: ELEMENT_ENCOUNTER_ANIMATION.value
    }
    
    // Set form to highest form available if it too great
    let maxForm = 0;
    
    switch(species) {
        case 201: maxForm = 27; break; // Unown
        case 386: maxForm = 3; break; // Deoxys
        case 412:
        case 413: maxForm = 2; break; // Burmy & Wormadam
        case 422:
        case 423: 
        case 487: maxForm = 1; break; // Shellos, Gastrodon & Giratina
        case 479: maxForm = 5; break; // Rotom
        case 493: maxForm = 16; break; // Arceus
        case 550: maxForm = 1; break; // Basculin
    }
    
    if(encounterData.form > maxForm) {
        encounterData.form = maxForm;
    }
    
    profile.encounters[encounterTableIndex] = encounterData;
    updateEncounterCell(encounterTableIndex);
    closeEncounterForm();
}

function removeEncounter() {
    if(encounterTableIndex < 0) {
        return;
    }
    
    let oldLength = profile.encounters.length;
    profile.encounters.splice(encounterTableIndex, 1);
    
    for(let i = encounterTableIndex; i < oldLength; i++) {
        updateEncounterCell(i);
    }
    
    closeEncounterForm();
}

function updateEncounterCell(index) {
    let cell = document.getElementById("encounter" + index);
    let encounterData = profile.encounters[index];
    let spriteBase = "/sprites/pokemon/normal/";
    let spriteImage = spriteBase + "0.png";
    
    if(encounterData) {
        spriteImage = spriteBase + encounterData.species + ".png";
        
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
    window.location.href = "#";
}

function configureItem(index) {
    itemTableIndex = Math.min(20, Math.min(index, profile.items.length));
    
    // Loadg existing settings
    let item = profile.items[itemTableIndex];
    ELEMENT_ITEM_ID.value = item ? item.id : 1;
    ELEMENT_ITEM_QUANTITY.value = item ? item.quantity : 1;
}

function saveItem() {
    if(itemTableIndex < 0) {
        return;
    }
    
    let itemData = {
        id: ELEMENT_ITEM_ID.value,
        quantity: ELEMENT_ITEM_QUANTITY.value
    }
    
    profile.items[itemTableIndex] = itemData;
    updateItemCell(itemTableIndex);
    closeItemForm();
}

function removeItem() {
    if(itemTableIndex < 0) {
        return;
    }
    
    let oldLength = profile.items.length;
    profile.items.splice(itemTableIndex, 1);
    
    for(let i = itemTableIndex; i < oldLength; i++) {
        updateItemCell(i);
    }
    
    closeItemForm();
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
    window.location.href = "#";
}

async function fetchData(path) {
    return fetchData(path, "GET", null);
}

async function fetchData(path, method, body) {
    let response = await fetch(path, {
        method: method,
        body: body
    });
    
    // Return to login page if unauthorized
    if(response.status == 401) {
        window.location.href = "/dashboard/login.html";
        return;
    }
    
    try {
        return await response.json();
    } catch(error) {
        window.alert(error);
    }
    
    return null;
}

function fetchDlcData() {
    let cgearType = profile.gameVersion.includes("2") ? "CGEAR2" : "CGEAR"; // Not a good way to do this!
    
    // Fetch CGear skins
    fetchData("/dashboard/dlc?type=" + cgearType).then((response) => {
        addValuesToComboBox(ELEMENT_CGEAR_SKIN_INPUT, response);
        ELEMENT_CGEAR_SKIN_INPUT.value = profile.cgearSkin;
    });
    
    // Fetch Dex skins
    fetchData("/dashboard/dlc?type=ZUKAN").then((response) => {
        addValuesToComboBox(ELEMENT_DEX_SKIN_INPUT, response);
        ELEMENT_DEX_SKIN_INPUT.value = profile.dexSkin;
    });
    
    // Fetch musicals
    fetchData("/dashboard/dlc?type=MUSICAL").then((response) => {
        addValuesToComboBox(ELEMENT_MUSICAL_INPUT, response);
        ELEMENT_MUSICAL_INPUT.value = profile.musical;
    });
}

// TODO
function fetchProfileData() {
    fetchData("/dashboard/profile").then((response) => {
        let gameVersion = response["gameVersion"];
        let dreamerSprite = response["dreamerSprite"];
        let dreamerInfo = response["dreamerInfo"];
        let encounters = response["encounters"];
        let items = response["items"];
        let cgearSkin = response["cgearSkin"];
        let dexSkin = response["dexSkin"];
        let musical = response["musical"];
        let levelsGained = response["levelsGained"];
        
        // Update game summary
        profile.gameVersion = gameVersion;
        ELEMENT_GAME_SUMMARY.innerHTML = "Game Card in use: " + gameVersion;
        
        // Still don't like this!
        if(gameVersion.includes("2")) {
            ELEMENT_ENCOUNTER_SPECIES.max = 649;
            ELEMENT_ITEM_ID.max = 638;
        }
        
        // Update dreamer summary
        if(dreamerInfo) {
            let species = dreamerInfo["species"];
            let nature = dreamerInfo["nature"];
            let nickname = dreamerInfo["nickname"];
            let gender = dreamerInfo["gender"];
            let trainerName = dreamerInfo["trainerName"];
            let trainerId = dreamerInfo["trainerId"];
            let level = dreamerInfo["level"];
            
            // Set element values
            ELEMENT_DREAMER_SPRITE.innerHTML = "<image src='" + dreamerSprite + "'/>";
            ELEMENT_DREAMER_SPECIES.innerHTML = "#" + species;
            ELEMENT_DREAMER_NATURE.innerHTML = stringToWord(nature);
            ELEMENT_DREAMER_NAME.innerHTML = nickname;
            ELEMENT_DREAMER_GENDER.innerHTML = stringToWord(gender);
            ELEMENT_DREAMER_TRAINER.innerHTML = trainerName;
            ELEMENT_DREAMER_TRAINER_ID.innerHTML = trainerId;
            ELEMENT_DREAMER_LEVEL.innerHTML = level;
        }
        
        // Update encounter table
        if(encounters){
            profile.encounters = encounters;
            
            for(let i = 0; i < 10; i++) {
                updateEncounterCell(i);
            }
        } 
        
        // Update item table
        if(items){
            profile.items = items;
            
            for(let i = 0; i < 20; i++) {
                updateItemCell(i);
            }
        }   
        
        // Update selected DLC
        profile.cgearSkin = cgearSkin ? cgearSkin : "none";
        profile.dexSkin = dexSkin ? dexSkin : "none";
        profile.musical = musical ? musical : "none";
        fetchDlcData();
        
        // Update level gain
        ELEMENT_LEVEL_GAIN_INPUT.value = levelsGained;
        
        // Show div
        document.getElementById("main-container").style.display = "grid";
    });
}

function postProfileData() {
    // Construct body
    let profileData = {
        encounters: profile.encounters,
        items: profile.items,
        cgearSkin: ELEMENT_CGEAR_SKIN_INPUT.value,
        dexSkin: ELEMENT_DEX_SKIN_INPUT.value,
        musical: ELEMENT_MUSICAL_INPUT.value,
        gainedLevels: ELEMENT_LEVEL_GAIN_INPUT.value
    }
    
    // Send data
    fetchData("/dashboard/profile", "POST", JSON.stringify(profileData)).then((response) => {
        alert(response.message);
    });
}

function postLogout() {
    fetchData("/dashboard/logout", "POST", null).then((response) => {
        // Assume it succeeded
        window.location.href = "/dashboard/login.html";
    });
}

// TODO bad
function checkURL(url) {
    var request = new XMLHttpRequest();
    request.open('HEAD', url, false);
    request.send();
    return request.status == 200;
}

function stringToWord(string) {
    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
}

function addValuesToComboBox(selectorElement, values) {
    for(i in values) {
        let value = values[i];
        selectorElement.options[selectorElement.options.length] = new Option(value, value);
    }
}
