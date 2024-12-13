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
        $('.shorten-url').text('短網址無效 / Invalid short URL.');
        $('.original-url').text('請使用有效的短網址 / Please use a valid short URL.');
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
            data: JSON.stringify({ code: code }),
            success: function (response) {
                console.log("Response received: ", response);

                if (response.errorMessage) {
                    $('.original-url').text(response.errorMessage).css("color", "red");
                } else {
                    const originalUrl = response.originalUrl;
                    const originalUrlSafe = response.originalUrlSafe;

                    $('.original-url').text(originalUrl);
                    $('.button.green').on('click', function () {
                        window.location.href = originalUrl;
                    });

                    updateGoogleSafeCheck(originalUrlSafe);
                }
            },
            error: function (xhr) {
                if (xhr.status === 429 && attempts <= maxRetries) {
                    setTimeout(tryFetch, delay);
                } else {
                    let errorMessage;
                    if (xhr.status === 404 || xhr.status === 410) {
                        errorMessage = xhr.responseText;
                    } else {
                        errorMessage = "內部伺服器錯誤! Internal Server Error!";
                    }
                    $('.original-url').text(errorMessage).css("color", "red");
                }
            }
        });
    }
    tryFetch();
}


function updateGoogleSafeCheck(originalUrlSafe) {
    const safeValue = parseInt(originalUrlSafe, 10);
    const iconElement = $('#google-safe-icon');

    if (safeValue === 0) {
        iconElement.attr('src', '/images/circle.png');
        iconElement.attr('title', 'Unchecked URL!');
    } else if (safeValue === 1) {
        iconElement.attr('src', '/images/check.png');
        iconElement.attr('title', 'Safe URL!');
    } else if (safeValue === 2) {
        iconElement.attr('src', '/images/warn.png');
        iconElement.attr('title', 'Unsafe URL!');
    }
}
