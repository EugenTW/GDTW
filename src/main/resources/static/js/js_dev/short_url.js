document.addEventListener('DOMContentLoaded', function () {
    const longUrlInput = document.getElementById('long_url');

    if (longUrlInput) {
        function autoResizeTextarea(el) {
            el.style.height = '40px';
            el.style.height = el.scrollHeight + 'px';
        }
        autoResizeTextarea(longUrlInput);
        longUrlInput.addEventListener('input', function () {
            autoResizeTextarea(this);
        });
        longUrlInput.addEventListener('paste', function () {
            setTimeout(() => autoResizeTextarea(this), 0);
        });
    }
    const generateButton = document.getElementById('generate');
    const shortenUrlDisplay = document.getElementById('shorten_url');
    const qrcodeContainer = document.getElementById('qrcode');
    const maxRetries = 5;
    let lastSubmittedUrl = '';

    function isValidUrl(url) {
        if (!url || url.length > 200) return false;
        const httpsRegex = /^https:\/\/[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,}(:\d+)?(\/.*)?$/;
        return httpsRegex.test(url);
    }

    function toggleButtonState() {
        const url = longUrlInput.value.trim();
        const isValid = isValidUrl(url);
        generateButton.disabled = !isValid || url === lastSubmittedUrl;
    }

    longUrlInput.addEventListener('input', toggleButtonState);

    longUrlInput.addEventListener('paste', function (e) {
        e.preventDefault();
        const clipboardData = e.clipboardData || window.clipboardData;
        const pastedText = clipboardData.getData('text');
        longUrlInput.value = pastedText.trim();
        toggleButtonState();
    });

    longUrlInput.addEventListener('blur', function () {
        const url = longUrlInput.value.trim();
        if (!isValidUrl(url)) {
            longUrlInput.value = '';
            showAlert("請輸入有效的 HTTPS 網址，且不超過 200 字元。 - Please enter a valid HTTPS URL, and it should not exceed 200 characters.", "darkred");
            generateButton.disabled = true;
        }
    });

    generateButton.addEventListener('click', function () {
        const longUrl = longUrlInput.value.trim();
        if (!isValidUrl(longUrl)) {
            showAlert("請輸入正確的 URL（https 開頭且不超過 200 字元）。 - Please enter a correct URL (starting with https and not exceeding 200 characters).", "darkred");
            return;
        }

        generateButton.disabled = true;
        let attempt = 0;

        function tryCreateShortUrl() {
            fetch('/su_api/create_new_short_url', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ originalUrl: longUrl })
            })
                .then(response => {
                    if (response.status === 429) {
                        attempt++;
                        if (attempt < maxRetries) {
                            setTimeout(tryCreateShortUrl, 1000);
                        } else {
                            shortenUrlDisplay.textContent = "伺服器忙碌，稍後再試。 - Server busy. Try later.";
                            shortenUrlDisplay.style.backgroundColor = "pink";
                            qrcodeContainer.style.display = "none";
                        }
                        return null;
                    }
                    return response.json();
                })
                .then(data => {
                    if (!data) return;
                    const shortUrl = data.fullShortUrl;
                    const message = data.message;

                    if (shortUrl) {
                        shortenUrlDisplay.textContent = shortUrl;
                        shortenUrlDisplay.style.backgroundColor = "yellow";
                        shortenUrlDisplay.onclick = () => copyToClipboard(shortUrl);

                        qrcodeContainer.innerHTML = "";
                        new QRCode(qrcodeContainer, {
                            text: shortUrl,
                            width: 100,
                            height: 100
                        });
                        qrcodeContainer.style.display = "block";

                        copyToClipboard(shortUrl);
                        lastSubmittedUrl = longUrl.trim();
                    } else {
                        shortenUrlDisplay.textContent = message;
                        shortenUrlDisplay.style.backgroundColor = "pink";
                        qrcodeContainer.style.display = "none";
                    }
                })
                .catch(err => {
                    console.error(err);
                    shortenUrlDisplay.textContent = "錯誤: 請稍後再試! Error: Please try again later.";
                    shortenUrlDisplay.style.backgroundColor = "pink";
                    qrcodeContainer.style.display = "none";
                })
                .finally(() => {
                    generateButton.disabled = true;
                });
        }

        tryCreateShortUrl();
    });

    function copyToClipboard(text) {
        navigator.clipboard.writeText(text)
            .then(() => {
                showAlert("短網址已複製到剪貼簿 / The short URL is copied!", "darkgreen");
            })
            .catch(err => {
                console.error('Failed to copy text: ', err);
                showAlert("無法複製短網址 / Unable to copy the short URL.", "darkred");
            });
    }

    function showAlert(message, color) {
        const alertDiv = document.createElement("div");
        alertDiv.textContent = message;
        alertDiv.style.position = "fixed";
        alertDiv.style.top = "50%";
        alertDiv.style.left = "50%";
        alertDiv.style.transform = "translate(-50%, -50%)";
        alertDiv.style.background = "#00FF00";
        alertDiv.style.color = "#000000";
        alertDiv.style.border="2px solid #227700";
        alertDiv.style.padding = "15px";
        alertDiv.style.borderRadius = "5px";
        alertDiv.style.zIndex = "9999";
        alertDiv.style.borderWidth = "3px";

        document.body.appendChild(alertDiv);
        setTimeout(() => {
            alertDiv.style.transition = "opacity 0.3s";
            alertDiv.style.opacity = 0;
            setTimeout(() => alertDiv.remove(), 300);
        }, 1500);
    }
});