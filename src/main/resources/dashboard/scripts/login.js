const ELEMENT_GSID_INPUT = document.getElementById("gsid");

function postLogin() {
    fetchData("POST", "/dashboard/login", new URLSearchParams({
        gsid: ELEMENT_GSID_INPUT.value
    })).then((response) => {
         if(response.error) {
            window.alert(response.message);
        } else {
            window.location.href = "/dashboard/profile.html";
        }
    });
    
    return false;
}
