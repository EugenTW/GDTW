document.addEventListener("DOMContentLoaded", function() {    
    fetch("header.html")
        .then(response => response.text())
        .then(data => {
            document.getElementById("header").innerHTML = data;
        });

    
    fetch("footer.html")
        .then(response => response.text())
        .then(data => {
            document.getElementById("footer").innerHTML = data;
            
            var startYear = 2023;
            var currentYear = new Date().getFullYear();
            var yearText = currentYear > startYear ? startYear + "-" + currentYear : startYear;
            document.querySelector("footer p").textContent = "GDTW © " + yearText;
        });
});