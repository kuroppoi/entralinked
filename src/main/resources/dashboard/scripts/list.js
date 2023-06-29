const AVAILABLE_GENERATION_V_POKEMON = new Array(
            505, 507, 510, 511, 513, 515, 519, 523, 525, 527, 529, 531, 533, 535, 538, 539, 542, 545, 546, 548, 
            550, 553, 556, 558, 559, 561, 564, 569, 572, 575, 578, 580, 583, 587, 588, 594, 596, 600, 605, 607, 
            610, 613, 616, 618, 619, 621, 622, 624, 626, 628, 630, 631, 632);

function fillTable(tableId, rowSize, elementCount, htmlAppender) {
    let tableHTML = "";
    let index = 0;
    
    for(let value = 1; value <= elementCount; value++) {
        // Call the provided HTML appender to see what should be appended to the table
        let toAppend = htmlAppender(value);
        
        // If there is nothing to append (that is, the function has decided this element should be skipped)
        // then continue to the next loop.
        if(!toAppend) {
            continue;
        }
        
        // Open a new table row if it should
        if(index % rowSize == 0) {
            tableHTML += "<tr>";
        }
        
        // Append the HTML data
        tableHTML += toAppend;
        
        // Close the table row if it has reached the maximum number of elements
        if(index % rowSize == rowSize) {
            tableHTML += "<tr>";
        }
        
        index++;
    }
    
    // Set the inner HTML
    document.getElementById(tableId).innerHTML = tableHTML;
}

function fillTableWithSpecies() {
    fillTable("species-table", 10, 649, (species) => {
        // Skip this element if species is from Generation V and cannot be downloaded
        if(species > 493 && !AVAILABLE_GENERATION_V_POKEMON.includes(species)) {
            return false;
        }
        
        return "<td style='width:96px;height:96px;'><image src='/sprites/pokemon/normal/" + species + ".png'/><br>#" + species + "</td>";
    });
}

function fillTableWithItems() {
    fillTable("item-table", 20, 638, (item) => "<td style='width:48px;height:48px;'><image src='/sprites/items/" + item + ".png'/><br>#" + item + "</td>");
}
