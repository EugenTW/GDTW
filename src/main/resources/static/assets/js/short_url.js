$(document).ready(function () {
    const $longUrlInput = $("#long_url");
    const $generateButton = $("#generate");
    const maxRetries = 5;

    function isValidUrl(url) {
        if (!url || url.length > 200) return false;
        const httpsRegex = /^https:\/\/[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,}(:\d+)?(\/.*)?$/;
        return httpsRegex.test(url);
    }

    function toggleButtonState() {
        const url = $longUrlInput.val().trim();
        $generateButton.prop('disabled', !isValidUrl(url));
    }

    $longUrlInput.on('input', function () {
        toggleButtonState();
    });

    $longUrlInput.on('blur', function () {
        const url = $(this).val().trim();
        if (!isValidUrl(url)) {
            $(this).val('');
            showAlert("請輸入有效的 HTTPS 網址，且不超過 200 字元。 - Please enter a valid HTTPS URL, and it should not exceed 200 characters.", "darkred");
            $generateButton.prop('disabled', true);
        }
    });

    $generateButton.on('click', function () {
        const longUrl = $longUrlInput.val().trim();
        if (!isValidUrl(longUrl)) {
            showAlert("請輸入正確的 URL（https 開頭且不超過 200 字元）。 - Please enter a correct URL (starting with https and not exceeding 200 characters).", "darkred");
            return;
        }

        $generateButton.prop('disabled', true);

        let attempt = 0;

        function tryCreateShortUrl() {
            $.ajax({
                url: '/su_api/create_new_short_url',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ originalUrl: longUrl })
            })
                .done(function (response) {
                    if (response && typeof response === "object") {
                        const shortUrl = response.fullShortUrl;
                        const message = response.message;

                        if (shortUrl) {
                            $("#shorten_url")
                                .text(shortUrl)
                                .css("background-color", "yellow")
                                .off("click")
                                .on("click", copyToClipboard);

                            $("#qrcode").empty();
                            new QRCode(document.getElementById("qrcode"), {
                                text: shortUrl,
                                width: 100,
                                height: 100
                            });
                            $("#qrcode").css("display", "block");
                        } else {
                            $("#shorten_url")
                                .text(message)
                                .css("background-color", "pink");
                            $("#qrcode").css("display", "none");
                        }
                    } else {
                        console.error("Invalid response!");
                        $("#shorten_url")
                            .text("無效的回應! 請稍後重試! - Invalid response! Try later.")
                            .css("background-color", "pink");
                        $("#qrcode").css("display", "none");
                    }
                })
                .fail(function (xhr) {
                    if (xhr.status === 429) {
                        attempt++;
                        console.warn(`Request rate limit hit. Retry attempt: ${attempt}`);
                        if (attempt < maxRetries) {
                            setTimeout(tryCreateShortUrl, 1000);
                        } else {
                            $("#shorten_url")
                                .text("伺服器忙碌，稍後再試。 - Server busy. Try later.")
                                .css("background-color", "pink");
                            $("#qrcode").css("display", "none");
                        }
                    } else {
                        console.error(`Error: ${xhr.status} - ${xhr.responseText}`);
                        $("#shorten_url")
                            .text("錯誤: 請稍後再試! Error: Please try again later.")
                            .css("background-color", "pink");
                        $("#qrcode").css("display", "none");
                    }
                })
                .always(function () {
                    $generateButton.prop('disabled', true);
                });
        }

        tryCreateShortUrl();
    });

    function copyToClipboard() {
        const copyText = document.getElementById("shorten_url").textContent;
        navigator.clipboard.writeText(copyText)
            .then(() => {
                showAlert("短網址已複製到剪貼簿 / The short URL is copied!", "darkgreen");
            })
            .catch(err => {
                console.error('Failed to copy text: ', err);
                showAlert("無法複製短網址 / Unable to copy the short URL.", "darkred");
            });
    }

    function showAlert(message, color) {
        const alertDiv = $("<div></div>")
            .text(message)
            .css({
                position: "fixed",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
                backgroundColor: color || "#484848",
                color: "#FFFFFF",
                padding: "15px",
                borderWidth: "3px",
                borderRadius: "5px",
                zIndex: "9999",
            });

        $("body").append(alertDiv);
        setTimeout(() => alertDiv.fadeOut(300, () => alertDiv.remove()), 1500);
    }
});
