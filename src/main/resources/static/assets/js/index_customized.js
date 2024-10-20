(function() {
    const startYear = 2024;
    const currentYear = new Date().getFullYear();
    let displayYear;

    if (currentYear === startYear) {
        displayYear = `${startYear}`;
    } else if (currentYear > startYear) {
        displayYear = `${startYear} - ${currentYear}`;
    } else {       
        displayYear = `${startYear}`;
    }

    document.getElementById('footer-year').textContent = `Copyright © ${displayYear} GDTW. All rights reserved.`;
})();