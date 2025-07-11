document.addEventListener('DOMContentLoaded', function () {
    const path = window.location.pathname;
    const code = path.split('/')[1];

    const currentPageUrl = window.location.href;
    const url = new URL(currentPageUrl);
    const cleanUrl = url.origin + url.pathname;

    if (code) {
        const shortenUrlElement = document.querySelector('.shorten-url');
        if (shortenUrlElement) {
            shortenUrlElement.textContent = cleanUrl;
        }
        fetchOriginalUrlWithRetry(code, 3, 1000);
    } else {
        showError("短網址無效 / Invalid short URL.");
    }

    function showCopyAlert(msg) {
        const alertDiv = document.createElement('div');
        alertDiv.style.position = "fixed";
        alertDiv.style.left = "50%";
        alertDiv.style.top = "50%";
        alertDiv.style.transform = "translate(-50%, -50%)";
        alertDiv.style.background = "#bbfafa";
        alertDiv.style.color = "#000000";
        alertDiv.style.padding = "15px 20px";
        alertDiv.style.border="3px solid #16d4ff";
        alertDiv.style.borderRadius = "5px";
        alertDiv.style.fontSize = "16px";
        alertDiv.style.fontWeight = "bold";
        alertDiv.style.zIndex = "9999";
        alertDiv.style.textAlign = "center";
        alertDiv.innerHTML = msg;
        document.body.appendChild(alertDiv);
        setTimeout(function () {
            document.body.removeChild(alertDiv);
        }, 1500);
    }

    function copyTextContent(element, fallbackMsg) {
        if (!element) return;
        const text = element.textContent.trim();
        if (!text) {
            showCopyAlert(fallbackMsg || "無內容可複製 / Nothing to copy");
            return;
        }

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(text)
                .then(() => showCopyAlert("複製成功！<br>Copied to clipboard!"))
                .catch(() => showCopyAlert("複製失敗 / Copy failed"));
        }
    }

    document.querySelector('.shorten-url')?.addEventListener('click', function () {
        copyTextContent(this, "短網址無內容");
    });

    document.querySelector('.original-url')?.addEventListener('click', function () {
        copyTextContent(this, "原始網址無內容");
    });

});

function fetchOriginalUrlWithRetry(code, maxRetries, delay) {
    let attempts = 0;

    function tryFetch() {
        attempts++;

        fetch('/su_api/get_original_url', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ code: code })
        })
            .then(response => response.json())
            .then(handleSuccess)
            .catch((err) => {
                handleFailure(err, attempts, maxRetries, delay, tryFetch);
            });
    }

    tryFetch();
}

function handleSuccess(response) {
    if (response.errorMessage) {
        showError(response.errorMessage);
    } else {
        const originalUrl = response.originalUrl;
        const originalUrlSafe = response.originalUrlSafe || "0";

        const originalUrlElement = document.querySelector('.original-url');
        if (originalUrlElement) {
            originalUrlElement.textContent = originalUrl;
        }

        const buttonGreen = document.querySelector('.button.green');
        if (buttonGreen) {
            const newButton = buttonGreen.cloneNode(true);
            buttonGreen.parentNode.replaceChild(newButton, buttonGreen);
            newButton.addEventListener('click', function () {
                window.location.href = originalUrl;
            });
        }

        updateGoogleSafeCheck(originalUrlSafe);
    }
}

function handleFailure(error, attempts, maxRetries, delay, retryFunction) {
    let errorMessage = "內部伺服器錯誤! Internal Server Error!";

    if (error instanceof Response) {
        error.text().then((text) => {
            try {
                const json = JSON.parse(text);
                if (json.errorMessage) {
                    errorMessage = json.errorMessage;
                }
            } catch (e) {}

            switch (error.status) {
                case 429:
                    errorMessage = "請求過於頻繁! Too many requests!";
                    break;
                case 404:
                    errorMessage = "此短網址尚未建立! Original URL not found!";
                    break;
                case 410:
                    errorMessage = "此短網址已失效! The short URL is banned.";
                    break;
            }

            showError(errorMessage);
            if (error.status === 429 && attempts <= maxRetries) {
                setTimeout(retryFunction, delay);
            }
        });
    } else {
        showError(errorMessage);
    }
}

function showError(message) {
    const originalUrlElement = document.querySelector('.original-url');
    if (originalUrlElement) {
        originalUrlElement.textContent = message;
        originalUrlElement.style.color = 'red';
        originalUrlElement.style.fontWeight = 'bold';
    }
}

function updateGoogleSafeCheck(originalUrlSafe) {
    const safeValue = parseInt(originalUrlSafe, 10);
    const iconElement = document.getElementById('google-safe-icon');

    const safeStatus = {
        0: { src: '/images/circle.png', title: 'Unchecked URL!' },
        1: { src: '/images/check.png', title: 'Safe URL!' },
        2: { src: '/images/warn.png', title: 'Unsafe URL!' }
    };

    const status = safeStatus[safeValue] || safeStatus[0];
    if (iconElement) {
        iconElement.setAttribute('src', status.src);
        iconElement.setAttribute('title', status.title);
    }
}
