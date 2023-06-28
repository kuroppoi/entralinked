const ELEMENT_SPECIES_TABLE = document.getElementById("species-table");
const AVAILABLE_GENERATION_V_POKEMON = new Array(
            505, 507, 510, 511, 513, 515, 519, 523, 525, 527, 529, 531, 533, 535, 538, 539, 542, 545, 546, 548, 
            550, 553, 556, 558, 559, 561, 564, 569, 572, 575, 578, 580, 583, 587, 588, 594, 596, 600, 605, 607, 
            610, 613, 616, 618, 619, 621, 622, 624, 626, 628, 630, 631, 632);

function fillSpeciesTable() {
    let tableHTML = "";
    let index = 0;
    
    for(let species = 1; species <= 649; species++) {
        // Skip if this species is not available
        if(species > 493 && !AVAILABLE_GENERATION_V_POKEMON.includes(species)) {
            continue;
        }
        
        // Open new row if it should
        if(index % 10 == 0) {
            tableHTML += "<tr>";
        }
        
        tableHTML += "<td><image src='/sprites/pokemon/normal/" + species + ".png'/><br>#" + species + "</td>";
        
        // Close current row if limit of 10 has been reached
        if(index % 10 == 10) {
            tableHTML += "</tr>";
        }
        
        index++;
    }
    
    ELEMENT_SPECIES_TABLE.innerHTML = tableHTML;
    document.getElementById("main-container").style.display = "grid";
}
