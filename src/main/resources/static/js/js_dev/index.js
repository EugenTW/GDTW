document.addEventListener("DOMContentLoaded", function () {
    const heroSection = document.querySelector('.hero-section');
    if (!heroSection) return;
    let hue = 0;
    setInterval(() => {
        heroSection.style.background = `linear-gradient(90deg, 
            hsl(${hue},100%,60%), 
            hsl(${(hue+60)%360},100%,60%), 
            hsl(${(hue+120)%360},100%,60%))`;
        hue = (hue + 2) % 360;
    }, 90);
});
