$(window).on('load', function () {
    const path = window.location.pathname;
    const code = path.split('/')[1];

    const currentPageUrl = window.location.href;
    const url = new URL(currentPageUrl);
    const cleanUrl = url.origin + url.pathname;

    if (code) {
        $('.shorten-url').text(cleanUrl);
        fetchOriginalUrlWithRetry(code, 3, 1000);
    } else {
        showError("短網址無效 / Invalid short URL.");
    }
});


function fetchOriginalUrlWithRetry(code, maxRetries, delay) {
    let attempts = 0;

    function tryFetch() {
        attempts++;

        $.ajax({
            url: '/su_api/get_original_url',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ code: code })
        })
            .done(handleSuccess)
            .fail(function (xhr) {
                handleFailure(xhr, attempts, maxRetries, delay, tryFetch);
            });
    }

    tryFetch();
}


function handleSuccess(response) {
    if (response.errorMessage) {
        showError(response.errorMessage);
    } else {
        const originalUrl = response.originalUrl;
        const originalUrlSafe = response.originalUrlSafe || "0"; // 預設為 "未檢查"

        $('.original-url').text(originalUrl);
        $('.button.green').off('click').on('click', function () {
            window.location.href = originalUrl;
        });

        updateGoogleSafeCheck(originalUrlSafe);
    }
}


function handleFailure(xhr, attempts, maxRetries, delay, retryFunction) {
    let errorMessage = "內部伺服器錯誤! Internal Server Error!";

    if (xhr.responseJSON && xhr.responseJSON.errorMessage) {
        errorMessage = xhr.responseJSON.errorMessage;
    } else {
        switch (xhr.status) {
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
    }

    showError(errorMessage);

    if (xhr.status === 429 && attempts <= maxRetries) {
        setTimeout(retryFunction, delay);
    }
}


function showError(message) {
    $('.original-url').text(message).css({ "color": "red", "font-weight": "bold" });
}


function updateGoogleSafeCheck(originalUrlSafe) {
    const safeValue = parseInt(originalUrlSafe, 10);
    const iconElement = $('#google-safe-icon');

    const safeStatus = {
        0: { src: '/images/circle.png', title: 'Unchecked URL!' },
        1: { src: '/images/check.png', title: 'Safe URL!' },
        2: { src: '/images/warn.png', title: 'Unsafe URL!' }
    };

    const status = safeStatus[safeValue] || safeStatus[0]; // 預設為未檢查狀態
    iconElement.attr('src', status.src);
    iconElement.attr('title', status.title);
}
