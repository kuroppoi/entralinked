function clampOnChange(inputElement) {
    inputElement.addEventListener("change", function() {
        let value = parseInt(this.value);
        
        if(value < this.min) {
            this.value = this.min;
        } else if(value > this.max) {
            this.value = this.max;
        }
    });
}

function clearSelectOptions(selectElement) {
   let startIndex = selectElement.options.length - 1;
   
   for(i = startIndex; i >= 0; i--) {
      selectElement.remove(i);
   }
}

function stringToWord(string) {
    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
}

function checkURL(url) {
    var request = new XMLHttpRequest();
    request.open('HEAD', url, false);
    request.send();
    return request.status == 200;
}

async function fetchData(method, path) {
    return fetchData(method, path, null);
}

async function fetchData(method, path, body) {
    let response = await fetch(path, {
        method: method,
        body: body
    });
    
    if(response.status != 200) {
        if(response.status == 401) {
            window.location.href = "/dashboard/login.html"; // TODO not epic idea to put this here
            return {};
        }
        
        window.alert("Server returned status code " + response.status + " while fetching " + path);
        return {};
    }
    
    try {
        return await response.json();
    } catch(error) {
        window.alert("Could not deserialize JSON response: " + error);
    }
    
    return {};
}
