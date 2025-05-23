document.addEventListener('DOMContentLoaded', function () {
    const longUrlInput = document.getElementById('long_url');
    const generateButton = document.getElementById('generate');
    const googleSafeIcon = document.getElementById('google-safe-icon');
    let lastValidUrl = '';

    function isValidUrl(url) {
        const pattern = /^https?:\/\/[\w\-]+(\.[\w\-]+)+.*$/i;
        return pattern.test(url) && url.length <= 200;
    }

    function toggleButtonState() {
        const urlValue = longUrlInput.value.trim();
        generateButton.disabled = !isValidUrl(urlValue);
    }

    longUrlInput.addEventListener('input', function () {
        toggleButtonState();
    });

    longUrlInput.addEventListener('blur', function () {
        const urlValue = longUrlInput.value.trim();

        if (urlValue !== '') {
            if (urlValue.length > 200) {
                longUrlInput.value = '';
                alert('網址長度不得超過 200 字 / URL length cannot exceed 200 characters.');
                generateButton.disabled = true;
                return;
            }
            if (!isValidUrl(urlValue)) {
                longUrlInput.value = '';
                alert('請輸入有效的網址(需含http或https) / Please enter a valid URL (must start with http or https).');
                generateButton.disabled = true;
                return;
            }
            lastValidUrl = urlValue;
            generateButton.disabled = false;
            googleSafeIcon.src = '/images/circle.png';
            googleSafeIcon.title = 'Unchecked URL!';
        } else {
            generateButton.disabled = true;
        }
    });

    generateButton.addEventListener('click', function () {
        const urlToCheck = longUrlInput.value.trim();
        if (!isValidUrl(urlToCheck)) return;

        generateButton.disabled = true;

        fetch('/usc_api/check_url_safety', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ original_url: urlToCheck })
        })
            .then(response => response.json())
            .then(data => {
                const safeValue = parseInt(data.safeValue, 10);
                let iconSrc = '/images/circle.png';
                let iconTitle = 'Unchecked URL!';
                if (safeValue === 1) {
                    iconSrc = '/images/check.png';
                    iconTitle = 'Safe URL!';
                } else if (safeValue === 2) {
                    iconSrc = '/images/warn.png';
                    iconTitle = 'Unsafe URL!';
                }
                googleSafeIcon.src = iconSrc;
                googleSafeIcon.title = iconTitle;
            })
            .catch(error => {
                console.error('Error:', error);
            })
            .finally(() => {
                if (longUrlInput.value.trim() === lastValidUrl) {
                    generateButton.disabled = true;
                }
            });
    });
});
