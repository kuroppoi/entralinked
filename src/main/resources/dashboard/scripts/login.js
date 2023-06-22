const ELEMENT_GSID_INPUT = document.getElementById("gsid");

function postLogin() {
    let loginData = {
        gsid: ELEMENT_GSID_INPUT.value
    }
    
    fetch("/dashboard/login", {
        method: "POST",
        body: new URLSearchParams(loginData)
    }).then((response) => {
        return response.json();
    }).then((response) => {
        console.log(response);
        
        if(response.error) {
            alert(response.message);
        } else {
            window.location.href = "/dashboard/profile.html";
        }
    });
}
