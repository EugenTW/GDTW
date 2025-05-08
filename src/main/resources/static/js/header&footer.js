fetch("/header.html")
    .then(response => response.text())
    .then(data => {
        document.getElementById("header").innerHTML = data;
    });

fetch("/footer.html")
    .then(response => response.text())
    .then(data => {
        document.getElementById("footer").innerHTML = data;

        const footer = document.getElementById("copyright");
        if (footer) {
            const year = new Date().getFullYear();
            footer.textContent = `Copyright © 2024 – ${year} GDTW. All rights reserved.`;
        }
    });
